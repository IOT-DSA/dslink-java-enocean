package esp3.profile.a5;

import java.io.IOException;
import java.util.Map;

import org.haystack.HDictBuilder;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.TextRenderer;

import esp3.EnOceanModule;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import esp3.message.request.RadioRequest;
import esp3.profile.Profile;
import esp3.profile.Profile.PointInfo;
import esp3.profile.a5.xml.EnocianType;

public class A5_38_08 extends GenericProfile {
	
	public static final String COM = "COM";
	public static final String TIM = "TIM";
	public static final String LCK = "LCK";
	public static final String DEL = "DEL";
	public static final String SW = "SW";

	public A5_38_08 (EnocianType en) {
		super("A5_38_8", 0x38, 0x08, en);
	}

	@Override
	protected void createPointInfo() {
		pointInfo.put(COM, new PointInfo(DataTypes.BINARY, false));
		pointInfo.put(TIM, new PointInfo(DataTypes.BINARY, true));
		pointInfo.put(LCK, new PointInfo(DataTypes.BINARY, true));
		pointInfo.put(DEL, new PointInfo(DataTypes.BINARY, true));
		pointInfo.put(SW, new PointInfo(DataTypes.BINARY, true));
	}
	
	@Override
	protected void addTags(String pointId, HDictBuilder builder) {
		builder.add("cmd");
	}

	@Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module, Map<String, DataValue> map)
            throws IOException {
		int tval = (int) (map.get(TIM).getDoubleValue() * 10);
		byte b1 = (byte) (tval >>> 8);
		byte b2 = (byte) (tval & 0xFF);
		byte b3 = 0;
		b3 = (byte) (b3 | 0x08);
		if (map.get(LCK).getIntegerValue() == 1)
			b3 = (byte) (b3 | 0x04);
		if (map.get(DEL).getIntegerValue() == 1)
			b3 = (byte) (b3 | 0x02);
		if (map.get(SW).getIntegerValue() == 1)
			b3 = (byte) (b3 | 0x01);
		RadioRequest req = new RadioRequest(targetId, baseIdOffset, RadioOrg.fourBS, new byte[] { 0x01, b1, b2, b3 }, 0x00);
		module.send(req, null);
	}

}
