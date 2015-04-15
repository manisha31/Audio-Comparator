import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;

public class AudioFileComparator {
    // Data structure to store audio files that have already been analyzed
    // Key = absolute path of the audio file
    // Value = Finger print value + length of the audio file data
    private static HashMap<String, ArrayList<Double>> normalizedFiles =
            new HashMap<String, ArrayList<Double>>();
    private static Logger LOGGER = MSGLogger.getInstance();

    // Default Constructor
    public AudioFileComparator() {

    }

    /**
     * Matches the audio files
     */
    public void matchAudioFiles(File lhsFile, File rhsFile) {
        // Checking if we have analyzed the file before else
        // Should obtain array list of finger prints of every 5 second
        // segment
        ArrayList<Double> lhsFingerPrints = fingerPrint(lhsFile);
        ArrayList<Double> rhsFingerPrints = fingerPrint(rhsFile);
        ResultSet rs =
                compareFingerPrints(lhsFingerPrints, rhsFingerPrints, 50,
                        5, 3);
        Util.printComparisonResults(rs.result, lhsFile.getName(),
                rhsFile.getName(), rs.lhsOffset/10.766f, rs.rhsOffset/10.766f);

    }

    /**
     * Checks if the file had been previously normalized If so, returns the
     * file location containing previous computation result Else, return
     * message stating that the key is invalid
     * 
     * @param key
     * @return file location of invalid key alert
     */
    private ArrayList<Double> fingerPrint(File file) {
        // The key for the hash map is the absolute path of the file name
        // This helps avoid name collision in the key parameter
        String key = file.getAbsolutePath();

        // Value in the hash table contains String having the sum of the
        // chunks
        // (finger print value) and the length of the audio file
        if (normalizedFiles.containsKey(key))
            return normalizedFiles.get(key);
        // Else performing identification, finger-printing and FFT on the
        // file
        else {
            LOGGER.info("Trying to get fingerprint for file : "+key);
            // Identify and format audio file
            AudioFormatter formatter = new AudioFormatter(file);
            AudioInputStream canonicalAIS =
                    formatter.getStandardAudioInputStream();
            /*
             * System.out.println(file.getName() + " format ***:\n" +
             * Util.printAudioFormat(canonicalAIS));
             */
            FingerPrint fingerPrint = new FingerPrint(canonicalAIS);

            // Finger print value of the audio file
            ArrayList<Double> fingerPrints = fingerPrint.scan();
            /*
             * // Length of the audio file data int audioDataLength =
             * fingerPrint.getAudioDataLength();
             * 
             * String value = sum + " " + audioDataLength;
             */

            // Adding the file to the hash table
            normalizedFiles.put(key, fingerPrints);

            return fingerPrints;
        }
    }

    /*
     * Tolerance and window size has to be int values, Tolerance need to be
     * calculated based on windowSize and passed to the method. Shift By
     * size is the number of elements to be incremented if no match is
     * found
     */
    private <E> ResultSet
            compareFingerPrints(ArrayList<E> f1, ArrayList<E> f2,
                    int windowSize, int tolerance, int shiftBySize) {
        // Boolean result = false;
        int counter = 0;

        for (int i = 0; i + windowSize < f1.size(); i += shiftBySize) {
            ArrayList<E> currentWindow = new ArrayList<E>();
            currentWindow = getRange(f1, i, i + windowSize);

            // (ArrayList<E>) f1.subList(i,windowSize);
            for (int j = 0; j + windowSize < f2.size(); j += shiftBySize) {
                ArrayList<E> w = getRange(f2, j, j + windowSize);
                // (ArrayList<E>)f2.subList(j, windowSize);
                // System.out.println("Comparingasub arrays");
                ++counter;
                if (compareWindow(currentWindow, w, tolerance)) {
                    /*
                     * System.out.println("First Array position: " + i +
                     * " Second array position : " + j);
                     * System.out.println("Number of Comparisons : " +
                     * counter);
                     */

                    // System.out.println(i + "-->" + (i + windowSize) +
                    // " : "
                    // + currentWindow);
                    // System.out.println(j + "-->" +(j + windowSize) +
                    // " : " +
                    // w);
                    return (new ResultSet(true, i, j));
                }
            }
        }
        return (new ResultSet(false, -1, -1));
    }

    private <E> ArrayList<E> getRange(ArrayList<E> al, int start, int end) {
        ArrayList<E> rangeList = new ArrayList<E>();
        if (end > al.size() || end < start) {
            System.out.println("Error: Incorrect range : " + start
                    + " --> " + end);
        } else {
            for (int i = start; i < end; i++) {
                rangeList.add(al.get(i));
            }
        }
        return rangeList;
    }

    private <E> Boolean compareWindow(ArrayList<E> w1, ArrayList<E> w2,
            int tolerance) {
        Boolean result = true;
        int unMatchCount = 0;

        for (int i = 0; i < w1.size(); ++i) {
            double v1 = (Double) w1.get(i);
            double v2 = (Double) w2.get(i);
            double percentDiff = (Math.abs(v1 - v2) / v1) * 100;
            if (percentDiff > 40) {
                // System.out.println("Unmatch");
                ++unMatchCount;
                if (unMatchCount > tolerance) {
                    return false;
                }
            }
        }
        return result;
    }
}
