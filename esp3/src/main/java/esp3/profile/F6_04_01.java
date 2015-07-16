package esp3.profile;

import org.haystack.HDictBuilder;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

public class F6_04_01 extends Profile {
    public static final String KC = "KC";

    public F6_04_01() {
        super("F6_04_01", RadioOrg.RPS, 4, 1);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(KC, new PointInfo(DataTypes.BINARY, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return new BinaryTextRenderer("Empty", null, "Inserted", null);
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        // no op
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        boolean inserted = ArrayUtils.bitRangeValue(radio.getUserData(), 0, 8) == 0x70;
        t.addValue(KC, new BinaryValue(inserted));
    }
}
