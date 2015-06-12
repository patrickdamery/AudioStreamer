package Services;

import Custom.Attributes;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;

/**
 * Class that converts binary file to WAV file.
 * Property of Cabalry Limited.
 * Author: Robert Patrick Damery
 */
public class Convert {

    private int sampleRate = 16000;

    public void toWAV() {
        // Define the files to use.
        String strOutputFilename = Attributes.alarmId+".wav";
        File inputFile = Attributes.binaryRecording;
        File outputFile = new File(strOutputFilename);

        // Prepare the input stream.
        InputStream inputStream = null;
        try
        {
            //Load the input stream from file
            inputStream = new FileInputStream(inputFile);
        }
        catch (FileNotFoundException e)
        {
            System.exit(1);
        }

        // Now Buffer the input stream.
        inputStream = new BufferedInputStream(inputStream);

        // Define the audio format.
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

        // Get the length of each frame and prepare the audio input stream.
        long lLengthInFrames = inputFile.length() / format.getFrameSize();
        AudioInputStream ais = new AudioInputStream(inputStream, format, lLengthInFrames);

        try
        {
            // Write to the file.
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
