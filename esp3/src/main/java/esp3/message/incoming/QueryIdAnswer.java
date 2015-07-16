package esp3.message.incoming;

import esp3.message.RadioOrg;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class QueryIdAnswer extends RemanResponse {
    private final RadioOrg rorg;
    private final int func;
    private final int type;

    public QueryIdAnswer(int functionNumber, ByteQueue data, ByteQueue optional) {
        super(functionNumber, data, optional);

        byte[] msg = getMessageData();
        rorg = RadioOrg.forId(ArrayUtils.bitRangeValue(msg, 0, 8));
        func = ArrayUtils.bitRangeValue(msg, 8, 6);
        type = ArrayUtils.bitRangeValue(msg, 14, 7);
    }

    public RadioOrg getRorg() {
        return rorg;
    }

    public int getFunc() {
        return func;
    }

    public int getType() {
        return type;
    }
}
