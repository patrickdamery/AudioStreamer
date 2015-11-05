import Custom.Attributes;
import Services.ConnectionListener;
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
            }
        }

        // Create record for recording.
        try {
            DB.addRecording(Integer.toString(Attributes.alarmId), Integer.toString(Attributes.userId));
        } catch (Exception e) {
            // Don't do anything.
        }

        // Start the server threads.
        Thread ConnectionListener = new Thread(new ConnectionListener());
        ConnectionListener.start();

        Thread Stream = new Thread(new Stream());
        Stream.start();

        Thread StateTracker = new Thread(new StateTracker());
        StateTracker.start();
    }
}
