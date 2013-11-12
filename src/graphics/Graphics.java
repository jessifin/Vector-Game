package graphics;

import static org.lwjgl.opengl.GL11.*;
import entity.Entity;
import game.Game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

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
	private static float LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR;
	
	private static Shader defaultShader;
		
	private static Matrix4f modelMatrix = new Matrix4f(), projectionMatrix = new Matrix4f(), viewMatrix = new Matrix4f();
	
	public static void update() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		setup3D();
		render(Game.entities);

		Display.update();
		Display.sync(OPTIMAL_FPS);
	}
	
	private static void setup2D() {
		LEFT = 0;
		RIGHT = 16 * WIDTH/HEIGHT;
		BOTTOM = 16;
		TOP = 0;
		NEAR = -1;
		FAR = 1;
		
		projectionMatrix.set(new float[] {
				2f/(RIGHT - LEFT), 0, 0, -(RIGHT + LEFT) / (RIGHT - LEFT),
				0, 2f/(TOP - BOTTOM), 0, -(TOP + BOTTOM) / (TOP - BOTTOM),
				0, 0, -2f/(FAR - NEAR), - (FAR + NEAR) / (FAR - NEAR),
				0, 0, 0, 1
		});
		
		viewMatrix.setIdentity();
		
		GL20.glUniformMatrix4(defaultShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
		GL20.glUniformMatrix4(defaultShader.getUniform("viewMat"), false, Util.toBuffer(viewMatrix));
	}
	
	private static void setup3D() {
		projectionMatrix.set(new float[] {
				(float)(((1f / Math.tan(Math.toRadians(Game.FoV/2))) / 2f) / (WIDTH / HEIGHT)), 0, 0, 0,
				0, (float)((1f / Math.tan(Math.toRadians(Game.FoV/2))) / 2f), 0, 0,
				0, 0, (Game.Z_NEAR + Game.Z_FAR) / (Game.Z_NEAR - Game.Z_FAR), (2 * Game.Z_NEAR * Game.Z_FAR) / (Game.Z_NEAR - Game.Z_FAR),
				0, 0, -1, 0
		});
		
		Vector3f forward = new Vector3f(
				Game.player.pos.x - Game.camPos.x,
				Game.player.pos.y - Game.camPos.y,
				Game.player.pos.z - Game.camPos.z
				);
		
		forward.normalize();
		
		Vector3f side = new Vector3f();
		side.cross(forward, Game.camUp);
		side.normalize();
		
		Vector3f up = new Vector3f();
		up.cross(side, forward);
		
		viewMatrix.set(new float[] {
				side.x, side.y, side.z, 0,
				up.x, up.y, up.z, 0,
				-forward.x, -forward.y, -forward.z, 0,
				0, 0, 0, 1
		});
		
		Matrix4f pootis = new Matrix4f(new float[] {
				1, 0, 0, -Game.camPos.x,
				0, 1, 0, -Game.camPos.y,
				0, 0, 1, -Game.camPos.z,
				0, 0, 0, 1
		});
		
		viewMatrix.mul(pootis);
		
		GL20.glUniformMatrix4(defaultShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
		GL20.glUniformMatrix4(defaultShader.getUniform("viewMat"), false, Util.toBuffer(viewMatrix));
	}

    private static void render(ArrayList<Entity> entities) {    	
    	for(int e = 0; e < entities.size(); e++) {
    		
    		Vector3f erot = new Vector3f(entities.get(e).rot.x, entities.get(e).rot.y, entities.get(e).rot.z);
			
			Matrix4f exRot = new Matrix4f(new float[] {
					1, 0, 0, 0,
					0, (float) Math.cos(erot.x), - (float) Math.sin(erot.x), 0,
					0, (float) Math.sin(erot.x), (float) Math.cos(erot.x), 0,
					0, 0, 0, 1
			});
			
			Matrix4f eyRot = new Matrix4f(new float[] {
					(float) Math.cos(erot.y), 0, (float) Math.sin(erot.y), 0,
					0, 1, 0, 0,
					- (float) Math.sin(erot.y), 0, (float) Math.cos(erot.y), 0,
					0, 0, 0, 1
			});
			
			Matrix4f ezRot = new Matrix4f(new float[] {
					(float) Math.cos(erot.z), - (float) Math.sin(erot.z), 0, 0,
					(float) Math.sin(erot.z), (float) Math.cos(erot.z), 0, 0,
					0, 0, 1, 0,
					0, 0, 0, 1
			});
			
			Matrix4f eTrans = new Matrix4f(new float[] {
					1, 0, 0, entities.get(e).pos.x,
					0, 1, 0, entities.get(e).pos.y,
					0, 0, 1, entities.get(e).pos.z,
					0, 0, 0, 1
			});
			
    		for(int m = 0; m < entities.get(e).model.length; m++) {
    			Model model = entities.get(e).model[m];
    			
    	    	GL20.glUniform1i(defaultShader.getUniform("stride"), (int)(Main.numLoops % (model.indexCount / 3)));
    			
    			Matrix4f trans = new Matrix4f(new float[] {
    					1, 0, 0, model.pos.x,
    					0, 1, 0, model.pos.y,
    					0, 0, 1, model.pos.z,
    					0, 0, 0, 1
    			});
    			
    			Vector3f rot = new Vector3f(model.rot.x, model.rot.y, model.rot.z);
    			
    			Matrix4f xRot = new Matrix4f(new float[] {
    					1, 0, 0, 0,
    					0, (float) Math.cos(rot.x), - (float) Math.sin(rot.x), 0,
    					0, (float) Math.sin(rot.x), (float) Math.cos(rot.x), 0,
    					0, 0, 0, 1
    			});
    			
    			Matrix4f yRot = new Matrix4f(new float[] {
    					(float) Math.cos(rot.y), 0, (float) Math.sin(rot.y), 0,
    					0, 1, 0, 0,
    					- (float) Math.sin(rot.y), 0, (float) Math.cos(rot.y), 0,
    					0, 0, 0, 1
    			});
    			
    			Matrix4f zRot = new Matrix4f(new float[] {
    					(float) Math.cos(rot.z), - (float) Math.sin(rot.z), 0, 0,
    					(float) Math.sin(rot.z), (float) Math.cos(rot.z), 0, 0,
    					0, 0, 1, 0,
    					0, 0, 0, 1
    			});			
    			
    			Matrix4f scale = new Matrix4f(new float[] {
    					entities.get(e).scale.x * model.scale.x, 0, 0, 0,
						0, entities.get(e).scale.y * model.scale.y, 0, 0,
						0, 0, entities.get(e).scale.z * model.scale.z, 0,
						0, 0, 0, 1});
    			
    			modelMatrix.setIdentity();
    			modelMatrix.mul(eTrans);
    			modelMatrix.mul(exRot); modelMatrix.mul(eyRot); modelMatrix.mul(ezRot);
    			modelMatrix.mul(trans);
    			modelMatrix.mul(xRot); modelMatrix.mul(yRot); modelMatrix.mul(zRot);

    			modelMatrix.mul(scale);
    			
    			GL30.glBindVertexArray(model.vaoID);
    			GL20.glEnableVertexAttribArray(0);
    			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);    			    			

    			GL20.glUniformMatrix4(defaultShader.getUniform("modelMat"), false, Util.toBuffer(modelMatrix));

    			GL20.glUniform4f(defaultShader.getUniform("color"), model.colorFill.x + (entities.get(e).distanceFromCam - entities.get(e).lastDistanceFromCam)/2f, model.colorFill.y, model.colorFill.z, model.colorFill.w);
    			glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);

    			GL20.glUniform4f(defaultShader.getUniform("color"), model.colorLine.x, model.colorLine.y, model.colorLine.z, model.colorLine.w);
    			
    			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    	    	GL20.glDisableVertexAttribArray(0);
    			GL30.glBindVertexArray(0);
    		}
    	}
    	
    }
    
    public static void takeScreenShot() {
    	ByteBuffer data = BufferUtils.createByteBuffer((int)(WIDTH * HEIGHT * 3));
    	glReadPixels(0, 0, (int) WIDTH, (int) HEIGHT, GL_RGB, GL_UNSIGNED_BYTE, data);
    	Util.saveScreenshot(data);
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
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glEnable(GL_DEPTH_TEST);
		
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
		GL11.glViewport(0,0,(int)WIDTH,(int)HEIGHT);
		
    	GL20.glUseProgram(defaultShader.programID);

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
