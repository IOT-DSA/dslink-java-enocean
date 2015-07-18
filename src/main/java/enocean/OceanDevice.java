package enocean;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

import com.serotonin.NotImplementedException;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

import esp3.message.TelegramData;
import esp3.profile.Profile;

public class OceanDevice {
	private static final Logger LOGGER;
	static {
		LOGGER = LoggerFactory.getLogger(OceanDevice.class);
	}

	OceanConn conn;
	Node node;
	final Map<String, OceanPoint> points = new HashMap<String, OceanPoint>();
	Profile profile;
	boolean learnable = true;
	boolean learned = false;
	private boolean enabled;
	
	OceanDevice(OceanConn c, Node n, boolean e) {
		conn = c;
		node = n;
		enabled = e;
	}
	
	void enable() {
		if (!enabled) {
			enabled = true;
			init();
		}
	}
	
	void disable() {
		enabled = false;
	}
	
	void init() {
		if (conn.module == null) conn.stop();
		Action act = new Action(Permission.READ, new RemoveHandler());
		Node anode = node.getChild("remove");
        if (anode == null) node.createChild("remove").setAction(act).build().setSerializable(false);
        else anode.setAction(act);
        
        act = getEditAction();
		anode = node.getChild("edit");
		if (anode == null) {
			anode = node.createChild("edit").setAction(act).build();
			anode.setSerializable(false);
		} else {
			anode.setAction(act);
		}
		
		if (enabled) setupPoints();
		//learnIn();
	}
	
	private Action getEditAction() {
		Action act = new Action(Permission.READ, new EditHandler());
		act.addParameter(new Parameter("name", ValueType.STRING, new Value(node.getName())));
		String sid = Long.toString(node.getAttribute("sender id").getNumber().longValue(), 16);
    	act.addParameter(new Parameter("sender id", ValueType.STRING, new Value(sid)));
    	Set<String> enums = new HashSet<String>();
    	for (Profile p: Profile.values()) enums.add(conn.link.tryToTranslate("enocean.profile."+p.name).replace(",", "%2C"));
    	String defprof = conn.link.tryToTranslate("enocean.profile."+node.getAttribute("profile").getString()).replace(",", "%2C");
    	act.addParameter(new Parameter("profile", ValueType.makeEnum(enums), new Value(defprof)));
    	String scode = Long.toString(node.getAttribute("security code").getNumber().longValue(), 16);
    	act.addParameter(new Parameter("security code", ValueType.STRING, new Value(scode)));
    	act.addParameter(new Parameter("base id offset", ValueType.NUMBER, node.getAttribute("base id offset")));
		return act;
	}

	private void setupPoints() {
		String profname = node.getAttribute("profile").getString();
		profile = Profile.getProfile(profname);
		for (String id: profile.getPointIds()) {
			String name = conn.link.tryToTranslate("enocean.profile."+profile.name+"."+id);
			node.removeChild(name);
			Node pnode = node.createChild(name).build();
			pnode.setAttribute("id", new Value(id));
			points.put(id, new OceanPoint(this, pnode));
		}
	}
	
	private class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			//String name = event.getParameter("name", ValueType.STRING).getString();
			String profName = conn.link.translateBack(event.getParameter("profile").getString().replace("%2C", ","));
			profile = Profile.getProfile(profName);
			long senderId = Long.parseLong(event.getParameter("sender id", ValueType.STRING).getString(), 16);
			long security = Long.parseLong(event.getParameter("security code", ValueType.STRING).getString(), 16);
			long baseIdOffset = event.getParameter("base id offset", ValueType.NUMBER).getNumber().longValue();
			
			node.setAttribute("sender id", new Value(senderId));
			node.setAttribute("profile", new Value(profile.name));
			node.setAttribute("security code", new Value(security));
			node.setAttribute("base id offset", new Value(baseIdOffset));
			
			init();
			//learnIn();
			
		}
	}
	
	private class RemoveHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			remove();
		}
	}
	
	private void remove() {
		conn.devices.remove(this);
		node.clearChildren();
		node.getParent().removeChild(node);
		conn.module.clearKnownDevices();
	}
	
	void learnIn() {
		long securityCode = node.getAttribute("security code").getNumber().longValue();
		int baseIdOffset = node.getAttribute("base id offset").getNumber().intValue();
		long targetId = node.getAttribute("sender id").getNumber().longValue();
		try {
			profile.learnIn(securityCode, baseIdOffset, targetId, conn.module);
			learnable = true;
			learned = true;
		} catch (NotImplementedException e) {
			learnable = false;
		} catch (IOException e) {
			LOGGER.debug("", e);
			learned = false;
		} 
	}
	
	void telegram(TelegramData telegram) {
		for (OceanPoint pt: points.values()) {
			DataValue value = telegram.getValue(pt.id);
			if (value != null) pt.update(new PointValueTime(value, System.currentTimeMillis()));
		}
	}
	
	void restoreLastSession() {
		init();
	}

}
