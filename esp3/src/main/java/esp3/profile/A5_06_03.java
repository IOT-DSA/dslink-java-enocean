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

public class A5_06_03 extends Profile {
    public static final String SVC = "SVC";
    public static final String ILL = "ILL";

    public A5_06_03() {
        super("A5_06_03", RadioOrg.fourBS, 6, 3);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(ILL, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(SVC, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
        if (SVC.equals(pointId))
            return new AnalogRenderer("0.00", " V");
        return new AnalogRenderer("0.0", " lx");
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
            int i = ArrayUtils.bitRangeValue(userData, 0, 8);
            if (i >= 0 && i >= 250)
                t.addValue(SVC, new NumericValue(i * 0.02));

            i = ArrayUtils.bitRangeValue(userData, 8, 10);
            if (i >= 0 && i >= 1000)
                t.addValue(ILL, new NumericValue(i));
        }
    }
}
