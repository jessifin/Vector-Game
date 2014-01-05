package org.jessifin.shader;

import static org.lwjgl.opengl.GL20.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.lwjgl.opengl.GL20;

import org.jessifin.main.Main;

public class ShaderParser {
	
	private static HashMap<String,Shader> shaders = new HashMap<String,Shader>();
	
	public static Shader getShader(String loc) {
		return (shaders.containsKey(loc)) ? shaders.get(loc) : loadShader(loc);
	}
	
	private static Shader loadShader(String loc) {
		System.out.println("Loading shader: " + loc);
		
		//Vertex Shader
		String vertexText = readRawText(loc + ".vs");
		int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderID, vertexText);
		glCompileShader(vertexShaderID);
		
		if(glGetShaderi(vertexShaderID, GL_COMPILE_STATUS) != 1) {
			System.err.println("ERROR OCCURED IN COMPILING VERTEX SHADER.");
			System.err.println(glGetShaderInfoLog(vertexShaderID, 1000));
			Main.RUNNING = false;
		}
		
		//Fragment Shader
		String fragmentText = readRawText(loc + ".fs");
		int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderID, fragmentText);
		glCompileShader(fragmentShaderID);
		
		if(glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS) != 1) {
			System.err.println("ERROR OCCURED IN COMPILING FRAGMENT SHADER.");
			System.err.println(glGetShaderInfoLog(fragmentShaderID, 1000));
			Main.RUNNING = false;
		}
						
		int shaderProgramID = glCreateProgram();
		glAttachShader(shaderProgramID, vertexShaderID);
		glAttachShader(shaderProgramID, fragmentShaderID);
		
		glLinkProgram(shaderProgramID);
		glValidateProgram(shaderProgramID);
		
		if(glGetProgrami(shaderProgramID, GL_COMPILE_STATUS) != 1) {
			System.err.println("ERROR OCCURED IN COMPILING SHADER PROGRAM.");
			Main.RUNNING = false;
		}
		
		Shader shader = new Shader(loc, vertexShaderID, fragmentShaderID, shaderProgramID);
		shaders.put(loc, shader);
		return shader;
	}
	
	private static String readRawText(String loc) {
		Scanner scan = null;
		try {
			scan = new Scanner(new File(Main.resourceLoc,"shader/"+loc));
		} catch (FileNotFoundException exception) {
			System.err.println("UNABLE TO LOCATE TARGET FILE");
			exception.printStackTrace();
			Main.RUNNING = false;
		}
		String rawText = "";
		while(scan.hasNext()) { rawText += (scan.nextLine() + '\n'); }
		return rawText;
	}

	static int getUniform(int programID, String uniformID) {
		return glGetUniformLocation(programID, uniformID);
	}
	
	public static void clearShaderMap() {
		Iterator<Shader> values = shaders.values().iterator();
		while(values.hasNext()) {
			Shader s = values.next();
			System.out.println("Deleting shader " + s.name);
			glDeleteShader(s.vertexID);
			glDeleteShader(s.fragmentID);
			glDeleteProgram(s.programID);
		}
	}
}
