package graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import entity.Entity;
import game.GameInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import main.Util;
import model.ModelParser;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
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

import shader.Shader;
import shader.ShaderParser;

public class Graphics {
	
	private static int OPTIMAL_FPS = 60;
	public static String VERSION, VENDOR;
	public static DisplayMode[] availableDisplayModes;
	
	public static float WIDTH, HEIGHT;
	
	private static Shader defaultShader;
	
	private static Matrix4f modelMatrix = new Matrix4f(), projectionMatrix = new Matrix4f(), viewMatrix = new Matrix4f();
	
	public static void update() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		setup3D();
		GLU.gluLookAt(GameInfo.camPos.x, GameInfo.camPos.y, GameInfo.camPos.z, GameInfo.player.pos.x, GameInfo.player.pos.y, GameInfo.player.pos.z, 0, 1 ,0);
		render(GameInfo.entities);
		
		setup2D();
		//render(GameInfo.entities);
		
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
    /*
    private static void rebuildProjectionMatrix() {
    	float yScale = 1f / (float) Math.tan(GameInfo.FoV / 2f);
    	projectionMatrix.setElement(0, 0, yScale / (WIDTH / HEIGHT));
    	projectionMatrix.setElement(1, 1, yScale);
    	projectionMatrix.setElement(2, 2, - (GameInfo.Z_FAR - GameInfo.Z_NEAR) / (GameInfo.Z_FAR + GameInfo.Z_NEAR));
    	projectionMatrix.setElement(2, 3, -1);
    	projectionMatrix.setElement(3, 2, -(2 * GameInfo.Z_FAR * GameInfo.Z_NEAR) / (GameInfo.Z_FAR + GameInfo.Z_NEAR));
    	projectionMatrix.setElement(3, 3, 0);
    }
    */
    private static void render(ArrayList<Entity> entities) {
    	for(int e = 0; e < entities.size(); e++) {
    		for(int m = 0; m < entities.get(e).model.length; m++) {
    	    	glPushMatrix();
    	    	glScalef(entities.get(e).scale.x * entities.get(e).model[m].scale.x,
    	    			entities.get(e).scale.y * entities.get(e).model[m].scale.y,
    	    			entities.get(e).scale.z * entities.get(e).model[m].scale.z);
    	    	glTranslatef(entities.get(e).pos.x + entities.get(e).model[m].pos.x,
    	    			entities.get(e).pos.y + entities.get(e).model[m].pos.y,
    	    			entities.get(e).pos.z + entities.get(e).model[m].pos.z);
    	    	glRotatef(entities.get(e).rot.z, 0, 0, 1);
    	    	glRotatef(entities.get(e).rot.y, 0, 1, 0);
    	    	glRotatef(entities.get(e).rot.x, 1, 0, 0);
	        	glEnableClientState(GL_VERTEX_ARRAY);
	        	glBindBuffer(GL_ARRAY_BUFFER, entities.get(e).model[m].vertexID);
	        	glVertexPointer(3, GL_FLOAT, 0, 0);
	        	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, entities.get(e).model[m].indexID);
	        	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    	    	glColor4f(1,1,1,1);
	        	glDrawElements(GL_TRIANGLES, entities.get(e).model[m].indicesToRender, GL_UNSIGNED_SHORT, 0);
	        	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    	    	glColor4f(0,0,0,1);
	        	glDrawElements(GL_TRIANGLES, entities.get(e).model[m].indicesToRender, GL_UNSIGNED_SHORT, 0);
	        	glDisableClientState(GL_VERTEX_ARRAY);
	        	glPopMatrix();
    		}
    	}
    }
    
	public static void init() {
		try {
			/*
			 * 	Display Initialization
			 */
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
			Display.setInitialBackground(1,1,1);
			Display.setTitle("Vector Game");
			Display.setDisplayModeAndFullscreen(getBestDisplayMode());
			Display.create(new PixelFormat().withSamples(maxSamples));
			
			/*
			 * GL Initialization
			 */
			
			defaultShader = ShaderParser.getShader("default");
			glClearColor(.1f,.3f,.8f,1);

		} catch(LWJGLException exception) {
			Sys.alert("CRITICAL ERROR", "Something bad happened.");
			exception.printStackTrace();
		}
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
		GL11.glViewport(0,0,(int)WIDTH,(int)HEIGHT);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluPerspective(GameInfo.FoV, WIDTH / HEIGHT, GameInfo.Z_NEAR, GameInfo.Z_FAR);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glEnable(GL_POLYGON_OFFSET_FILL);
		glPolygonOffset(1f,0.5f);
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
		ShaderParser.clearShaderMap();
		Display.destroy();
	}
}
