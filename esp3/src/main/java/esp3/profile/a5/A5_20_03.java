package esp3.profile.a5;

import java.io.IOException;
import java.util.Map;

import org.haystack.HDictBuilder;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;

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
		caseInfo.get(1).put(VSP, new PointInfo(DataTypes.BINARY, true));
		caseInfo.get(1).put(SPS, new PointInfo(DataTypes.BINARY, true));
		caseInfo.get(1).put(ATS, new PointInfo(DataTypes.NUMERIC, true));
		caseInfo.get(1).put(TMPRC, new PointInfo(DataTypes.NUMERIC, false));
		
	}
	
	@Override
	public int defaultCase() {
		return 1;
	}
	
	@Override
	public int getNumCases() {
		return 2;
	}

	@Override
	protected void addTags(String pointId, HDictBuilder builder) {
		builder.add("cmd");
	}
	
	@Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module, Map<String, DataValue> allVals)
            throws IOException {
		DataValue spsv = allVals.getOrDefault(SPS, new BinaryValue(false));
		if (spsv == null) spsv = new BinaryValue(false);
		boolean sps = spsv.getBooleanValue();
		DataValue vspv = allVals.getOrDefault(VSP, new BinaryValue(false));
		if (vspv == null) vspv = new BinaryValue(false);
		boolean vsp = vspv.getBooleanValue();
		DataValue dvalv = allVals.getOrDefault(ATS, new NumericValue(0));
		if (dvalv == null) dvalv = new NumericValue(0);
		double dval =dvalv.getDoubleValue();
		if (dval < 0) dval = 0;
		if (sps) {
			if (dval > 40) dval = 40;
			dval = dval*(255.0/40.0);
		} else {
			if (dval > 100) dval = 100;
		}
		byte bval3 = (byte) dval;
		int spsbit = sps ? 0x4: 0x0;
		int vspbit = vsp ? 0x2: 0x0;
		byte bval1 = (byte) (spsbit | vspbit);
		RadioRequest req = new RadioRequest(targetId, baseIdOffset, RadioOrg.fourBS, new byte[] { bval3, 0x00, bval1, 0x08 }, 0x00);
		module.send(req, null);
	}
	
	@Override
    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
        
	}

}
