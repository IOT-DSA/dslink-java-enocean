package esp3.message;

public enum PacketType {
    reserved0(0), //
    radio(1), //
    response(2), //
    radioSubTel(3), //
    event(4), //
    commonCommand(5), //
    smartAckCommand(6), //
    remoteManCommand(7), //
    reserved8(8), //
    radioMessage(9), //
    radioAdvanced(10), //
    reservedEnOcean(11, 127), //
    vendorSpecific(128, 255), //
    ;

    public final byte from;
    public final byte to;

    private PacketType(int id) {
        this(id, id);
    }

    private PacketType(int from, int to) {
        this.from = (byte) from;
        this.to = (byte) to;
    }

    public static PacketType forId(int id) {
        for (PacketType e : values()) {
            if (id >= e.from && id <= e.to)
                return e;
        }
        return null;
    }
}
