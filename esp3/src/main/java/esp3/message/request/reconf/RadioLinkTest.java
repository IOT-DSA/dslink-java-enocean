package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.queue.ByteQueue;

public class RadioLinkTest extends RecomRequest {
    private final boolean enable;
    private final int autoDisable;

    public RadioLinkTest(long destinationId, boolean enable, int autoDisable) {
        super(destinationId);
        this.enable = enable;
        this.autoDisable = autoDisable;

        if (autoDisable < 1 || autoDisable > 0x7f)
            throw new RuntimeException("Invalid autoDisable value");
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        int b = enable ? 0x80 : 0;
        b |= autoDisable;
        queue.push(b);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x230;
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
