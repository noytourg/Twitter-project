package bgu.spl.net.api.bidi.message;

import java.util.List;

public class AckMessage extends Message {

    private final short OPCode = 10;
    private short returnOPCode;
    private Object object;

    public AckMessage(short returnOPCode, Object object) {
        this.returnOPCode = returnOPCode;
        this.object = object;
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
        if (returnOPCode == 1 | returnOPCode == 2 | returnOPCode == 3 |
                returnOPCode == 5 |returnOPCode == 6)
            return encodeWithoutOptional();
        else {
            switch (returnOPCode) {
                case 4: return encodeFollowORUserList();
                case 7: return encodeFollowORUserList();
                case 8: return encodeStat();
            }
            return new byte[0];
        }
    }


    private byte[] encodeWithoutOptional (){
        return combineArray(shortToBytes(OPCode), shortToBytes(returnOPCode)) ;
    }

    private byte[] encodeFollowORUserList (){
        byte[] temp1 = combineArray(shortToBytes(OPCode), shortToBytes(returnOPCode));
        temp1 = combineArray(temp1, shortToBytes((short)((List)object).size()));
        byte[] temp2 = {(byte)delimiter};
        for (String userName: (List<String>)object) {
            temp1 = combineArray(temp1, (userName).getBytes());
            temp1 = combineArray(temp1, temp2);
        }
        return temp1;
    }

    private byte[] encodeStat (){
        byte[] temp1 = combineArray(shortToBytes(OPCode), shortToBytes(returnOPCode));
        temp1 = combineArray(temp1, (shortToBytes((short)((int[])object)[0])));
        temp1 = combineArray(temp1, shortToBytes((short)((int[])object)[1]));
        temp1 = combineArray(temp1, shortToBytes((short)((int[])object)[2]));
        return temp1;
    }


}
