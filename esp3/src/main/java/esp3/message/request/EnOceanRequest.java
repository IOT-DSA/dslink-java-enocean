package esp3.message.request;

import esp3.message.CRC;
import esp3.message.Packet;
import esp3.message.PacketType;
import esp3.message.incoming.ResponsePacket;
import esp3.message.incoming.ResponsePacket.ReturnCode;
import com.serotonin.messaging2.IncomingMessage;
import com.serotonin.messaging2.OutgoingMessage;
import com.serotonin.util.queue.ByteQueue;

abstract public class EnOceanRequest extends Packet implements OutgoingMessage {
    protected ResponsePacket localResponse;

    @Override
    public byte[] getMessageData() {
        ByteQueue data = new ByteQueue();
        addData(data);

        ByteQueue opt = new ByteQueue();
        addOptionalData(opt);

        int packetTypeId = getPacketType().from;

        ByteQueue queue = new ByteQueue();
        queue.push(START);
        queue.pushU2B(data.size());
        queue.push(opt.size());
        queue.push(packetTypeId);
        queue.push(CRC.calculateCRC(data.size(), opt.size(), packetTypeId));
        queue.push(data);
        queue.push(opt);
        queue.push(CRC.calculateCRC(data, opt));

        return queue.popAll();
    }

    abstract protected void addData(ByteQueue queue);

    abstract protected void addOptionalData(ByteQueue queue);

    abstract public PacketType getPacketType();

    public int getTimeout() {
        return 500;
    }

    @Override
    public boolean consume(IncomingMessage res) {
        if (res instanceof ResponsePacket) {
            localResponse = (ResponsePacket) res;

            if (localResponse.getReturnCode() == ReturnCode.ok)
                parseResponseData(localResponse.getResponseData(), localResponse.getResponseOpt());

            return true;
        }

        return false;
    }

    public ReturnCode getReturnCode() {
        if (localResponse == null)
            return null;
        return localResponse.getReturnCode();
    }

    @Override
    public boolean completed() {
        return localResponse != null;
    }

    @Override
    public void reset() {
        localResponse = null;
    }

    public boolean isBadLocalResponse() {
        return getReturnCode() != ReturnCode.ok;
    }

    abstract protected void parseResponseData(byte[] data, byte[] opt);
}
