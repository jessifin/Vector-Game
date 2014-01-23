package org.jessifin.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jessifin.main.Main;
import org.jessifin.main.Util;

public class AudioUtil {
	
	static void record() {
		Thread recordThread = new Thread() {	
			TargetDataLine microphone = null;
			
			public void run() {
				AudioFormat mircophormat = new AudioFormat(8000f, 16, 1, true, true);
				try {
					microphone = AudioSystem.getTargetDataLine(mircophormat);
					microphone.open();
					microphone.start();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				
				int arrayLength = microphone.getBufferSize();
				byte[] byteArray = new byte[arrayLength];
				microphone.read(byteArray, 0, arrayLength);
				
				long length = (long)(byteArray.length / microphone.getFormat().getFrameRate());
				ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
				AudioInputStream freakinStream = new AudioInputStream(byteStream, microphone.getFormat(), length);
				
				try {
					AudioSystem.write(freakinStream, Type.WAVE, new File("output.wav"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
		};
		
		recordThread.start();
		
	}
	
	static BufferData readBuffer(String loc) {
		AudioInputStream inputStream = null;
		AudioFormat format = null;
		byte[] data = null;
		try {
			inputStream = AudioSystem.getAudioInputStream(new FileInputStream(new File(Main.resourceLoc,"audio/"+loc)));
			
			format = inputStream.getFormat();

			data = new byte[inputStream.available()];
			inputStream.read(data);
			
		} catch (UnsupportedAudioFileException exception) {
	        exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		ByteBuffer buffer = Util.toBuffer(data);
		
		BufferData bufferData = new BufferData(buffer, format.getChannels(), format.getSampleSizeInBits(), (int)format.getSampleRate());
		
		return bufferData;
	}
	
	static class BufferData {
		
		final ByteBuffer BUFFER;
		final int CHANNELS, SAMPLE_SIZE, SAMPLE_RATE;

		BufferData(ByteBuffer buffer, int channels, int sampleSize, int sampleRate) {
			BUFFER = buffer;
			CHANNELS = channels;
			SAMPLE_SIZE = sampleSize;
			SAMPLE_RATE = sampleRate;
		}
	}
}