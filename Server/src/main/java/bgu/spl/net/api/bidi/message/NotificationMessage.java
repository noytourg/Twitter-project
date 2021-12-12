package bgu.spl.net.api.bidi.message;


public class NotificationMessage extends Message {
    private final short OPCode = 9;
    private String postingUser;
    private char type;
    private String content;

    public NotificationMessage(String postingUser, char type, String content) {
        this.postingUser = postingUser;
        this.type = type;
        this.content = content;
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
        byte[] temp0 = {(byte)type};
        byte[] temp2 = {(byte) delimiter};
        byte[] temp1 = combineArray(shortToBytes(OPCode), temp0);
        temp1 = combineArray(temp1, postingUser.getBytes());
        temp1 = combineArray(temp1, temp2);
        temp1 = combineArray(temp1, content.getBytes());
        temp1 = combineArray(temp1, temp2);
        return temp1;
    }
}
