package esp3.message.request.reconf;

import esp3.message.incoming.ProductIdAnswer;
import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.queue.ByteQueue;

public class ProductIdQuery extends RecomRequest {
    public ProductIdQuery() {
        this(BROADCAST_ID);
    }

    public ProductIdQuery(long destinationId) {
        super(destinationId);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x231;
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

    public ProductIdAnswer getAnswer() {
        return (ProductIdAnswer) getDeviceResponse();
    }
}
