package esp3;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.QueryIdAnswer;
import esp3.message.incoming.RadioPacket;
import esp3.profile.Profile;
import com.serotonin.io.StreamUtils;
import com.serotonin.messaging2.IncomingMessage;
import com.serotonin.messaging2.IncomingMessageHandler;
import com.serotonin.messaging2.OutgoingMessage;
import com.serotonin.util.ArrayUtils;

public class PacketHandler implements IncomingMessageHandler {
    private static final Logger logger =  LogManager.getLogger(PacketHandler.class.getName());

    private final EnOceanModuleImpl module;
    private final Map<Long, Profile> deviceProfiles = new HashMap<>();

    PacketHandler(EnOceanModuleImpl module) {
        this.module = module;
    }

    public void clearKnownDevices() {
        synchronized (deviceProfiles) {
            deviceProfiles.clear();
        }
    }

    @Override
    public OutgoingMessage handleRequest(IncomingMessage request) throws Exception {
        if (request instanceof RadioPacket) {
            RadioPacket req = (RadioPacket) request;
            long senderId = req.getSenderId();

            Profile profile = getSenderProfile(senderId);
            if (profile == null) {
                // The profile is unknown.
                RadioOrg rorg = req.getRorg();
                byte[] userData = req.getUserData();

                // If this is a 4BS with the learn byte (bit 24 == 0), we might be able to determine the profile
                if (rorg == RadioOrg.fourBS && ArrayUtils.bitRangeValue(userData, 28, 1) == 0) {
                    int func = ArrayUtils.bitRangeValue(userData, 0, 6);
                    int type = ArrayUtils.bitRangeValue(userData, 6, 7);
                    profile = Profile.getProfile(rorg, func, type);

                    if (profile != null)
                        // We found the profile. Fire a teach in notification.
                        module.fireTeachIn(senderId, profile, req.getdBm());
                    else
                        logger.error("Profile not found: func=" + func + ", type=" + type + ", userData="
                                + StreamUtils.dumpHex(userData));
                }

                if (profile == null)
                    // We didn't find the profile. Fire a new sender notification.
                    module.fireNewSender(senderId, rorg, req.getdBm());
            }
            else {
                // Received a telegram from a know sender. Parse and fire a telegram notification.
                TelegramData t = new TelegramData();
                profile.parseTelegram(req, t);
                module.fireTelegram(senderId, t);
            }

            return null;
        }
        else if (request instanceof QueryIdAnswer) {
            QueryIdAnswer req = (QueryIdAnswer) request;
            Profile profile = Profile.getProfile(req.getRorg(), req.getFunc(), req.getType());
            if (profile == null)
                logger.info("Unrecognized profile in QueryIdAnswer: " + req.getRorg() + "-" + req.getFunc() + "-"
                        + req.getType());
            else
                module.fireNewSender(req.getSourceId(), profile, req.getdBm());

            return null;
        }

        logger.info("Unhandled request: " + request);

        return null;
    }

    private Profile getSenderProfile(long senderId) {
        Profile profile = deviceProfiles.get(senderId);
        if (profile == null) {
            synchronized (deviceProfiles) {
                profile = deviceProfiles.get(senderId);
                if (profile == null) {
                    profile = module.requestSenderProfile(senderId);
                    if (profile != null)
                        deviceProfiles.put(senderId, profile);
                }
            }
        }
        return profile;
    }
}
