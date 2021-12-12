package bgu.spl.net.api.bidi.message;

import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class Message {

    protected final char delimiter = '\0';
    protected BidiMessagingProtocolImpl protocol;
    protected byte[] bytes;
    protected int len;

    public Message(){
        protocol = new BidiMessagingProtocolImpl();
        bytes = new byte[1 << 10];
        len=0;
    }


    // message will have access to ServerData and to the protocol for changing shouldTerminate
    public void setProtocol(BidiMessagingProtocolImpl protocol){
        this.protocol = protocol;
    }


    public static Message opToMsg (byte[] byteArr){
        switch (bytesToShort(byteArr)) {
            case 1 : return new RegisterMessage();
            case 2 : return new LoginMessage();
            case 3 : return new LogoutMessage();
            case 4 : return new FollowMessage();
            case 5 : return new PostMessage();
            case 6 : return new PMMessage();
            case 7 : return new UserListMessage();
            case 8 : return new StatMessage();
        }
        return null;
    }


    public static short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }


    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


    public abstract Message act();
    public abstract Message decode(byte nextByte);
    public abstract byte[] encode();


    protected void pushByte(byte nextByte) {
        if (len >= bytes.length)
            bytes = Arrays.copyOf(bytes, len * 2);

        bytes[len++] = nextByte;
    }

    protected String popString() {
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    protected byte[] combineArray (byte[] one, byte[] two){
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        return combined;
    }
    
}
