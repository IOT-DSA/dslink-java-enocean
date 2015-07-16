package esp3.message.incoming;

import java.util.Arrays;

import esp3.message.RadioOrg;
import com.serotonin.util.queue.ByteQueue;

/**
 * An unsolicited packet from a device.
 * 
 * @author Matthew
 */
public class RadioPacket extends IncomingPacket {
    private RadioOrg rorg;
    private byte[] userData;
    private long senderId;
    private int status;

    private int subTelNum;
    private long destinationId;
    private int dBm;
    private int securityLevel;

    //    public RadioPacket() {
    //        // no op
    //    }
    //
    public RadioPacket(ByteQueue data, ByteQueue optional) {
        int radioTypeId = data.popU1B();
        rorg = RadioOrg.forId(radioTypeId);
        userData = new byte[data.size() - 5];
        data.pop(userData);
        senderId = data.popU4B();
        status = data.popU1B();

        subTelNum = optional.popU1B();
        destinationId = optional.popU4B();
        dBm = -optional.popU1B();
        securityLevel = optional.popU1B();
    }

    public RadioOrg getRorg() {
        return rorg;
    }

    public void setRorg(RadioOrg rorg) {
        this.rorg = rorg;
    }

    public byte[] getUserData() {
        return userData;
    }

    public void setUserData(byte[] userData) {
        this.userData = userData;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSubTelNum() {
        return subTelNum;
    }

    public void setSubTelNum(int subTelNum) {
        this.subTelNum = subTelNum;
    }

    public long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(long destinationId) {
        this.destinationId = destinationId;
    }

    public int getdBm() {
        return dBm;
    }

    public void setdBm(int dBm) {
        this.dBm = dBm;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    @Override
    public String toString() {
        return "RadioPacket [rorg=" + rorg + ", userData=" + Arrays.toString(userData) + ", senderId=" + senderId
                + ", status=" + status + ", subTelNum=" + subTelNum + ", destinationId=" + destinationId + ", dBm="
                + dBm + ", securityLevel=" + securityLevel + "]";
    }
}
