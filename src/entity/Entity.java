package entity;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

import main.Physics;
import model.Bone;
import model.Model;
import model.ModelData;
import model.ModelParser;

public abstract class Entity {
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Vector3f lastPos = new Vector3f(0,0,0), vel = new Vector3f(0,0,0);
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(1,1,1,1);
	public float distanceFromCam;

	public RigidBody body;

	public Model[] model;
	public Bone rootBone;
	
	public int maxHealth = 100;
	public int health = maxHealth;
	
	
	public Entity(String model) {
		if(!model.equals("null") && model.endsWith(".dae")) {
			this.model = ModelParser.getModel(model);
		}
	}
	
	public Entity(ModelData[] modelData) {
		this.model = ModelParser.buildModel("MODEL DATA", modelData);
	}
	
	protected void loopThroughBones(Bone bone) {			
		//fixMatrix(bone.model.matrix);
		
		System.out.println(bone.model.name);
		System.out.println(bone.model.pos.x + " " + bone.model.pos.y + " " + bone.model.pos.z);
		//decomposeMatrix(bone.model.matrix);
		
		if(bone.children != null) {
			for(int i = 0; i < bone.children.length; i++) {
				loopThroughBones(bone.children[i]);
			}
		}
	}
	
	private void decomposeMatrix(Matrix4f matrix) {
		Vector3f translation = new Vector3f(matrix.m30, matrix.m31, matrix.m32);
		
		Matrix3f rotation = new Matrix3f();
		matrix.get(rotation);
				
		System.out.println(translation.x + " " + translation.y + " " + translation.z);
		System.out.println(rotation.m00 + " " + rotation.m01 + " " + rotation.m02);
		System.out.println(rotation.m10 + " " + rotation.m11 + " " + rotation.m12);
		System.out.println(rotation.m20 + " " + rotation.m21 + " " + rotation.m22);

	}
	
	private void fixMatrix(Matrix4f matrix) {
		Matrix4f out = new Matrix4f();
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 4; y++) {
				out.setElement(x,y,out.getElement(y,x));
			}
		}
		matrix = out;
	}
	
	public abstract void update();
	
}