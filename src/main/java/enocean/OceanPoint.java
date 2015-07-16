package enocean;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

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
	private final Node valueNode;
	private final Node dvalueNode;
	private final Node timeNode;
	String id;
	private boolean settable = false;
	
	OceanPoint(OceanDevice d, Node n) {
		device = d;
		profile = d.profile;
		node = n;
		id = node.getAttribute("id").getString();
		
		valueNode = node.createChild("Value").setValueType(getValueType()).build();
		if (profile.getDataTypeId(id) == DataTypes.NUMERIC) dvalueNode = node.createChild("Display Value").setValueType(ValueType.STRING).build();
		else dvalueNode = null;
		timeNode = node.createChild("Time").setValueType(ValueType.STRING).build();
		
		 if (profile.isOutput(id)) {
			 device.learnIn();
			 if (device.learned) settable = true;
		 }

	}
	
	
	
	private ValueType getValueType() {
		int dataType = profile.getDataTypeId(id);
		switch (dataType) {
		case DataTypes.ALPHANUMERIC: return ValueType.STRING;
		case DataTypes.BINARY: {
			TextRenderer tr = profile.createTextRenderer(id);
			String on = tr.getText(true, TextRenderer.HINT_FULL);
			String off = tr.getText(false, TextRenderer.HINT_FULL);
			return ValueType.makeBool(on, off);
		}
		case DataTypes.MULTISTATE: {
			TextRenderer tr = profile.createTextRenderer(id);
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
		int dataType = profile.getDataTypeId(id);
		TextRenderer tr = profile.createTextRenderer(id);
		Value dval = new Value(tr.getText(pvt.getValue(), TextRenderer.HINT_FULL));
		Value tval = new Value(new DateTime(pvt.getTime()).toString());
		Value val;
		switch (dataType) {
		case DataTypes.ALPHANUMERIC: {
			val = new Value(pvt.getStringValue()); break;
		}
		case DataTypes.BINARY: val = new Value(pvt.getBooleanValue()); break;
		case DataTypes.MULTISTATE: {
			String valstr = tr.getText(pvt.getValue(), TextRenderer.HINT_FULL);
			Set<String> enums = valueNode.getValueType().getEnums();
			if (enums != null && !enums.contains(valstr)) valueNode.setValueType(ValueType.STRING);
			val = new Value(valstr); break;
		}
		case DataTypes.NUMERIC: val = new Value(pvt.getDoubleValue()); break;
		default: val = dval; break;
		}
		
		valueNode.setValue(val);
		if (dataType == DataTypes.NUMERIC) dvalueNode.setValue(dval);
		timeNode.setValue(tval);
		
		if (settable) {
			valueNode.setWritable(Writable.WRITE);
			valueNode.getListener().setValueHandler(new SetHandler());
		} else {
			valueNode.setWritable(Writable.NEVER);
		}
	}
	
	private class SetHandler implements Handler<ValuePair> {
		public void handle(ValuePair event) {
			if (!event.isFromExternalSource()) {
    			return;
    		}
    		Value newval = event.getCurrent();
    		long targetId = device.node.getAttribute("sender id").getNumber().longValue();
    		int baseIdOffset = device.node.getAttribute("base id offset").getNumber().intValue();
    		DataValue value = null;
    		int dataType = profile.getDataTypeId(id);
    		switch(dataType) {
    		case DataTypes.ALPHANUMERIC: value = new AlphanumericValue(newval.getString()); break;
    		case DataTypes.BINARY: value = new BinaryValue(newval.getBool()); break;
    		case DataTypes.MULTISTATE: {
    			TextRenderer tr = profile.createTextRenderer(id);
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
    				return;
    			}
    			break;
    		}
    		case DataTypes.NUMERIC: value = new NumericValue(newval.getNumber().doubleValue()); break;
    		default: {
    			LOGGER.error("invalid data type");
    			return;
    		}
    		}
    		try {
				profile.setPoint(targetId, baseIdOffset, value, id, device.conn.module);
			} catch (IOException e) {
				LOGGER.debug("", e);
			}
		}
	}
	
}
