package org.jessifin.audio;

import static org.lwjgl.openal.AL10.*;

import org.jessifin.audio.AudioUtil.BufferData;
import org.jessifin.entity.Entity;
import org.jessifin.game.Game;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.vecmath.Vector3f;

import org.jessifin.main.Main;
import org.jessifin.main.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

public class Audio {
		
	public static int numSources = 32;
	
	private static HashMap<String,Buffer> loadedBuffers = new HashMap<String,Buffer>();
	private static Source[] sources = new Source[numSources];
				
	private static long lastUpdate = System.currentTimeMillis();
	private static int timePassed;
	
	public static void init() {
		try {
			AL.create();
		} catch (LWJGLException exception) {
			Main.RUNNING = false;
			System.err.println("Error occured in creating the OpenAL Context.");
	        exception.printStackTrace();
		}
		
		System.out.println("Creating " + sources.length + " audio sources");
		for(int i = 0; i < sources.length; i++) {
			int sourceID = alGenSources();
			sources[i] = new Source(sourceID);
		}
		AudioUtil.record();
	}
	
	public static void update() {
		timePassed = (int) (System.currentTimeMillis() - lastUpdate);
		for(Source source: sources) {
			if(source.hasStopped()) {
				alSourceStop(source.id);
			}
			if(source.isPlaying()) {
				source.update(timePassed);
				if(source.currentBuffer.isMusic) {
					source.currentBuffer.gain = Game.musicVolume;
					source.currentBuffer.pitch = 1;
				}
				bindSource(source, source.currentBuffer);
			}
		}

		float[] orientation = {Game.player.pos.x, Game.player.pos.y, Game.player.pos.z,
				Game.player.rot.x, Game.player.rot.y, Game.player.rot.z};
		FloatBuffer orientationBuffer = BufferUtils.createFloatBuffer(6);
		orientationBuffer.put(orientation);
		orientationBuffer.flip();
		
		alListener(AL_POSITION, Util.toBuffer(Game.camPos));
		//I can't get these to work properly, but the audio should be sufficient for now.
		//alListener(AL_VELOCITY, Util.toBuffer(Game.player.vel));
		//alListener(AL_ORIENTATION, orientationBuffer);
		
		lastUpdate = System.currentTimeMillis();
	}
	
	public static Buffer play(String loc, Vector3f pos, Vector3f vel, float volume) {
		if(loadedBuffers.containsKey(loc)) {
			int i = getAvailableSource();
			Buffer buffer = loadedBuffers.get(loc);
			buffer.looping = false;
			buffer.gain = volume * Game.fxVolume;
			buffer.isMusic = false;
			sources[i].currentBuffer = buffer;
			sources[i].pos = pos;
			alSource(sources[i].id, AL_POSITION, Util.toBuffer(pos));
			alSource(sources[i].id, AL_VELOCITY, Util.toBuffer(vel));
			bindSource(sources[i], buffer);
			alSourcePlay(sources[i].id);
			return buffer;
		} else {
			loadBuffer(loc);
			return play(loc, pos, vel, volume);
		}
	}
	
	public static Buffer playAtEntity(String loc, Entity entity, float volume) {
		return play(loc, entity.pos, entity.vel, volume);
	}
	
	public static Buffer playMusic(String loc) {
		if(loadedBuffers.containsKey(loc)) {
			int i = getAvailableSource();
			Buffer buffer = loadedBuffers.get(loc);
			buffer.looping = true;
			buffer.gain = Game.musicVolume;
			buffer.isMusic = true;
			bindSource(sources[i], buffer);
			alSourcePlay(sources[i].id);
			return buffer;
		} else {
			loadBuffer(loc);
			return playMusic(loc);
		}
	}
	
	private static void loadBuffer(String loc) {
		System.out.println("Loading audio buffer \"" + loc + "\"");

		BufferData data = AudioUtil.readBuffer(loc);
		
		int bufferID = alGenBuffers();
		
		boolean mono = data.CHANNELS == 1;
		boolean sample8bits = data.SAMPLE_SIZE == 8;
		
		int alFormat;
		if(mono && sample8bits) {
			alFormat = AL10.AL_FORMAT_MONO8;
		} else if (mono && !sample8bits) {
			alFormat = AL10.AL_FORMAT_MONO16;
		} else if(!mono && sample8bits) {
			alFormat = AL10.AL_FORMAT_STEREO8;
		} else {
			alFormat = AL10.AL_FORMAT_STEREO16;
		}
		
		alBufferData(bufferID, alFormat, data.BUFFER, data.SAMPLE_RATE);
						
		int size = alGetBufferi(bufferID, AL_SIZE);
		int freq = alGetBufferi(bufferID, AL_FREQUENCY);
		int channels = alGetBufferi(bufferID, AL_CHANNELS);
		int bits = alGetBufferi(bufferID, AL_BITS);
		float duration = (float)((size)/(freq*channels*(bits/8f)));
		
		Buffer sound = new Buffer(bufferID, duration, loc);
		
		loadedBuffers.put(loc,sound);
	}
	
	private static int getAvailableSource() {		
		for(int i = 0; i < sources.length; i++) {
			if(!sources[i].isPlaying()) {
				sources[i].setPlaying();
				return i;
			}
		}
		
		int bestSource = 0;
		double amountLeftToPlay = 0;
		for(int i = 0; i < sources.length; i++) {
			double percentPlayed = sources[i].millisPlayed/sources[i].currentBuffer.duration;
			if(percentPlayed > amountLeftToPlay && !sources[i].currentBuffer.isMusic) {
				amountLeftToPlay = percentPlayed;
				bestSource = i;
			}
		}
		sources[bestSource].setPlaying();
		sources[bestSource].millisPlayed = 0;
		return bestSource;
	}

	private static void bindSource(Source source, Buffer buffer) {
		source.currentBuffer = buffer;
		alSourcei(source.id, AL_BUFFER, buffer.id);
		alSourcei(source.id, AL_LOOPING, buffer.looping ? AL_TRUE : AL_FALSE);
		alSourcef(source.id, AL_PITCH, buffer.pitch);
		alSourcef(source.id, AL_GAIN, buffer.gain);
		source.setPlaying();
	}
	
	static void updateSource(Source source) {
		alSource(source.id, AL_POSITION, Util.toBuffer(source.pos));
	}
	
	public static void destroy() {
		System.out.println("Removing " + sources.length + " audio sources ");
		for(int i = 0; i < sources.length; i++) { 
			alDeleteSources(sources[i].id);
		}
		
		Iterator<Entry<String,Buffer>> i = loadedBuffers.entrySet().iterator();
		while(i.hasNext()) {
			Buffer buffer = i.next().getValue();
			System.out.println("Removing audio buffer \"" + buffer.name + "\"");
			alDeleteBuffers(buffer.id);
		}

		AL.destroy();
	}
}
