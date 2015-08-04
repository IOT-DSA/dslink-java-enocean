package esp3.profile.a5;

import java.io.IOException;
import java.util.Map;

import org.haystack.HDictBuilder;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

import esp3.EnOceanModule;
import esp3.message.RadioOrg;
import esp3.message.request.RadioRequest;
import esp3.profile.a5.xml.EnocianType;

public class A5_20_03 extends GenericProfile {
	public static final String VSP = "VSP";
	public static final String SPS = "SPS";
	public static final String ATS = "ATS";
	public static final String TMPRC = "TMPRC";

	public A5_20_03(EnocianType enocianType) {
		super("A5_20_3", 0x20, 0x03, enocianType);
	}
	
	@Override
	protected void createPointInfo() {
		pointInfo.put(VSP, new PointInfo(DataTypes.BINARY, false));
		pointInfo.put(SPS, new PointInfo(DataTypes.BINARY, false));
		pointInfo.put(ATS, new PointInfo(DataTypes.NUMERIC, true));
		pointInfo.put(TMPRC, new PointInfo(DataTypes.NUMERIC, false));
		
	}

	@Override
	protected void addTags(String pointId, HDictBuilder builder) {
		builder.add("cmd");
	}
	
	@Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module, Map<String, DataValue> allVals)
            throws IOException {
		double dval = value.getDoubleValue();
		if (dval < 0) dval = 0;
		if (dval > 100) dval = 100;
		byte bval = (byte) dval;
		RadioRequest req = new RadioRequest(targetId, baseIdOffset, RadioOrg.fourBS, new byte[] { bval, 0x00, 0x00, 0x08 }, 0x00);
		module.send(req, null);
	}
	
	@Override
    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
        
	}

}
