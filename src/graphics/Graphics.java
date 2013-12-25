package graphics;

import static org.lwjgl.opengl.GL11.*;
import entity.Entity;
import game.Game;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import main.Main;
import main.Util;
import model.Bone;
import model.Model;
import model.ModelData;
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
	public static float LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR;
	public static Color4f clearColor = new Color4f(0,0,0,1);
	
	private static Shader defaultShader;
	
	private static Model boxModel;
	private static Model[] font;
	public static final float charWidth = 0.49341f, charHeight = 0.69407f;
		
	private static Matrix4f projectionMatrix = new Matrix4f(), viewMatrix = new Matrix4f();
	private static Matrix4f modelMatrix = new Matrix4f();
	private static Stack<Matrix4f> matrixStack = new Stack<Matrix4f>();
		
	public static void update() {
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
		setup3D();
		renderBatch(Game.entities);
		Vector3f scale = new Vector3f(1,1,1);
		renderText(Main.getTime() + 
				"\nCamera Position: (" + (int)Game.camPos.x + "," + (int)Game.camPos.y + "," + (int)Game.camPos.z + 
				")\nCamera Orientation: (" + Game.camUp.x + "," + Game.camUp.y + "," + Game.camUp.z + 
				")\nPlayer Position: (" + (int)Game.player.pos.x + "," + (int)Game.player.pos.y + "," + (int)Game.player.pos.z + 
				")\nFoV: " + Game.FoV + "  Player speed: " + Game.speed + "\nGame ticks: " + Main.numTicks + 
				"\nGame loops: " + Main.numLoops + "\nNumber of Entities: "  + Game.entities.size() + 
				"\nRandom number: "+Main.rng.nextInt(),Game.player.pos,new Vector3f(0,0,0),scale,new Color4f(1,1,0,1));
		//renderBox(Game.player.pos.x,Game.player.pos.y,Game.player.pos.x+10,Game.player.pos.y-9*charHeight,0,new Color4f(1,1,1,1));
				
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
		
		Matrix4f shift = new Matrix4f(new float[] {
				1, 0, 0, -Game.camPos.x,
				0, 1, 0, -Game.camPos.y,
				0, 0, 1, -Game.camPos.z,
				0, 0, 0, 1
		});
		
		viewMatrix.mul(shift);
		
		GL20.glUniformMatrix4(defaultShader.getUniform("projectMat"), false, Util.toBuffer(projectionMatrix));
		GL20.glUniformMatrix4(defaultShader.getUniform("viewMat"), false, Util.toBuffer(viewMatrix));
	}

    private static Matrix4f generateMatrix(Entity e) {
		return generateMatrix(e.pos, e.rot, e.scale);
	}

	private static Matrix4f generateMatrix(Vector3f pos, Vector3f rot, Vector3f scale) { 		
		Matrix4f transMat = new Matrix4f(new float[] {
				1, 0, 0, pos.x,
				0, 1, 0, pos.y,
				0, 0, 1, pos.z,
				0, 0, 0, 1
		});
		
		Matrix4f xRotMat = new Matrix4f(new float[] {
				1, 0, 0, 0,
				0, (float) Math.cos(rot.x), - (float) Math.sin(rot.x), 0,
				0, (float) Math.sin(rot.x), (float) Math.cos(rot.x), 0,
				0, 0, 0, 1
		});
		
		Matrix4f yRotMat = new Matrix4f(new float[] {
				(float) Math.cos(rot.y), 0, (float) Math.sin(rot.y), 0,
				0, 1, 0, 0,
				- (float) Math.sin(rot.y), 0, (float) Math.cos(rot.y), 0,
				0, 0, 0, 1
		});
		
		Matrix4f zRotMat = new Matrix4f(new float[] {
				(float) Math.cos(rot.z), - (float) Math.sin(rot.z), 0, 0,
				(float) Math.sin(rot.z), (float) Math.cos(rot.z), 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
		});
	
		Matrix4f scaleMat = new Matrix4f(new float[] {
			scale.x, 0, 0, 0,
			0, scale.y, 0, 0,
			0, 0, scale.z, 0,
			0, 0, 0, 1
			
		});
		
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		mat.mul(transMat);
		mat.mul(xRotMat); mat.mul(yRotMat); mat.mul(zRotMat);
		mat.mul(scaleMat);
		
		return mat;
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
		GL20.glUniform1i(defaultShader.getUniform("offset"), (int) (Main.numTicks * entity.flashSpeed));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		GL20.glUniformMatrix4(defaultShader.getUniform("modelMat"), false, Util.toBuffer(matrixStack.peek()));
		GL20.glUniform4f(defaultShader.getUniform("color"), model.colorFill.x * entity.colorFill.x,
				model.colorFill.y * entity.colorFill.y,
				model.colorFill.z * entity.colorFill.z,
				model.colorFill.w * entity.colorFill.w);
		
		int indicesToDraw = (int)(model.indexCount * entity.maxHealth/(float)(entity.maxHealth));
		glDrawElements(GL_TRIANGLES, indicesToDraw, GL_UNSIGNED_SHORT, 0);
		if(model.indexCount != indicesToDraw) {
			GL20.glUniform4f(defaultShader.getUniform("color"), model.colorFill.x * entity.colorFill.x,
					model.colorFill.y * entity.colorFill.y,
					model.colorFill.z * entity.colorFill.z,
					0.4f);
		//	glDrawElements(GL_TRIANGLES, model.indexCount - indicesToDraw, GL_UNSIGNED_SHORT, indicesToDraw);
		}
	
		GL20.glUniform4f(defaultShader.getUniform("color"), model.colorLine.x * entity.colorLine.x,
				model.colorLine.y * entity.colorLine.y,
				model.colorLine.z * entity.colorLine.z,
				model.colorLine.w * entity.colorLine.w);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
    
    private static void renderModel(Model model, Color4f color) {
		GL20.glUniform1i(defaultShader.getUniform("offset"), (int)(Main.numTicks * 2));
		GL30.glBindVertexArray(model.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.indexID);
		GL20.glUniformMatrix4(defaultShader.getUniform("modelMat"), false, Util.toBuffer(matrixStack.peek()));
		GL20.glUniform4f(defaultShader.getUniform("color"), color.x,
				color.y,
				color.z,
				color.w);
		
		glDrawElements(GL_TRIANGLES, model.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	public static void renderBox(float x1, float y1, float x2, float y2, float rotation, Color4f color) {
		Vector3f pos = new Vector3f(x1,y1,0);
		Vector3f rot = new Vector3f(0,0,rotation);
		Vector3f scale = new Vector3f(x2-x1,y2-y1,0);
		
		modelMatrix = generateMatrix(pos,rot,scale);
		    	
		GL20.glUniform1i(defaultShader.getUniform("offset"), 0);
		GL30.glBindVertexArray(boxModel.vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, boxModel.indexID);
		GL20.glUniformMatrix4(defaultShader.getUniform("modelMat"), false, Util.toBuffer(modelMatrix));
		GL20.glUniform4f(defaultShader.getUniform("color"),color.x,color.y,color.z,color.w);
		
		glDrawElements(GL_TRIANGLES, boxModel.indexCount, GL_UNSIGNED_SHORT, 0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	public static void renderText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
		//glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
		GL20.glUniform1i(defaultShader.getUniform("flash"), 1);

		modelMatrix.setIdentity();
		matrixStack.push(modelMatrix);
				
		String[] lines = text.split("\n");

		Matrix4f textPos = generateMatrix(pos,rot,scale);
		pushMatrix(textPos);
		
		for(int line = 0; line < lines.length; line++) {
	
			//Moving from line to line, up -> down
			Vector3f linePos = new Vector3f(0,-scale.y*charHeight,0);
			Vector3f lineRot = new Vector3f(0,0,0);
			Vector3f lineScale = new Vector3f(1,1,1);
						
			Matrix4f lineMat = generateMatrix(linePos,lineRot,lineScale);
			
			char[] characters = lines[line].toCharArray();

			//Moving from character to character, left -> right
			Vector3f charPos = new Vector3f(scale.x*charWidth,0,0);
			Vector3f charRot = new Vector3f(0,0,0);
			Vector3f charScale = new Vector3f(1,1,1);
			Matrix4f charMat = generateMatrix(charPos,charRot,charScale);
			
			Matrix4f mat = generateMatrix(new Vector3f(0,0,0),new Vector3f(0,0,0),scale);
			
			for(int character = 0; character < characters.length; character++) {
				if(characters[character] >= '!' && characters[character] <= '~') {
					pushMatrix(mat);
					renderModel(font[characters[character] - 33],color);
					popMatrix();
				}
				pushMatrix(charMat);
			}
			
			int i = 0;
			while(i < characters.length) {
				popMatrix();
				i++;
			}
			
			pushMatrix(lineMat);

		}
		
		int j = 0;
		while(j < lines.length) {
			popMatrix();
			j++;
		}
		
		popMatrix();
		
		matrixStack.pop();
		
		GL20.glUniform1i(defaultShader.getUniform("flash"), 0);
		glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);

		//glEnable(GL_CULL_FACE);
	}

	private static void pushMatrix(Matrix4f matrix) {
		Matrix4f m = new Matrix4f(matrixStack.peek());
		m.mul(matrix);
		matrixStack.push(m);
	}

	private static void popMatrix() {
		matrixStack.pop();
	}

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
	//		Display.setDisplayModeAndFullscreen(getBestDisplayMode());
			Display.create(new PixelFormat().withSamples(maxSamples), new ContextAttribs(3,2).withForwardCompatible(true).withProfileCore(true));
		} catch(LWJGLException exception) {
			Sys.alert("CRITICAL ERROR", "Something bad happened.");
			exception.printStackTrace();
		}
		
		/*
		 * GL Initialization
		 */
		
		defaultShader = ShaderParser.getShader("default");
		GL20.glUseProgram(defaultShader.programID);

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
		
		//glEnable(GL_CULL_FACE);
		//glCullFace(GL_FRONT);
		
		LEFT = 0;
		RIGHT = 16 * WIDTH / HEIGHT;
		BOTTOM = 0;
		TOP = 16;
		NEAR = -1;
		FAR = 1;
		
	}

	public static void destroy() {
		ModelParser.clearModelMap();
		ShaderParser.clearShaderMap();
		Display.destroy();
	}
}