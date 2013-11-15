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
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Matrix4f matrix;

	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(0,0,0,1);
	
	public Model(String name, int vaoID, int vertexID, int indexID, int indexCount, ModelData data) {
		this.name = name;
		this.vaoID = vaoID;
		this.vertexID = vertexID;
		this.indexID = indexID;
		this.indexCount = indexCount;
		this.indicesToRender = indexCount;
		this.data = data;
		this.matrix = new Matrix4f();
	}
	
	public void calculateMatrix() {
		Matrix4f posMat = new Matrix4f(new float[] {
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
				0, 0, 0, 1});
		
		matrix.setIdentity();
		matrix.mul(posMat);
		matrix.mul(xRotMat); matrix.mul(yRotMat); matrix.mul(zRotMat);
		matrix.mul(scaleMat);
	}
}
