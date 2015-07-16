package esp3.message.request.recom;

import java.util.ArrayList;
import java.util.List;

import esp3.message.LinkTableEntry;
import com.serotonin.util.queue.ByteQueue;

public class GetLinkTableContent extends RecomRequest {
    private final boolean inbound;
    private final int startIndex;
    private final int endIndex;

    private final List<LinkTableEntry> content = new ArrayList<>();

    /**
     * Start and end indices are inclusive.
     * 
     * @param destinationId
     * @param inbound
     * @param startIndex
     * @param endIndex
     */
    public GetLinkTableContent(long destinationId, boolean inbound, int startIndex, int endIndex) {
        super(destinationId);
        this.inbound = inbound;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        queue.push(inbound ? 0 : 0x80);
        queue.push(startIndex);
        queue.push(endIndex);
    }

    @Override
    protected int getFunctionNumber() {
        return 0x213;
    }

    @Override
    public int getTimeout() {
        return 5000;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        for (int i = startIndex; i <= endIndex; i++) {
            LinkTableEntry e = new LinkTableEntry();
            e.read(data, i * 9 + 1);
            content.add(e);
        }
    }

    public List<LinkTableEntry> getContent() {
        return content;
    }
}
