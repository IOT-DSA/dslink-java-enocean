package esp3.profile.config;

import com.serotonin.m2m2.i18n.ProcessResult;

abstract public class Validator {
    abstract public boolean validate(String key, long value, ProcessResult result);
}
