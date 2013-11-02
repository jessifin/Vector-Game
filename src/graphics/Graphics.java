package graphics;

import static org.lwjgl.opengl.GL11.*;
import entity.Entity;
import game.Game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import main.Main;
import main.Util;
import model.Model;
import model.ModelParser;

import org.lwjgl.BufferUtils;
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
		glClear(GL_COLOR_BUFFER_BIT);
		
		//configMats();
		render(Game.entities);
		
		//setup2D();
		//render(Game.entities);
		
		Display.update();
		Display.sync(OPTIMAL_FPS);
	}
	
	private static void configMats() {
		projectionMatrix = new Matrix4f();
		viewMatrix = new Matrix4f();
		modelMatrix = new Matrix4f();
	}

    private static void render(ArrayList<Entity> entities) {
    	GL20.glUseProgram(defaultShader.programID);
    	
    	for(int e = 0; e < entities.size(); e++) {
    		for(int m = 0; m < entities.get(e).model.length; m++) {
    			Model model = entities.get(e).model[m];
    			GL30.glBindVertexArray(model.vaoID);
    			GL20.glEnableVertexAttribArray(0);
    			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);    			    			

    			GL20.glUniform3f(defaultShader.getUniform("trans"),
    					entities.get(e).pos.x + model.pos.x,
    					entities.get(e).pos.y + model.pos.y,
    					entities.get(e).pos.z + model.pos.z);
    			
    			GL20.glUniform3f(defaultShader.getUniform("scale"),
    					entities.get(e).scale.x * model.scale.x,
    					entities.get(e).scale.y * model.scale.y,
    					entities.get(e).scale.z * model.scale.z);

    			GL20.glUniform3f(defaultShader.getUniform("rot"),
    					entities.get(e).rot.x + model.rot.x,
    					entities.get(e).rot.y + model.rot.y,
    					entities.get(e).rot.z + model.rot.z + (Main.numLoops / 20f));
    			
    			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    			GL20.glUniform4f(defaultShader.getUniform("color"), model.colorFill.x, model.colorFill.y, model.colorFill.z, model.colorFill.w);
    			glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);

    			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    			GL20.glUniform4f(defaultShader.getUniform("color"), model.colorLine.x, model.colorLine.y, model.colorLine.z, model.colorLine.w);
    			glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);
    			
    			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    			GL20.glDisableVertexAttribArray(0);
    			GL30.glBindVertexArray(0);
    		}
    	}
    	
    	GL20.glUseProgram(0);
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
			Display.create(new PixelFormat().withSamples(maxSamples), new ContextAttribs(3,2).withForwardCompatible(true).withProfileCore(true));
		} catch(LWJGLException exception) {
			Sys.alert("CRITICAL ERROR", "Something bad happened.");
			exception.printStackTrace();
		}
		
		/*
		 * GL Initialization
		 */
		
		defaultShader = ShaderParser.getShader("default");
		glClearColor(.1f,.3f,.8f,1);
		
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
		GL11.glViewport(0,0,(int)WIDTH,(int)HEIGHT);
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
