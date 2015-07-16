package esp3.profile.config;

import java.util.ArrayList;
import java.util.List;

public class ParameterList {
    private final List<ParameterDef> parameterDefs = new ArrayList<>();

    public void add(ParameterDef def) {
        parameterDefs.add(def);
    }

    public List<ParameterDef> getParameterDefs() {
        return parameterDefs;
    }
}
