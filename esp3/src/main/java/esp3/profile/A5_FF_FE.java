package esp3.profile;

import java.io.IOException;

import org.haystack.HDictBuilder;

import esp3.EnOceanModule;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import esp3.message.incoming.ResponsePacket.ReturnCode;
import esp3.message.request.RadioRequest;
import esp3.message.request.reman.Learn;
import esp3.message.request.reman.Lock;
import esp3.message.request.reman.Unlock;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.ThreadUtils;

public class A5_FF_FE extends Profile {
    public static final String REL = "REL";

    public A5_FF_FE() {
        super("A5_FF_FE", RadioOrg.fourBS, 0xff, 0xfe);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(REL, new PointInfo(DataTypes.BINARY, true));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        return new BinaryTextRenderer("Open", null, "Closed", null);
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        builder.add("cmd");
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            boolean closed = ArrayUtils.bitRangeValue(userData, 31, 1) == 1;
            t.addValue(REL, new BinaryValue(closed));
        }
    }

    @Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module)
            throws IOException {
        RadioRequest req;
        if (value.getBooleanValue())
            // Close
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x70 }, 0xb0);
        else
            // Open            
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x50 }, 0xb0);
        module.send(req, null);
    }

    @Override
    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
        Unlock unlock = new Unlock(securityCode);
        module.send(unlock);
        if (unlock.getReturnCode() != ReturnCode.ok)
            throw new IOException("Bad unlock return code: " + unlock.getReturnCode());

        Learn startLearn = new Learn(targetId, true);
        module.send(startLearn);
        if (startLearn.getReturnCode() != ReturnCode.ok)
            throw new IOException("Bad startLearn return code: " + startLearn.getReturnCode());

        RadioRequest radioTL = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x50 }, 0xb0);
        RadioRequest radioOff = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x0 }, 0xa0);
        for (int i = 0; i < 3; i++) {
            module.send(radioTL);
            if (radioTL.getReturnCode() != ReturnCode.ok)
                throw new IOException("Bad press " + (i + 1) + " return code: " + radioTL.getReturnCode());

            ThreadUtils.sleep(300);

            module.send(radioOff);
            if (radioOff.getReturnCode() != ReturnCode.ok)
                throw new IOException("Bad release " + (i + 1) + " return code: " + radioOff.getReturnCode());
            ThreadUtils.sleep(300);
        }

        Learn stopLearn = new Learn(targetId, false);
        module.send(stopLearn);
        if (stopLearn.getReturnCode() != ReturnCode.ok)
            throw new IOException("Bad stopLearn return code: " + stopLearn.getReturnCode());

        Lock lock = new Lock(securityCode);
        module.send(lock);
        if (lock.getReturnCode() != ReturnCode.ok)
            throw new IOException("Bad lock return code: " + lock.getReturnCode());
    }
}
