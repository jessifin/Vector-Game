package main;

import game.GameInfo;
import graphics.Graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

import javax.vecmath.Vector3f;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import audio.Audio;

public class Main {

	public static final OS SYSTEM_OS;
	private static PrintStream log;
	private static Calendar calendar;
	
	public static boolean RUNNING = true;
	public static long numLoops, numTicks;
	private static Timer tickTimer = new Timer(100);
	
	static {
		SYSTEM_OS = OS.getOS();
		
		new File("LOGS").mkdir();
		try {
			log = new PrintStream("LOGS/LOG" + getLogNumber() + ".TXT");
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
		
		calendar = Calendar.getInstance();
	}
	
	public static void run() {
		Graphics.init();
		Audio.init();
		Input.init();
		
		Audio.playMusic("le_elephante.wav");
						
		while(RUNNING && !Display.isCloseRequested()) {
			numLoops++;
			
			long initialTime = System.currentTimeMillis();
			
			Input.keyboardUpdate();
			Input.mouseUpdate();
			Graphics.update();
			Audio.update();

			Timer.updateAll((int) (System.currentTimeMillis() - initialTime));
			
			while(tickTimer.poll()) {
				tick();
			}
		}

		Audio.destroy();
		Graphics.destroy();
		
		System.exit(0);
	}
	
	public static void tick() {
		numTicks++;

		if(Display.wasResized()) {
			Graphics.WIDTH = Display.getWidth();
			Graphics.HEIGHT = Display.getHeight();
			Display.setTitle(Graphics.WIDTH + " " + Graphics.HEIGHT);
		}
	}
	
	public static void main(String... args) {
		System.setProperty("org.lwjgl.librarypath", new File("lwjgl-2.9.0/native/" + SYSTEM_OS.nativePath).getAbsolutePath());
		System.setOut(log);
		System.setErr(log);
				
		System.out.println(getTime());
		System.out.println("System OS: " + SYSTEM_OS.toString());
		
		if(args.length == 2) {
			System.out.println("User Name: " + args[0] + "\nScreen Name: " + args[1]);
		}

		run();
	}
	
	private static int getLogNumber() {
		int i = 0;
		while(new File("LOGS/LOG" + i + ".TXT").exists()) { i++; }
		return i;
	}
	
	public static String getTime() {
		calendar = Calendar.getInstance();
		String datetime = calendar.get(Calendar.MONTH) + "/" +
				calendar.get(Calendar.DAY_OF_MONTH) +  "/" +
				calendar.get(Calendar.YEAR) + " - " +
				calendar.get(Calendar.HOUR_OF_DAY) + ":" +
				calendar.get(Calendar.MINUTE) + ":" +
				calendar.get(Calendar.SECOND) + "." +
				calendar.get(Calendar.MILLISECOND);
		return datetime;
	}

	public enum OS {
		WINDOWS("windows"), MAC("macosx"), LINUX("linux"), SOLARIS("solaris"), FREE_BSD("freebsd"), NULL("");
		
		public final String nativePath;
		
		private OS(String nativePath) {
			this.nativePath = nativePath;
		}
		
		private static OS getOS() {
			try {
				String osName = System.getProperty("os.name");
				if(osName.contains("Windows")) {
					return WINDOWS;
				} else if(osName.contains("Mac")) {
					return MAC;
				} else if(osName.contains("Linux")) {
					return LINUX;
				} else if(osName.contains("Solaris")) {
					return SOLARIS;
				} else if(osName.contains("FreeBSD")) {
					return FREE_BSD;
				} else {
					throw new LWJGLException("Your OS is not supported. Sorry.");
				}
			} catch(LWJGLException exception) {
				exception.printStackTrace();
				RUNNING = false;
			}
			return NULL;
		}
	}
}
