package esp3.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.haystack.HDictBuilder;

import esp3.EnOceanModule;
import esp3.message.LinkTableEntry;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import esp3.message.incoming.ResponsePacket.ReturnCode;
import esp3.message.request.RadioRequest;
import esp3.message.request.recom.GetLinkTableContent;
import esp3.message.request.recom.GetLinkTableInfo;
import esp3.message.request.recom.SetLinkTableContent;
import esp3.message.request.reman.Lock;
import esp3.message.request.reman.QueryStatus;
import esp3.message.request.reman.Unlock;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.messaging.TimeoutException;
import com.serotonin.util.ArrayUtils;

public class A5_11_01 extends Profile {
    public static final String REL = "REL";
    public static final String REP = "REP";
    public static final String OCC = "OCC";

    public A5_11_01() {
        super("A5_11_01", RadioOrg.fourBS, 17, 1);
    }

    @Override
    protected void createPointInfo() {
        pointInfo.put(REL, new PointInfo(DataTypes.BINARY, true));
        pointInfo.put(REP, new PointInfo(DataTypes.BINARY, false));
        pointInfo.put(OCC, new PointInfo(DataTypes.BINARY, false));
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        if (REP.equals(pointId))
            return new BinaryTextRenderer("Disabled", null, "Enabled", null);
        if (OCC.equals(pointId))
            return new BinaryTextRenderer("Occupied", null, "Unoccupied", null);
        return new BinaryTextRenderer("Open", null, "Closed", null);
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
        if (REL.equals(pointId))
            builder.add("cmd");
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        byte[] userData = radio.getUserData();
        if (ArrayUtils.bitRangeValue(userData, 28, 1) == 0)
            t.setLearn(true);
        else {
            t.addValue(REP, new BinaryValue(ArrayUtils.bitRangeValue(userData, 24, 1) == 1));
            t.addValue(OCC, new BinaryValue(ArrayUtils.bitRangeValue(userData, 30, 1) == 1));

            boolean inverse = ArrayUtils.bitRangeValue(userData, 29, 1) == 1;
            boolean closed = ArrayUtils.bitRangeValue(userData, 31, 1) == 1;
            if (inverse)
                closed = !closed;
            t.addValue(REL, new BinaryValue(closed));
        }
    }

    @Override
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module)
            throws IOException {
        RadioRequest req;
        if (value.getBooleanValue())
            // Close
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x50 }, 0xb0);
        else
            // Open            
            req = new RadioRequest(targetId, baseIdOffset, RadioOrg.RPS, new byte[] { 0x70 }, 0xb0);

        module.send(req, null);
        //
        //        // TODO do we really need to do this?
        //        ThreadUtils.sleep(110);
        //        req.reset();
        //        module.send(req);
    }

    @Override
    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
        long baseIdToUse = module.getBaseId() + baseIdOffset;

        String stage = null;
        try {
            // Unlock the device.
            module.send(new Unlock(targetId, securityCode));

            // Ensure the unlock was successful.
            stage = "Unlock status";
            QueryStatus queryStatus = module.send(new QueryStatus(targetId));
            if (queryStatus.getAnswer().getLastReturnCode() != ReturnCode.ok)
                throw new IOException("Unlock return code: " + queryStatus.getAnswer().getLastReturnCode());

            stage = "GetLinkTableInfo";
            GetLinkTableInfo info = module.send(new GetLinkTableInfo(targetId));
            if (!info.isInboundLinkTableSupported())
                throw new IOException("Device does not support inbound link table");

            int index;
            if (info.getLengthOfInboundTable() > 0) {
                index = -1;

                // TODO don't try to get the whole table at once. Split requests into smallish chunks, like 3 at a time.

                // Get the link table contents.
                stage = "GetLinkTableContent";
                GetLinkTableContent content = module.send(new GetLinkTableContent(targetId, true, 0, info
                        .getMaxSizeOfInboundTable() - 1));

                // Find an unused index.
                for (LinkTableEntry e : content.getContent()) {
                    if (!e.isInUse() || e.getId() == baseIdToUse) {
                        index = e.getIndex();
                        break;
                    }
                }
            }
            else
                index = 0;

            if (index == -1)
                throw new IOException("No more room in device's inbound link table");

            // Add the link.
            List<LinkTableEntry> entries = new ArrayList<>();
            entries.add(new LinkTableEntry(index, baseIdToUse, 0xf60202, 3));
            module.send(new SetLinkTableContent(targetId, true, entries));

            // Ensure the add was successful.
            stage = "SetLinkTableContent status";
            queryStatus = module.send(new QueryStatus(targetId));
            if (queryStatus.getAnswer().getLastReturnCode() != ReturnCode.ok)
                throw new IOException("SetLinkTableContent return code: " + queryStatus.getAnswer().getLastReturnCode());

            // Re-lock the device.
            stage = "Lock";
            Lock lock = module.send(new Lock(targetId, securityCode));
            if (lock.getReturnCode() != ReturnCode.ok)
                throw new IOException("Lock return code: " + lock.getReturnCode());
        }
        catch (TimeoutException e) {
            throw new IOException("Timeout waiting for response: " + stage);
        }
    }
}
