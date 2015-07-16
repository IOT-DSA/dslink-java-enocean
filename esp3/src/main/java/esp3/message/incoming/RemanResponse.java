package esp3.message.incoming;

import com.serotonin.messaging.IncomingResponseMessage;
import com.serotonin.util.queue.ByteQueue;

/**
 * Generalized remote management response.
 * 
 * @author Matthew
 */
public class RemanResponse extends RemanCommandPacket implements IncomingResponseMessage {
    public RemanResponse(int functionNumber, ByteQueue data, ByteQueue optional) {
        super(functionNumber, data, optional);
    }
}
