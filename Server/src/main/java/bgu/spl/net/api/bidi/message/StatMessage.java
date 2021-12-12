package bgu.spl.net.api.bidi.message;

public class StatMessage extends Message {

    private final short OPCode = 8;
    private String userName;

    @Override
    public Message act() {
        int[] data = protocol.getData().stat(protocol.getConnectionId(), userName);
        if (data[0] == -1)
            return new ErrorMessage(OPCode);
        return new AckMessage(OPCode, data);
    }


    @Override
    public Message decode(byte nextByte) {
        if (nextByte == delimiter) {
            userName = popString();
            return this;
        }
        else {
            pushByte(nextByte);
            return null;
        }
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }
}
