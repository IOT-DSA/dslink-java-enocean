package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

public class A5_02_b extends Profile {
    public static final String TMP = "TMP";

    private final double from;
    private final double to;

    public A5_02_b(String name, int type, double from, double to) {
        super(name, RadioOrg.fourBS, 2, type);
        this.from = from;
        this.to = to;
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(TMP, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return new AnalogRenderer("0.0", "${unit}");
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        builder.add("temp");
        builder.add("unit", "\u00b0C");
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            double d = ArrayUtils.bitRangeValue(userData, 14, 10);
            d /= 255;
            d = 1 - d;
            d *= to - from;
            d -= from;
            t.addValue(TMP, new NumericValue(d));
        }
    }
}
