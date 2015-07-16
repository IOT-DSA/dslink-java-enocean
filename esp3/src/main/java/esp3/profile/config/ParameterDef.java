package esp3.profile.config;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.i18n.ProcessResult;

public class ParameterDef {
    public static enum Type {
        notSupported, numeric, enumeration;
    }

    private final int index;
    private final String nameKey;
    private final int size;
    private final Type type;
    private final String unitsKey;
    private final IntStringPair[] enumValues;
    private final Validator validator;

    public ParameterDef(int index, String nameKey, int size, Type type, String unitsKey, IntStringPair[] enumValues) {
        this(index, nameKey, size, type, unitsKey, enumValues, null);
    }

    public ParameterDef(int index, String nameKey, int size, Type type, String unitsKey, IntStringPair[] enumValues,
            Validator validator) {
        this.index = index;
        this.nameKey = nameKey;
        this.size = size;
        this.type = type;
        this.unitsKey = unitsKey;
        this.enumValues = enumValues;

        if (validator == null) {
            if (type == Type.numeric)
                this.validator = new RangeValidator(0, ((long) Math.pow(2, size * 8)) - 1);
            else if (type == Type.enumeration)
                this.validator = new EnumerationValidator(enumValues);
            else
                this.validator = null;
        }
        else
            this.validator = validator;
    }

    public int getIndex() {
        return index;
    }

    public String getNameKey() {
        return nameKey;
    }

    public int getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

    public String getUnitsKey() {
        return unitsKey;
    }

    public IntStringPair[] getEnumValues() {
        return enumValues;
    }

    public boolean validate(String key, long value, ProcessResult result) {
        return validator.validate(key, value, result);
    }
}
