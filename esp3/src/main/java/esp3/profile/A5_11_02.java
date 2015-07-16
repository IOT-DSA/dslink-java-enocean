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

public class A5_11_02 extends Profile {
    public static final String CVAR = "CVAR";
    public static final String FAN = "FAN";
    public static final String ASP = "ASP";
    public static final String ALR = "ALR";
    public static final String CTM = "CTM";
    public static final String CST = "CST";
    public static final String ERH = "ERH";
    public static final String RO = "RO";

    public A5_11_02() {
        super("A5_11_02", RadioOrg.fourBS, 17, 2);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(CVAR, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(FAN, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(ASP, new PointInfo(DataTypes.NUMERIC, false));
        pointInfo.put(ALR, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(CTM, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(CST, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(ERH, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(RO, new PointInfo(DataTypes.MULTISTATE, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        if (CVAR.equals(pointId))
            return new AnalogRenderer("0.0", "%");
        if (FAN.equals(pointId)) {
            MultistateRenderer r = new MultistateRenderer();
            r.addMultistateValue(0, "Stage 0 manual", null);
            r.addMultistateValue(1, "Stage 1 manual", null);
            r.addMultistateValue(2, "Stage 2 manual", null);
            r.addMultistateValue(3, "Stage 3 manual", null);
            r.addMultistateValue(16, "Stage 0 automatic", null);
            r.addMultistateValue(17, "Stage 1 automatic", null);
            r.addMultistateValue(18, "Stage 2 automatic", null);
            r.addMultistateValue(19, "Stage 3 automatic", null);
            r.addMultistateValue(255, "Not available", null);
            return r;
        }
        if (ASP.equals(pointId))
            return new AnalogRenderer("0.0", " &deg;C");
        if (ALR.equals(pointId))
            return new BinaryTextRenderer("No alarm", "green", "Alarm", "red");
        if (CTM.equals(pointId)) {
            MultistateRenderer r = new MultistateRenderer();
            r.addMultistateValue(1, "Heating", null);
            r.addMultistateValue(2, "Cooling", null);
            r.addMultistateValue(3, "Off", null);
            return r;
        }
        if (CST.equals(pointId))
            return new BinaryTextRenderer("Automatic", null, "Override", null);
        if (ERH.equals(pointId))
            return new BinaryTextRenderer("Normal", null, "Energy hold-off / Dew point", null);

        MultistateRenderer r = new MultistateRenderer();
        r.addMultistateValue(0, "Occupied", null);
        r.addMultistateValue(1, "Unoccupied", null);
        r.addMultistateValue(2, "StandBy", null);
        r.addMultistateValue(3, "Frost", null);
        return r;
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
            t.addValue(CVAR, new NumericValue(d / 2.55));
            t.addValue(FAN, new MultistateValue(ArrayUtils.bitRangeValue(userData, 8, 8)));
            t.addValue(ASP, new NumericValue(ArrayUtils.bitRangeValue(userData, 16, 8) / 5D));
            t.addValue(ALR, new BinaryValue(ArrayUtils.bitRangeValue(userData, 24, 1) == 1));
            t.addValue(CTM, new MultistateValue(ArrayUtils.bitRangeValue(userData, 25, 2)));
            t.addValue(CST, new BinaryValue(ArrayUtils.bitRangeValue(userData, 27, 1) == 1));
            t.addValue(ERH, new BinaryValue(ArrayUtils.bitRangeValue(userData, 29, 1) == 1));
            t.addValue(RO, new MultistateValue(ArrayUtils.bitRangeValue(userData, 30, 2)));
        }
    }
}
