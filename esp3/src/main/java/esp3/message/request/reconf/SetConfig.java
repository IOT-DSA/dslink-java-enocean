package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.queue.ByteQueue;

public class SetConfig extends RecomRequest {
    private final boolean parameterCommand;
    private final int index;
    private final byte[] payload;

    public SetConfig(long destinationId, boolean parameterCommand, int index, byte[] payload) {
        super(destinationId);
        this.parameterCommand = parameterCommand;
        this.index = index;
        this.payload = payload;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.push(parameterCommand ? 0x80 : 0);
        queue.push(index);
        queue.push(payload);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x222;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        // no op
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return false;
    }
}
