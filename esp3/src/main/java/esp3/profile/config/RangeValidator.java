package esp3.profile.config;

import com.serotonin.m2m2.i18n.ProcessResult;

public class RangeValidator extends Validator {
    // Both values are inclusive
    private final long from;
    private final long to;

    public RangeValidator(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean validate(String key, long value, ProcessResult result) {
        if (value < from || value > to) {
            result.addContextualMessage(key, "validate.betweenInc", from, to);
            return false;
        }
        return true;
    }
}
