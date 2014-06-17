package org.jessifin.model;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class Model {

	public final String name;
	public final int vaoID, vertexID, indexID, indexCount;
	public final Texture color, normal;
	
	public final ModelData data;
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Matrix4f matrix = new Matrix4f();
	
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(0,0,0,1);
	
	public Armature armature;
	
	public Model(String name, int vaoID, int vertexID, int indexID, int indexCount, ModelData data, Texture color, Texture normal) {
		this.name = name;
		this.vaoID = vaoID;
		this.vertexID = vertexID;
		this.indexID = indexID;
		this.indexCount = indexCount;
		this.data = data;
		this.matrix = new Matrix4f();
		this.color = color;
		this.normal = normal;
	}
	
	public Model(Model model) {
		this.name = new String(model.name);
		this.vaoID = new Integer(model.vaoID);
		this.vertexID = new Integer(model.vertexID);
		this.indexID = new Integer(model.indexID);
		this.indexCount = new Integer(model.indexCount);
		this.data = model.data;
		this.matrix = new Matrix4f(model.matrix);
		this.color = model.color;
		this.normal = model.normal;
	}
}