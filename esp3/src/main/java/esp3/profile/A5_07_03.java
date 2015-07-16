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

public class A5_07_03 extends Profile {
    public static final String SVC = "SVC";
    public static final String ILL = "ILL";
    public static final String PIRS = "PIRS";

    public A5_07_03() {
        super("A5_07_03", RadioOrg.fourBS, 7, 3);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(PIRS, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(ILL, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(SVC, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        if (SVC.equals(pointId))
            return new AnalogRenderer("0.00", "V");
        if (ILL.equals(pointId))
            return new AnalogRenderer("0.00", "lx");
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
            double d = ((double) ArrayUtils.bitRangeValue(userData, 0, 8)) / 50;
            t.addValue(SVC, new NumericValue(d));

            int i = ArrayUtils.bitRangeValue(userData, 8, 10);
            if (i >= 0 && i <= 1000)
                t.addValue(ILL, new NumericValue(i));

            boolean pir = ArrayUtils.bitRangeValue(userData, 24, 1) == 1;
            t.addValue(PIRS, new BinaryValue(pir));
        }
    }
}
