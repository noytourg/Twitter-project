package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class TPCMain{
    public static void main(String args[]){
        int port= Integer.parseInt(args[0]);
        Server server= Server.threadPerClient(port,
                ()-> new BidiMessagingProtocolImpl<>(),
                ()-> new BidiMessageEncoderDecoderImpl());
        server.serve();


        }


}
