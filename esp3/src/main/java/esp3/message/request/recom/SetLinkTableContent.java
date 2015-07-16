package esp3.message.request.recom;

import java.util.ArrayList;
import java.util.List;

import esp3.message.LinkTableEntry;
import com.serotonin.util.queue.ByteQueue;

public class SetLinkTableContent extends RecomRequest {
    private final boolean inbound;
    private final List<LinkTableEntry> content;

    public SetLinkTableContent(long destinationId, boolean inbound) {
        this(destinationId, inbound, new ArrayList<LinkTableEntry>());
    }

    public SetLinkTableContent(long destinationId, boolean inbound, List<LinkTableEntry> content) {
        super(destinationId);
        this.inbound = inbound;
        this.content = content;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.push(inbound ? 0 : 0x80);
        for (LinkTableEntry e : content)
            e.write(queue);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x214;
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return false;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        // no op
    }
}
