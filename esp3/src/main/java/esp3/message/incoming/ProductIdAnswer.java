package esp3.message.incoming;

import com.serotonin.util.queue.ByteQueue;

public class ProductIdAnswer extends RemanResponse {
    private final byte[] productId;

    public ProductIdAnswer(int functionNumber, ByteQueue data, ByteQueue optional) {
        super(functionNumber, data, optional);
        productId = getMessageData();
    }

    public byte[] getProductId() {
        return productId;
    }
}
