package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

public class A5_07_01 extends Profile {
    public static final String SVC = "SVC";
    public static final String PIRS = "PIRS";

    public A5_07_01() {
        super("A5_07_01", RadioOrg.fourBS, 7, 1);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(PIRS, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(SVC, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
        if (SVC.equals(pointId))
            return new AnalogRenderer("0.00", "V");
        return new BinaryTextRenderer("Vacant", null, "Occupied", null);
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        // no op
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            if (ArrayUtils.bitRangeValue(userData, 31, 1) == 1) {
                double d = ((double) ArrayUtils.bitRangeValue(userData, 0, 8)) / 50;
                t.addValue(SVC, new NumericValue(d));
            }

            boolean pir = ArrayUtils.bitRangeValue(userData, 16, 8) > 127;
            t.addValue(PIRS, new BinaryValue(pir));
        }
    }
}
