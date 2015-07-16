package esp3.message.request.local;

import com.serotonin.util.queue.ByteQueue;

public class WriteIdBase extends CommonCommand {
    private final long baseId;

    public WriteIdBase(long baseId) {
        this.baseId = baseId;
    }

    @Override
    protected CommandCode getCommandCode() {
        return CommandCode.coWrIdbase;
    }

    @Override
    protected void addCommandData(ByteQueue queue) {
        queue.pushU4B(baseId);
    }

    @Override
    public void parseResponseData(byte[] data, byte[] opt) {
        // no op
    }
}
