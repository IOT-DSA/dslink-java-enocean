package enocean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Pattern;

import jssc.SerialNativeInterface;
import jssc.SerialPortList;

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

import com.serotonin.io.serial.CommPortConfigException;
import com.serotonin.io.serial.CommPortIdentifier;
import com.serotonin.io.serial.CommPortProxy;
import com.serotonin.provider.ExecutorServiceProvider;
import com.serotonin.provider.InputStreamEPollProvider;
import com.serotonin.provider.Providers;
import com.serotonin.provider.TimerProvider;
import com.serotonin.provider.impl.ExecutorServiceProviderImpl;
import com.serotonin.provider.impl.InputStreamEPollProviderImpl;
import com.serotonin.provider.impl.RealTimeTimerProvider;


public class OceanLink {
	private static final Logger LOGGER;
	static {
		LOGGER = LoggerFactory.getLogger(OceanLink.class);
	}
	
	private Node node;
	final Set<OceanConn> conns = new HashSet<OceanConn>();
	
	private final Map<String, String> translations = new HashMap<String, String>();
	private final Map<String, String> translationsBack = new HashMap<String, String>();
	
	private OceanLink(Node node) {
		this.node = node;
	}
	
	public static void start(Node parent) {
		OceanLink ocean = new OceanLink(parent);
		ocean.init();
	}
	
	private void init() {
		
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> urls = cl.getResources("i18n.properties");

	        while (urls.hasMoreElements()) {
	            URL url = urls.nextElement();

	            Properties props = new Properties();
	            InputStreamReader r = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
	            props.load(r);
	            r.close();

	            for (Object keyo : props.keySet()) {
	                String key = (String) keyo;
	                translations.put(key, props.getProperty(key));
	                if (key.startsWith("enocean.profile.")) {
	                	String nkey = key.replaceFirst("enocean.profile.", "");
	                	if (!nkey.contains(".")) translationsBack.put(props.getProperty(key), nkey);
	                }
	            }
	        }

		} catch (IOException e) {
			
		}
		
		try {
			Providers.add(ExecutorServiceProvider.class, new ExecutorServiceProviderImpl(new ScheduledThreadPoolExecutor(32)));
		} catch (Exception e) {}
		try {
			InputStreamEPollProviderImpl isepp = new InputStreamEPollProviderImpl();
			isepp.initialize();
			Providers.add(InputStreamEPollProvider.class, isepp);
		} catch (Exception e) {}
		try {
			RealTimeTimerProvider tp = new RealTimeTimerProvider();
			tp.initialize();
			Providers.add(TimerProvider.class, tp);
		} catch (Exception e) {}
		
		restoreLastSession();
		
		Action act = getAddSerialAction();
		node.createChild("add connection").setAction(act).build().setSerializable(false);
		
		act = new Action(Permission.READ, new PortScanHandler());
		node.createChild("scan for serial ports").setAction(act).build().setSerializable(false);
	}
	
	public void restoreLastSession() {
		if (node.getChildren() == null) return;
		for (Node child: node.getChildren().values()) {
			Value commPortId = child.getAttribute("comm port id");
			Value baseIdOffset = child.getAttribute("base id offset"); 
			if (commPortId!=null && baseIdOffset!=null) {
				OceanConn oc = new OceanConn(getMe(), child);
				oc.restoreLastSession();
			} else {
				node.removeChild(child);
			}
		}
	}
		
	private Action getAddSerialAction() {
		Action act = new Action(Permission.READ, new AddConnHandler());
		act.addParameter(new Parameter("name", ValueType.STRING));
		Set<String> portids = listPorts();
		if (portids.size() > 0) {
			act.addParameter(new Parameter("comm port id", ValueType.makeEnum(portids)));
			act.addParameter(new Parameter("comm port id (manual entry)", ValueType.STRING));
		} else {
			act.addParameter(new Parameter("comm port id", ValueType.STRING));
		}
		act.addParameter(new Parameter("base id offset", ValueType.NUMBER, new Value(0)));
		return act;
	}
	
	private class AddConnHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			String name = event.getParameter("name", ValueType.STRING).getString();
			String commPort = event.getParameter("comm port id", ValueType.STRING).getString();
			long baseIdOffset = event.getParameter("base id offset", ValueType.NUMBER).getNumber().longValue();
			
			Node child = node.createChild(name).build();
			child.setAttribute("comm port id", new Value(commPort));
			child.setAttribute("base id offset", new Value(baseIdOffset));
			
			OceanConn conn = new OceanConn(getMe(), child);
			conn.init();
		}
	}
	
	private class PortScanHandler implements Handler<ActionResult> {
		public void handle(ActionResult event) {
			Action act = getAddSerialAction();
			Node anode = node.getChild("add connection");
			if (anode == null) {
				anode = node.createChild("add connection").setAction(act).build();
				anode.setSerializable(false);
			} else {
				anode.setAction(act);
			}
			
//			for (BacnetConn conn: serialConns) {
//				anode = conn.node.getChild("edit");
//				if (anode != null) { 
//					act = conn.getEditAction();
//					anode.setAction(act);
//				}
//			}
		}
	}
	
	static Set<String> listPorts() {
		Set<String> portids = new HashSet<String>();
		try {
			List<CommPortProxy> cports = getCommPorts();
			for (CommPortProxy port: cports)  {
				portids.add(port.getId());
				LOGGER.debug("comm port found: " + port.getId() );
			}
		} catch (CommPortConfigException e) {
			// TODO Auto-generated catch block
			LOGGER.debug("error scanning for ports: ", e);
		}
		return portids;
	}
	
	private static List<CommPortProxy> getCommPorts() throws CommPortConfigException {
        try {
            List<CommPortProxy> ports = new LinkedList<CommPortProxy>();
            String[] portNames;
            
            switch(SerialNativeInterface.getOsType()){
            	case SerialNativeInterface.OS_LINUX:
            		portNames = SerialPortList.getPortNames(Pattern.compile("(cu|ttyS|ttyUSB|ttyACM|ttyAMA|rfcomm|ttyO)[0-9]{1,3}"));
                break;
            	case SerialNativeInterface.OS_MAC_OS_X:
                    portNames = SerialPortList.getPortNames(Pattern.compile("(cu|tty)..*")); //Was "tty.(serial|usbserial|usbmodem).*")
                break;
                default:
                	 portNames = SerialPortList.getPortNames();
                break;
            }
            
            for (String portName : portNames) {
                CommPortIdentifier id = new CommPortIdentifier(portName, false);
                ports.add(new CommPortProxy(id));
            }

            return ports;
        }
        catch (UnsatisfiedLinkError e) {
            throw new CommPortConfigException(e.getMessage());
        }
        catch (NoClassDefFoundError e) {
            throw new CommPortConfigException(
                    "Comm configuration error. Check that rxtx DLL or libraries have been correctly installed.");
        }
    }
	
	String tryToTranslate(String s) {
		String r = translations.get(s);
		if (r == null) {
			String[] a = s.split("\\.");
			return a[a.length-1];
		}
		else return r;
	}
	
	String translateBack(String s) {
		String r = translationsBack.get(s);
		if (r == null) return s;
		else return r;
	}
	
	private OceanLink getMe() {
		return this;
	}
	
}
