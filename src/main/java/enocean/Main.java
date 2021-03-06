package enocean;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.serializer.Deserializer;
import org.dsa.iot.dslink.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends DSLinkHandler {
	
private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		//args = new String[] { "-b", "http://localhost:8080/conn", "-l", "debug" };
		DSLinkFactory.start(args, new Main());
	}
	
	@Override
	public boolean isResponder() {
		return true;
	}
	
	@Override
	public void onResponderInitialized(DSLink link){
		LOGGER.info("Initialized");
		
//		c = Configuration.autoConfigure();
//		h = new DSLinkHandlerInstance();
//		h.setConfig(c);
//		provider = DSLinkFactory.generate(h);
//		provider.start();
		
		NodeManager manager = link.getNodeManager();
		Serializer copyser = new Serializer(manager);
		Deserializer copydeser = new Deserializer(manager);
        Node superRoot = manager.getNode("/").getNode();
        OceanLink.start(superRoot, copyser, copydeser);
	}
	
}
