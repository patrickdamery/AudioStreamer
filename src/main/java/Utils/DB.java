package Utils;
/*
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;*/

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patrick on 11/12/14.
 */
public class DB {

    public static final String SUCCESS = "success";

    public static final String IP = "cabalry.com";
    public static final String STATE_URL = "https://"+IP+"/cabalry/alarmState.php";
    public static final String REC_URL = "https://"+IP+"/cabalry/getRecordings.php";
    public static final String ADD_REC_URL = "https://"+IP+"/cabalry/addRecording.php";
    public static final String UPDATE_REC_URL = "https://"+IP+"/cabalry/updateRecording.php";
    public static final String DELETE_REC_URL = "https://"+IP+"/cabalry/deleteRecording.php";
    public static final String LOST_URL = "https://"+IP+"/cabalry/stopServer.php";
    public static final String INACTIVE_URL = "https://"+IP+"/cabalry/inactive.php";

    /***
     * Function that returns the state of the alarm
     */
    public static JSONObject state() {
        // Building Empty Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(STATE_URL, "POST", params);

        return json;
    }

    /***
     * Function that returns list of recordings
     */
    public static JSONObject recordings(final String userId) {
        // Building Empty Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", userId));

        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(REC_URL, "POST", params);

        return json;
    }

    /***
     * Function that deletes recording from table
     */
    public static JSONObject deleteRecording(final String recId) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("recId", recId));

        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(DELETE_REC_URL, "POST", params);

        return json;
    }

    /***
     * Function that deletes recording from table
     */
    public static JSONObject addRecording(final String recId, String userId) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("recId", recId));
        params.add(new BasicNameValuePair("id", userId));


        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(ADD_REC_URL, "POST", params);

        return json;
    }

    /***
     * Function that deletes recording from table
     */
    public static JSONObject updateRecording(final String recId, String userId, String size) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("recId", recId));
        params.add(new BasicNameValuePair("id", userId));
        params.add(new BasicNameValuePair("size", size));


        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(UPDATE_REC_URL, "POST", params);

        return json;
    }

    /***
     * Function that changes alarm state to lost
     */
    public static JSONObject lost(final String alarmId) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("alarmId", alarmId));

        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(LOST_URL, "POST", params);

        return json;
    }

    /***
     * Function that sets current server to terminated
     */
    public static JSONObject terminate() {
        // Building Empty Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        // getting JSON Object
        JSONObject json;
        json = new JSONParser().makeHttpRequest(INACTIVE_URL, "POST", params);

        return json;
    }
}
