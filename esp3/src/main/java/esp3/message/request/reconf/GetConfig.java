package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.io.StreamUtils;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class GetConfig extends RecomRequest {
    private final boolean parameterCommand;
    private final int startIndex;
    private final int endIndex;

    private byte[] payload;

    public GetConfig(long destinationId) {
        super(destinationId);
        parameterCommand = false;
        startIndex = endIndex = 0;
    }

    public GetConfig(long destinationId, int startIndex, int endIndex) {
        super(destinationId);
        parameterCommand = true;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        if (parameterCommand) {
            queue.push(0x80);
            queue.push(startIndex);
            queue.push(endIndex);
        }
        else
            queue.push(0);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x221;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        payload = new byte[data.length - 1];
        System.arraycopy(data, 1, payload, 0, payload.length);
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPayloadAsInt() {
        return ArrayUtils.bitRangeValue(payload, 0, payload.length * 8);
    }

    @Override
    public String toString() {
        return "GetConfig [payload=" + (payload == null ? "null" : StreamUtils.dumpHex(payload)) + "]";
    }
}
