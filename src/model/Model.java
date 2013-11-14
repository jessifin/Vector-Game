package model;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class Model {

	public final String name;
	public final int vaoID, vertexID, indexID, indexCount;
	public int indicesToRender;
	
	public final ModelData data;
	public /*final*/ Model parentNode;
	
	public Matrix4f modelMatrix;
	
	//This will be replaced by the above.
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(0,0,0,1);
	
	public Model(String name, int vaoID, int vertexID, int indexID, int indexCount, ModelData data) {
		this.name = name;
		this.vaoID = vaoID;
		this.vertexID = vertexID;
		this.indexID = indexID;
		this.indexCount = indexCount;
		this.indicesToRender = indexCount;
		this.data = data;
	}
}
