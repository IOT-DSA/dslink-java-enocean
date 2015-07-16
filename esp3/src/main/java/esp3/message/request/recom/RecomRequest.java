package esp3.message.request.recom;

import esp3.message.request.reman.RemanRequest;

abstract public class RecomRequest extends RemanRequest {
    public RecomRequest(long destinationId) {
        super(destinationId);
    }

    @Override
    protected boolean expectsDeviceResponse() {
        return true;
    }
}
