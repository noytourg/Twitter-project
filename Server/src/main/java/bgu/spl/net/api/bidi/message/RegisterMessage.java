package bgu.spl.net.api.bidi.message;

import bgu.spl.net.api.bidi.User;

public class RegisterMessage extends Message {

    private final short OPCode = 1;
    private String userName;
    private String password;

    @Override
    public Message act() {
        if (protocol.getData().register(new User(userName, password)))
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
