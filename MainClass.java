import java.io.File;
import java.util.ArrayList;

public class MainClass {

    public static void main(String[] args) {

        // Checking if only 2 arguments are passed to the program
        // Files obtained from both locations
        ArrayList<File> filesLHS;
        ArrayList<File> filesRHS;

        // Checking for command line arguments parsing
		if ((args.length == 4)
				&& ((args[0].equals("-f") && (args[1]
						.endsWith(Util.MP3_FILE_EXT)
						|| args[1].endsWith(Util.WAV_FILE_EXT) || args[1]
							.endsWith(Util.OGG_FILE_EXT)))
						|| ((args[0].equals("-d") && !(args[1]
								.endsWith(Util.MP3_FILE_EXT)
								|| args[1].endsWith(Util.WAV_FILE_EXT) || args[1]
									.endsWith(Util.OGG_FILE_EXT))))
						&& (args[2].equals("-f") && (args[3]
								.endsWith(Util.MP3_FILE_EXT)
								|| args[3].endsWith(Util.WAV_FILE_EXT) || args[3]
									.endsWith(Util.OGG_FILE_EXT))) || ((args[2]
						.equals("-d") && !(args[1].endsWith(Util.MP3_FILE_EXT)
						|| args[1].endsWith(Util.WAV_FILE_EXT) || args[3]
							.endsWith(Util.OGG_FILE_EXT)))))) {
            // Checking if the files have the right audio extensions if
            // they
            // are files. Else, check if the files in the directory are
            // correct
            filesLHS = obtainFiles(args[1]);
            filesRHS = obtainFiles(args[3]);
            // Sending a pair of files to the AudioFileComparator
            AudioFileComparator audioFileAnalyzer =
                    new AudioFileComparator();
            for (File lhsFile : filesLHS) {
                for (File rhsFile : filesRHS) {
                    // Analyzing and compare audio files
                    audioFileAnalyzer.matchAudioFiles(lhsFile, rhsFile);
                }
            }
            System.exit(0);
        } else {
            Util.printErrorAndExit("Command line error");
        }
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
        if ((location.endsWith(Util.MP3_FILE_EXT) || location
                .endsWith(Util.WAV_FILE_EXT) 
                || location.endsWith(Util.OGG_FILE_EXT))) {
            files.add(new File(location));
        } else if (location.endsWith("/")) {
            // Obtaining the directory
            File directory = new File(location);
            // List of files in the directory
            File[] contents = directory.listFiles();
            // Only adding the MP3 and WAVE files into the list
            for (File file : contents) {
                if (file.getName().endsWith(Util.MP3_FILE_EXT)
                        || file.getName().endsWith(Util.WAV_FILE_EXT) ||
                        file.getName().endsWith(Util.OGG_FILE_EXT))
                    files.add(file);
                else {
                    Util.printErrorAndExit(file.getName()
                            + " is not a valid file" + " format");
                }
            }
        } else {
            Util.printErrorAndExit("Unsupported file formats");
        }
        return files;
    }
}
