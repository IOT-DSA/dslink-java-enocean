package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.queue.ByteQueue;

public class Reset extends RecomRequest {
    private final boolean resetConfiguration;
    private final boolean clearInboundLinkTable;
    private final boolean clearOutboundLinkTable;

    public Reset(long destinationId, boolean resetConfiguration, boolean clearInboundLinkTable,
            boolean clearOutboundLinkTable) {
        super(destinationId);
        this.resetConfiguration = resetConfiguration;
        this.clearInboundLinkTable = clearInboundLinkTable;
        this.clearOutboundLinkTable = clearOutboundLinkTable;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        int b = resetConfiguration ? 0x80 : 0;
        b |= clearInboundLinkTable ? 0x40 : 0;
        b |= clearOutboundLinkTable ? 0x20 : 0;
        queue.push(b);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x224;
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
