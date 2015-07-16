package esp3.message.request.reman;

import esp3.message.incoming.QueryStatusAnswer;
import com.serotonin.util.queue.ByteQueue;

public class QueryStatus extends RemanRequest {
    public QueryStatus() {
        this(BROADCAST_ID);
    }

    public QueryStatus(long destinationId) {
        super(destinationId);
    }

    @Override
    protected int getFunctionNumber() {
        return 8;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // no op
    }

    @Override
    public void parseResponseData(byte[] data) {
        // no op
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return true;
    }

    //    @Override
    //    public boolean useBaseId() {
    //        return true;
    //    }

    public QueryStatusAnswer getAnswer() {
        return (QueryStatusAnswer) getDeviceResponse();
    }
}
