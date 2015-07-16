package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.queue.ByteQueue;

public class ApplyChanges extends RecomRequest {
    private final boolean applyLinkTableChanges;
    private final boolean applyConfigurationChanges;

    public ApplyChanges(long destinationId, boolean applyLinkTableChanges, boolean applyConfigurationChanges) {
        super(destinationId);
        this.applyLinkTableChanges = applyLinkTableChanges;
        this.applyConfigurationChanges = applyConfigurationChanges;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        int b = applyLinkTableChanges ? 0x80 : 0;
        b |= applyConfigurationChanges ? 0x40 : 0;
        queue.push(b);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x223;
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
