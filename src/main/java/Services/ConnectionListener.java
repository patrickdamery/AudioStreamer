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
        } catch(IOException io) {
            io.printStackTrace();
        }

        while(Attributes.active) {

            try {

                // Listen for any incoming connections.
                data = new byte[1024];
                packet = new DatagramPacket(data, data.length);
                serverSocket.receive(packet);
                System.out.println("Connected to client");

                // Extract port and ip.
                InetAddress ip = packet.getAddress();
                int port = packet.getPort();

                String received = new String(packet.getData());
                received = received.trim();
                int clientLocalPort = Integer.parseInt(received);

                // Declare clientAddress for later use.
                InetSocketAddress clientAddress = new InetSocketAddress(ip, portSelector(ip, port, clientLocalPort));

                // Prepare new port to communicate with client and send dedicatedPort to client.
                DatagramSocket dedicatedSocket = new DatagramSocket();
                dedicatedSocket.setSoTimeout(3000);
                String dedicatedPort = Integer.toString(dedicatedSocket.getLocalPort());
                data = dedicatedPort.getBytes();
                System.out.println("Dedicated Port: "+dedicatedPort);

                packet = new DatagramPacket(data, data.length, clientAddress);
                serverSocket.send(packet);
                System.out.println("Sent Client Dedicated Port.");

                // Now wait for client to establish contact with dedicated Datagram socket.
                data = new byte[1024];
                packet = new DatagramPacket(data, data.length);
                dedicatedSocket.receive(packet);
                System.out.println("Received pack.");

                // Now add socket to listener Sockets.
                Attributes.listenerSocket.add(dedicatedSocket);

                // Now add Socket Address to the listener Addresses.
                Attributes.listenerAddress.add(clientAddress);
                System.out.println("Added to listenerSocket and listenerAddress. ");
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }

    /**
     * Function picks which port to use when communicating with client. This is done in case client is behind
     * a NAT firewall.
     * @param ip IP Address of client.
     * @param port Port received from Packet.
     * @param localPort Port received from client Data.
     * @return Selected Port to use for Data transmission.
     */
    private int portSelector(InetAddress ip, int port, int localPort) {

        // Define default value for selected port.
        int selectedPort = port;
        // Compare ports, if not the same test each one.
        if (port != localPort) {
            System.out.println("Ports don't match.");

            // Receive Reply.
            boolean decision = false;
            int counter = 1;
            while(!decision) {
                if(counter == 3) {
                    return selectedPort;
                }
                try {
                    // Reply to clientLocalPort.
                    String reply = "TEST";
                    data = reply.getBytes();
                    if(counter == 1) {
                        InetSocketAddress clientAddress = new InetSocketAddress(ip, localPort);
                        packet = new DatagramPacket(data, data.length, clientAddress);
                        serverSocket.send(packet);
                        System.out.println("Testing clientLocalPort.");
                    } else {
                        InetSocketAddress clientAddress = new InetSocketAddress(ip, port);
                        packet = new DatagramPacket(data, data.length, clientAddress);
                        serverSocket.send(packet);
                        System.out.println("Testing port.");
                    }

                    byte[] confirmationData = new byte[1024];
                    DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length);
                    serverSocket.receive(confirmationPacket);
                    String received = new String(packet.getData());
                    received = received.trim();

                    if(received.equalsIgnoreCase("GOOD")) {
                        decision = true;
                        if(counter == 2) {
                            selectedPort = localPort;
                        }
                    }
                } catch (IOException io) {
                    System.out.println("Port check failed.");
                    counter++;
                }
            }
        }
        return selectedPort;
    }
}
