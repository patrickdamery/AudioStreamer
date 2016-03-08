import Custom.Attributes;
import Services.ConnectionListener;
import Services.TCPConnectionListener;
import Services.StateTracker;
import Services.Stream;
import org.apache.log4j.varia.NullAppender;
import org.json.JSONException;
import org.json.JSONObject;
import Utils.DB;

/**
 * Main Class of Audio Streaming Server.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class Server {

    public static void main(String args[]) {

        // Define current alarm state.
        boolean alarm = false;

        // Configure apache.
        org.apache.log4j.BasicConfigurator.configure(new NullAppender());

        // Loop until alarm is activated.
        while(!alarm) {
            try {
                try {
                    JSONObject result = DB.state();
                    System.out.println(result.getString("state"));

                    try {

                        // Check that query was successful.
                        boolean success = result.getBoolean(DB.SUCCESS);
                        if (success) {

                            // Check if alarm was activated.
                            Attributes.state = result.getString("state");
                            /*if (Attributes.state.equalsIgnoreCase("inactive")) {

                                // If alarm was activated get Attributes.
                                Attributes.alarmId = 10;
                                Attributes.userId = 1;
                                alarm = true;
                            }*/
                            if (!Attributes.state.equalsIgnoreCase("inactive")) {

                                // If alarm was activated get Attributes.
                                Attributes.alarmId = result.getInt("alarmId");
                                Attributes.userId = result.getInt("id");
                                alarm = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(2000);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        // Create record for recording.
        try {
            DB.addRecording(Integer.toString(Attributes.alarmId), Integer.toString(Attributes.userId));
        } catch (Exception e) {
            // Don't do anything.
            e.printStackTrace();
        }

        // Start the server threads.
        System.out.println("Starting Connection Listener Thread.");
        Thread ConnectionListener = new Thread(new ConnectionListener());
        ConnectionListener.start();

        System.out.println("Starting TCP Fallback Listener Thread.");
        Thread TCPConnectionListener = new Thread(new TCPConnectionListener());
        TCPConnectionListener.start();

        System.out.println("Starting Stream Thread.");
        Thread Stream = new Thread(new Stream());
        Stream.start();

        System.out.println("Starting State Tracker Thread.");
        Thread StateTracker = new Thread(new StateTracker());
        StateTracker.start();
    }
}
