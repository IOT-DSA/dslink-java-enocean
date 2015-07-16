package esp3.message.request.reman;

import com.serotonin.util.queue.ByteQueue;

public class SetCode extends RemanRequest {
    private final long securityCode;

    public SetCode(long securityCode) {
        this(BROADCAST_ID, securityCode);
    }

    public SetCode(long destinationId, long securityCode) {
        super(destinationId);
        this.securityCode = securityCode;
    }

    @Override
    protected int getFunctionNumber() {
        return 3;
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
