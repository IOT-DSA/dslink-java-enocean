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

public class A5_09_04 extends Profile {

    public static final String TMP = "TMP";
    public static final String HUM = "HUM";
    public static final String CO2 = "CO2";

    public A5_09_04() {
        super("A5_09_04", RadioOrg.fourBS, 9, 4);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(TMP, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(HUM, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(CO2, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return new AnalogRenderer("0.0", "${unit}");
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        switch (pointId) {
            case TMP:
                builder.add("temp");
                builder.add("unit", "C");
                break;
            case HUM:
                builder.add("humidity");
                builder.add("unit", "%");
                break;
            default:
                builder.add("co2");
                builder.add("unit", "ppm");
        }
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0) {
            t.setLearn(true);
        } else {
            double d = ArrayUtils.bitRangeValue(userData, 0, 8);
            t.addValue(HUM, new NumericValue(d / 2));

            d = ArrayUtils.bitRangeValue(userData, 8, 8);
            t.addValue(CO2, new NumericValue(d * 10));

            d = ArrayUtils.bitRangeValue(userData, 16, 8);
            t.addValue(TMP, new NumericValue(d / 5));
        }
    }
}
