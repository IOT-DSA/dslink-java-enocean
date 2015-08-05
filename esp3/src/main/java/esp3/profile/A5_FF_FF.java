package esp3.profile;

import java.io.IOException;
import java.util.Map;

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
import esp3.profile.config.ParameterDef;
import esp3.profile.config.ParameterDef.Type;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.ThreadUtils;

public class A5_FF_FF extends Profile {
    public static final String REL = "REL";

    public A5_FF_FF() {
        super("A5_FF_FF", RadioOrg.fourBS, 0xff, 0xff);

        // Parameters
        parameterList.add(new ParameterDef(0, "enocean.param.A5_FF_FF.0", 2, Type.numeric, "common.tp.seconds", null));
        parameterList.add(new ParameterDef(1, "enocean.param.A5_FF_FF.1", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(2, "enocean.param.A5_FF_FF.2", 2, Type.numeric, "common.tp.seconds", null));
        parameterList.add(new ParameterDef(3, "enocean.param.A5_FF_FF.3", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(4, "enocean.param.A5_FF_FF.4", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(5, "enocean.param.A5_FF_FF.5", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(6, "enocean.param.A5_FF_FF.6", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(7, "enocean.param.A5_FF_FF.7", 2, Type.numeric, "common.tp.minutes", null));
        parameterList.add(new ParameterDef(8, "enocean.param.A5_FF_FF.8", 2, Type.numeric, "common.tp.seconds", null));
        parameterList.add(new ParameterDef(9, "enocean.param.A5_FF_FF.9", 2, Type.notSupported, null, null));
        parameterList.add(new ParameterDef(10, "enocean.param.A5_FF_FF.10", 2, Type.notSupported, null, null));
        parameterList.add(new ParameterDef(11, "enocean.param.A5_FF_FF.11", 2, Type.notSupported, null, null));
        parameterList.add(new ParameterDef(12, "enocean.param.A5_FF_FF.12", 2, Type.notSupported, null, null));
        parameterList.add(new ParameterDef(13, "enocean.param.A5_FF_FF.13", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.1hop"),
                        new IntStringPair(2, "enocean.param.unit.2hop"), }));
        parameterList.add(new ParameterDef(14, "enocean.param.A5_FF_FF.14", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"), }));
        parameterList.add(new ParameterDef(15, "enocean.param.A5_FF_FF.15", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"),
                        new IntStringPair(255, "enocean.param.unit.auto"), }));
        parameterList.add(new ParameterDef(16, "enocean.param.A5_FF_FF.16", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"), }));
        parameterList.add(new ParameterDef(17, "enocean.param.A5_FF_FF.17", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.normal"),
                        new IntStringPair(1, "enocean.param.unit.inverse"), }));
        parameterList.add(new ParameterDef(18, "enocean.param.A5_FF_FF.18", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"), }));
        parameterList
                .add(new ParameterDef(19, "enocean.param.A5_FF_FF.19", 2, Type.numeric, "common.tp.seconds", null));
        parameterList.add(new ParameterDef(20, "enocean.param.A5_FF_FF.20", 4, Type.numeric, "blank", null));
        parameterList.add(new ParameterDef(21, "enocean.param.A5_FF_FF.21", 1, Type.numeric, "blank", null));
        parameterList.add(new ParameterDef(22, "enocean.param.A5_FF_FF.22", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"), }));
        parameterList.add(new ParameterDef(23, "enocean.param.A5_FF_FF.23", 1, Type.enumeration, null,
                new IntStringPair[] { new IntStringPair(0, "enocean.param.unit.disabled"),
                        new IntStringPair(1, "enocean.param.unit.enabled"), }));
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(REL, new PointInfo(DataTypes.BINARY, true));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId, int caseNum) {
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
            //            boolean inverse = ArrayUtils.bitRangeValue(userData, 29, 1) == 1;
            boolean closed = ArrayUtils.bitRangeValue(userData, 31, 1) == 1;
            //            if (inverse)
            //                closed = !closed;
            t.addValue(REL, new BinaryValue(closed));
        }
    }

    @Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module, Map<String, DataValue> allVals)
            throws IOException {
        RadioRequest req;
        if (value.getBooleanValue())
            // Close
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x50 }, 0xb0);
        else
            // Open            
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x70 }, 0xb0);
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
