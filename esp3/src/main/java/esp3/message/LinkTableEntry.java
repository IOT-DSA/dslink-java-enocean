package esp3.message;

import com.serotonin.util.ArrayUtils;
import com.serotonin.util.queue.ByteQueue;

public class LinkTableEntry {
    public static final int ALL_CHANNELS = 3;

    private int index;
    private long id;
    private int eep;
    private int channel;

    public LinkTableEntry() {
        // no op
    }

    public LinkTableEntry(int index) {
        this(index, 0xffffffffL, 0xffffff, 0xff);
    }

    public LinkTableEntry(int index, long id, int eep, int channel) {
        this.index = index;
        this.id = id;
        this.eep = eep;
        this.channel = channel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getEep() {
        return eep;
    }

    public void setEep(int eep) {
        this.eep = eep;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isInUse() {
        return id != 0xffffffffL;
    }

    public void read(byte[] data, int offset) {
        index = ArrayUtils.bitRangeValue(data, offset * 8, 8);
        id = ArrayUtils.bitRangeValueLong(data, (offset + 1) * 8, 32);
        eep = ArrayUtils.bitRangeValue(data, (offset + 5) * 8, 24);
        channel = ArrayUtils.bitRangeValue(data, (offset + 8) * 8, 8);
    }

    public void write(ByteQueue queue) {
        queue.push(index);
        queue.pushU4B(id);
        queue.pushU3B(eep);
        queue.push(channel);
    }

    @Override
    public String toString() {
        return "LinkTableEntry [index=" + Integer.toString(index, 16) + ", id=" + Long.toString(id, 16) + ", eep="
                + Integer.toString(eep, 16) + ", channel=" + channel + "]";
    }
}
