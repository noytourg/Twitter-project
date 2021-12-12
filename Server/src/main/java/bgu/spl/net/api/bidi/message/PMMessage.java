package bgu.spl.net.api.bidi.message;

public class PMMessage extends Message {

    private final short OPCode = 6;
    private String userName;
    private String content;


    @Override
    public Message act() {
        if (!protocol.getData().PM(protocol, this))
            return new ErrorMessage(OPCode);
        else
            return new AckMessage(OPCode, null);
    }




    @Override
    public Message decode(byte nextByte) {
        if (nextByte == delimiter){
            if (userName == null) {
                userName = popString();
                return null;
            }
            else {
                content = popString();
                return this;
            }
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

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "1";
    }
}
