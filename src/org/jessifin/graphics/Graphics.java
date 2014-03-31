package org.jessifin.graphics;

import static org.lwjgl.opengl.GL11.*;

import org.jessifin.entity.Entity;
import org.jessifin.game.Game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.jessifin.main.MacUtil;
import org.jessifin.main.Main;
import org.jessifin.main.OS;
import org.jessifin.main.Util;
import org.jessifin.model.Bone;
import org.jessifin.model.Model;
import org.jessifin.model.ModelData;
import org.jessifin.model.ModelParser;
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
import org.jessifin.shader.Shader;
import org.jessifin.shader.ShaderParser;

public class Graphics {
	
	private static int OPTIMAL_FPS = 60;
	public static String VERSION, VENDOR;
	public static DisplayMode[] availableDisplayModes;
	public static boolean fullscreen;
	
	public static float WIDTH, HEIGHT;
	public static float LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR;
	public static Color4f clearColor = new Color4f(0,0,0,1);
	private static final int CLEAR_MASK = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
	
	private static Shader currentShader;
	
	private static Model boxModel;
	private static Model[] font;
	public static final float charWidth = 0.39341f, charHeight = 0.59507f;
		
	private static Matrix4f projectionMatrix = new Matrix4f(), viewMatrix = new Matrix4f(), modelMatrix = new Matrix4f();
	private static Matrix4f projectionViewModelMatrix = new Matrix4f();
	private static Matrix4f generateMatrix = new Matrix4f();
	private static Stack<Matrix4f> matrixStack = new Stack<Matrix4f>();
	
	public static float gamma = 1, brightness = 0.5f, contrast = 1;
		
	public static void update() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(CLEAR_MASK);
		
		GL20.glUniform3f(currentShader.getUniform("camPos"), Game.camPos.x, Game.camPos.y, Game.camPos.z);
		GL20.glUniform1f(currentShader.getUniform("time"), Main.numLoops/100f);
		GL20.glUniform1f(currentShader.getUniform("freq"), 1);
		
		setup3D();
		renderBatch(Game.entities);
		renderText(Main.getTime() + 
				"\nCamera Position: (" + (int)Game.camPos.x + "," + (int)Game.camPos.y + "," + (int)Game.camPos.z + 
				")\nCamera Orientation: (" + Game.camUp.x + "," + Game.camUp.y + "," + Game.camUp.z + 
				")\nPlayer Position: (" + (int)Game.player.pos.x + "," + (int)Game.player.pos.y + "," + (int)Game.player.pos.z + 
				")\nFoV: " + Game.FoV + "  Player speed: " + Game.speed + "\nGame ticks: " + Main.numTicks + 
				"\nGame loops: " + Main.numLoops + "\nNumber of Entities: "  + Game.entities.size() + 
				"\nRandom number: "+Main.rng.nextInt(),
				new Vector3f(200,0,0),
				new Vector3f(0,0,0),
				new Vector3f(30,30,30),
				new Color4f(1,1,0,1),
				false);
		setup2D();
		Game.gui.render();
		Display.update();
		Display.sync(OPTIMAL_FPS);
	}
	
	private static void setup2D() {
		LEFT = 0;
		RIGHT = 16 * WIDTH / HEIGHT;
		BOTTOM = 0;
		TOP = 16;
		NEAR = -1;
		FAR = 1;
		
		projectionMatrix.set(new float[] {
				2f/(RIGHT - LEFT), 0, 0, -(RIGHT + LEFT) / (RIGHT - LEFT),
				0, 2f/(TOP - BOTTOM), 0, -(TOP + BOTTOM) / (TOP - BOTTOM),
				0, 0, -2f/(FAR - NEAR), - (FAR + NEAR) / (FAR - NEAR),
				0, 0, 0, 1
		});
		
		viewMatrix.setIdentity();
		
		GL20.glUniformMatrix4(currentShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
		GL20.glUniformMatrix4(currentShader.getUniform("viewMat"), false, Util.toBuffer(viewMatrix));
	}
	
	private static void setup3D() {
		float cotFOV = (float) (0.5f / Math.tan(Math.toRadians(Game.FoV/2)));
		
		projectionMatrix.set(new float[] {
				cotFOV * HEIGHT / WIDTH, 0, 0, 0,
				0, cotFOV, 0, 0,
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
				side.x, side.y, side.z, -side.x * Game.camPos.x - side.y * Game.camPos.y - side.z * Game.camPos.z,
				up.x, up.y, up.z, -up.x * Game.camPos.x - up.y * Game.camPos.y - up.z * Game.camPos.z,
				-forward.x, -forward.y, -forward.z, forward.x * Game.camPos.x + forward.y * Game.camPos.y + forward.z * Game.camPos.z,
				0, 0, 0, 1
		});

		GL20.glUniformMatrix4(currentShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
		GL20.glUniformMatrix4(currentShader.getUniform("viewMat"), false, Util.toBuffer(viewMatrix));
	}

    private static Matrix4f generateMatrix(Entity e) {
		return generateMatrix(e.pos, e.rot, e.scale);
	}

	private static Matrix4f generateMatrix(Vector3f pos, Vector3f rot, Vector3f scale) {
		float cosX = (float)Math.cos(rot.x);
		float sinX = (float)Math.sin(rot.x);
		float cosY = (float)Math.cos(rot.y);
		float sinY = (float)Math.sin(rot.y);
		float cosZ = (float)Math.cos(rot.z);
		float sinZ = (float)Math.sin(rot.z);
		
		float cosXsinY = cosX * sinY;
		float sinXsinY = sinX * sinY;

		generateMatrix.set(new float[] {
			cosY * cosZ * scale.x, -cosY * sinZ * scale.y, sinY * scale.z, pos.x,
			(sinXsinY * cosZ + cosX * sinZ) * scale.x, (-sinXsinY * sinZ + cosX * cosZ) * scale.y, -sinX * cosY * scale.z, pos.y,
			(-cosXsinY * cosZ + sinX * sinZ) * scale.x, (cosXsinY * sinZ + sinX * cosZ) * scale.y, cosX * cosY * scale.z, pos.z,
			0, 0, 0, 1
		});

		return generateMatrix;
	}

	private static void renderBatch(ArrayList<Entity> entities) {
    	for(int e = 0; e < entities.size(); e++) {
			
    		modelMatrix = generateMatrix(entities.get(e));
			matrixStack.push(modelMatrix);
			
			//if(entities.get(e).rootBone != null) {
			//	renderBone(entities.get(e), entities.get(e).rootBone);
			//} else {
	    		for(int m = 0; m < entities.get(e).model.length; m++) {
	    			pushMatrix(entities.get(e).model[m].matrix);
	    			renderModel(entities.get(e), entities.get(e).model[m]);
	    			popMatrix();
	       		}
			//}
			
			popMatrix();
    	}
    }
    
    private static void renderModel(Entity entity, Model model) {		
		GL20.glUniform1i(currentShader.getUniform("offset"), (int) (Main.numTicks * entity.flashSpeed));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		GL20.glUniformMatrix4(currentShader.getUniform("modelMat"), false, Util.toBuffer(matrixStack.peek()));
		GL20.glUniform4f(currentShader.getUniform("color"), model.colorFill.x * entity.colorFill.x,
				model.colorFill.y * entity.colorFill.y,
				model.colorFill.z * entity.colorFill.z,
				model.colorFill.w * entity.colorFill.w);
		
		int indicesToDraw = (int)(model.indexCount * entity.health/entity.maxHealth);
		
		glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);

		GL20.glUniform4f(currentShader.getUniform("color"), model.colorLine.x * entity.colorLine.x,
				model.colorLine.y * entity.colorLine.y,
				model.colorLine.z * entity.colorLine.z,
				model.colorLine.w * entity.colorLine.w);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
    
    private static void renderModel(Model model, Color4f color) {
		GL20.glUniform1i(currentShader.getUniform("offset"), (int)(Main.numTicks * 2));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		GL20.glUniformMatrix4(currentShader.getUniform("modelMat"), false, Util.toBuffer(matrixStack.peek()));
		GL20.glUniform4f(currentShader.getUniform("color"), color.x, color.y, color.z, color.w);
		
		glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
    
    public static void renderBox(Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
    	modelMatrix = generateMatrix(pos,rot,scale);
    	
		GL20.glUniform1i(currentShader.getUniform("offset"), 0);
		GL30.glBindVertexArray(boxModel.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, boxModel.indexID);
		GL20.glUniformMatrix4(currentShader.getUniform("modelMat"), false, Util.toBuffer(modelMatrix));
		GL20.glUniform4f(currentShader.getUniform("color"),color.x,color.y,color.z,color.w);
		
		glDrawElements(GL_TRIANGLES, boxModel.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
    }

	public static void renderBox(float x1, float y1, float x2, float y2, float rotation, Color4f color) {
		Vector3f pos = new Vector3f(x1,y1,0);
		Vector3f rot = new Vector3f(0,0,rotation);
		Vector3f scale = new Vector3f(x2-x1,y2-y1,0);
		
		renderBox(pos,rot,scale,color);
	}
	/*
	public static void renderText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
		
		glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
		GL20.glUniform1i(defaultShader.getUniform("deformColor"), 1);
				
		String[] lines = text.split("\n");

		Matrix4f textPos = generateMatrix(pos,rot,scale);
		matrixStack.push(textPos);
		
		Matrix4f xShift = new Matrix4f(new float[] {
			1, 0, 0, scale.x,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0 ,0, 1
		});
		
		for(int line = 0; line < lines.length; line++) {

			char[] characters = lines[line].toCharArray();

			for(int character = 0; character < characters.length; character++) {
				pushMatrix(xShift);
				if(characters[character] >= '!' && characters[character] <= '~') {
					renderModel(font[characters[character] - 33],color);
				}
			}
			int i = 0; while(i < characters.length) { popMatrix(); i++; }
		}
		
		matrixStack.pop();
		
		glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);

	}
	*/
	
	
	public static void renderText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color, boolean renderBox) {
		String[] lines = text.split("\n");
		int longestLine = 0;
		for(String line: lines) {
			if(line.length() > longestLine) {
				longestLine = line.length();
			}
		}
		if(renderBox) {
			Vector3f posBox = new Vector3f(pos.x,pos.y,pos.z-2);
			Vector3f scaleBox = new Vector3f(scale.x*longestLine,-scale.y*lines.length,1);
			renderBox(posBox,rot,scaleBox,new Color4f(1-color.x,1-color.y,1-color.z,color.w));
		}
		
		GL20.glUniform1f(currentShader.getUniform("lineWidth"), 1);
		GL20.glUniform1i(currentShader.getUniform("deformColor"), 1);
				
		Matrix4f textPos = generateMatrix(pos,rot, new Vector3f(1,1,1));
		matrixStack.push(textPos);
		
		//Moving from line to line, up -> down
		Matrix4f yMat = new Matrix4f(new float[] {
				1,0,0,0,
				0,1,0,-scale.y,
				0,0,1,0,
				0,0,0,1});
		
		//Moving from character to character, left -> right
		Matrix4f xMat = new Matrix4f(new float[] {
				1,0,0,scale.x,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1
			});

		Matrix4f charMat = new Matrix4f(new float[] {
			scale.x,0,0,0,
			0,scale.y,0,0,
			0,0,scale.z,0,
			0,0,0,1
		});

		
		for(int line = 0; line < lines.length; line++) {

			char[] characters = lines[line].toCharArray();

			for(int character = 0; character < characters.length; character++) {
				if(characters[character] >= '!' && characters[character] <= '~') {
					pushMatrix(charMat);
					renderModel(font[characters[character] - 33],color);
					popMatrix();
				}
				pushMatrix(xMat);
			}
			
			int i = 0; while(i < characters.length) { popMatrix(); i++; }
			
			pushMatrix(yMat);
		}
		
		int i = 0; while(i < lines.length) { popMatrix(); i++; }
				
		matrixStack.pop();
		
		GL20.glUniform1i(currentShader.getUniform("deformColor"), 0);
		GL20.glUniform1f(currentShader.getUniform("lineWidth"), 0);
	}
	
	private static void setProjectionViewModelMatrix() {
		projectionViewModelMatrix.set(projectionMatrix);
		projectionViewModelMatrix.mul(viewMatrix);
		projectionViewModelMatrix.mul(modelMatrix);
		GL20.glUniformMatrix4(currentShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
	}
	
	private static void pushMatrix(Matrix4f matrix) {
		Matrix4f m = new Matrix4f(matrixStack.peek());
		m.mul(matrix);
		matrixStack.push(m);
	}

	private static void popMatrix() {
		matrixStack.pop();
	}

	@SuppressWarnings("unused")
	private static void renderBone(Entity entity, Bone bone) {
		pushMatrix(bone.model.matrix);
    	renderModel(entity, entity.model[0]);
    	if(bone.children != null) {
    		for(Bone b: bone.children) {
    			renderBone(entity,b);
    		}
    	}
    	popMatrix();
    }
    
    public static void takeScreenShot() {
		ByteBuffer data = BufferUtils.createByteBuffer((int)(WIDTH * HEIGHT * 3));
		glReadPixels(0, 0, (int) WIDTH, (int) HEIGHT, GL_RGB, GL_UNSIGNED_BYTE, data);
		Util.saveScreenshot(data);
	}
    
    public static void setIcon(String loc) {
    	if(Main.SYSTEM_OS == OS.WINDOWS) {
    		//Windows gets 16x16 and 32x32
    		byte[] powers = new byte[]{4,5};
        	ByteBuffer[] buffers = Util.getIcon(powers);
        	System.out.println(Display.setIcon(buffers));
    	} else if(Main.SYSTEM_OS == OS.MAC) {
    		//Mac is derpy when it comes to some java stuff, so here is a workaround. It's REALLY sloppy.
    		try {
    			BufferedImage icon = ImageIO.read(new File(Main.resourceLoc + loc));
    			MacUtil.setIconMac(icon);
    			MacUtil.requestForeground(true);
   		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
    	} else {
    		//Everything else should get 32x32
    		byte[] powers = new byte[]{5};
    		ByteBuffer[] buffers = Util.getIcon(powers);
        	System.out.println(Display.setIcon(buffers));
    	}
    }

	public static void setDisplayMode(DisplayMode dm, boolean fullscreen) {
		Graphics.fullscreen = fullscreen;
		System.out.println("Setting DisplayMode: " + dm.toString() + " " + fullscreen);
		try {
			if(fullscreen) {
				Display.setDisplayModeAndFullscreen(dm);
			} else {
				Display.setDisplayMode(dm);
			}
		} catch(LWJGLException exception) {
			exception.printStackTrace();
		}
		
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
		glViewport(0,0,(int)WIDTH,(int)HEIGHT);
	}
	
	public static DisplayMode getDefaultDisplayMode() {
		return Display.getDesktopDisplayMode();
	}

	public static DisplayMode getBestDisplayMode() {
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
	
	public static void updateProps() {
		try {
			Display.setDisplayConfiguration(gamma, brightness, contrast);
		} catch (LWJGLException e) {
			e.printStackTrace();
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
			System.out.println("OPENGL VENDOR: " + VENDOR + "\nOPENGL VERSION: 3.2 (" + VERSION + ")\nSHADER VERSION: " + glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
			Display.destroy();
			
			Class<?>[] classesWithTonsOfStuffInThem = {GL11.class, GL12.class, GL13.class, GL14.class,
					GL15.class, GL20.class, GL21.class, GL30.class, GL31.class, GL32.class,
					GL33.class, GL40.class, GL41.class, GL42.class, GL43.class,Graphics.class};
			
			ArrayList<ArrayList<String>> code = new ArrayList<ArrayList<String>>();
			
			for(Class<?> awesomeClass: classesWithTonsOfStuffInThem) {
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
			Display.setDisplayMode(getBestDisplayMode());
			Display.setFullscreen(fullscreen);
			Display.create(new PixelFormat().withSamples(maxSamples), new ContextAttribs(3,2).withForwardCompatible(true).withProfileCore(true));
		} catch(LWJGLException exception) {
			Sys.alert("CRITICAL ERROR", "Something bad happened.");
			exception.printStackTrace();
		}
		
		/*
		 * GL Initialization
		 */

		currentShader = ShaderParser.getShader("default");
		GL20.glUseProgram(currentShader.programID);

			//box init
			float[] verts = {0,0,0,0,1,0,1,0,0,1,1,0};
			short[] inds = {0,1,2,1,3,2};
			ModelData square = new ModelData("box",verts,inds);
			boxModel = ModelParser.buildModel("box", new ModelData[] {square})[0];
			
			//font init
			font = ModelParser.getModel("font.dae");
			
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
		
		WIDTH = Display.getWidth(); HEIGHT = Display.getHeight();
		glViewport(0,0,(int)WIDTH,(int)HEIGHT);
		
		LEFT = 0;
		RIGHT = 16 * WIDTH / HEIGHT;
		BOTTOM = 0;
		TOP = 16;
		NEAR = -1;
		FAR = 1;
		
		setIcon("/icns/16.png");
	}
	
	public static void destroy() {
		ModelParser.clearModelMap();
		ShaderParser.clearShaderMap();
		Display.destroy();
	}
}