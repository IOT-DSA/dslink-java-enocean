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
import org.dsa.iot.dslink.util.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dsa.iot.dslink.util.handler.Handler;

import com.serotonin.m2m2.module.SerialPortListDefinition;
import com.serotonin.messaging.TimeoutException;
import com.serotonin.util.ThreadUtils;

import esp3.EnOceanModule;
import esp3.EnOceanModuleListener;
import esp3.message.RadioOrg;
import esp3.message.TelegramData;
import esp3.message.request.RadioRequest;
import esp3.message.request.reman.Ping;
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
			statnode.setValue(new Value("Connected."));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("EnOceanModule.init() error", e);
			statnode.setValue(new Value("Error initializing connection."));
			//stop();
			module = null;
		}
        
        act = new Action(Permission.READ, new RestartHandler());
        anode = node.getChild("restart");
        if (anode == null) node.createChild("restart").setAction(act).build().setSerializable(false);
        else anode.setAction(act);
        
        if (module != null) {
        	act = new Action(Permission.READ, new UnlockQueryHandler());
        	act.addParameter(new Parameter("security code", ValueType.STRING, new Value("56657276")));
			anode = node.getChild("unlock and query");
			if (anode == null) node.createChild("unlock and query").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
			
			act = new Action(Permission.READ, new A5TeachHandler());
        	act.addParameter(new Parameter("controller profile func", ValueType.STRING, new Value("3F")));
        	act.addParameter(new Parameter("controller profile type", ValueType.STRING, new Value("7F")));
			anode = node.getChild("send 4BS (A5) teach telegram");
			if (anode == null) node.createChild("send 4BS (A5) teach telegram").setAction(act).build().setSerializable(false);
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
        	for (Profile p: Profile.values()) enums.add(link.tryToTranslate("enocean.profile."+p.name).replace(",", "%2C").replace("+", "%2B"));
        	act.addParameter(new Parameter("profile", ValueType.makeEnum(enums)));
        	act.addParameter(new Parameter("security code", ValueType.STRING, new Value("0")));
        	act.addParameter(new Parameter("base id offset", ValueType.NUMBER));
			anode = node.getChild("add device");
			if (anode == null) node.createChild("add device").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
			
			act = new Action(Permission.READ, new StopHandler());
			anode = node.getChild("stop");
			if (anode == null) node.createChild("stop").setAction(act).build().setSerializable(false);
			else anode.setAction(act);
			
			for (OceanDevice od: devices) od.enable();
        }
        
	}
	
	public void restoreLastSession() {
		init();
		if (node.getChildren() == null) return;
		for (Node child: node.getChildren().values()) {
			if (child.getName().equals("Devices")) devsNode = child;
			else if (child != statnode && child != discNode && child.getAction() == null) node.removeChild(child);
		}
		if (devsNode == null || devsNode.getChildren() == null) return;
		for (Node child: devsNode.getChildren().values()) {
			restoreDevice(child);
		}
	}
	
	private void restoreDevice(Node child) {
		Value senderId = child.getAttribute("sender id");
		Value profile = child.getAttribute("profile");
		Value security = child.getAttribute("security code");
		Value baseIdOffset = child.getAttribute("base id offset");
		if (senderId!=null && profile!=null && security!=null && baseIdOffset!=null) {
			OceanDevice od = new OceanDevice(getMe(), child, (module!=null));
			od.restoreLastSession();
		} else if (child.getAction() == null) {
			devsNode.removeChild(child);
		}
	}
	
	private class UnlockQueryHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			long code =  Long.parseLong(event.getParameter("security code", ValueType.STRING).getString(), 16);
	        
			try {

	            module.send(new Unlock(code));

	            // For fun, we'll pause for a sec.
	            ThreadUtils.sleep(1000);

	            module.send(new QueryId(0, 0, 0));
	            
	        } catch (NumberFormatException e) {
                LOGGER.error("enocean.badSecurityCode");
	        } catch (IOException e) {
	            LOGGER.debug("", e);
	        }
		}
	}
	
	private class A5TeachHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			int func =  (int) Long.parseLong(event.getParameter("controller profile func", ValueType.STRING).getString(), 16);
			int type =  (int) Long.parseLong(event.getParameter("controller profile type", ValueType.STRING).getString(), 16);
			
			byte b2 = (byte) ((type & 0x1F) << 3);
			byte b1 = (byte) (((func & 0x3F) << 2) | ((type & 0x60) >>> 5));
			
			byte[] data = {b1, b2, 0x0, (byte) 0x80};
	        RadioRequest radreq = new RadioRequest(RadioOrg.fourBS, data, 0);
	        try {
	        	module.send(radreq);
			} catch (IOException e) {
				LOGGER.debug("", e);
			}
		}
	}
 	
	private class PingHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			long pingId = Long.parseLong(event.getParameter("address", ValueType.STRING).getString(), 16);

            try {
				Ping ping = module.send(new Ping(pingId));
				Profile profile = Profile.getProfile(ping.getRorg(), ping.getFunc(), ping.getType());
				if (profile == null) {
				    LOGGER.debug("enocean.unknownEEP: " + Profile.prettyEEP(ping.getRorg(), ping.getFunc(), ping.getType()));
				} else {
				    enoceanNewSender(pingId, profile, ping.getRssi());
				}
            } catch (NumberFormatException e) {
                LOGGER.error("enocean.badPingAddress");
            } catch (TimeoutException e) {
            	LOGGER.error("enocean.noPingResponse");
            } catch (IOException e) {
                LOGGER.debug("", e);
            }
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
	
	void stop() {
		if (module != null) {
			module.removeListener(getMe());
			module.destroy();
			module = null;
			for (OceanDevice od: devices) od.disable();
		}
		node.removeChild("stop");
		node.removeChild("unlock and query");
		node.removeChild("send 4BS (A5) teach telegram");
		node.removeChild("ping");
		node.removeChild("add device");
		statnode.setValue(new Value("Stopped"));
	}
	
	private class StopHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			stop();
		}
	}
	
	private class RestartHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			stop();
			init();
		}
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
			String name = event.getParameter("name", ValueType.STRING).getString();
			String commPort = event.getParameter("comm port id", ValueType.STRING).getString();
			long baseIdOffset = event.getParameter("base id offset", ValueType.NUMBER).getNumber().longValue();
			
			node.setAttribute("comm port id", new Value(commPort));
			node.setAttribute("base id offset", new Value(baseIdOffset));
			
			if (name!=null && !name.equals(node.getName())) {
				rename(name);
			} else {
				stop();
				init();
			}
		}
	}
	
	private void rename(String name) {
		JsonObject jobj = link.copySerializer.serialize();
		JsonObject nodeobj = jobj.get(node.getName());
		jobj.put(name, nodeobj);
		link.copyDeserializer.deserialize(jobj);
		Node newnode = node.getParent().getChild(name);
		OceanConn oc = new OceanConn(link, newnode);
		remove();
		oc.restoreLastSession();
	}
	
	public void enoceanTelegram(long senderId, TelegramData telegram) {
		LOGGER.info("got telegram from " + Long.toString(senderId, 16));
		for (OceanDevice dev: devices) {
			if (dev.node.getAttribute("sender id") != null && senderId == dev.node.getAttribute("sender id").getNumber().longValue()) {
				dev.telegram(telegram);
			}
		}
	}

	public Profile enoceanSenderProfile(long senderId) {
		LOGGER.info("got enoceanSenderProfile call from " + Long.toString(senderId, 16));
		for (OceanDevice dev: devices) {
			if (dev.node.getAttribute("sender id") != null && senderId == dev.node.getAttribute("sender id").getNumber().longValue()) {
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
    			if (child.getAttribute("senderId") != null && senderId == child.getAttribute("senderId").getNumber().longValue()) discNode.removeChild(child);
    		}
    	}
    	
    	for (OceanDevice dev: devices) {
    		if (dev.node.getAttribute("sender id") != null && senderId == dev.node.getAttribute("sender id").getNumber().longValue()) return;
    	}

    	// Not already in the list. Add it.
    	if (discNode == null) return;
    	Node devNode = discNode.createChild(name).setValueType(ValueType.NUMBER).setValue(new Value(rssi)).build();
    	devNode.setSerializable(false);
    	devNode.setAttribute("senderId", new Value(senderId));
    	devNode.setAttribute("rssi", new Value(rssi));
    	
    	Action act = new Action(Permission.READ, new AddDeviceHandler(devNode));
    	act.addParameter(new Parameter("name", ValueType.STRING, new Value(name)));
    	Set<String> enums = new HashSet<String>();
    	if (rorg != null) {
    		for (Profile p: Profile.getProfiles(rorg)) enums.add(link.tryToTranslate("enocean.profile."+p.name).replace(",", "%2C").replace("+", "%2B"));
    	}
    	else enums.add(link.tryToTranslate("enocean.profile."+profile.name).replace(",", "%2C").replace("+", "%2B"));
    	act.addParameter(new Parameter("profile", ValueType.makeEnum(enums)));
    	act.addParameter(new Parameter("base id offset", ValueType.NUMBER));
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
			String profName = link.translateBack(event.getParameter("profile").getString().replace("%2C", ",").replace("%2B", "+"));
			Profile profile = Profile.getProfile(profName);
			Value baseIdOffVal = event.getParameter("base id offset");
			long baseIdOff;
			if (baseIdOffVal != null && baseIdOffVal.getType() == ValueType.NUMBER) baseIdOff = baseIdOffVal.getNumber().longValue();
			else baseIdOff = getUnusedOffset(profile);
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
		    			if (child.getAttribute("senderId") != null && senderId == child.getAttribute("senderId").getNumber().longValue()) discNode.removeChild(child);
		    		}
		    	}
			}
			
			if (devsNode == null) devsNode = node.createChild("Devices").build();
			
			Node newNode = devsNode.createChild(name).build();
			newNode.setAttribute("sender id", new Value(senderId));
			newNode.setAttribute("profile", new Value(profile.name));
			newNode.setAttribute("security code", new Value(security));
			newNode.setAttribute("base id offset", new Value(baseIdOff));
			OceanDevice od = new OceanDevice(getMe(), newNode, (module!=null));
			od.init();
			
		}
	}
	
	public long getUnusedOffset(Profile profile) {
		HashSet<Long> used = new HashSet<Long>();
		for (OceanDevice dev: devices) {
			if (profile.name.equals(dev.profile.name)) used.add(dev.node.getAttribute("base id offset").getNumber().longValue());
		}
		long off = 0;
		while (used.contains(off)) off += 1;
		return off;
	}
	
	private OceanConn getMe() {
		return this;
	}
	
}
