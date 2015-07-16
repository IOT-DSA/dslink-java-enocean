package esp3.message.request.reconf;

import esp3.message.request.recom.RecomRequest;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class GetConfigManagementInfo extends RecomRequest {
    private boolean configurationFileSupported;
    private boolean parameterListSupported;

    public GetConfigManagementInfo(long destinationId) {
        super(destinationId);
    }

    @Override
    protected void addMessageData(ByteQueue queue) {
        // no op
    }

    @Override
    protected int getFunctionNumber() {
        return 0x220;
    }

    @Override
    protected void parseResponseData(byte[] data) {
        configurationFileSupported = ArrayUtils.bitRangeValue(data, 0, 1) == 1;
        parameterListSupported = ArrayUtils.bitRangeValue(data, 1, 1) == 1;
    }

    public boolean isConfigurationFileSupported() {
        return configurationFileSupported;
    }

    public boolean isParameterListSupported() {
        return parameterListSupported;
    }

    @Override
    public String toString() {
        return "GetConfigManagementInfo [configurationFileSupported=" + configurationFileSupported
                + ", parameterListSupported=" + parameterListSupported + "]";
    }
}
