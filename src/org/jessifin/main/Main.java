package org.jessifin.main;

import org.jessifin.game.Game;
import org.jessifin.graphics.Graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

import org.jessifin.model.ModelParser;
import org.jessifin.net.WebUtil;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.jessifin.audio.Audio;

public class Main {

	//2596 lines of code!
	
	public static final OS SYSTEM_OS;
	public final static boolean debug = true;
	private static PrintStream log;
	private static Calendar calendar;
	public static final File resourceLoc;
	
	public static boolean RUNNING = true;
	public static long numLoops, numTicks, millisPassed;
	private static Timer tickTimer = new Timer(100), performanceTimer = new Timer(100000);
	
	public static Random rng = new Random();
	
	static {
		if(!debug) {
			new File("LOGS").mkdir();
			try {
				log = new PrintStream("LOGS/LOG" + getLogNumber() + ".TXT");
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			}
			System.setOut(log);
			System.setErr(log);
		}
		SYSTEM_OS = OS.getOS();
		resourceLoc = new File((debug?"src/":"bin/") + "org/jessifin/res");
		calendar = Calendar.getInstance();
	}
	
	public static void run() {
		Graphics.init();
		Audio.init();
		Input.init();
		Physics.init(false);
		Game.init();

		long lastTime = System.currentTimeMillis();
		while(RUNNING && !Display.isCloseRequested()) {
			numLoops++;
			
			Graphics.update();
			Audio.update();

			long currentTime = System.currentTimeMillis();
			int timePassed = (int) (currentTime - lastTime);
			lastTime = currentTime;
			if(!Game.gui.pausesGame) {
				Physics.update(timePassed);
			}
			Game.update(timePassed);
			Input.keyboardUpdate(timePassed);
			Input.mouseUpdate(timePassed);
			millisPassed += timePassed;
			Timer.updateAll(timePassed);
			
			while(tickTimer.poll()) {
				tick();
			}
			
			while(performanceTimer.poll()) {
				System.gc();
			}
		}

		Physics.destroy();
		Audio.destroy();
		Graphics.destroy();
		
		System.out.println("FPS AVG: " + 1000f/millisPassed*numLoops);
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
		URL nativeLoc = Main.class.getResource("/org/jessifin/res/lwjgl_natives/"+SYSTEM_OS.nativePath);
		URI decodedURL = null;
		try {
			decodedURL = nativeLoc.toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.setProperty("org.lwjgl.librarypath", decodedURL.getPath());
		System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
		String username = System.getProperty("user.name");
		String refinedName = Character.toUpperCase(username.charAt(0)) + username.substring(1,username.length());
		System.out.println("Hello, " + refinedName + ". We've been waiting for you.");
		System.out.println(getTime());
		System.out.println("System OS: " + SYSTEM_OS.toString());
		
		if(args.length == 2) {
			System.out.println("User Name: " + args[0] + "\nScreen Name: " + args[1]);
		}
		
		run();
	}
	
	private static int getLogNumber() {
		int i = 0;
		while(new File("LOGS/LOG" + i + ".txt").exists()) { i++; }
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
