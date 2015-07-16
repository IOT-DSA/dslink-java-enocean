package esp3.message.request;

abstract public class DeviceRequest extends EnOceanRequest {
    public static final long BROADCAST_ID = 0xFFFFFFFFL;

    private final long destinationId;
    private long baseId;
    private final int baseIdOffset;

    public DeviceRequest(long destinationId) {
        this(destinationId, 0);
    }

    public DeviceRequest(long destinationId, int baseIdOffset) {
        this.destinationId = destinationId;
        this.baseIdOffset = baseIdOffset;
    }

    public void setBaseId(long baseId) {
        this.baseId = baseId;
    }

    public boolean isBroadcast() {
        return destinationId == BROADCAST_ID;
    }

    public long getDestinationId() {
        return destinationId;
    }

    public boolean useBaseId() {
        return true;
    }

    public long getBaseIdToSend() {
        return baseId + baseIdOffset;
    }
}
