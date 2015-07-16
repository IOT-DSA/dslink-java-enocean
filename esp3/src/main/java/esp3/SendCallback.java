package esp3;

import java.io.IOException;

import esp3.message.request.EnOceanRequest;

public interface SendCallback<T extends EnOceanRequest> {
    void sent(T req);

    void exception(IOException ioex);
}
