package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String args[]){
        int port = Integer.parseInt(args[0]);
        int numOfThreads= Integer.parseInt(args[1]);
        Server server= Server.reactor(numOfThreads, port,
                ()-> new BidiMessagingProtocolImpl<>(),
                ()-> new BidiMessageEncoderDecoderImpl());
        server.serve();

    }
}
