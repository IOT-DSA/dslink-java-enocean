package esp3.profile.a5;

import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.incoming.RadioPacket;
import esp3.profile.Profile;
import esp3.profile.a5.xml.DataField;
import esp3.profile.a5.xml.EnocianType;
import esp3.profile.a5.xml.Item;
import esp3.profile.a5.xml.Range;
import esp3.profile.a5.xml.Scale;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.AnalogRenderer;
import com.serotonin.m2m2.view.text.BinaryTextRenderer;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.util.ArrayUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.haystack.HDictBuilder;

/**
 *
 * @author rplatonov
 */
public class GenericProfile extends Profile {

    EnocianType enocianType;

    public GenericProfile(String id, int func, int type, EnocianType enocianType) {
        super(id, RadioOrg.fourBS, func, type);
        this.enocianType = enocianType;
        createPointInfo();
    }

    public EnocianType getEnocianType() {
        return enocianType;
    }

    @Override
    protected TextRenderer _createTextRenderer(String pointId) {
        for (DataField dataField : enocianType.getDatafields()) {
            if (!dataField.isLearn() && pointId.equals(dataField.getShortcut())) {
                if (dataField.getRange() != null) {
                    return new AnalogRenderer("0.00", dataField.getUnit());
                } else {
                    List<Item> items = dataField.getItems();
                    if (items.get(0).getScale() != null || items.size() != 2) {
                        MultistateRenderer result = new MultistateRenderer();
                        for (Item item : items) {
                            result.addMultistateValue(item.getValue(), item.getDescription(), null);
                        }
                        return result;
                    } else {
                        if (items.get(0).getValue() == 0) {
                            return new BinaryTextRenderer(items.get(0).getDescription(), null, items.get(1).getDescription(), name);
                        } else {
                            return new BinaryTextRenderer(items.get(1).getDescription(), null, items.get(0).getDescription(), name);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void _parseTelegram(RadioPacket radio, TelegramData t) {
        List<DataField> postponedProcessing = new LinkedList<>();
        Map<String, Scale> refs = new HashMap<>();

        byte[] userData = radio.getUserData();
        for (DataField dataField : enocianType.getDatafields()) {
            int value = ArrayUtils.bitRangeValue(userData, dataField.getBitoffs(), dataField.getBitsize());

            if (dataField.isLearn()) {
                if (value == 0) {
                    t.setLearn(true);
                }
            } else {
                Scale scale = dataField.getScale();
                if (scale != null) {
                    if (scale.getRef() != null) {
                        postponedProcessing.add(dataField);
                        continue;
                    }
                    t.addValue(dataField.getShortcut(), new NumericValue(getScaledValue(value, dataField.getRange(), scale)));
                } else if (dataField.getItems() != null) {
                    List<Item> items = dataField.getItems();
                    if (items.get(0).getScale() != null || items.size() != 2) {//multistate
                        t.addValue(dataField.getShortcut(), new MultistateValue(value));
                        if (items.get(0).getScale() != null) {
                            for (Item item : items) {
                                if (item.getValue() == value) {
                                    refs.put(dataField.getShortcut(), item.getScale());
                                    break;
                                }
                            }
                        }
                    } else {
                        t.addValue(dataField.getShortcut(), new BinaryValue(value == 1));

                    }
                }
            }
        }
        
        if (!postponedProcessing.isEmpty()) { //required to dynamically resolve reference variables
            for (DataField dataField : postponedProcessing) {
                int value = ArrayUtils.bitRangeValue(userData, dataField.getBitoffs(), dataField.getBitsize());
                String ref = dataField.getScale().getRef();
                Scale scale = refs.get(ref);
                if (scale != null) {
                    t.addValue(dataField.getShortcut(), new NumericValue(getScaledValue(value, dataField.getRange(), scale)));
                }
            }
        }
    }

    private double getScaledValue(int value, Range range, Scale scale) {
        double data = (value - range.getMin()) * scale.getDiff() / range.getDiff()  + scale.getMin();
        return data;
    }

    @Override
    protected void addTags(String pointId, HDictBuilder builder) {
    }

    @Override
    protected void createPointInfo() {
        if (enocianType != null && enocianType.getDatafields() != null) {
            for (DataField dataField : enocianType.getDatafields()) {
                if (!dataField.isLearn()) {
                	boolean isOut = false;
//                	String name = dataField.getData().toLowerCase();
//                	if (name.contains("setpoint") || name.contains("set point") || name.contains("command")) isOut = true;
                    List<Item> items = dataField.getItems();
                    if (items != null) {
                        if (items.get(0).getScale() != null || items.size() != 2) {
                            pointInfo.put(dataField.getShortcut(), new PointInfo(DataTypes.MULTISTATE, isOut));
                        } else {
                            pointInfo.put(dataField.getShortcut(), new PointInfo(DataTypes.BINARY, isOut));
                        }
                    } else {
                        pointInfo.put(dataField.getShortcut(), new PointInfo(DataTypes.NUMERIC, isOut));
                    }
                }
            }
        }
    }
    
//    @Override
//    public void setPoint(long targetId, int baseIdOffset, DataValue value, String pointId, EnOceanModule module)
//            throws IOException {
//    	
//    }
//    
//    @Override
//    public void learnIn(long securityCode, int baseIdOffset, long targetId, EnOceanModule module) throws IOException {
//    	
//    }

}
