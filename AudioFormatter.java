import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFormatter {

    // Instance variables
    private File audioFile;
    private AudioInputStream audioInputStream;

    // Constructor initialized with audio file
    AudioFormatter(File audioFile) {
        this.audioFile = audioFile;
    }

    // Setter for audioInputStream
    private void setAudioInputStream() {
        try {
            this.audioInputStream =
                    AudioSystem.getAudioInputStream(audioFile);
        } catch (UnsupportedAudioFileException e) {
            Util.printErrorAndExit("Unsupported audio file"
                    + " exception for " + audioFile.getName());
        } catch (IOException e) {
            Util.printErrorAndExit("I/O exception opening audio "
                    + "input stream to " + audioFile.getName());
        }
    }

    /**
     * Returns a WAVE file audio input stream that is in a standard form
     * for comparison. Assuming the standard format as CD quality WAVE file
     * If file is a WAVE file, convert to CD quality if not already. Else
     * if file is MP3, convert to CD quality WAVE.
     * 
     * @return standard WAVE file audio input stream
     */
    public AudioInputStream getStandardAudioInputStream() {
        // Check file headers to identify audio file type
        String audioType = checkAudioFormat();
        // If not an invalid audio type
        if (audioType != null) {
            // If file is a WAVE audio file
            if (audioType.equals(Util.WAVE_AUDIO)) {
                // Set audio input stream of file
                setAudioInputStream();
                // Check audio format of the WAVE file for required spec
                if (isWAVEFormatValid()) {
                    // Check if WAVE file is CD quality format
                    if (isWAVEFileCDQuality()) {
                        return audioInputStream;
                    }
                } else {
                    Util.printErrorAndExit("WAVE file, "
                            + audioFile.getName()
                            + ", format is not as per specificatiton");
                }
            }
            // If file is a MP3 file or WAVE not in CD quality,
            // convert to standard WAVE format
            return convertToStandardWAVEAudioInputStream(audioType);

        } else {
            Util.printErrorAndExit("Invalid audio type detected for "
                    + audioFile.getName());
        }
        return audioInputStream;
    }

    /**
     * Converts the audio file to a standard WAVE format for comparison and
     * returns its audio input stream
     * 
     * @return standard WAVE file
     */
    private AudioInputStream convertToStandardWAVEAudioInputStream(
            String audioType) {

        AudioInputStream convertedAIS = null;

        String inputFilePath = audioFile.getAbsolutePath();
        int slashLoc = inputFilePath.lastIndexOf("/");
        String inputFile = inputFilePath.substring(slashLoc + 1);
        int pos = inputFile.lastIndexOf(".");
        String fname = inputFile.substring(0, pos);

        // Creating canonical file in temp directory
        // with file name appended with original audio format
        if (audioType.equals(Util.MP3_AUDIO)
                || audioType.equals(Util.MP3ID3V1_AUDIO)
                || audioType.equals(Util.MP3ID3V2_AUDIO)) {
            try {
                String outputFilePath = Util.TMP_DIR + fname + "_mp3.wav";
                // Run lame converter to create the canonical file
                Process mp3Process =
                        Runtime.getRuntime().exec(
                                new String[] { Util.LAME_EXECUTABLE,
                                        "--decode", "--silent",
                                        inputFilePath, outputFilePath });
                mp3Process.waitFor();
                File newWave = new File(outputFilePath);
                try {
                	convertedAIS =
                            AudioSystem.getAudioInputStream(newWave);
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Util.printErrorAndExit("I/O exception encountered while "
                        + "performing audio conversion using lame");
            } catch (InterruptedException e) {
                Util.printErrorAndExit("Interrupted exception encountered "
                        + "while performing audio file"
                        + " conversion using lame");
            }
        }
        else if(audioType.equals(Util.Ogg_AUDIO)){
        	String outputFilePath = Util.TMP_DIR + fname + "_ogg.wav";
        	try {
				Process oggProcess =
				        Runtime.getRuntime().exec(
				                new String[] { Util.OGG_EXECUTABLE,
				                        inputFilePath,"-o", outputFilePath });
				oggProcess.waitFor();
				File newWave = new File(outputFilePath);
				try {
					convertedAIS =
                        AudioSystem.getAudioInputStream(newWave);
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				} 
			}catch (IOException e) {
            	Util.printErrorAndExit("I/O exception encountered while "
                        + "performing audio conversion using lame");
            }catch (InterruptedException e) {
                Util.printErrorAndExit("Interrupted exception encountered "
                        + "while performing audio file"
                        + " conversion using lame");
                }
        	 }
        // Converting WAVE file into standard form by converting it first
        // to MP3 and then to WAVE
        else {
            try {
                String tmpFilePath = Util.TMP_DIR + fname + "_wav.mp3";
                // Run lame converter to create the canonical filep;
                Process waveProcess =
                        Runtime.getRuntime().exec(
                                new String[] { Util.LAME_EXECUTABLE,
                                        "--silent", inputFilePath,
                                        tmpFilePath });
                waveProcess.waitFor();
                File newMp3 = new File(tmpFilePath);
                String tmpFileName = newMp3.getName();
                int pos1 = tmpFileName.lastIndexOf(".");
                String finame = tmpFileName.substring(0, pos1);
                // converting back to .wav
                String newInputFilePath = tmpFilePath;
                String outputFilePath = Util.TMP_DIR + finame + "_mp3.wav";
                // Run lame converter to create the canonical file
                Process p1;
                p1 =
                        Runtime.getRuntime()
                                .exec(new String[] { Util.LAME_EXECUTABLE,
                                        "--decode", "--silent",
                                        newInputFilePath, outputFilePath });
                p1.waitFor();
                File newWave = new File(outputFilePath);
                try {
                	convertedAIS =
                            AudioSystem.getAudioInputStream(newWave);
                } catch (UnsupportedAudioFileException e) {
                    Util.printErrorAndExit("Unsupported audio file format");
                }

            } catch (IOException e) {
                Util.printErrorAndExit("I/O exception encountered while "
                        + "performing audio conversion using lame");
            } catch (InterruptedException e) {
                Util.printErrorAndExit("Interrupted exception "
                        + "encountered while performing audio"
                        + " file conversion using lame");
            }
        }
        return convertedAIS;
    }

    /**
     * Identifying the audio type of the file
     * 
     * @param file
     * @return String identifying type of audio file
     */
    private String checkAudioFormat() {
        // Reading the file header to identify type of audio file
        // MP3, MP3 IDv2, WAVE
        FileInputStream fis = null;
        try {
            // Reading the file
            fis = new FileInputStream(audioFile);
            byte[] buffer = new byte[12];
            int numberOfBytesRead = fis.read(buffer, 0, buffer.length);
            fis.close();

            // Reading 12 bytes as that is the minimum required to identify
            // the longest header in all the three file types
            if (numberOfBytesRead == 12) {
                // Checking extension of the file before verifying audio
                // type
                if (audioFile.getName().endsWith(Util.MP3_FILE_EXT)) {
                    // Checking for sync bits in header of MP3 format w/o
                    // ID
                    // tags
                    if ((buffer[0] & 0x000000ff) == 255) {
                        return Util.MP3_AUDIO;
                    }
                    // Checking for ID3v1 MP3 format
                    else if ((char) buffer[0] == 'T'
                            && (char) buffer[1] == 'A'
                            && (char) buffer[2] == 'G') {
                        return Util.MP3ID3V1_AUDIO;
                    }
                    // Checking for ID3v2 MP3 format
                    else if ((char) buffer[0] == 'I'
                            && (char) buffer[1] == 'D'
                            && (char) buffer[2] == '3') {
                        return Util.MP3ID3V2_AUDIO;
                    }
                }
                // Checking for Ogg files
                else if(audioFile.getName().endsWith(Util.OGG_FILE_EXT)){
                	if (((char)buffer[0] == 'O' &&
                			(char)buffer[1] == 'g' && 
                			(char)buffer[2] =='g')&&
                			(char)buffer[3] == 'S'){
                		return Util.Ogg_AUDIO;
                	}	
                }
                else {
                    // Checking for WAVE type
                    if ((char) buffer[8] == 'W' && (char) buffer[9] == 'A'
                            && (char) buffer[10] == 'V'
                            && (char) buffer[11] == 'E') {
                        return Util.WAVE_AUDIO;
                    }
                }
            } else {
                Util.printErrorAndExit("Unable to read header for "
                        + audioFile.getName());
            }
        } catch (FileNotFoundException exception) {
            Util.printErrorAndExit(audioFile.getName()
                    + ", file not found");
        } catch (IOException exception) {
            Util.printErrorAndExit("I/O exception when reading "
                    + audioFile.getName() + " header");
        }
        return null;
    }

    /**
     * Checks if the WAVE files adhere to CD quality spec
     * 
     * @param fileAudioInputStream
     * @return true iff the WAVE file is CD quality spec
     */
    private boolean isWAVEFileCDQuality() {
        if (audioInputStream.getFormat().toString()
                .equals(Util.CD_WAV_FORMAT.toString())) {
            return true;
        } else
            return false;
    }

    /**
     * Checks if the obtained WAVE file is in the specifications required
     * 
     * @param aInStream
     * @return true if the specification is valid
     */
    private boolean isWAVEFormatValid() {
        // Identifying properties of the audio file
        int channel = audioInputStream.getFormat().getChannels();
        Encoding encoding = audioInputStream.getFormat().getEncoding();
        /*
         * float frameRate = aInStream.getFormat().getFrameRate(); float
         * frameSize = aInStream.getFormat().getFrameSize();
         */
        float sampleRate = audioInputStream.getFormat().getSampleRate();
        int sampleSizeInBits =
                audioInputStream.getFormat().getSampleSizeInBits();
        boolean isBigEndian = audioInputStream.getFormat().isBigEndian();

        // WAVE file must be in little-endian (RIFF) WAVE format with
        // PCM encoding (AudioFormat 1), stereo or mono, 8- or 16-bit
        // samples, with a sampling rate of 11.025, 22.05, 44.1, or 48 kHz
        // if((channel ==2 ) &&
        // sampleSizeInBits == 16 &&
        // sampleRate == 44100.00)
        if ((channel == 1 || channel == 2)
                && (sampleSizeInBits == 16 || sampleSizeInBits == 8)
                && (sampleRate == 11025.00 || sampleRate == 22050.00
                        || sampleRate == 44100.00 || sampleRate == 48000.00)
                && (!isBigEndian)
                && (encoding.toString()
                        .equals(AudioFormat.
                                Encoding.PCM_SIGNED.toString()))) {
            return true;
        }
        return false;
    }
}
