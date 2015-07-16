package esp3.message.incoming;

import com.serotonin.io.StreamUtils;
import com.serotonin.util.queue.ByteQueue;

public class ResponsePacket extends IncomingPacket {
    public enum ReturnCode {
        ok(0), //
        wrongTargetId(1), //
        notSupported(2), //
        wrongParam(3), //
        operationDenied(4), //
        wrongDataSize(5), //
        noCodeSet(6), //
        rpcFailed(8), //
        messageTimeout(9), //
        messsageTooLong(0xa), //
        wrongData(0xf), //
        ;

        public final int id;

        private ReturnCode(int id) {
            this.id = id;
        }

        static ReturnCode forId(int id) {
            for (ReturnCode e : values()) {
                if (e.id == id)
                    return e;
            }
            return null;
        }
    }

    private final ReturnCode returnCode;
    private final byte[] responseData;
    private final byte[] responseOpt;

    public ResponsePacket(ByteQueue data, ByteQueue opt) {
        int returnCodeId = data.popU1B();
        returnCode = ReturnCode.forId(returnCodeId);
        responseData = data.popAll();
        responseOpt = opt.popAll();
    }

    public ReturnCode getReturnCode() {
        return returnCode;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public byte[] getResponseOpt() {
        return responseOpt;
    }

    @Override
    public String toString() {
        return "ResponsePacket [returnCode=" + returnCode + ", responseData=" + StreamUtils.dumpHex(responseData)
                + ", responseOpt=" + StreamUtils.dumpHex(responseOpt) + "]";
    }
}
