package bgu.spl.net.api.bidi.message;

public class ErrorMessage extends Message {

    private final short OPCode = 11;
    private short returnOPCode;

    public ErrorMessage(short returnOPCodeOPCode) {
        this.returnOPCode = returnOPCodeOPCode;
    }

    @Override
    public Message act() {
        return null;
    }

    @Override
    public Message decode(byte nextByte) {
        return null;
    }

    @Override
    public byte[] encode() {
        return combineArray(shortToBytes(OPCode), shortToBytes(returnOPCode));
    }

}

