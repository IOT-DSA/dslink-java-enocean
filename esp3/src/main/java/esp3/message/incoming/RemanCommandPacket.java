package esp3.message.incoming;

import com.serotonin.util.queue.ByteQueue;

abstract public class RemanCommandPacket extends IncomingPacket {
    public static RemanCommandPacket parse(ByteQueue data, ByteQueue optional) {
        int functionNumber = data.popU2B();
        if (functionNumber == 0x604)
            return new QueryIdAnswer(functionNumber, data, optional);
        if (functionNumber == 0x608)
            return new QueryStatusAnswer(functionNumber, data, optional);
        return new RemanResponse(functionNumber, data, optional);
    }

    private final int functionNumber;
    private final int manufacturerId;
    private final byte[] messageData;

    private final long destinationId;
    private final long sourceId;
    private final int dBm;
    private final boolean sendWithDelay;

    public RemanCommandPacket(int functionNumber, ByteQueue data, ByteQueue optional) {
        this.functionNumber = functionNumber;
        manufacturerId = data.popU2B();
        messageData = data.popAll();

        destinationId = optional.popU4B();
        sourceId = optional.popU4B();
        dBm = optional.popU1B();
        sendWithDelay = optional.popU1B() == 1;
    }

    public int getFunctionNumber() {
        return functionNumber;
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public byte[] getMessageData() {
        return messageData;
    }

    public long getDestinationId() {
        return destinationId;
    }

    public long getSourceId() {
        return sourceId;
    }

    public int getdBm() {
        return dBm;
    }

    public boolean isSendWithDelay() {
        return sendWithDelay;
    }
}
