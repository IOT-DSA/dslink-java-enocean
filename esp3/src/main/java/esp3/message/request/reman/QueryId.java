package esp3.message.request.reman;

import esp3.message.incoming.QueryIdAnswer;
import com.serotonin.util.queue.ByteQueue;

public class QueryId extends RemanRequest {
    private final int rorg;
    private final int func;
    private final int type;

    public QueryId() {
        this(0, 0, 0);
    }

    public QueryId(int rorg, int func, int type) {
        super(BROADCAST_ID);
        this.rorg = rorg;
        this.func = func;
        this.type = type;
    }

    @Override
    protected int getFunctionNumber() {
        return 4;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        int i = 0;
        i |= rorg << 16;
        i |= func << 10;
        i |= type << 3;
        queue.pushU3B(i);
    }

    @Override
    public void parseResponseData(byte[] data) {
        // no op
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return true;
    }

    public QueryIdAnswer getAnswer() {
        return (QueryIdAnswer) getDeviceResponse();
    }
}
