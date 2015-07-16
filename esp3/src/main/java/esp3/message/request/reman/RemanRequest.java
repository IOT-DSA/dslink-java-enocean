package esp3.message.request.reman;

import esp3.message.PacketType;
import esp3.message.incoming.RemanResponse;
import esp3.message.request.DeviceRequest;
import com.serotonin.messaging2.IncomingMessage;
import com.serotonin.util.queue.ByteQueue;

/**
 * A request sent from the module over the radio to a device or devices (broadcast). These messages will receive a
 * response from the gateway, only containing a return code. A subsequent response may be received from the targeted
 * device(s).
 * 
 * @author Matthew
 */
abstract public class RemanRequest extends DeviceRequest {
    private RemanResponse deviceResponse;

    public RemanRequest(long destinationId) {
        super(destinationId);
    }

    @Override
    final protected void addData(ByteQueue queue) {
        queue.pushU2B(getFunctionNumber());
        queue.pushU2B(0x07ff); // Manufacturer id
        addMessageData(queue);
    }

    abstract protected void addMessageData(ByteQueue queue);

    @Override
    protected void addOptionalData(ByteQueue queue) {
        queue.pushU4B(getDestinationId());
        queue.pushU4B(useBaseId() ? getBaseIdToSend() : 0);
        queue.push(0xff); // dBm
        queue.push(0); // send with delay
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.remoteManCommand;
    }

    @Override
    public int getTimeout() {
        return 1000;
    }

    @Override
    public boolean consume(IncomingMessage m) {
        if (super.consume(m))
            return true;

        if (m instanceof RemanResponse) {
            RemanResponse rr = (RemanResponse) m;
            if (rr.getFunctionNumber() == getResponseFunctionNumber() && rr.getSourceId() == getDestinationId()) {
                deviceResponse = (RemanResponse) m;
                parseResponseData(deviceResponse.getMessageData());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean completed() {
        if (!super.completed())
            return false;
        if (isBroadcast() || isBadLocalResponse() || !expectsDeviceResponse())
            return true;
        return deviceResponse != null;
    }

    @Override
    public void reset() {
        super.reset();
        deviceResponse = null;
    }

    @Override
    public boolean useBaseId() {
        return false;
    }

    @Override
    protected void parseResponseData(byte[] data, byte[] opt) {
        // no op
    }

    protected boolean expectsDeviceResponse() {
        return false;
    }

    abstract protected void parseResponseData(byte[] data);

    abstract protected int getFunctionNumber();

    protected int getResponseFunctionNumber() {
        return getFunctionNumber() + 0x600;
    }

    public RemanResponse getDeviceResponse() {
        return deviceResponse;
    }
}
