package esp3.message.request;

import java.util.Arrays;

import esp3.message.PacketType;
import esp3.message.RadioOrg;
import com.serotonin.util.queue.ByteQueue;

public class RadioRequest extends DeviceRequest {
    private final RadioOrg rorg;
    private final byte[] data;
    private final int status;

    public RadioRequest(RadioOrg rorg, byte[] data, int status) {
        this(BROADCAST_ID, 0, rorg, data, status);
    }

    public RadioRequest(long destinationId, int baseIdOffset, RadioOrg rorg, byte[] data, int status) {
        super(destinationId, baseIdOffset);
        this.rorg = rorg;
        this.data = data;
        this.status = status;
    }

    @Override
    final protected void addData(ByteQueue queue) {
        queue.push(rorg.id);
        queue.push(data);
        queue.pushU4B(getBaseIdToSend());
        queue.push(status);
    }

    @Override
    protected void addOptionalData(ByteQueue queue) {
        queue.push(3); // subTelNum
        queue.pushU4B(getDestinationId());
        queue.push(0xff); // dBm
        queue.push(0); // security level
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.radio;
    }

    @Override
    protected void parseResponseData(byte[] data, byte[] opt) {
        // no op
    }

    @Override
    public String toString() {
        return "RadioRequest [rorg=" + rorg + ", data=" + Arrays.toString(data) + ", status=" + status + "]";
    }

    //    @Override
    //    public boolean consume(IncomingMessage res) {
    //        if (super.consume(res))
    //            return true;
    //
    //        //        if (m instanceof RemanResponse) {
    //        //            RemanResponse rr = (RemanResponse) m;
    //        //            if (rr.getFunctionNumber() == getResponseFunctionNumber() && rr.getSourceId() == getDestinationId()) {
    //        //                deviceResponse = (RemanResponse) m;
    //        //                parseResponseData(deviceResponse.getMessageData());
    //        //                return true;
    //        //            }
    //        //        }
    //
    //        return false;
    //    }
    //
    //    @Override
    //    public boolean completed() {
    //        if (!super.completed())
    //            return false;
    //        return false;
    //    }
    //
    //    @Override
    //    public void reset() {
    //        throw new NotImplementedException();
    //    }
}
