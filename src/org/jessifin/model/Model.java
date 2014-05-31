package org.jessifin.model;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class Model {

	public final String name;
	public final int vaoID, vertexID, indexID, indexCount;
	
	public final ModelData data;
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Matrix4f matrix = new Matrix4f();
	
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(0,0,0,1);
	
	public Armature armature;
	
	public Model(String name, int vaoID, int vertexID, int indexID, int indexCount, ModelData data) {
		this.name = name;
		this.vaoID = vaoID;
		this.vertexID = vertexID;
		this.indexID = indexID;
		this.indexCount = indexCount;
		this.data = data;
		this.matrix = new Matrix4f();
	}
	
	public Model(Model model) {
		this.name = new String(model.name);
		this.vaoID = new Integer(model.vaoID);
		this.vertexID = new Integer(model.vertexID);
		this.indexID = new Integer(model.indexID);
		this.indexCount = new Integer(model.indexCount);
		this.data = model.data;
		this.matrix = new Matrix4f(model.matrix);
	}
	
	public void calculateMatrix() {
		float cosX = (float)Math.cos(rot.x);
		float sinX = (float)Math.sin(rot.x);
		float cosY = (float)Math.cos(rot.y);
		float sinY = (float)Math.sin(rot.y);
		float cosZ = (float)Math.cos(rot.z);
		float sinZ = (float)Math.sin(rot.z);
		
		float cosXsinY = cosX * sinY;
		float sinXsinY = sinX * sinY;
		
		matrix.set(new float[] {
				cosY * cosZ * scale.x, -cosY * sinZ * scale.y, sinY * scale.z, pos.x,
				(sinXsinY * cosZ + cosX * sinZ) * scale.x, (-sinXsinY * sinZ + cosX * cosZ) * scale.y, -sinX * cosY * scale.z, pos.y,
				(-cosXsinY * cosZ + sinX * sinZ) * scale.x, (cosXsinY * sinZ + sinX * cosZ) * scale.y, cosX * cosY * scale.z, pos.z,
				0, 0, 0, 1
			});
	}
}