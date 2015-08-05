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

public class F6_10_00 extends Profile {
    public static final String WIN = "WIN";

    //    public static final int TR_CW = 0;
    //    public static final int BR_CW = 1;
    //    public static final int BL_CW = 2;
    //    public static final int TL_CW = 3;
    //    public static final int TR_CCW = 4;
    //    public static final int BR_CCW = 5;
    //    public static final int BL_CCW = 6;
    //    public static final int TL_CCW = 7;

    public static final int HORZ = 0;
    public static final int DOWN = 1;
    public static final int UP = 2;

    public F6_10_00() {
        super("F6_10_00", RadioOrg.RPS, 16, 0);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(WIN, new PointInfo(DataTypes.MULTISTATE, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
        MultistateRenderer r = new MultistateRenderer();

        //        r.addMultistateValue(TR_CW, "Top right CW", null);
        //        r.addMultistateValue(BR_CW, "Bottom right CW", null);
        //        r.addMultistateValue(BL_CW, "Bottom left CW", null);
        //        r.addMultistateValue(TL_CW, "Top left CW", null);
        //        r.addMultistateValue(TR_CCW, "Top right CCW", null);
        //        r.addMultistateValue(BR_CCW, "Bottom right CCW", null);
        //        r.addMultistateValue(BL_CCW, "Bottom left CCW", null);
        //        r.addMultistateValue(TL_CCW, "Top left CCW", null);

        r.addMultistateValue(HORZ, "Horizontal", null);
        r.addMultistateValue(DOWN, "Down", null);
        r.addMultistateValue(UP, "Up", null);

        return r;
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        // no op
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        int movement = -1;

        int i = ArrayUtils.bitRangeValue(radio.getUserData(), 0, 4);

        //        switch (i) {
        //        case 12:
        //        case 14:
        //            movement = TR_CW;
        //            break;
        //        case 15:
        //            movement = BR_CW;
        //            break;
        //        case 15:
        //            movement = BR_CW;
        //            break;
        //        }

        switch (i) {
        case 12:
        case 14:
            movement = HORZ;
            break;
        case 15:
            movement = DOWN;
            break;
        case 13:
            movement = UP;
            break;
        }

        if (movement != -1)
            t.addValue(WIN, new MultistateValue(movement));
    }
}
