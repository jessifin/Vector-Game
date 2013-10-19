package shader;

import java.util.HashMap;

public class Shader {
	
	public final String name;
	public final int vertexID, fragmentID, programID;
	private HashMap<String,Integer> uniforms = new HashMap<String,Integer>();

	public Shader(String name, int vertexID, int fragmentID, int programID) {
		this.name = name;
		this.vertexID = vertexID;
		this.fragmentID = fragmentID;
		this.programID = programID;
	}
	
	public int getUniform(String id) {
		if(uniforms.containsKey(id)) {
			return uniforms.get(id);
		} else {
			int uniform = ShaderParser.getUniform(programID,id);
			uniforms.put(id, uniform);
			return getUniform(id);
		}
	}
}
