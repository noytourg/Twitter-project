package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.message.Message;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {

    private Connections connections;
    private ServerData data;
    private int connectionId;
    private boolean shouldTerminate;
    private boolean connected;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        shouldTerminate = false;
        connected = false;
        data = ServerData.getInstance();
    }

    @Override
    public void process(T message) {
        ((Message)message).setProtocol(this);
        if (message != null) {
            Message response = ((Message) message).act();
            if (response != null){
                connections.send(connectionId, response);
            }
        }
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    public void terminate(){
        shouldTerminate = true;
        connected = false;
    }

    public int getConnectionId() {
        return connectionId;
    }

    //for Posts and PMs
    public void send (int connectionId, Message response){
        connections.send(connectionId, response);
    }

    public ServerData getData() {
        return data;
    }

    public boolean connect (){
        if (!connected) {
            connected = true;
            return true;
        }
        return false;
    }
}
