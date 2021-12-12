package bgu.spl.net.api.bidi.message;

public class LoginMessage extends Message{

    private final short OPCode = 2;
    private String userName;
    private String password;

    @Override
    public Message act() {
        if (protocol.getData().logIn(userName, password, protocol) && protocol.connect())
            return new AckMessage(OPCode, null);
        else
            return new ErrorMessage(OPCode);
    }

    @Override
    public Message decode(byte nextByte) {
        if (nextByte == delimiter){
            if (userName==null) {
                userName = popString();
                return null;
            }
            else {
                password = popString();
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
}
