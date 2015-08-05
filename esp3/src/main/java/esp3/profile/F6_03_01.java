package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

public class F6_03_01 extends Profile {
    public static final String CHANNEL_A = "A";
    public static final String CHANNEL_B = "B";
    public static final String CHANNEL_C = "C";
    public static final String CHANNEL_D = "D";

    public static final int STATE_OFF = 0;
    public static final int STATE_O = 1;
    public static final int STATE_I = 2;

    public F6_03_01() {
        super("F6_03_01", RadioOrg.RPS, 3, 1);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(CHANNEL_A, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(CHANNEL_B, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(CHANNEL_C, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(CHANNEL_D, new PointInfo(DataTypes.MULTISTATE, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
        MultistateRenderer r = new MultistateRenderer();
        r.addMultistateValue(STATE_OFF, "Off", null);
        r.addMultistateValue(STATE_O, "O", null);
        r.addMultistateValue(STATE_I, "I", null);
        return r;
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        // no op
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        int channelA = -1;
        int channelB = -1;
        int channelC = -1;
        int channelD = -1;

        int r1 = ArrayUtils.bitRangeValue(radio.getUserData(), 0, 3);
        boolean p1 = ArrayUtils.bitRangeValue(radio.getUserData(), 3, 1) == 1;

        if (r1 == 0)
            channelA = p1 ? STATE_I : STATE_OFF;
        else if (r1 == 1)
            channelA = p1 ? STATE_O : STATE_OFF;
        else if (r1 == 2)
            channelB = p1 ? STATE_I : STATE_OFF;
        else if (r1 == 3)
            channelB = p1 ? STATE_O : STATE_OFF;
        else if (r1 == 4)
            channelC = p1 ? STATE_I : STATE_OFF;
        else if (r1 == 5)
            channelC = p1 ? STATE_O : STATE_OFF;
        else if (r1 == 6)
            channelD = p1 ? STATE_I : STATE_OFF;
        else if (r1 == 7)
            channelD = p1 ? STATE_O : STATE_OFF;

        int r2 = ArrayUtils.bitRangeValue(radio.getUserData(), 4, 3);
        boolean p2 = ArrayUtils.bitRangeValue(radio.getUserData(), 7, 1) == 1;

        if (r2 == 0)
            channelA = p2 ? STATE_I : STATE_OFF;
        else if (r2 == 1)
            channelA = p2 ? STATE_O : STATE_OFF;
        else if (r2 == 2)
            channelB = p2 ? STATE_I : STATE_OFF;
        else if (r2 == 3)
            channelB = p2 ? STATE_O : STATE_OFF;
        else if (r2 == 4)
            channelC = p2 ? STATE_I : STATE_OFF;
        else if (r2 == 5)
            channelC = p2 ? STATE_O : STATE_OFF;
        else if (r2 == 6)
            channelD = p2 ? STATE_I : STATE_OFF;
        else if (r2 == 7)
            channelD = p2 ? STATE_O : STATE_OFF;

        if (channelA != -1)
            t.addValue(CHANNEL_A, new MultistateValue(channelA));
        if (channelB != -1)
            t.addValue(CHANNEL_B, new MultistateValue(channelB));
        if (channelC != -1)
            t.addValue(CHANNEL_C, new MultistateValue(channelC));
        if (channelD != -1)
            t.addValue(CHANNEL_D, new MultistateValue(channelD));
    }
}
