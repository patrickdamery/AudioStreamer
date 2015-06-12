package Services;

import Utils.DB;
import Custom.Attributes;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Patrick Damery on 06/04/15.
 */
public class StateTracker implements Runnable {

    public void run() {
        while (Attributes.active) {
            try {
                JSONObject result = DB.state();

                try {
                    boolean success = result.getBoolean(DB.SUCCESS);

                    // Check that query was successful.
                    if (success) {

                        // Get state.
                        Attributes.state = result.getString("state");
                        if(!Attributes.state.equalsIgnoreCase("active")) {

                            // If alarm is no longer active set to false.
                            Attributes.active = false;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

                // Now wait for 15 seconds before checking server again.
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
