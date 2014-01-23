package org.jessifin.shader;

import java.util.HashMap;

public class Shader {
	
	public final String name;
	public final int vertexID, fragmentID, geometryShaderID, programID;
	private HashMap<String,Integer> uniforms = new HashMap<String,Integer>();

	Shader(String name, int vertexID, int fragmentID, int geometryShaderID, int programID) {
		this.name = name;
		this.vertexID = vertexID;
		this.fragmentID = fragmentID;
		this.geometryShaderID = geometryShaderID;
		this.programID = programID;
	}
	
	public int getUniform(String id) {
		if(uniforms.containsKey(id)) {
			return uniforms.get(id);
		} else {
			int uniform = ShaderParser.getUniform(programID,id);
			if(uniform == 0) {
				System.err.println("MISSING SHADER UNIFORM " + id);
				return 0;
			} else {
				uniforms.put(id, uniform);
				return getUniform(id);
			}
		}
	}
}
