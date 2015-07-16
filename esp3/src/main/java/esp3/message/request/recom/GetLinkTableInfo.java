package esp3.message.request.recom;

import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class GetLinkTableInfo extends RecomRequest {
    private boolean remoteTeachOutboundSupported;
    private boolean remoteTeachInboundSupported;
    private boolean outboundLinkTableSupported;
    private boolean inboundLinkTableSupported;
    private int lengthOfOutboundTable;
    private int maxSizeOfOutboundTable;
    private int lengthOfInboundTable;
    private int maxSizeOfInboundTable;

    public GetLinkTableInfo(long destinationId) {
        super(destinationId);
    }

    @Override
    public int getTimeout() {
        return 1000;
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // no op
    }

    @Override
    protected int getFunctionNumber() {
        return 0x210;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        remoteTeachOutboundSupported = ArrayUtils.bitRangeValue(data, 0, 1) == 1;
        remoteTeachInboundSupported = ArrayUtils.bitRangeValue(data, 1, 1) == 1;
        outboundLinkTableSupported = ArrayUtils.bitRangeValue(data, 2, 1) == 1;
        inboundLinkTableSupported = ArrayUtils.bitRangeValue(data, 3, 1) == 1;
        lengthOfOutboundTable = ArrayUtils.bitRangeValue(data, 8, 8);
        maxSizeOfOutboundTable = ArrayUtils.bitRangeValue(data, 16, 8);
        lengthOfInboundTable = ArrayUtils.bitRangeValue(data, 24, 8);
        maxSizeOfInboundTable = ArrayUtils.bitRangeValue(data, 32, 8);
    }

    public boolean isRemoteTeachOutboundSupported() {
        return remoteTeachOutboundSupported;
    }

    public boolean isRemoteTeachInboundSupported() {
        return remoteTeachInboundSupported;
    }

    public boolean isOutboundLinkTableSupported() {
        return outboundLinkTableSupported;
    }

    public boolean isInboundLinkTableSupported() {
        return inboundLinkTableSupported;
    }

    public int getLengthOfOutboundTable() {
        return lengthOfOutboundTable;
    }

    public int getMaxSizeOfOutboundTable() {
        return maxSizeOfOutboundTable;
    }

    public int getLengthOfInboundTable() {
        return lengthOfInboundTable;
    }

    public int getMaxSizeOfInboundTable() {
        return maxSizeOfInboundTable;
    }

    @Override
    public String toString() {
        return "GetLinkTableInfo [remoteTeachOutboundSupported=" + remoteTeachOutboundSupported
                + ", remoteTeachInboundSupported=" + remoteTeachInboundSupported + ", outboundLinkTableSupported="
                + outboundLinkTableSupported + ", inboundLinkTableSupported=" + inboundLinkTableSupported
                + ", lengthOfOutboundTable=" + lengthOfOutboundTable + ", maxSizeOfOutboundTable="
                + maxSizeOfOutboundTable + ", lengthOfInboundTable=" + lengthOfInboundTable
                + ", maxSizeOfInboundTable=" + maxSizeOfInboundTable + "]";
    }
}
