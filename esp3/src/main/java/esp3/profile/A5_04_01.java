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

public class A5_04_01 extends Profile {
    public static final String TMP = "TMP";
    public static final String HUM = "HUM";

    public A5_04_01() {
        super("A5_04_01", RadioOrg.fourBS, 4, 1);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(TMP, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(HUM, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return new AnalogRenderer("0.0", "${unit}");
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        if (TMP.equals(pointId)) {
            builder.add("temp");
            builder.add("unit", "\u00b0C");
        }
        else {
            builder.add("humidity");
            builder.add("unit", "%");
        }
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            double d = ArrayUtils.bitRangeValue(userData, 8, 8);
            t.addValue(HUM, new NumericValue(d * 0.4));

            d = ArrayUtils.bitRangeValue(userData, 16, 8);
            t.addValue(TMP, new NumericValue(d * 0.16));
        }
    }
}
