package Services;

import Custom.Attributes;

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
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader clientReader;

    public void run() {

        // Set up Server Sockets.
        try {
            serverSocket = new ServerSocket(listenPort);
        } catch(IOException io) {}


        while(Attributes.active) {
            try {
                System.out.println("Waiting for client");

                // Listen for any incoming connections.
                clientSocket = serverSocket.accept();
                System.out.println("Connected to client");

                // Extract ip address and port.
                InetAddress ip = clientSocket.getInetAddress();
                clientReader = new InputStreamReader(clientSocket.getInputStream(), "UTF-8");
                StringBuilder receivedData = new StringBuilder();
                int data = clientReader.read();
                while(data != -1){
                    char theChar = (char) data;
                    receivedData.append(theChar);
                    data = clientReader.read();
                }
                int port = Integer.parseInt(receivedData.toString());
                InetSocketAddress clientAddress = new InetSocketAddress(ip, port);

                // Now add Socket Address to the listener Addresses.
                Attributes.listenerAddress.add(clientAddress);
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }
}
