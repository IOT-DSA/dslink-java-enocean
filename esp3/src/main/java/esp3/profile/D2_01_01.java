package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.view.text.TextRenderer;

// TODO
public class D2_01_01 extends Profile {
    //    public static final String CO = "CO";

    public D2_01_01() {
        super("D2_01_01", RadioOrg.VLD, 1, 1);
    }

    @Override
    protected void createPointInfo() {
        //        pointInfo.put(CO, new PointInfo(DataTypes.BINARY));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return null;
        //        return new BinaryTextRenderer("No contact", null, "Contact", null);
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        // no op
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        //        t.setLearn(ArrayUtils.bitRangeValue(radio.getUserData(), 4, 1) == 0);
        //
        //        boolean contact = ArrayUtils.bitRangeValue(radio.getUserData(), 7, 1) == 1;
        //        t.addValue(CO, new BinaryValue(contact));
    }
}
