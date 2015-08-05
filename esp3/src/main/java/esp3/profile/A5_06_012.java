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

public class A5_06_012 extends Profile {
    public static final String SVC = "SVC";
    public static final String ILL1 = "ILL1";
    public static final String ILL2 = "ILL2";

    private final int from1;
    private final int to1;
    private final int from2;
    private final int to2;

    public A5_06_012(String name, int type, int from1, int to1, int from2, int to2) {
        super(name, RadioOrg.fourBS, 6, type);
        this.from1 = from1;
        this.to1 = to1;
        this.from2 = from2;
        this.to2 = to2;
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(ILL1, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(ILL2, new PointInfo(DataTypes.NUMERIC, false));
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
            double d = ArrayUtils.bitRangeValue(userData, 0, 8);
            t.addValue(SVC, new NumericValue(d * 0.02));

            if (ArrayUtils.bitRangeValue(userData, 31, 1) == 0) {
                d = ArrayUtils.bitRangeValue(userData, 8, 8);
                t.addValue(ILL2, new NumericValue(d / 255 * (to1 - from1) + from1));
            }
            else {
                d = ArrayUtils.bitRangeValue(userData, 16, 8);
                t.addValue(ILL1, new NumericValue(d / 255 * (to2 - from2) + from2));
            }
        }
    }
}
