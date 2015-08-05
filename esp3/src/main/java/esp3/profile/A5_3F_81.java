package esp3.profile;

import org.haystack.HDictBuilder;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;

public class A5_3F_81 extends Profile {

	public static final String IV1 = "IV1";
	public static final String IV2 = "IV2";
	public static final String IV3 = "IV3";
	
	public A5_3F_81() {
		super("A5_3F_81", RadioOrg.fourBS, 0x3f, 0x81);
	}

//	public A5_3F_81(String name, RadioOrg rorg, int func, int type) {
//		super(name, rorg, func, type);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
		return new AnalogRenderer("0.00", "V");
	}

	@Override
	protected void _parseTelegram(RadioPacket radio, TelegramData t) {
		byte[] userData = radio.getUserData();
		if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0) t.setLearn(true);
		
		parseVoltage(userData, t, IV3, 0, 8);
        parseVoltage(userData, t, IV2, 8, 8);
        parseVoltage(userData, t, IV1, 16, 8);
		

	}
	
	private void parseVoltage(byte[] userData, TelegramData t, String id, int offset, int length) {
        int i = ArrayUtils.bitRangeValue(userData, offset, length);
        
        double d = i/25.5;

        t.addValue(id, new NumericValue(d));
    }

	@Override
	protected void addTags(String pointId, HDictBuilder builder) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createPointInfo() {
		pointInfo.put(IV1, new PointInfo(DataTypes.NUMERIC, false));
		pointInfo.put(IV2, new PointInfo(DataTypes.NUMERIC, false));
		pointInfo.put(IV3, new PointInfo(DataTypes.NUMERIC, false));

	}

}
