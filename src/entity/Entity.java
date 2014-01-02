package entity;

import java.util.ArrayList;

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

public abstract class Entity implements Cloneable {
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Vector3f lastPos = new Vector3f(0,0,0), vel = new Vector3f(0,0,0), lastVel = new Vector3f(0,0,0), accel = new Vector3f(0,0,0), lastAccel = new Vector3f(0,0,0);
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(1,1,1,1);
	public float distanceFromCam;

	public RigidBody body;
	public int physID;
	public ArrayList<Entity> collisions = new ArrayList<Entity>();

	public Model[] model;
	public Bone rootBone;
	
	public int maxHealth = 100;
	public int health = maxHealth;
	public int flashSpeed = 0;
	
	public Entity(String model) {
		if(!model.equals("null") && model.endsWith(".dae")) {
			this.model = ModelParser.getModel(model);
		}
	}

	public Entity(ModelData[] modelData) {
		this.model = ModelParser.buildModel("MODEL DATA", modelData);
	}
	
	public Entity copy() {
		try {
			return (Entity) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected void loopThroughBones(Bone bone) {			
		System.out.println(bone.model.name);
		System.out.println(bone.model.pos.x + " " + bone.model.pos.y + " " + bone.model.pos.z);
		
		if(bone.children != null) {
			for(int i = 0; i < bone.children.length; i++) {
				loopThroughBones(bone.children[i]);
			}
		}
	}
	
	public abstract void update();	
}