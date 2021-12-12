package bgu.spl.net.api.bidi.message;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class PostMessage extends Message {

    private final short OPCode = 5;
    private String content;
    private boolean inHashtag;
    private int count;
    private byte[] currentUserName;
    private List<String> userNames;

    public PostMessage (){
        currentUserName = new byte[1 << 10];
        userNames = new LinkedList<>();
    }

    @Override
    public Message act() {
        if (protocol.getData().post(userNames, protocol, this))
            return new AckMessage(OPCode, null);
        else
            return new ErrorMessage(OPCode);
    }

    @Override
    public Message decode(byte nextByte) {
        if (nextByte == delimiter){
            if (inHashtag)
                userNames.add(new String(currentUserName, 0, count, StandardCharsets.UTF_8));
            content = popString();
            return this;
        }
        else {
            if (nextByte == '@')
                inHashtag = true;
            else if (inHashtag & nextByte != ' '){
                currentUserName[count] = nextByte;
                count++;
            }
            else if (inHashtag & nextByte == ' '){
                inHashtag = false;
                userNames.add(new String(currentUserName, 0, count, StandardCharsets.UTF_8));
                count=0;
            }
            pushByte(nextByte);
            return null;
        }
    }


    @Override
    public byte[] encode() {
        return new byte[0];
    }


    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "0";
    }
}
