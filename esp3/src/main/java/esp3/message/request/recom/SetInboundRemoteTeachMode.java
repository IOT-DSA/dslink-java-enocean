package esp3.message.request.recom;

import com.serotonin.util.queue.ByteQueue;

public class SetInboundRemoteTeachMode extends RecomRequest {
    private final boolean enterLearnMode;
    private final int index;

    public SetInboundRemoteTeachMode(long destinationId, boolean exitLearnMode) {
        this(destinationId, exitLearnMode, 0xff);
    }

    public SetInboundRemoteTeachMode(long destinationId, boolean exitLearnMode, int index) {
        super(destinationId);
        this.enterLearnMode = exitLearnMode;
        this.index = index;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // TODO does not match spec, but the spec is messed.
        queue.push(enterLearnMode ? 0 : 0x80);
        queue.push(index);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x211;
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return false;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        // no op
    }
}
