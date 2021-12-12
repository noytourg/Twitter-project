package bgu.spl.net.api.bidi.message;

public class LogoutMessage extends Message {

    private final short OPCode = 3;



    @Override
    public Message act() {
        if (protocol.getData().logOut(protocol.getConnectionId())){
            protocol.terminate();
            return new AckMessage(OPCode, null);
        }
        else
            return new ErrorMessage(OPCode);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public Message decode(byte nextByte) {
        return null;
    }
}
