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

public class A5_02_7F extends Profile {
    public static final String CH1 = "CH1";
    public static final String CH2 = "CH2";
    public static final String CH3 = "CH3";

    public A5_02_7F() {
        super("A5_02_7F", RadioOrg.fourBS, 2, 0x7f);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(CH1, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(CH2, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(CH3, new PointInfo(DataTypes.NUMERIC, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
        return new AnalogRenderer("0.0", "${unit}");
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        builder.add("temp");
        builder.add("unit", "\u00b0F");
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            parseTemp(userData, t, CH1, 0, 8);
            parseTemp(userData, t, CH2, 8, 8);
            parseTemp(userData, t, CH3, 16, 8);
        }
    }

    private void parseTemp(byte[] userData, TelegramData t, String id, int offset, int length) {
        int i = ArrayUtils.bitRangeValue(userData, offset, length);
        if (i == 255)
            return;

        double d;
        if (i == 253)
            d = 39;
        else if (i == 254)
            d = 167;
        else
            d = i * 0.5 + 40;

        t.addValue(id, new NumericValue(d));
    }
}
