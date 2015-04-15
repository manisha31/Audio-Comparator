import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;

public final class Util {
    // CONSTANTS
    public static final String WAV_FILE_EXT = ".wav";
    public static final String MP3_FILE_EXT = ".mp3";
    public static final String OGG_FILE_EXT = ".ogg";
    public static final String MP3_AUDIO = "MP3";
    public static final String Ogg_AUDIO ="OggS";
    public static final String MP3ID3V1_AUDIO = "MP3 ID3v1";
    public static final String MP3ID3V2_AUDIO = "MP3 ID3v2";
    public static final String WAVE_AUDIO = "WAVE";
    public static final AudioFormat CD_WAV_FORMAT = new AudioFormat(
            (float) 44100.0, 16, 2, true, false);
    public static final AudioFormat REDUCED_WAV_FORMAT = new AudioFormat(
            (float) 8192.0, 8, 1, true, false);
    public static final int SAMPLING_RATE = (int) CD_WAV_FORMAT
            .getSampleRate();
    public static final int CLOSEST_TWO_PWR_SAMPLING_RATE = 32768;
    public static final int DOWN_SAMPLED_SIZE = 8192;
    public static final String LAME_EXECUTABLE =
            "/course/cs4500f14/bin/lame";
    public static final String OGG_EXECUTABLE = "/usr/bin/oggdec";
    public static final String TMP_DIR = "/tmp/4AS";
    public static final int CHUNK_SIZE = 4096;
	public static final int MAX_AMP = 32767;

    /**
     * Prints an error message to console and exits the application with a
     * status other than 0
     * 
     * @param msg
     */
    public static void printErrorAndExit(String msg) {
        System.err.print("ERROR: " + msg + "\n");
        System.exit(-1);
    }

    /**
     * Prints the result of the comparison to the console
     * 
     * @param areAudiosMatched
     */
    public static void printComparisonResults(boolean areAudiosMatched,
            String lhsFile, String rhsFile, float lhsOffset, float rhsOffset) {
        String outputMsg = "";
        if (areAudiosMatched) {
            outputMsg =
                "MATCH " + lhsFile + " " + rhsFile + " " + lhsOffset
                + " " + rhsOffset;
            System.out.println(outputMsg);
        }
        
    }

    /**
     * Calculates the next number which is power of two
     */
    public static int getNextPower(float n, int power) {
        int currentValue = 2;
        while (currentValue < n) {
            currentValue = (int) Math.pow(currentValue, power);
        }
        return currentValue;
    }

    /**
     * Converts little endian byte data to Integer value
     */
    public static int converToIntFromLittleEndian(byte[] leBytes) {
        int value = 0;
        switch (leBytes.length) {
        case 4: {
            value =
                    ((0xFF & leBytes[3]) << 24)
                            | ((0xFF & leBytes[2]) << 16)
                            | ((0xFF & leBytes[1]) << 8)
                            | (0xFF & leBytes[0]);
            break;
        }
        case 2: {
            value = ((0xFF & leBytes[1]) << 8) | (0xFF & leBytes[0]);
            break;
        }
        default:
            value = 0;
        }
        return value;
    }

    /**
     * Adds the files to an array list if they are MP3 or WAVE
     * 
     * @param location
     *            of the file or directory containing files
     * @return array list containing valid files
     */
    public static ArrayList<File> obtainFiles(String location) {
        ArrayList<File> files = new ArrayList<File>();
        // Checking if the location is a file location or directory
        if (location.endsWith(MP3_FILE_EXT)
                || location.endsWith(WAV_FILE_EXT)) {
            files.add(new File(location));
        } else if (location.endsWith("/")) {
            // Obtaining the directory
            File directory = new File(location);
            // List of files in the directory
            File[] contents = directory.listFiles();
            // Only adding the MP3 and WAVE files into the list
            for (File file : contents) {
                if (file.getName().endsWith(MP3_FILE_EXT)
                        || file.getName().endsWith(WAV_FILE_EXT))
                    files.add(file);
                else {
                    Util.printErrorAndExit(file.getName()
                            + " is not a valid file" + " format");
                }
            }
        } else {
            Util.printErrorAndExit("Command line error");
        }
        return files;
    }

    /**
     * Printing the format of an audio input stream
     */
    public static String printAudioFormat(AudioInputStream ais) {
        int channel = ais.getFormat().getChannels();
        Encoding encoding = ais.getFormat().getEncoding();
        float sampleRate = ais.getFormat().getSampleRate();
        int sampleSizeInBits = ais.getFormat().getSampleSizeInBits();
        boolean isBigEndian = ais.getFormat().isBigEndian();
        String output =
                "Channels: " + channel + "\n" + "Sample rate: "
                        + sampleRate + "\n" + "Sample size (bits): "
                        + sampleSizeInBits + "\n" + "Big Endian?? "
                        + isBigEndian + "\n" + "Encoding: "
                        + encoding.toString() + "\n";
        return output;
    }
}
