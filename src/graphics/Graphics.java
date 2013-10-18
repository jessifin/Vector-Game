package graphics;


import static org.lwjgl.opengl.GL11.*;
import entity.Entity;
import game.GameInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import main.Input;
import model.ModelParser;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

public class Graphics {
	
	private static int OPTIMAL_FPS = 60;
	public static String VERSION, VENDOR;
	public static DisplayMode[] availableDisplayModes;
	
	public static float WIDTH, HEIGHT;
	
	public static void update() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		setup2D();

		render(GameInfo.entities);
		
		Display.update();
		Display.sync(OPTIMAL_FPS);
	}
	
	private static void setup3D() {
		glLoadIdentity();
		GLU.gluPerspective(GameInfo.FoV, WIDTH / HEIGHT, GameInfo.Z_NEAR, GameInfo.Z_FAR);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_DEPTH_TEST);
    }
    
    private static void setup2D() {
		glLoadIdentity();
    	glMatrixMode(GL_PROJECTION);
    	glDisable(GL_DEPTH_TEST);
    	glLoadIdentity();
    	glOrtho(0,16*WIDTH/HEIGHT,0,16,0,1);
    }
    
    private static void render(ArrayList<Entity> entities) {
    	//glPushMatrix();
    	glColor3f(1,1,1);
    	
		glBegin(GL_TRIANGLES);
		glVertex2f(0,0);
		glVertex2f(0,1);
		glVertex2f(1,0);
		glVertex2f(-1,0);
		glVertex2f(-1,1);
		glVertex2f(0,0);
		glVertex2f(-1,-1);
		glVertex2f(-1,0);
		glVertex2f(0,-1);
		glVertex2f(0,-1);
		glVertex2f(0,0);
		glVertex2f(1,-1);
		glEnd();
		 
    	
    	//glPopMatrix();
    }
    
	public static void init() {
		try {
			//Creating a "dummy" context to query the max # of samples (for antialiasing)
			Display.create();
			int maxSamples = glGetInteger(GL30.GL_MAX_SAMPLES);
			System.out.println("MAX SAMPLES: " + maxSamples);
			VERSION = glGetString(GL_VERSION);
			VENDOR = glGetString(GL_VENDOR);
			System.out.println("OPENGL VENDOR: " + VENDOR + "\nVERSION: " + VERSION);
			Display.destroy();
			
			Class[] classesWithTonsOfStuffInThem = {GL11.class, GL12.class, GL13.class, GL14.class,
					GL15.class, GL20.class, GL21.class, GL30.class, GL31.class, GL32.class,
					GL33.class, GL40.class, GL41.class, GL42.class, GL43.class};
			
			ArrayList<ArrayList<String>> code = new ArrayList<ArrayList<String>>();
			
			for(Class awesomeClass: classesWithTonsOfStuffInThem) {
				Field[] fields = awesomeClass.getFields();
				Method[] methods = awesomeClass.getMethods();
				ArrayList<String> classCode = new ArrayList<String>(fields.length + methods.length);
				for(Field field: fields) {
					classCode.add(field.getName());
				}
				for(Method method: methods) {
					String methodString = method.getName() + "(";
					for(int parameter = 0; parameter < method.getParameterTypes().length; parameter++) {
						methodString += method.getParameterTypes()[parameter].getSimpleName();
						if(parameter != method.getParameterTypes().length - 1) {
							methodString += ',';
						}
					}
					methodString += ')';
					classCode.add(methodString);
				}
				code.add(classCode);
			}
			
			availableDisplayModes = Display.getAvailableDisplayModes();

			//Creating the actual context that will be used by the game.
			Display.setVSyncEnabled(true);
			Display.setResizable(true);
			Display.setTitle("Vector Game");
			Display.setInitialBackground(.1f,.3f,.8f);
			//Display.setDisplayModeAndFullscreen(getBestDisplayMode());
			Display.create(new PixelFormat().withSamples(maxSamples));
		} catch(LWJGLException exception) {
			Sys.alert("CRITICAL ERROR", "Something bad happened.");
			exception.printStackTrace();
		}
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
	}
	
	private static DisplayMode getBestDisplayMode() {
		int bestScore = 0, index = 0;
		for(int i = 0; i < availableDisplayModes.length; i++) {
			DisplayMode dm = availableDisplayModes[i];
			int score = dm.getWidth() * dm.getHeight() * dm.getBitsPerPixel();
			if(score > bestScore && dm.isFullscreenCapable()) {
				bestScore = score;
				index = i;
			}
		}
		return availableDisplayModes[index];
	}
	
	public static void destroy() {
		ModelParser.clearModelMap();
		Display.destroy();
	}
}
