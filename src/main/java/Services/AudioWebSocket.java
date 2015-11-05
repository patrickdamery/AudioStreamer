package Services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Class that creates a java web socket. Also handles data transfers with web socket.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class AudioWebSocket extends WebSocketServer {
    public AudioWebSocket( int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public AudioWebSocket( InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected to audio socket!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param sound
     *            The sound to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    public void sendToAll( byte[] sound ) {

        // Convert the byte array to a byte buffer.
        ByteBuffer soundBuffer = ByteBuffer.wrap(sound);
        Collection<WebSocket> con = connections();
        synchronized ( con ) {
            for( WebSocket c : con ) {
                c.send( soundBuffer );
            }
        }
    }
}
