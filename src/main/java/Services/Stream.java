package Services;

import Utils.DB;
import Custom.Attributes;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class that handles the audio recording and streaming.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class Stream implements Runnable {
    private int audioPort = 50005;
    private DatagramSocket audioInputSocket, audioOutputSocket;
    private byte[] receiveData;
    private DatagramPacket receivePacket;
    private DataOutputStream fileWriter;

    public void run() {
        // Setup the Server Sockets.
        try {
            audioInputSocket = new DatagramSocket(audioPort);
            System.out.println("IP address :" + audioInputSocket.getLocalAddress());
            audioInputSocket.setSoTimeout(120000);
            receiveData = new byte[Attributes.buffer];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
        } catch(SocketException se) {}

        // Set up the DataOutputStream.
        try {
            fileWriter = new DataOutputStream(new FileOutputStream(Attributes.binaryRecording));
        } catch(FileNotFoundException fe) {}

        // Get packets while alarm is active.
        while(Attributes.active) {
            getPackets();
        }
    }

    /***
     * Function that gets the audio data packets
     * saves them, and streams the audio.
     */
    private void getPackets() {
        try {
            // Wait until packet is received.
            //System.out.println("Receiving Data");
            audioInputSocket.receive(receivePacket);

            // Write to Binary file.
            fileWriter.write(receiveData, 0, receiveData.length);

            // Send data.
            sendData(receivePacket.getData());
        } catch (IOException e) {
            e.printStackTrace();
            if(Attributes.active) {
                System.out.println("Connection Lost.");
                Attributes.active = false;

                // If connection times out close it.
                lost();
            } else {
                System.out.println("Alarm finished correctly.");

                // If alarm is not active finish server.
                finish();
            }
        }
    }

    /***
     * Function that sends data to all established listeners
     * @param data = data to be sent to multiple listeners
     */
    private void sendData(byte[] data) {
        try {

            // Take current listeners addresses and sockets
            // and loop through them to send audio.
            ArrayList<InetSocketAddress> l = Attributes.listenerAddress;
            ArrayList<DatagramSocket> s = Attributes.listenerSocket;
            for(int i = 0; i < l.size(); i++) {

                // Get Datagram Socket.
                audioOutputSocket = s.get(i);

                // Set up Datagram Packet to send data.
                DatagramPacket audioPacket = new DatagramPacket(data, data.length, l.get(i).getAddress(), l.get(i).getPort());

                // Send audio packet.
                audioOutputSocket.send(audioPacket);
                System.out.println("Sending Data.");
            }
        } catch (Exception e) {}
    }

    /***
     * Function that saves recording and terminates alarm
     */
    private void finish() {
        System.out.println("Converting to audio");

        // Convert audio file.
        new Convert().toWAV();

        // Get recording size.
        File recording = new File(Attributes.alarmId+".wav");
        long size = recording.length();
        JSONArray recordings = new JSONArray();

        // Now get existing recordings of user.
        try {

            // Get recordings list.
            JSONObject result = DB.recordings(Integer.toString(Attributes.userId));
            try {
                boolean success = result.getBoolean(DB.SUCCESS);

                // Check that query was successful.
                if (success) {

                    // Make sure it's not empty.
                    if(!result.getBoolean("empty")) {

                        // Get recordings.
                        recordings = result.getJSONArray("recordings");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Define the limit at 10Gb.
        long limit = 10000000000l;

        // Get user's used space.
        long used = 0;
        try {
            if (recordings.length() > 0) {
                for (int i = 0; i < recordings.length(); i++) {
                    used += recordings.getJSONObject(i).getLong("size");
                }
            }

            // Check that we are still within limit.
            if (limit < used+size) {

                // Delete oldest recording.
                // We are not being too picky with space,
                // They probably won't use 10Gb anyway.
                DB.deleteRecording(Integer.toString(recordings.getJSONObject(0).getInt("recId")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean succeed = false;
        while(!succeed) {
            System.out.println("Attempting to upload.");
            try {
                // Set up Swift account.
                AccountConfig config = new AccountConfig();
                config.setUsername("patrick@alonica.net");
                config.setPassword("\"Larzond\"");
                config.setAuthUrl("https://auth.runabove.io/v2.0/tokens");
                config.setTenantId("1adae5549ebc418e9a0f05d70febef76");
                config.setTenantName("32384612");
                Account account = new AccountFactory(config).createAccount();

                // Now get the Cabalry Container.
                Container container = account.getContainer("Cabalry");

                // Now upload the recording.
                StoredObject object = container.getObject(Attributes.alarmId + ".wav");
                object.uploadObject(recording);

                // Now let's check to ensure file was uploaded successfully.
                Collection<StoredObject> objects = container.list();
                for (StoredObject currentObject : objects) {
                    if(currentObject.getName().equals(Attributes.alarmId + ".wav")) {
                        System.out.println("Uploaded Successfully.");
                        succeed = true;
                    }
                }

            } catch (Exception e) {

                // Don't do anything.
                e.printStackTrace();
            }
        }

        // Now update recording in db.
        DB.updateRecording(Integer.toString(Attributes.alarmId), Integer.toString(Attributes.userId), Long.toString(size));

        // Kill audio server.
        DB.terminate();
    }

    /***
     * Function that saves recording and terminates alarm in case
     * of lost connection
     */
    private void lost() {
        System.out.println("Converting to audio");

        // Convert audio file.
        new Convert().toWAV();
        // Get recording size.
        File recording = new File(Attributes.alarmId+".wav");
        long size = recording.length();
        JSONArray recordings = new JSONArray();

        // Now get existing recordings of user.
        try {
            // Get recordings list.
            JSONObject result = DB.recordings(Integer.toString(Attributes.userId));
            try {
                boolean success = result.getBoolean(DB.SUCCESS);

                // Check that query was successful.
                if (success) {

                    // Make sure it's not empty.
                    if(!result.getBoolean("empty")) {

                        // Get recordings.
                        recordings = result.getJSONArray("recordings");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Define the limit at 10Gb.
        long limit = 10000000000l;

        // Get user's used space.
        long used = 0;
        try {
            if (recordings.length() > 0) {
                for (int i = 0; i < recordings.length(); i++) {
                    used += recordings.getJSONObject(i).getLong("size");
                }
            }

            // Check that we are still within limit.
            if (limit < used+size) {
                // Delete oldest recording.
                // We are not being too picky with space,
                // They probably won't use 10Gb anyway.
                DB.deleteRecording(Integer.toString(recordings.getJSONObject(0).getInt("recId")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean succeed = false;
        while(!succeed) {
            System.out.println("Attempting to upload.");
            try {
                // Set up Swift account.
                AccountConfig config = new AccountConfig();
                config.setUsername("patrick@alonica.net");
                config.setPassword("\"Larzond\"");
                config.setAuthUrl("https://auth.runabove.io/v2.0/tokens");
                config.setTenantId("1adae5549ebc418e9a0f05d70febef76");
                config.setTenantName("32384612");
                Account account = new AccountFactory(config).createAccount();

                // Now get the Cabalry Container.
                Container container = account.getContainer("Cabalry");

                // Now upload the recording.
                StoredObject object = container.getObject(Attributes.alarmId + ".wav");
                object.uploadObject(recording);

                // Now let's check to ensure file was uploaded successfully.
                Collection<StoredObject> objects = container.list();
                for (StoredObject currentObject : objects) {
                    if(currentObject.getName().equals(Attributes.alarmId + ".wav")) {
                        System.out.println("Uploaded Successfully.");
                        succeed = true;
                    }
                }

            } catch (Exception e) {

                // Don't do anything.
                e.printStackTrace();
            }
        }

        // Now update recording in db.
        DB.updateRecording(Integer.toString(Attributes.alarmId), Integer.toString(Attributes.userId), Long.toString(size));

        // Kill audio server.
        DB.lost(Integer.toString(Attributes.alarmId));
    }
}
