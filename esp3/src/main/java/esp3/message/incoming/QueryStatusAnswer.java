package esp3.message.incoming;

import esp3.message.incoming.ResponsePacket.ReturnCode;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class QueryStatusAnswer extends RemanResponse {
    private final int lastFunctionNumber;
    private final ReturnCode lastReturnCode;

    public QueryStatusAnswer(int functionNumber, ByteQueue data, ByteQueue optional) {
        super(functionNumber, data, optional);

        byte[] msg = getMessageData();
        lastFunctionNumber = ArrayUtils.bitRangeValue(msg, 12, 12);
        lastReturnCode = ReturnCode.forId(ArrayUtils.bitRangeValue(msg, 24, 8));
    }

    public int getLastFunctionNumber() {
        return lastFunctionNumber;
    }

    public ReturnCode getLastReturnCode() {
        return lastReturnCode;
    }
}
