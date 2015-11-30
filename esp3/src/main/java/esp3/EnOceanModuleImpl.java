package esp3;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.ResponsePacket.ReturnCode;
import esp3.message.request.DeviceRequest;
import esp3.message.request.EnOceanRequest;
import esp3.message.request.local.ReadIdBase;
import esp3.message.request.local.WriteIdBase;
import esp3.message.request.reman.QueryStatus;
import esp3.profile.Profile;
import com.dglogik.serial.JSSCInputStreamAdapter;
import com.dglogik.serial.JSSCOutputStreamAdapter;
import com.serotonin.NotImplementedException;
import com.serotonin.bkgd.WorkItemProcessor;
import com.serotonin.bkgd.WorkItemQueue;
import com.serotonin.epoll.InputStreamEPoll;
import com.serotonin.log.IOLog;
import com.serotonin.messaging.EpollStreamTransport;
import com.serotonin.messaging.MessagingExceptionHandler;
import com.serotonin.messaging.TimeoutException;
import com.serotonin.messaging.Transport;
import com.serotonin.messaging2.MessageControl;
import com.serotonin.provider.ExecutorServiceProvider;
import com.serotonin.provider.InputStreamEPollProvider;
import com.serotonin.provider.Providers;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class EnOceanModuleImpl implements MessagingExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnOceanModuleImpl.class);

    private final String serialPortId;
    private final long baseId;

    // Runtime fields.
    private SerialPort serialPort;
    private Transport transport;
    private MessageControl conn;
    private PacketHandler packetRequestHandler;

    private final List<EnOceanModuleListener> listeners = new CopyOnWriteArrayList<>();

    private final int retries = 1;
    private final Object sendLock = new Object();
    private final int sendDelay = 50;
    private long lastSend;

    private int remainingBaseIdWrites = -1;

    EnOceanModuleImpl(String serialPortId) {
        this(serialPortId, 0);
    }

    EnOceanModuleImpl(String serialPortId, long baseId) {
        if (baseId != 0) {
            if (baseId < 0xFF800000L || baseId > 0xFFFFFF80L) {
                throw new IllegalArgumentException("Base id must be between 0xFF800000 and 0xFFFFFF80 inclusive");
            }
        }

        this.serialPortId = serialPortId;
        this.baseId = baseId;
    }

    //
    // Lifecycle
    //
    void init() throws Exception {
        serialPort = new SerialPort(serialPortId);
        serialPort.openPort();
        serialPort.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
        InputStreamEPoll epoll = Providers.get(InputStreamEPollProvider.class).getInputStreamEPoll();
        transport = new EpollStreamTransport(new JSSCInputStreamAdapter(serialPort), new JSSCOutputStreamAdapter(serialPort), epoll);

        PacketParser packetParser = new PacketParser();
        conn = new MessageControl();
        conn.setRetries(retries);
        conn.setTimeout(500);
        conn.setExceptionHandler(this);
        //        conn.DEBUG = true;

        packetRequestHandler = new PacketHandler(this);
        conn.start(transport, packetParser, packetRequestHandler);

        if (baseId != 0) {
            ReadIdBase readIdBase = new ReadIdBase();
            send(readIdBase);
            remainingBaseIdWrites = readIdBase.getRemainingWrites();
            if (readIdBase.getBaseId() != baseId) {
                WriteIdBase writeIdBase = new WriteIdBase(baseId);
                send(writeIdBase);
            }
        }
    }

    void destroy() {
        if (conn != null) {
            conn.close();
        }
        try {
            serialPort.closePort();
        } catch (SerialPortException ex) {
            logger.error("Unable to close port", ex);
        }
    }

    //
    // Properties
    //
    void addListener(EnOceanModuleListener l) {
        listeners.add(l);
    }

    void removeListener(EnOceanModuleListener l) {
        listeners.remove(l);
    }

    void clearKnownDevices() {
        if (packetRequestHandler != null) {
            packetRequestHandler.clearKnownDevices();
        }
    }

    long getBaseId() {
        return baseId;
    }

    int getRemainingBaseIdWrites() {
        return remainingBaseIdWrites;
    }

    void setIoLog(IOLog log) {
        conn.setIoLog(log);
    }

    IOLog getIoLog() {
        if (conn == null) {
            return null;
        }
        return conn.getIoLog();
    }

    //
    // Events
    //
    void fireTelegram(long senderId, TelegramData telegram) {
        for (EnOceanModuleListener l : listeners) {
            l.enoceanTelegram(senderId, telegram);
        }
    }

    Profile requestSenderProfile(long senderId) {
        for (EnOceanModuleListener l : listeners) {
            Profile profile = l.enoceanSenderProfile(senderId);
            if (profile != null) {
                return profile;
            }
        }
        return null;
    }

    void fireNewSender(long senderId, RadioOrg rorg, int rssi) {
        for (EnOceanModuleListener l : listeners) {
            l.enoceanNewSender(senderId, rorg, rssi);
        }
    }

    void fireTeachIn(long senderId, Profile profile, int rssi) {
        for (EnOceanModuleListener l : listeners) {
            l.enoceanTeachIn(senderId, profile, rssi);
        }
    }

    void fireNewSender(long senderId, Profile profile, int rssi) {
        for (EnOceanModuleListener l : listeners) {
            l.enoceanNewSender(senderId, profile, rssi);
        }
    }

    void fireException(Exception e) {
        for (EnOceanModuleListener l : listeners) {
            l.enoceanException(e);
        }
    }

    //
    //
    // Asynchronous send requests
    //
    <T extends EnOceanRequest> void send(T req, SendCallback<T> cb) {
        @SuppressWarnings("unchecked")
        AsyncRequest<EnOceanRequest> ar = (AsyncRequest<EnOceanRequest>) new AsyncRequest<>(req, cb);
        asyncQueue.addWorkItem(ar);
    }

    private final WorkItemQueue<AsyncRequest<EnOceanRequest>> asyncQueue = new WorkItemQueue<>(Providers.get(
            ExecutorServiceProvider.class).getExecutorService(), new AsyncRequestProcessor<>());

    private class AsyncRequest<T extends EnOceanRequest> {

        T req;
        SendCallback<T> cb;

        public AsyncRequest(T req, SendCallback<T> cb) {
            this.req = req;
            this.cb = cb;
        }
    }

    class AsyncRequestProcessor<T extends EnOceanRequest> implements WorkItemProcessor<AsyncRequest<T>> {

        @Override
        public int maxBatchSize() {
            return 1;
        }

        @Override
        public boolean process(List<AsyncRequest<T>> items) {
            throw new NotImplementedException();
        }

        @Override
        public boolean process(AsyncRequest<T> item) {
            try {
                T req = send(item.req);
                if (item.cb != null) {
                    item.cb.sent(req);
                }
            } catch (IOException e) {
                if (item.cb != null) {
                    item.cb.exception(e);
                }
            }
            return true;
        }
    }

    //
    //
    // Synchronous send requests
    //
    <T extends EnOceanRequest> T send(T req) throws IOException {
        synchronized (sendLock) {
            if (conn == null) {
                throw new IOException("No connection");
            }

            long delay = lastSend + sendDelay - System.currentTimeMillis();
            if (delay > 0) {
                try {
                    sendLock.wait(delay);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }

            if (req instanceof DeviceRequest) {
                ((DeviceRequest) req).setBaseId(baseId);
            }

            try {
                try {
                    conn.send(req, req.getTimeout(), retries);
                } catch (TimeoutException e) {
                    if (req instanceof DeviceRequest && !(req instanceof QueryStatus)) {
                        DeviceRequest dr = (DeviceRequest) req;
                        QueryStatus queryStatus = new QueryStatus(dr.getDestinationId());
                        try {
                            conn.send(queryStatus, queryStatus.getTimeout(), retries);
                            if (queryStatus.getAnswer().getLastReturnCode() != ReturnCode.ok) {
                                throw new IOException("Bad device response: " + req.getReturnCode());
                            }
                            throw e;
                        } catch (TimeoutException e2) {
                            throw e;
                        }
                    }

                    throw e;
                }

                if (req.isBadLocalResponse()) {
                    throw new IOException("Bad local response: " + req.getReturnCode());
                }
            } finally {
                lastSend = System.currentTimeMillis();
            }

            return req;
        }
    }

    //
    // Exception handler
    //
    @Override
    public void receivedException(Exception e) {
        fireException(e);
    }
}
