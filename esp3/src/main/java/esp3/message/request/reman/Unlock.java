package esp3.message.request.reman;

import com.serotonin.util.queue.ByteQueue;

public class Unlock extends RemanRequest {
    private final long securityCode;

    public Unlock(long securityCode) {
        this(BROADCAST_ID, securityCode);
    }

    public Unlock(long destinationId, long securityCode) {
        super(destinationId);
        this.securityCode = securityCode;
    }

    @Override
    protected int getFunctionNumber() {
        return 1;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.pushU4B(securityCode);
    }

    @Override
    public void parseResponseData(byte[] data) {
        // no op
    }
}
