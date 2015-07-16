package esp3.message.request.local;

import java.nio.charset.Charset;

import com.serotonin.util.ArrayUtils;

public class ReadVersion extends CommonCommand {
    @Override
    protected CommandCode getCommandCode() {
        return CommandCode.coRdVersion;
    }

    private long appVersion;
    private long apiVersion;
    private long chipId;
    private long chipVersion;
    private String appDescription;

    @Override
    public void parseResponseData(byte[] data, byte[] opt) {
        appVersion = ArrayUtils.bitRangeValueLong(data, 0, 32);
        apiVersion = ArrayUtils.bitRangeValueLong(data, 32, 32);
        chipId = ArrayUtils.bitRangeValueLong(data, 64, 32);
        chipVersion = ArrayUtils.bitRangeValueLong(data, 96, 32);

        int len = 16;
        while (data[15 + len] == 0)
            len--;
        appDescription = new String(data, 16, len, Charset.forName("ASCII"));
    }

    /**
     * @return the appVersion
     */
    public long getAppVersion() {
        return appVersion;
    }

    /**
     * @return the apiVersion
     */
    public long getApiVersion() {
        return apiVersion;
    }

    /**
     * @return the chipId
     */
    public long getChipId() {
        return chipId;
    }

    /**
     * @return the chipVersion
     */
    public long getChipVersion() {
        return chipVersion;
    }

    /**
     * @return the appDescription
     */
    public String getAppDescription() {
        return appDescription;
    }

    @Override
    public String toString() {
        return "ReadVersion [appVersion=" + appVersion + ", apiVersion=" + apiVersion + ", chipId=" + chipId
                + ", chipVersion=" + chipVersion + ", appDescription=" + appDescription + "]";
    }
}
