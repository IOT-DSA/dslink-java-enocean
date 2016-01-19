package enocean;

import java.io.IOException;
import java.util.*;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsa.iot.dslink.util.handler.Handler;

import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.text.MultistateRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;

import esp3.profile.Profile;

public class OceanPoint {
	private static final Logger LOGGER;
	static {
		LOGGER = LoggerFactory.getLogger(OceanPoint.class);
	}

	private final OceanDevice device;
	private final Profile profile;
	//private final int dataType;
	private final Node node;
	private final Node dvalueNode;
	String id;
	boolean settable = false;
	
	OceanPoint(OceanDevice d, Node n) {
		device = d;
		profile = d.profile;
		node = n;
		id = node.getAttribute("id").getString();
		
		node.setValueType(getValueType());
		if (profile.getDataTypeId(id, device.activeCase) == DataTypes.NUMERIC) dvalueNode = node.createChild("Display Value").setValueType(ValueType.STRING).build();
		else dvalueNode = null;
		
		 if (profile.isOutput(id, device.activeCase)) {
			 device.learnIn();
			 if (device.learned) settable = true;
		 }
		 
		 if (settable) {
				node.setWritable(Writable.WRITE);
				node.getListener().setValueHandler(new SetHandler());
			} else {
				node.setWritable(Writable.NEVER);
			}

	}
	
	
	
	private ValueType getValueType() {
		int dataType = profile.getDataTypeId(id, device.activeCase);
		switch (dataType) {
		case DataTypes.ALPHANUMERIC: return ValueType.STRING;
		case DataTypes.BINARY: {
			TextRenderer tr = profile.createTextRenderer(id, device.activeCase);
			String on = tr.getText(true, TextRenderer.HINT_FULL);
			String off = tr.getText(false, TextRenderer.HINT_FULL);
			return ValueType.makeBool(on, off);
		}
		case DataTypes.MULTISTATE: {
			TextRenderer tr = profile.createTextRenderer(id, device.activeCase);
			if (tr instanceof MultistateRenderer) {
				MultistateRenderer mtr = (MultistateRenderer) tr;
				Set<String> enums = new HashSet<String>();
				for (com.serotonin.m2m2.view.text.MultistateValue v: mtr.getMultistateValues()) {
					enums.add(v.getText());
				}
				return ValueType.makeEnum(enums);
			} else return ValueType.STRING;
		}
		case DataTypes.NUMERIC: return ValueType.NUMBER;
		default: return ValueType.STRING;
		}
	}
	
	void update(PointValueTime pvt) {
		int dataType = profile.getDataTypeId(id, device.activeCase);
		TextRenderer tr = profile.createTextRenderer(id, device.activeCase);
		Value dval = new Value(tr.getText(pvt.getValue(), TextRenderer.HINT_FULL));
		Value val;
		switch (dataType) {
		case DataTypes.ALPHANUMERIC: {
			val = new Value(pvt.getStringValue()); break;
		}
		case DataTypes.BINARY: val = new Value(pvt.getBooleanValue()); break;
		case DataTypes.MULTISTATE: {
			String valstr = tr.getText(pvt.getValue(), TextRenderer.HINT_FULL);
			Collection<String> enums = node.getValueType().getEnums();
			if (enums != null && !enums.contains(valstr)) node.setValueType(ValueType.STRING);
			val = new Value(valstr); break;
		}
		case DataTypes.NUMERIC: val = new Value(pvt.getDoubleValue()); break;
		default: val = dval; break;
		}
		
		node.setValue(val);
		if (dataType == DataTypes.NUMERIC) dvalueNode.setValue(dval);
		
		if (settable) {
			node.setWritable(Writable.WRITE);
			node.getListener().setValueHandler(new SetHandler());
		} else {
			node.setWritable(Writable.NEVER);
		}
	}
	
	private class SetHandler implements Handler<ValuePair> {
		public void handle(ValuePair event) {
			if (!event.isFromExternalSource()) {
    			return;
    		}
			if (device.conn.module == null) {
				device.conn.stop();
				return;
			}
    		Value newval = event.getCurrent();
    		long targetId = device.node.getAttribute("sender id").getNumber().longValue();
    		int baseIdOffset = device.node.getAttribute("base id offset").getNumber().intValue();
    		DataValue value = convertVal(newval);
    		if (value == null) return;
    		Map<String, DataValue> allVals = new HashMap<String, DataValue>();
    		for (OceanPoint pt: device.setpoints) {
    			DataValue v;
    			if (pt != getMe()) v = pt.convertVal(pt.node.getValue());
    			else v = value;
    			allVals.put(pt.id, v);
    		}
			
    		try {
				profile.setPoint(targetId, baseIdOffset, value, id, device.conn.module, allVals);
			} catch (IOException e) {	LOGGER.debug("", e);
			}
		}
	}
	
	DataValue convertVal(Value newval) {
		if (newval == null) return null;
		DataValue value = null;
		int dataType = profile.getDataTypeId(id, device.activeCase);
		switch(dataType) {
		case DataTypes.ALPHANUMERIC: value = new AlphanumericValue(newval.getString()); break;
		case DataTypes.BINARY: value = new BinaryValue(newval.getBool()); break;
		case DataTypes.MULTISTATE: {
			TextRenderer tr = profile.createTextRenderer(id, device.activeCase);
			if (tr instanceof MultistateRenderer) {
				MultistateRenderer mtr = (MultistateRenderer) tr;
				for (com.serotonin.m2m2.view.text.MultistateValue v: mtr.getMultistateValues()) {
					if (v.getText().equals(newval.getString())) {
						value = new MultistateValue(v.getKey()); break;
					}
				}
			}
			if (value == null) {
				LOGGER.error("Invalid multistate value");
				return null;
			}
			break;
		}
		case DataTypes.NUMERIC: value = new NumericValue(newval.getNumber().doubleValue()); break;
		default: {
			LOGGER.error("invalid data type");
			return null;
		}
		}
		return value;
	}



	public OceanPoint getMe() {
		return this;
	}
	
}
