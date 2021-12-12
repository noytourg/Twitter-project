package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    ConcurrentHashMap<Integer, ConnectionHandler> active;



    public ConnectionsImpl() {
        active = new ConcurrentHashMap<>();
    }


    @Override
    public boolean send(int connectionId, T msg) {
        if (active.containsKey(connectionId) & msg != null) {
            active.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        if (msg != null) {
            for (Map.Entry<Integer, ConnectionHandler> entry : active.entrySet())
                send(entry.getKey(), msg);
        }
    }


    public void connect (int connectionId, ConnectionHandler handler){
        active.put(connectionId, handler);
    }


    @Override
    public void disconnect(int connectionId) {
        active.remove(connectionId);
    }



}
