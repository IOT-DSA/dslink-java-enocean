package esp3.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.haystack.HDictBuilder;

import esp3.EnOceanModule;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import esp3.profile.a5.ProfilesGenerator;
import esp3.profile.config.ParameterList;
import com.serotonin.NotImplementedException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.RangeRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;

abstract public class Profile {

    public static final String DBM = "DBM";
    private static final Map<String, Profile> PROFILES = new LinkedHashMap<>();

    static {

        for (Profile profile : new ProfilesGenerator().getProfiles()) {
            addProfile(profile);
        }

        addProfile(new A5_3F_81());
        
        addProfile(new A5_FF_FE());
        addProfile(new A5_FF_FF());

        addProfile(new D5_00_01());

        addProfile(new F6_02_01());
        addProfile(new F6_02_02());
        addProfile(new F6_02_03());
        addProfile(new F6_03_01());
        addProfile(new F6_03_02());
        addProfile(new F6_04_01());
        addProfile(new F6_10_00());

        //addProfile(new Z7_11_01());
    }

    public final int func;

    public final String name;
    public final ParameterList parameterList = new ParameterList();
    public final RadioOrg rorg;
    public final int type;
    protected final Map<String, PointInfo> pointInfo = new LinkedHashMap<>();

    public static Profile getProfile(String name) {
        for (Profile p : PROFILES.values()) {
            if (p.name.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public static Profile getProfile(RadioOrg rorg, int func, int type) {
        for (Profile p : PROFILES.values()) {
            if (p.rorg == rorg && p.func == func && p.type == type) {
                return p;
            }
        }
        return null;
    }

    public static List<Profile> getProfiles(RadioOrg rorg) {
        List<Profile> profiles = new ArrayList<>();
        for (Profile p : PROFILES.values()) {
            if (p.rorg == rorg) {
                profiles.add(p);
            }
        }
        return profiles;
    }

    public static String prettyEEP(RadioOrg rorg, int func, int type) {
        if (rorg == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.leftPad(Integer.toString(rorg.id, 16), 2, '0')).append('-');
        sb.append(StringUtils.leftPad(Integer.toString(func, 16), 2, '0')).append('-');
        sb.append(StringUtils.leftPad(Integer.toString(type, 16), 2, '0'));
        return sb.toString();
    }

    public static Collection<Profile> values() {
        return PROFILES.values();
    }

    private static void addProfile(Profile p) {
        PROFILES.put(p.name, p);
    }

    public Profile() {
        this.func = 0;
        this.name = null;
        this.rorg = null;
        this.type = 0;
    }

    protected Profile(String name, RadioOrg rorg, int func, int type) {
        this.name = name;
        this.rorg = rorg;
        this.func = func;
        this.type = type;

        createPointInfo();

        // Points common to all devices.
        pointInfo.put(DBM, new PointInfo(DataTypes.NUMERIC, false));
    }

    public TextRenderer createTextRenderer(String pointId) {
        if (DBM.equals(pointId)) {
            RangeRenderer r = new RangeRenderer("0");
            r.addRangeValues(-Double.MAX_VALUE, -90, "0 bars", "red");
            r.addRangeValues(-90, -85, "1 bar", "red");
            r.addRangeValues(-85, -70, "2 bars", "yellow");
            r.addRangeValues(-70, -60, "3 bars", "#80FF00");
            r.addRangeValues(-60, Double.MAX_VALUE, "4 bars", "#80FF00");
            return r;
            //            return new AnalogRenderer("0", "${unit}");
        }
        return _createTextRenderer(pointId);
    }

    public HDictBuilder createUserTags(String pointId) {
        HDictBuilder builder = new HDictBuilder();

        // Add common tags.
        if (DBM.equals(pointId)) {
            builder.add("signal");
            builder.add("unit", "dBm");
        }

        addTags(pointId, builder);
        return builder;
    }

    public String createUserTagsZinc(String pointId) {
        return createUserTags(pointId).toDict().toZinc();
    }

    public String defaultPointId() {
        return pointInfo.keySet().iterator().next();
    }

    public int getDataTypeId(String pointId) {
        PointInfo i = pointInfo.get(pointId);
        return i == null ? DataTypes.UNKNOWN : i.dataTypeId;
    }

    public Set<String> getPointIds() {
        return pointInfo.keySet();
    }

    public boolean hasId(String pointId) {
        return pointInfo.containsKey(pointId);
    }

    public boolean isOutput() {
        for (PointInfo pi : pointInfo.values()) {
            if (pi.isOutput()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOutput(String pointId) {
        PointInfo i = pointInfo.get(pointId);
        return i == null ? false : i.output;
    }

    /**
     * Override as required
     * <p>
     * @param securityCode
     * @param baseIdOffset
     * @param targetId
     * @param module
     * @throws IOException
     */
    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
        throw new NotImplementedException();
    }

    public void parseTelegram(RadioPacket radio, TelegramData t) {
        t.addValue(DBM, new NumericValue(radio.getdBm()));
        _parseTelegram(radio, t);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        Map<String, Object> points = new LinkedHashMap<>();
        map.put("points", points);
        for (Map.Entry<String, PointInfo> point : pointInfo.entrySet()) {
            points.put(point.getKey(), point.getValue().serialize());
        }

        return map;
    }

    /**
     * Override as required
     * <p>
     * @param targetId
     * @param baseIdOffset
     * @param value
     * @param pointId
     * @param module
     * @throws IOException
     */
    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module, Map<String, DataValue> allValues) throws IOException {
        throw new NotImplementedException();
    }

    protected abstract TextRenderer _createTextRenderer(String pointId);

    protected abstract void _parseTelegram(RadioPacket radio, TelegramData t);

    protected abstract void addTags(String pointId, HDictBuilder builder);

    protected abstract void createPointInfo();

    public static class PointInfo {

        final int dataTypeId;
        final boolean output;

        public PointInfo(int dataTypeId, boolean output) {
            this.dataTypeId = dataTypeId;
            this.output = output;
        }

        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("dataTypeId", dataTypeId);
            map.put("output", output);
            return map;
        }

        public int getDataTypeId() {
            return dataTypeId;
        }

        public boolean isOutput() {
            return output;
        }
    }
}
