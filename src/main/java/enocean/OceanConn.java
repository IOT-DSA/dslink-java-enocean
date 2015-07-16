package enocean;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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

import com.serotonin.m2m2.module.SerialPortListDefinition;
import com.serotonin.util.ThreadUtils;

import esp3.EnOceanModule;
import esp3.EnOceanModuleListener;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.request.reman.QueryId;
import esp3.message.request.reman.Unlock;
import esp3.profile.Profile;

public class OceanConn implements EnOceanModuleListener {
	private static final Logger LOGGER;
	static {
		LOGGER = LoggerFactory.getLogger(OceanConn.class);
	}
	
    private static final long BASE_ID_BASE = 0xFFFFDA80L;
	
	Node node;
	OceanLink link;
    EnOceanModule module;
	private final Node statnode;
	private Node discNode = null;
	private Node devsNode = null;
	final Set<OceanDevice> devices = new HashSet<OceanDevice>();
	
	OceanConn(OceanLink link, Node node) {
		this.link = link;
		this.node = node;
		
		link.conns.add(this);
		
		this.statnode = node.createChild("STATUS").setValueType(ValueType.STRING).setValue(new Value("")).build();
		this.statnode.setSerializable(false);
		
		discNode = node.createChild("Discovery").build();
		discNode.setSerializable(false);
	}
	
	void init() {
		
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
		
		statnode.setValue(new Value("Connecting..."));
		String commPort = node.getAttribute("comm port id").getString();
		long baseIdOffset = node.getAttribute("base id offset").getNumber().longValue();
		module = new EnOceanModule(SerialPortListDefinition.resolveCommPortId(commPort), BASE_ID_BASE + baseIdOffset);
		module.addListener(this);
        if (module != null) try {
			module.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("EnOceanModule.init() error", e);
			statnode.setValue(new Value("Error initializing connection."));
			//stop();
			module = null;
			return;
		}
        statnode.setValue(new Value("Connected."));
        
        if (module != null) {
        	act = new Action(Permission.READ, new UnlockQueryHandler());
        	act.addParameter(new Parameter("security code", ValueType.STRING, new Value("56657276")));
			anode = node.getChild("unlock and query");
			if (anode == null) node.createChild("unlock and query").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
			
			act = new Action(Permission.READ, new PingHandler());
        	act.addParameter(new Parameter("address", ValueType.STRING, new Value("0")));
			anode = node.getChild("ping");
			if (anode == null) node.createChild("ping").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
			
			act = new Action(Permission.READ, new AddDeviceHandler());
			act.addParameter(new Parameter("name", ValueType.STRING));
        	act.addParameter(new Parameter("sender id", ValueType.STRING, new Value("0")));
        	Set<String> enums = new HashSet<String>();
        	for (Profile p: Profile.values()) enums.add(link.tryToTranslate("enocean.profile."+p.name).replace(",", "%2C"));
        	act.addParameter(new Parameter("profile", ValueType.makeEnum(enums)));
        	act.addParameter(new Parameter("security code", ValueType.STRING, new Value("0")));
			anode = node.getChild("add device");
			if (anode == null) node.createChild("add device").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
        }
        
	}
	
	private class UnlockQueryHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			long code =  Long.parseLong(event.getParameter("security code", ValueType.STRING).getString(), 16);
	        
			try {

	            module.send(new Unlock(code));

	            // For fun, we'll pause for a sec.
	            ThreadUtils.sleep(1000);

	            module.send(new QueryId());
	        }
	        catch (IOException e) {
	            LOGGER.debug("", e);
	        }
		}
	}
	
	private static class PingHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			LOGGER.debug("Ping does nothing!");
		}
	}
	
	private class RemoveHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			remove();
		}
	}
	
	private void remove() {
		stop();
		node.clearChildren();
		link.conns.remove(getMe());
		node.getParent().removeChild(node);
	}
	
	private void stop() {
		if (module != null) {
			module.removeListener(getMe());
			module.destroy();
			module = null;
			
		}
		
		statnode.setValue(new Value("Stopped"));
	}
	
	Action getEditAction() {
		Action act = new Action(Permission.READ, new EditHandler());
        act.addParameter(new Parameter("name", ValueType.STRING, new Value(node.getName())));
        Set<String> portids = OceanLink.listPorts();
		if (portids.size() > 0) {
			 if (portids.contains(node.getAttribute("comm port id").getString())) {
				 act.addParameter(new Parameter("comm port id", ValueType.makeEnum(portids), node.getAttribute("comm port id")));
				 act.addParameter(new Parameter("comm port id (manual entry)", ValueType.STRING));
			 } else {
				 act.addParameter(new Parameter("comm port id", ValueType.makeEnum(portids)));
				 act.addParameter(new Parameter("comm port id (manual entry)", ValueType.STRING, node.getAttribute("comm port id")));
			 }
		} else {
			act.addParameter(new Parameter("comm port id", ValueType.STRING, node.getAttribute("comm port id")));
		}
		act.addParameter(new Parameter("base id offset", ValueType.NUMBER, node.getAttribute("base id offset")));
		return act;
	}
	
	private class EditHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			//String name = event.getParameter("name", ValueType.STRING).getString();
			String commPort = event.getParameter("comm port id", ValueType.STRING).getString();
			long baseIdOffset = event.getParameter("base id offset", ValueType.NUMBER).getNumber().longValue();
			
			node.setAttribute("comm port id", new Value(commPort));
			node.setAttribute("base id offset", new Value(baseIdOffset));
			
			stop();
			init();
			
		}
	}
	
	public void enoceanTelegram(long senderId, TelegramData telegram) {
		for (OceanDevice dev: devices) {
			if (new Value(senderId).equals(dev.node.getAttribute("sender id"))) {
				dev.telegram(telegram);
			}
		}
		
	}

	public Profile enoceanSenderProfile(long senderId) {
		for (OceanDevice dev: devices) {
			if (new Value(senderId).equals(dev.node.getAttribute("sender id"))) {
				String profname = dev.node.getAttribute("profile").getString();
				return Profile.getProfile(profname);
			}
		}
		return null;
	}

	public void enoceanNewSender(long senderId, RadioOrg rorg, int rssi) {
		LOGGER.debug("new sender rorg");
        discovered(senderId, rorg, null, rssi);
		
	}

	public void enoceanTeachIn(long senderId, Profile profile, int rssi) {
		LOGGER.debug("teach in prof");
        discovered(senderId, null, profile, rssi);
		
	}

	public void enoceanNewSender(long senderId, Profile profile, int rssi) {
		LOGGER.debug("new sender prof");
        discovered(senderId, null, profile, rssi);
		
	}

	public void enoceanException(Exception e) {
		// TODO Auto-generated method stub
		
	}
	
	private void discovered(long senderId, RadioOrg rorg, Profile profile, int rssi) {
    	String name = StringUtils.leftPad(Long.toHexString(senderId), 8, '0');
    	LOGGER.info("discovered device with id = " + senderId + ", rssi = " + rssi + ", pretty id = " + StringUtils.leftPad(Long.toHexString(senderId), 8, '0'));
    	
    	// Check if the device is already in the data source.
    	if (discNode != null && discNode.getChildren() != null) {
    		for (Node child: discNode.getChildren().values()) {
    			if (new Value(senderId).equals(child.getAttribute("senderId"))) discNode.removeChild(child);
    		}
    	}
    	
    	for (OceanDevice dev: devices) {
    		if (new Value(senderId).equals(dev.node.getAttribute("sender id"))) return;
    	}

    	// Not already in the list. Add it.
    	if (discNode == null) return;
    	Node devNode = discNode.createChild(name).build();
    	devNode.setSerializable(false);
    	devNode.setAttribute("senderId", new Value(senderId));
    	devNode.setAttribute("rssi", new Value(rssi));
    	devNode.setValueType(ValueType.NUMBER);
    	devNode.setValue(new Value(rssi));
    	
    	Action act = new Action(Permission.READ, new AddDeviceHandler(devNode));
    	act.addParameter(new Parameter("name", ValueType.STRING, new Value(name)));
    	Set<String> enums = new HashSet<String>();
    	if (rorg != null) {
    		for (Profile p: Profile.getProfiles(rorg)) enums.add(link.tryToTranslate("enocean.profile."+p.name).replace(",", "%2C"));
    	}
    	else enums.add(link.tryToTranslate("enocean.profile."+profile.name).replace(",", "%2C"));
    	act.addParameter(new Parameter("profile", ValueType.makeEnum(enums)));
    	devNode.createChild("add").setAction(act).build().setSerializable(false);
    	
//    	Node child = node.createChild()
//    	if (rorg != null) {
//    		discovered = new OceanDevice(senderId, rorg, rssi);
//    	} else {
//    		discovered = new OceanDevice(senderId, profile.name, rssi);
//    	}
    }
	
	private class AddDeviceHandler implements Handler<ActionResult> {
		Node devNode;
		AddDeviceHandler() {
			this(null);
		}
		AddDeviceHandler(Node n) {
			devNode = n;
		}
		public void handle(ActionResult event) {
			String name = event.getParameter("name", ValueType.STRING).getString();
			String profName = link.translateBack(event.getParameter("profile").getString().replace("%2C", ","));
			Profile profile = Profile.getProfile(profName);
			long senderId;
			long security;
			if (devNode != null) {
				senderId = devNode.getAttribute("senderId").getNumber().longValue();
				discNode.removeChild(devNode);
				security = 0;
			} else {
				senderId = Long.parseLong(event.getParameter("sender id", ValueType.STRING).getString(), 16);
				security = Long.parseLong(event.getParameter("security code", ValueType.STRING).getString(), 16);
				if (discNode != null && discNode.getChildren() != null) {
		    		for (Node child: discNode.getChildren().values()) {
		    			if (new Value(senderId).equals(child.getAttribute("senderId"))) discNode.removeChild(child);
		    		}
		    	}
			}
			
			if (devsNode == null) devsNode = node.createChild("Devices").build();
			
			Node newNode = devsNode.createChild(name).build();
			newNode.setAttribute("sender id", new Value(senderId));
			newNode.setAttribute("profile", new Value(profile.name));
			newNode.setAttribute("security code", new Value(security));
			newNode.setAttribute("base id offset", new Value(0));
			devices.add(new OceanDevice(getMe(), newNode));
			
		}
	}
	
	private OceanConn getMe() {
		return this;
	}
	
}
