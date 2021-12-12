package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private int connectionId;
    private Connections connections;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader,
                                     BidiMessagingProtocol<T> protocol, int connectionId,
                                     Connections connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            ((ConnectionsImpl)connections).connect(connectionId, this);

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

            connections.disconnect(connectionId);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void send(T response) {
        if (response != null) {
            try {
                out.write(encdec.encode(response));
                out.flush();
            } catch (IOException e) {
                System.out.println("IO Exception");
            }
        }
    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}
