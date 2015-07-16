package esp3;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.profile.Profile;

public interface EnOceanModuleListener {
    /**
     * Received a telegram from a device.
     * 
     * @param senderId
     * @param telegram
     */
    void enoceanTelegram(long senderId, TelegramData telegram);

    /**
     * Received a telegram from an unrecognized device. This provides the listener the opportunity to return a profile
     * if the listener knows the sender.
     * 
     * @param senderId
     * @return
     */
    Profile enoceanSenderProfile(long senderId);

    void enoceanNewSender(long senderId, RadioOrg rorg, int rssi);

    void enoceanTeachIn(long senderId, Profile profile, int rssi);

    void enoceanNewSender(long senderId, Profile profile, int rssi);

    void enoceanException(Exception e);
}
