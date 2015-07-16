package esp3.message;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

public class TelegramData {
    private boolean learn;
    private final Map<String, DataValue> values = new HashMap<>();

    public boolean isLearn() {
        return learn;
    }

    public void setLearn(boolean learn) {
        this.learn = learn;
    }

    public void addValue(String id, DataValue value) {
        values.put(id, value);
    }

    public DataValue getValue(String id) {
        return values.get(id);
    }
}
