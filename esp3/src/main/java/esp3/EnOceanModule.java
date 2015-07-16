package esp3;

import java.io.IOException;

import esp3.message.request.EnOceanRequest;
import com.serotonin.log.IOLog;

public class EnOceanModule {
    private final EnOceanModuleImpl impl;

    public EnOceanModule(String serialPortId) {
        impl = new EnOceanModuleImpl(serialPortId);
    }

    public EnOceanModule(String serialPortId, long baseId) {
        impl = new EnOceanModuleImpl(serialPortId, baseId);
    }

    //
    // Lifecycle
    //
    public void init() throws Exception {
        impl.init();
    }

    public void destroy() {
        impl.destroy();
    }

    //
    // Properties
    //

    public void addListener(EnOceanModuleListener l) {
        impl.addListener(l);
    }

    public void removeListener(EnOceanModuleListener l) {
        impl.removeListener(l);
    }

    public void clearKnownDevices() {
        impl.clearKnownDevices();
    }

    public long getBaseId() {
        return impl.getBaseId();
    }

    public int getRemainingBaseIdWrites() {
        return impl.getRemainingBaseIdWrites();
    }

    public void setIoLog(IOLog log) {
        impl.setIoLog(log);
    }

    public IOLog getIoLog() {
        return impl.getIoLog();
    }

    //
    // Send requests
    //
    public <T extends EnOceanRequest> T send(T req) throws IOException {
        return impl.send(req);
    }

    public <T extends EnOceanRequest> void send(T req, SendCallback<T> cb) {
        impl.send(req, cb);
    }
}
