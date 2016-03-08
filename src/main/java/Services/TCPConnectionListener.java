package Services;

import Custom.Attributes;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class that handles tcp connection requests.
 * This is used as a final try to get the audio stream.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class TCPConnectionListener implements Runnable {

    private ServerSocket serverSocket;
    private int listenPort = 50000;
    private Socket clientSocket;

    public void run() {

        // Set up Server Sockets.
        try {
            serverSocket = new ServerSocket(listenPort);
            serverSocket.setSoTimeout(6000);
        } catch(IOException io) {
            io.printStackTrace();
        }

        while(Attributes.active) {
            try {
                System.out.println("TCP Socket waiting.");

                // Listen for any incoming connections.
                clientSocket = serverSocket.accept();
                System.out.println("Connected to client via TCP.");

                // Set up the data output stream.
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                // Now add it to TCP Listeners array.
                Attributes.tcpListeners.add(dos);
                System.out.println("Added client TCP connection to arraylist.");
            } catch(IOException io) {
                io.printStackTrace();
            }
        }
    }
}
