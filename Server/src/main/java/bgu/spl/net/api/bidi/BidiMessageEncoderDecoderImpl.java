package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.message.Message;

public class BidiMessageEncoderDecoderImpl implements MessageEncoderDecoder {

    private short len;
    Message message;
    byte[] opCode;

    public BidiMessageEncoderDecoderImpl(){
        len = 0;
        opCode = new byte[2];
    }


    @Override
    public Object decodeNextByte(byte nextByte) {
        if (len < 2){
            opCode[len] = nextByte;
            len++;
            if (len==2){
                message = Message.opToMsg(opCode);
                if (Message.bytesToShort(opCode)==3 | Message.bytesToShort(opCode)==7) { //case message is logout
                    len=0;
                    return message;
                }
            }
            return null;
        }
        else {
            Message response = message.decode(nextByte);
            if (response==null)
                return null;
            else {
                len=0;
                return response;
            }
        }
    }


    @Override
    public byte[] encode(Object message) {
        return ((Message)message).encode();
    }

}
