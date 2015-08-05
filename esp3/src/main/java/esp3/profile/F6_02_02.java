package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;

public class F6_02_02 extends Profile {
    public static final String CHANNEL_A = "A";
    public static final String CHANNEL_B = "B";

    public static final int STATE_OFF = 0;
    public static final int STATE_O = 1;
    public static final int STATE_I = 2;

    public F6_02_02() {
        super("F6_02_02", RadioOrg.RPS, 2, 2);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(CHANNEL_B, new PointInfo(DataTypes.MULTISTATE, false));
        pointInfo.put(CHANNEL_A, new PointInfo(DataTypes.MULTISTATE, false));
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
        int channelA;
        int channelB;
        if (radio.getStatus() == 0x20) {
            channelA = STATE_OFF;
            channelB = STATE_OFF;
        }
        else {
            byte b = radio.getUserData()[0];
            switch (b) {
            case 0x50:
                channelA = STATE_OFF;
                channelB = STATE_I;
                break;
            case 0x70:
                channelA = STATE_OFF;
                channelB = STATE_O;
                break;
            case 0x10:
                channelA = STATE_I;
                channelB = STATE_OFF;
                break;
            case 0x30:
                channelA = STATE_O;
                channelB = STATE_OFF;
                break;
            case 0x15:
                channelA = STATE_I;
                channelB = STATE_I;
                break;
            case 0x35:
                channelA = STATE_O;
                channelB = STATE_I;
                break;
            case 0x17:
                channelA = STATE_I;
                channelB = STATE_O;
                break;
            case 0x37:
                channelA = STATE_O;
                channelB = STATE_O;
                break;
            default:
                channelA = STATE_OFF;
                channelB = STATE_OFF;
            }
        }

        t.addValue(CHANNEL_A, new MultistateValue(channelA));
        t.addValue(CHANNEL_B, new MultistateValue(channelB));
    }
}
