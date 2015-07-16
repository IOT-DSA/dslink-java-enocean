package esp3.message.request.local;

import esp3.message.PacketType;
import esp3.message.request.EnOceanRequest;
import com.serotonin.util.queue.ByteQueue;

abstract public class CommonCommand extends EnOceanRequest {
    public enum CommandCode {
        coWrSleep(1), //
        coWrReset(2), //
        coRdVersion(3), //
        coRdSysLog(4), //
        coWrSysLog(5), //
        coWrBist(6), //
        coWrIdbase(7), //
        coRdIdbase(8), //
        coWrRepeater(9), //
        coRdRepeater(10), //
        coWrFilterAdd(11), //
        coWrFilterDel(12), //
        coWrFilterDelAll(13), //
        coWrFilterEnable(14), //
        coRdFilter(15), //
        coWrWaitMaturity(16), //
        coWrSubtel(17), //
        coWrMem(18), //
        coRdMem(19), //
        coRdMemAddress(20), //
        @Deprecated
        coRdSecurity(21), //
        @Deprecated
        coWrSecurity(22), //
        coWrLearnMode(23), //
        coRdLearnMode(24), //
        coWrSecuredeviceAdd(25), //
        coWrSecuredeviceDel(26), //
        coRdSecuredevice(27), //
        coWrMode(28), //
        coRdNumsecuredevices(29), //
        ;

        public final int id;

        private CommandCode(int id) {
            this.id = id;
        }
    }

    abstract protected CommandCode getCommandCode();

    //    private int deepSleepPeriod;
    //
    //    private boolean repEnable;
    //    private int repLevel;
    //
    //    private int filterType;
    //    private long filterValue;
    //    private boolean filterBlocks;
    //
    //    private boolean filterEnable;
    //    private boolean filterAnd;
    //
    //    private boolean waitMaturity;
    //
    //    private boolean subtelEnable;
    //
    //    private int memoryType;
    //    private long memoryAddress;
    //    private byte[] memoryData;
    //
    //    private int memoryLength;
    //
    //    private int memoryArea;
    //
    //    private boolean learnEnable;
    //    private long learnTimeout;
    //    private int learnChannel;
    //
    //    private int secureSlf;
    //    private long secureDeviceId;
    //    private long secureKey1;
    //    private long secureKey2;
    //    private long secureKey3;
    //    private long secureKey4;
    //    private int secureRollingCode;
    //
    //    private int secureDeviceIndex;
    //
    //    private int mode;

    @Override
    protected void addData(ByteQueue queue) {
        queue.push(getCommandCode().id);
        addCommandData(queue);

        //        switch (commandCode) {
        //        case coWrSleep:
        //            queue.push(0);
        //            queue.pushU3B(deepSleepPeriod);
        //            break;
        //        case coWrRepeater:
        //            queue.push(repEnable ? 1 : 0);
        //            queue.push(repLevel);
        //            break;
        //        case coWrFilterAdd:
        //            queue.push(filterType);
        //            queue.pushU4B(filterValue);
        //            queue.push(filterBlocks ? 0 : 0x80);
        //            break;
        //        case coWrFilterDel:
        //            queue.push(filterType);
        //            queue.pushU4B(filterValue);
        //            break;
        //        case coWrFilterEnable:
        //            queue.push(filterEnable ? 1 : 0);
        //            queue.push(filterAnd ? 1 : 0);
        //            break;
        //        case coWrWaitMaturity:
        //            queue.push(waitMaturity ? 1 : 0);
        //            break;
        //        case coWrSubtel:
        //            queue.push(subtelEnable ? 1 : 0);
        //            break;
        //        case coWrMem:
        //            queue.push(memoryType);
        //            queue.pushU4B(memoryAddress);
        //            queue.push(memoryData);
        //            break;
        //        case coRdMem:
        //            queue.push(memoryType);
        //            queue.pushU4B(memoryAddress);
        //            queue.pushU2B(memoryLength);
        //            break;
        //        case coRdMemAddress:
        //            queue.push(memoryArea);
        //            break;
        //        case coWrLearnMode:
        //            queue.push(learnEnable ? 1 : 0);
        //            queue.pushU4B(learnTimeout);
        //            break;
        //        case coWrSecuredeviceAdd:
        //            queue.push(secureSlf);
        //            queue.pushU4B(secureDeviceId);
        //            queue.pushU4B(secureKey1);
        //            queue.pushU4B(secureKey2);
        //            queue.pushU4B(secureKey3);
        //            queue.pushU4B(secureKey4);
        //            queue.pushU3B(secureRollingCode);
        //            break;
        //        case coWrSecuredeviceDel:
        //            queue.push(secureDeviceId);
        //            break;
        //        case coRdSecuredevice:
        //            queue.push(secureDeviceIndex);
        //            break;
        //        case coWrMode:
        //            queue.push(mode);
        //            break;
        //        }
    }

    /**
     * @param queue
     */
    protected void addCommandData(ByteQueue queue) {
        // Override as required
    }

    @Override
    protected void addOptionalData(ByteQueue queue) {
        // Override as required

        //        switch (commandCode) {
        //        case coWrLearnMode:
        //            queue.push(learnChannel);
        //            break;
        //        }
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.commonCommand;
    }
}
