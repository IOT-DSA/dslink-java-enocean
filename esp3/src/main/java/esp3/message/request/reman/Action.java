package esp3.message.request.reman;

import com.serotonin.util.queue.ByteQueue;

public class Action extends RemanRequest {
    public Action() {
        this(BROADCAST_ID);
    }

    public Action(long destinationId) {
        super(destinationId);
    }

    @Override
    protected int getFunctionNumber() {
        return 5;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // no op
    }

    @Override
    public void parseResponseData(byte[] data) {
        // no op
    }
}
