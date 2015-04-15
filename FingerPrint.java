import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;

public class FingerPrint {
	// Audio input stream of file to finger print
	AudioInputStream audioInputStream;

	public int[] freqDivision;
	private static Logger LOGGER = MSGLogger.getInstance();

	// Constructor
	public FingerPrint(AudioInputStream audioInputStream) {
		this.audioInputStream = audioInputStream;
		//Frequency divisions for which we need to identify feature poitns
		int[] div = {2,4,8,15,30,60,119,238,476,951,1902};
		freqDivision = div;
	}

	
	/**
	 * Scans and performs the finger printing of the audio file
	 * 
	 * @return the double value indicating the finger print of the file
	 */
	public ArrayList<Double> scan() {
		// ArrayList holding the finger-print value of 1/10 sec split
		// of the audio file
		ArrayList<Double> fpList = new ArrayList<Double>();

		// Original audio data
		byte[] originalData = retrieveByteData(audioInputStream);
		//System.out.println("Original data size: "+originalData.length);

		// Down-sampling from 44.1kHz to approximately 8kHz
		// byte[] downSampledData = sampleData(originalData, 6);
		//System.out.println("Down sampled data size: "+downSampledData.length);

		// Single channel integer data of the initial audio file data
		int[] singleChannelIntData = createSingleChannelIntData(originalData);

		// Normalize spectrogram
		int[] normalizedData = normalize(singleChannelIntData);

		float sum = 0;
		int chunkSize = Util.CHUNK_SIZE;
		FFT fft = new FFT(chunkSize);
		for (int i = 0; (i + chunkSize < normalizedData.length); i +=
				chunkSize) {
			LOGGER.info("Processing chunk : "+(i*chunkSize));
			ComplexNumber[] complexFile =
					createComplexNumberData(
							Arrays.copyOfRange(normalizedData, i, i
									+ chunkSize), chunkSize);
			ComplexNumber[] fftResult = fft.fft(complexFile);
//			fpList.add(getMagnitude(Arrays.copyOfRange(fftResult, 1, fftResult.length / 2)));
			fpList.add(computeMagnitude(fftResult));
			/*
			 * double magnitude = computeMagnitude(fftResult); // Capturing
			 * sum of magnitudes of chunk sizes sum += magnitude;
			 */
		}
		return fpList;

	}

	/**
	 * Retrieve a single channel of the audio file
	 * The data is converted to int from the initial little endian form
	 * @return array of integers representing a single data channel
	 * of the audio file
	 */
	private int[] createSingleChannelIntData(byte[] audioData) {

		int numberOfChannels = audioInputStream.getFormat().getChannels();
//		System.out.println("Number of channels in DS format: "+numberOfChannels);
		int numberOfBytesPerSample =
				audioInputStream.getFormat().getSampleSizeInBits() / 8;
//		System.out.println("Number of bytes/sample DS format: "+numberOfBytesPerSample);


		// Separating the data for each channel
		int oneChannelLength = (int)Math.ceil((double)audioData.length / (numberOfBytesPerSample * numberOfChannels));
//		System.out.println("Once channel length: "+oneChannelLength);
		int[][] channelDataBuffer = new int[numberOfChannels][oneChannelLength];

		for (int i = 0; i < numberOfChannels; i++) {
			int pos = 0;
			for (int j = 0 + (i * numberOfBytesPerSample); 
					j <= audioData.length - numberOfBytesPerSample; 
					j += numberOfBytesPerSample * numberOfChannels) {
				short value =
						(short) Util.converToIntFromLittleEndian(Arrays
								.copyOfRange(audioData, j, j
										+ numberOfBytesPerSample));

				channelDataBuffer[i][pos] = value;
				pos += 1;
			}
		}

		int[] singleChannelIntData = new int[channelDataBuffer[0].length];

		// If the number of channels is 2, then take avg of sample from
		// first
		// and second channel
		if (numberOfChannels == 2) {
			for (int i = 0; i < singleChannelIntData.length; i++) {
				singleChannelIntData[i] =
						(channelDataBuffer[0][i] + channelDataBuffer[1][i]) / 2;

			}
		} else {
			singleChannelIntData = channelDataBuffer[0];
		}

		return singleChannelIntData;
	}
	

	/**
	 * Retrieves the byte representation of only the data in the audio
	 * file. Header is ignored.
	 * 
	 * @param fileAudioInputStream
	 * @return byte array containing data of the audio file
	 */
	private byte[] retrieveByteData(AudioInputStream ais) {
		// Byte array containing the audio data
		byte[] data = null;
		final ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream();
		try {
			// Buffer array for reading the audio input stream
			// Initialized to the size of the array
			byte[] buffer = new byte[ais.available()];
			// Reading till EOF and storing the value in the byte output
			// stream
			while (ais.read(buffer) != -1) {
				byteArrayOutputStream.write(buffer);
			}
			// Closing the input and output streams
			ais.close();
			byteArrayOutputStream.close();
			// Converting output stream into byte array
			data = byteArrayOutputStream.toByteArray();

		} catch (IOException ioException) {
			Util.printErrorAndExit("I/O Exception encountered while"
					+ " retrieving byte data from audio file");
		}
		return data;
	}

	
	/**
	 * Samples data from the audio file Sample size of 3 to extract left
	 * channel Sample size of 6 to down sample to 8kHz
	 * 
	 * @param audioFileData
	 * @param sampleSize
	 * @return byte array containing the sampled data
	 */
	private byte[] sampleData(byte[] audioFileData, int sampleSize) {
		int iterator = sampleSize;
		// ArrayList to store the left channel data bytes
		ArrayList<Byte> sampledDataList = new ArrayList<Byte>();
		// Iterating over the entire audio file data
		for (int i = 0; i < audioFileData.length; i++) {

			if (iterator == sampleSize) {
				// For left channel sampling, obtaining the
				// first 2 bytes every 4 bytes
				if (sampleSize == 3) {
					sampledDataList.add(new Byte(audioFileData[i]));
					sampledDataList.add(new Byte(audioFileData[i + 1]));
				}
				// Else, selecting bytes based on the sample
				// size required
				else {
					sampledDataList.add(new Byte(audioFileData[i]));
				}
				iterator = 0;
				continue;
			}
			iterator++;
		}

		// Converting Byte array to byte array
		Byte[] buffer =
				sampledDataList.toArray(new Byte[sampledDataList.size()]);
		byte[] sampledData = new byte[sampledDataList.size()];
		int i = 0;
		for (Byte b : buffer) {
			sampledData[i] = b.byteValue();
			i++;
		}
		return sampledData;
	}

	
	/**
	 * Creates an array of ComplexNumber numbers from the left channel data
	 * 
	 * @param leftChannelData
	 * @return the array of complex numbers produced
	 */
	private ComplexNumber[] createComplexNumberData(
			int[] leftChannelData, int sampleRate) {

		ComplexNumber[] complexData = new ComplexNumber[sampleRate];
		for (int i = 0; i < leftChannelData.length; i++) {
			if (i < sampleRate)
				complexData[i] = new ComplexNumber(leftChannelData[i], 0);
			/*
			 * } else { complexData[i] = new ComplexNumber(0, 0); }
			 */
		}
		return complexData;
	}

	
	/**
	 * Computes the the magnitude of the FFT signal
	 * 
	 * @param signal1
	 * @param signal2
	 * @return the magnitude of the signal / sum of root mean square values
	 */
	private double getMagnitude(ComplexNumber[] signal) {
		double result = 0.0;
		for (ComplexNumber c : signal) {
			result += c.magnitude();
		}
		return result;
	}
	
	
	private double computeMagnitude(ComplexNumber[] signal){
		double result = 0.0;
		String fingerPrint = "";
		for(int i = 0;i < freqDivision.length-1;++i){
			double maxValue =  getMagnitude(Arrays.copyOfRange(signal,freqDivision[i],freqDivision[i+1]));
			fingerPrint = fingerPrint+":"+maxValue;
			result += maxValue;
		}
		LOGGER.info("FingerPrint: "+fingerPrint+" Result :"+result);
		return result;
	}


	private int[] normalize(int[] singleChannelIntData) {
//		System.out.println("Normalizing...");
		int max = 0;
		for(int i : singleChannelIntData) {
			if(max < i) 
				max = i;
		}
//		System.out.println("MAX: "+max);
		float multiplier = (float)Util.MAX_AMP / max;
//		System.out.println("multiplier: "+multiplier);

		// Normalize channel data
		int[] normalizedData = new int[singleChannelIntData.length];
		for(int i = 0; i < normalizedData.length; i++) {
			normalizedData[i] = (int)Math.floor((double)singleChannelIntData[i] * multiplier);
		}
		return normalizedData;
	}

}
