package org.jessifin.graphics;

//All unspecified OpenGL calls are for Opengl Version 1.1, thus the static import
import static org.lwjgl.opengl.GL11.*;

import org.jessifin.entity.Entity;
import org.jessifin.game.Game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import org.jessifin.model.TextureParser;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
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
	private static Matrix4f viewProjectionMatrix = new Matrix4f();
	private static Matrix4f modelViewProjectionMatrix = new Matrix4f();
	private static Matrix4f genMatrix = new Matrix4f();
	private static Stack<Matrix4f> matrixStack = new Stack<Matrix4f>();
	
	public static float gamma = 1, brightness = 0.5f, contrast = 1;
		
	public static void update() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(CLEAR_MASK);
		
		GL20.glUniform3f(currentShader.getUniform("camPos"), Game.camPos.x, Game.camPos.y, Game.camPos.z);
		GL20.glUniform1f(currentShader.getUniform("time"), Main.numLoops/100f);
		GL20.glUniform2f(currentShader.getUniform("resolution"), WIDTH, HEIGHT);
		GL20.glUniform1f(currentShader.getUniform("freq"), 5);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		setup3D();
		renderBatch(Game.entities);
		renderText(Main.getTime() + 
			"\nCamera Position: (" + (int)Game.camPos.x + "," + (int)Game.camPos.y + "," + (int)Game.camPos.z + 
			")\nCamera Orientation: (" + Game.camUp.x + "," + Game.camUp.y + "," + Game.camUp.z + 
			")\nPlayer Position: (" + (int)Game.player.pos.x + "," + (int)Game.player.pos.y + "," + (int)Game.player.pos.z + 
			")\nFoV: " + Game.FoV + "  Player speed: " + Game.speed + "\nGame ticks: " + Main.numTicks + 
			"\nGame loops: " + Main.numLoops + "\nNumber of Entities: "  + Game.entities.size() + 
			"\nRandom number: " + Main.rng.nextInt() + 
			"\nFPS: " + 1000f/Main.loopTime,
			new Vector3f(200,0,0),
			new Vector3f(0,0,0),
			new Vector3f(30,30,30),
			new Color4f((Main.numTicks % 20) / 19f,1,0,1),
			true);
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
				2f / (RIGHT - LEFT), 0, 0, -(RIGHT + LEFT) / (RIGHT - LEFT),
				0, 2f / (TOP - BOTTOM), 0, -(TOP + BOTTOM) / (TOP - BOTTOM),
				0, 0, -2f / (FAR - NEAR), -(FAR + NEAR) / (FAR - NEAR),
				0, 0, 0, 1
		});
				
		viewProjectionMatrix.set(projectionMatrix);
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
		
		viewProjectionMatrix.set(projectionMatrix);
		viewProjectionMatrix.mul(viewMatrix);
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

		genMatrix.set(new float[] {
			cosY * cosZ * scale.x, -cosY * sinZ * scale.y, sinY * scale.z, pos.x,
			(sinXsinY * cosZ + cosX * sinZ) * scale.x, (-sinXsinY * sinZ + cosX * cosZ) * scale.y, -sinX * cosY * scale.z, pos.y,
			(-cosXsinY * cosZ + sinX * sinZ) * scale.x, (cosXsinY * sinZ + sinX * cosZ) * scale.y, cosX * cosY * scale.z, pos.z,
			0, 0, 0, 1
		});

		return genMatrix;
	}

	private static void renderBatch(ArrayList<Entity> entities) {
    	for(int e = 0; e < entities.size(); e++) {
    		if(entities.get(e).squaredDistanceFromCam <= Game.Z_FAR * Game.Z_FAR) {
    			if(entities.get(e).model.color != null) {
    				glBindTexture(GL_TEXTURE_2D, entities.get(e).model.color.texID);
    				GL20.glUniform1i(currentShader.getUniform("useTex"), 1);
    				GL20.glUniform1f(currentShader.getUniform("width"), entities.get(e).model.color.WIDTH);
    				GL20.glUniform1f(currentShader.getUniform("height"), entities.get(e).model.color.HEIGHT);
    			} else {
    				GL20.glUniform1i(currentShader.getUniform("useTex"), 0);
    			}
    			
	    		modelMatrix = generateMatrix(entities.get(e));
				matrixStack.push(modelMatrix);
					/*
	    			if(entities.get(e).model.armature != null) {
	    				for(Bone rootBone: entities.get(e).model.armature.rootBones) {
	    					rootBone.tempMatrix = new Matrix4f(rootBone.matrix);
	    					updateBones(rootBone);
	    				}
	    				
	    				GL20.glUniform1i(currentShader.getUniform("num_bones"), entities.get(e).model.armature.bones.length);
	    				for(int b = 0; b < entities.get(e).model.armature.bones.length; b++) {
	    					GL20.glUniformMatrix4(currentShader.getUniform("bones["+b+"]"), false, Util.toBuffer(entities.get(e).model.armature.bones[b].matrix));
	    				}
	    			}
	    			*/
					pushMatrix(entities.get(e).model.matrix);
					renderModel(entities.get(e), entities.get(e).model);
					popMatrix();
					//GL20.glUniform1i(currentShader.getUniform("num_bones"), 0);
			
				popMatrix();
	    	}
    	}
    }
	
	private static void updateBones(Bone parent) {
		for(Bone child: parent.children) {
			if(child != null) {
				System.out.println(parent.name + " " + child.name);
				child.tempMatrix.mul(parent.tempMatrix, child.matrix);
				updateBones(child);
			}
		}
	}
    
    private static void renderModel(Entity entity, Model model) {
		GL20.glUniform1i(currentShader.getUniform("offset"), (int) (Main.numTicks * entity.flashSpeed));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		modelViewProjectionMatrix.set(viewProjectionMatrix);
		modelViewProjectionMatrix.mul(matrixStack.peek());
		GL20.glUniformMatrix4(currentShader.getUniform("modelViewProjectionMatrix"), false, Util.toBuffer(modelViewProjectionMatrix));
		GL20.glUniform4f(currentShader.getUniform("color[0]"), model.colorFill.x * entity.colorFill.x,
				model.colorFill.y * entity.colorFill.y,
				model.colorFill.z * entity.colorFill.z,
				model.colorFill.w * entity.colorFill.w);
		GL20.glUniform4f(currentShader.getUniform("color[1]"), 1,1,1,1);
		
		int indicesToDraw = (int)(model.indexCount * entity.health / entity.maxHealth);
		
		glDrawElements(GL_TRIANGLES, indicesToDraw, GL_UNSIGNED_SHORT, 0);

		/*
		GL20.glUniform4f(currentShader.getUniform("color"), model.colorLine.x * entity.colorLine.x,
				model.colorLine.y * entity.colorLine.y,
				model.colorLine.z * entity.colorLine.z,
				model.colorLine.w * entity.colorLine.w);
		*/

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
    
    private static void renderModel(Model model, Color4f color) {
		GL20.glUniform1i(currentShader.getUniform("offset"), (int)(Main.numTicks * 2));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		modelViewProjectionMatrix.set(viewProjectionMatrix);
		modelViewProjectionMatrix.mul(matrixStack.peek());
		GL20.glUniformMatrix4(currentShader.getUniform("modelViewProjectionMatrix"), false, Util.toBuffer(modelViewProjectionMatrix));
		GL20.glUniform4f(currentShader.getUniform("color[0]"), color.x, color.y, color.z, color.w);

		glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
    
    public static void renderBox(Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
		GL20.glUniform1i(currentShader.getUniform("useTex"), 0);

    	modelMatrix = generateMatrix(pos,rot,scale);
    	
		GL20.glUniform1i(currentShader.getUniform("offset"), 0);
		GL30.glBindVertexArray(boxModel.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, boxModel.indexID);
		modelViewProjectionMatrix.set(viewProjectionMatrix);
		modelViewProjectionMatrix.mul(modelMatrix);
		GL20.glUniformMatrix4(currentShader.getUniform("modelViewProjectionMatrix"), false, Util.toBuffer(modelViewProjectionMatrix));
		GL20.glUniform4f(currentShader.getUniform("color[0]"),color.x,color.y,color.z,color.w);
		
		glDrawElements(GL_TRIANGLES, boxModel.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUniform1i(currentShader.getUniform("useTex"), 1);
    }

	public static void renderBox(float x1, float y1, float x2, float y2, float rotation, Color4f color) {
		Vector3f pos = new Vector3f(x1,y1,0);
		Vector3f rot = new Vector3f(0,0,rotation);
		Vector3f scale = new Vector3f(x2-x1,y2-y1,0);
		
		renderBox(pos,rot,scale,color);
	}
	
	public static void renderText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color, boolean renderBox) {
		GL20.glUniform1i(currentShader.getUniform("useTex"), 0);

		String[] lines = text.split("\n");
		
		if(renderBox) {
			int longestLine = 0;
			for(String line: lines) {
				if(line.length() > longestLine) {
					longestLine = line.length();
				}
			}
			
			Vector3f posBox = new Vector3f(pos.x,pos.y,pos.z-2);
			Vector3f scaleBox = new Vector3f(scale.x*longestLine,-scale.y*lines.length,1);
			renderBox(posBox,rot,scaleBox,new Color4f(1,1,1,1));
		}
		
		GL20.glUniform1f(currentShader.getUniform("lineWidth"), 1);
				
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
		
		GL20.glUniform1f(currentShader.getUniform("lineWidth"), 0);
		GL20.glUniform1i(currentShader.getUniform("useTex"), 1);
	}
	
	private static void pushMatrix(Matrix4f matrix) {
		Matrix4f m = new Matrix4f(matrixStack.peek());
		m.mul(matrix);
		matrixStack.push(m);
	}

	private static void popMatrix() {
		matrixStack.pop();
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
		boxModel = ModelParser.buildModel("box", new ModelData("box", new float[] {0,0,0,0,1,0,1,0,0,1,1,0}, new short[] {0,1,2,1,3,2}))[0];
		
		//font init
		font = ModelParser.getModel("font");
			
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
		
		setIcon("/icns/32.png");
	}
	
	public static void destroy() {
		ModelParser.clearModelMap();
		TextureParser.clearTextureMap();
		ShaderParser.clearShaderMap();
		Display.destroy();
	}
}