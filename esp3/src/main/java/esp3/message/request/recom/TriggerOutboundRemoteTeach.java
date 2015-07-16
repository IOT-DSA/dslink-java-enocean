package esp3.message.request.recom;

import com.serotonin.util.queue.ByteQueue;

public class TriggerOutboundRemoteTeach extends RecomRequest {
    private final int channel;

    public TriggerOutboundRemoteTeach(long destinationId, int channel) {
        super(destinationId);
        this.channel = channel;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.push(channel);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x212;
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
