package esp3.message.request.reman;

import com.serotonin.util.queue.ByteQueue;

public class Learn extends RemanRequest {
    private final boolean start;

    public Learn(boolean start) {
        this(BROADCAST_ID, start);
    }

    public Learn(long destinationId, boolean start) {
        super(destinationId);
        this.start = start;
    }

    @Override
    protected int getFunctionNumber() {
        return 0x201;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.pushU3B(0); // No EEP
        queue.push(start ? 1 : 3); // Start learn
    }

    @Override
    public void parseResponseData(byte[] data) {
        // no op
    }
}
