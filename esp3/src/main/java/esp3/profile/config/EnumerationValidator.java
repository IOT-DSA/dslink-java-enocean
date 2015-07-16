package esp3.profile.config;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.i18n.ProcessResult;

public class EnumerationValidator extends Validator {
    private final IntStringPair[] enumeration;

    public EnumerationValidator(IntStringPair[] enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean validate(String key, long value, ProcessResult result) {
        for (IntStringPair e : enumeration) {
            if (e.getKey() == value)
                return true;
        }
        result.addContextualMessage(key, "validate.invalidValue");
        return false;
    }
}
