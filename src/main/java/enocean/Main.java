package enocean;

import enocean.log.Log4jBridge;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends DSLinkHandler {
	
private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		//args = new String[] { "-b", "http://localhost:8080/conn", "-l", "debug" };
		Log4jBridge.init();
		DSLinkFactory.startResponder("EnOcean", args, new Main());
	}
	
	@Override
	public void onResponderConnected(DSLink link){
		LOGGER.info("Connected");
		
//		c = Configuration.autoConfigure();
//		h = new DSLinkHandlerInstance();
//		h.setConfig(c);
//		provider = DSLinkFactory.generate(h);
//		provider.start();
		
		NodeManager manager = link.getNodeManager();
        Node superRoot = manager.getNode("/").getNode();
        OceanLink.start(superRoot);
	}
	
}
