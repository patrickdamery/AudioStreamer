package Services;

import Custom.Attributes;
import org.w3c.dom.Attr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Class that handles connection requests.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class ConnectionListener implements Runnable {

    private int listenPort = 50010;
    private DatagramSocket serverSocket;
    private DatagramPacket packet;
    private byte[] data;

    public void run() {

        // Set up Server Sockets.
        try {
            serverSocket = new DatagramSocket(listenPort);
        } catch(IOException io) {}


        while(Attributes.active) {
            try {

                // Listen for any incoming connections.
                data = new byte[1024];
                packet = new DatagramPacket(data, data.length);
                serverSocket.receive(packet);
                System.out.println("Connected to client");

                // Extract ip address and port.
                InetAddress ip = packet.getAddress();
                int port = packet.getPort();
                InetSocketAddress clientAddress = new InetSocketAddress(ip, port);

                // Create a Datagram socket to communicate exclusively with client.
                DatagramSocket dedicatedSocket = new DatagramSocket();
                String dedicatedPort = Integer.toString(dedicatedSocket.getLocalPort());
                data = dedicatedPort.getBytes();

                // Send port to client.
                packet = new DatagramPacket(data, data.length, clientAddress);
                serverSocket.send(packet);

                // Now wait for client to establish contact with dedicated Datagram socket.
                data = new byte[1024];
                packet = new DatagramPacket(data, data.length);
                dedicatedSocket.setSoTimeout(15000);
                dedicatedSocket.receive(packet);

                // Now add socket to listener Sockets.
                Attributes.listenerSocket.add(dedicatedSocket);

                // Now add Socket Address to the listener Addresses.
                Attributes.listenerAddress.add(clientAddress);
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }
}
