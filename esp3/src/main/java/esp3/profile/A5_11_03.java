package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

public class A5_11_03 extends Profile {
    public static final String BSP = "BSP";
    public static final String AN = "AN";
    public static final String ES = "ES";
    public static final String EP = "EP";
    public static final String ST = "ST";
    public static final String SM = "SM";
    public static final String MOTP = "MOTP";

    public A5_11_03() {
        super("A5_11_03", RadioOrg.fourBS, 17, 3);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(BSP, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(AN, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(ES, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(EP, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(ST, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(SM, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(MOTP, new PointInfo(DataTypes.BINARY, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        if (BSP.equals(pointId))
            return new AnalogRenderer("0", "%");
        if (AN.equals(pointId))
            return new AnalogRenderer("0", "&deg;");
        if (ES.equals(pointId)) {
            MultistateRenderer r = new MultistateRenderer();
            r.addMultistateValue(0, "No error", "green");
            r.addMultistateValue(1, "End-positions are not configured", "red");
            r.addMultistateValue(2, "Internal failure", "red");
            return r;
        }
        if (EP.equals(pointId)) {
            MultistateRenderer r = new MultistateRenderer();
            r.addMultistateValue(0, "No end position available", null);
            r.addMultistateValue(1, "No end position reached", null);
            r.addMultistateValue(2, "Blind fully open", null);
            r.addMultistateValue(3, "Blind fully closed", null);
            return r;
        }
        if (ST.equals(pointId)) {
            MultistateRenderer r = new MultistateRenderer();
            r.addMultistateValue(0, "No status available", null);
            r.addMultistateValue(1, "Blind is stopped", null);
            r.addMultistateValue(2, "Blind opens", null);
            r.addMultistateValue(3, "Blind closes", null);
            return r;
        }
        if (SM.equals(pointId))
            return new BinaryTextRenderer("Normal", null, "Service", null);
        return new BinaryTextRenderer("Normal", null, "Inverse", null);
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
            if (ArrayUtils.bitRangeValue(userData, 16, 1) == 1) {
                int i = ArrayUtils.bitRangeValue(userData, 0, 8);
                if (i >= 0 && i <= 100)
                    t.addValue(BSP, new NumericValue(i));
            }

            if (ArrayUtils.bitRangeValue(userData, 17, 1) == 1) {
                int sign = ArrayUtils.bitRangeValue(userData, 8, 1) == 1 ? 1 : -1;
                t.addValue(AN, new NumericValue(ArrayUtils.bitRangeValue(userData, 9, 7) * 2 * sign));
            }

            t.addValue(ES, new MultistateValue(ArrayUtils.bitRangeValue(userData, 18, 2)));
            t.addValue(EP, new MultistateValue(ArrayUtils.bitRangeValue(userData, 20, 2)));
            t.addValue(ST, new MultistateValue(ArrayUtils.bitRangeValue(userData, 22, 2)));
            t.addValue(SM, new BinaryValue(ArrayUtils.bitRangeValue(userData, 24, 1) == 1));
            t.addValue(MOTP, new BinaryValue(ArrayUtils.bitRangeValue(userData, 25, 1) == 1));
        }
    }
}
