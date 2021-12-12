package bgu.spl.net.api.bidi.message;

import java.util.List;

public class UserListMessage extends Message {

    private final short OPCode = 7;

    @Override
    public Message act() {
        List<String> userList = protocol.getData().userList(protocol.getConnectionId());
        if (userList == null)
            return new ErrorMessage(OPCode);
        return new AckMessage (OPCode, userList);
    }


    @Override
    public Message decode(byte nextByte) {
        return null;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }




}
