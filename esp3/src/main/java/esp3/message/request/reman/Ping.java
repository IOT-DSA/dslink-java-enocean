package esp3.message.request.reman;

import esp3.message.RadioOrg;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class Ping extends RemanRequest {
    private RadioOrg rorg;
    private int func;
    private int type;
    private int rssi;

    public Ping(long destinationId) {
        super(destinationId);

        if (destinationId == BROADCAST_ID)
            throw new IllegalArgumentException("Cannot broadcast a ping");
    }

    @Override
    protected int getFunctionNumber() {
        return 6;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // no op
    }

    @Override
    public void parseResponseData(byte[] data) {
        rorg = RadioOrg.forId(ArrayUtils.bitRangeValue(data, 0, 8));
        func = ArrayUtils.bitRangeValue(data, 8, 6);
        type = ArrayUtils.bitRangeValue(data, 14, 7);
        rssi = -ArrayUtils.bitRangeValue(data, 24, 8);
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return true;
    }

    @Override
    public boolean useBaseId() {
        return false;
    }

    public RadioOrg getRorg() {
        return rorg;
    }

    public int getFunc() {
        return func;
    }

    public int getType() {
        return type;
    }

    public int getRssi() {
        return rssi;
    }

    @Override
    public String toString() {
        return "Ping [rorg=" + rorg + ", func=" + func + ", type=" + type + ", rssi=" + rssi + "]";
    }
}
