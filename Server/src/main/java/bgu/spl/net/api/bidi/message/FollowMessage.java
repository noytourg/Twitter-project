package bgu.spl.net.api.bidi.message;

import java.util.LinkedList;
import java.util.List;

public class FollowMessage extends Message {

    private final short OPCode = 4;
    private int follow; // -1 - before reading, 0 - follow, 1 - unfollow
    private int numOfUsers;
    private List<String> userNames;


    public FollowMessage() {
        follow = -1;
        numOfUsers = -1;
        this.userNames = new LinkedList<>();
    }

    @Override
    public Message act() {
        List<String> successfulUsers;
        if (follow == 0)
            successfulUsers = protocol.getData().follow(protocol.getConnectionId(), userNames);
        else
            successfulUsers = protocol.getData().unfollow(protocol.getConnectionId(), userNames);

        if (successfulUsers.size() > 0)
            return new AckMessage(OPCode, successfulUsers);
        else //failed operation for all users
            return new ErrorMessage(OPCode);
    }



    @Override
    public Message decode(byte nextByte) {
        if (follow == -1) {
            follow = nextByte - '0';
            return null;
        }
        else if (numOfUsers == -1 & len == 0){
            pushByte(nextByte);
            numOfUsers = Integer.parseInt(popString());
            return null;
        }
        else if (nextByte == delimiter & numOfUsers > 0){
            userNames.add(popString());
            numOfUsers--;
            if (numOfUsers == 0)
                return this;
            return null;
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
