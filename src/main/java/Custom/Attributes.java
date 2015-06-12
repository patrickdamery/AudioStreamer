package Custom;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Class holds attributes of Audio Streaming Server.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class Attributes {

    public static String state = "";
    public static int buffer = 2048;
    public static int alarmId = 0;
    public static int userId = 0;
    public static File binaryRecording = new File("recording.bin");
    public static volatile boolean active = true;
    public static volatile ArrayList<InetSocketAddress> listenerAddress = new ArrayList<InetSocketAddress>();

}
