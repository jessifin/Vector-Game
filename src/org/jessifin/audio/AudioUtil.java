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
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jessifin.main.Main;
import org.jessifin.main.Util;

public class AudioUtil {
	
	static void record() {
		byte[] data = new byte[16*44100*20];
		for(int i = 0; i < data.length; i++) {
			//System.out.println((byte)(i/8));
			data[i] = (byte)(Math.sin(i/10));
		}
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		AudioFormat format = new AudioFormat(44100, 16, 1, false, false);
		AudioInputStream freakinStream = new AudioInputStream(stream, format, 44100*20);
		
		try {
			AudioSystem.write(freakinStream, Type.WAVE, new File("output.wav"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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