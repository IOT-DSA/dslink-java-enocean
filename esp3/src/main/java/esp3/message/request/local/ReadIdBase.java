package esp3.message.request.local;

import com.serotonin.util.ArrayUtils;

public class ReadIdBase extends CommonCommand {
    @Override
    protected CommandCode getCommandCode() {
        return CommandCode.coRdIdbase;
    }

    private long baseId;
    private int remainingWrites;

    @Override
    protected void parseResponseData(byte[] data, byte[] opt) {
        baseId = ArrayUtils.bitRangeValueLong(data, 0, 32);
        remainingWrites = ArrayUtils.bitRangeValue(opt, 0, 8);
    }

    /**
     * @return the baseId
     */
    public long getBaseId() {
        return baseId;
    }

    /**
     * @return the remainingWrites
     */
    public int getRemainingWrites() {
        return remainingWrites;
    }

    @Override
    public String toString() {
        return "ReadIdBase [baseId=" + baseId + ", remainingWrites=" + remainingWrites + "]";
    }
}
