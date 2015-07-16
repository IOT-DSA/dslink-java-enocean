package esp3.message;

public enum RadioOrg {
    RPS(0xF6), //
    oneBS(0xD5), //
    fourBS(0xA5), //
    MSC(0xD1), //
    VLD(0xD2), //
    //    ADT(0xA6), //
    //    SM_LRN_REQ(0xC6), //
    //    SM_LRN_ANS(0xC7), //
    //    SM_REQ(0xA7), //
    SYS_EX(0xC5), //
    //    SEC(0x30), //
    //    SEC_ENCAPS(0x31), //

    custom07(0x7), //
    unknown(0xFF), //
    ;

    public final int id;

    private RadioOrg(int id) {
        this.id = id;
    }

    public static RadioOrg forId(int id) {
        for (RadioOrg e : values()) {
            if (e.id == id)
                return e;
        }
        System.out.println("No ROrg: " + id);
        return unknown;
    }
}
