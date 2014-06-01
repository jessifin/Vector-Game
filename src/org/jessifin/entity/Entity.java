package org.jessifin.entity;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;

import org.jessifin.model.Bone;
import org.jessifin.model.Mesh;
import org.jessifin.model.Model;
import org.jessifin.model.ModelData;
import org.jessifin.model.MeshParser;

public abstract class Entity implements Comparable<Entity> {
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Vector3f lastPos = new Vector3f(0,0,0), vel = new Vector3f(0,0,0), lastVel = new Vector3f(0,0,0), accel = new Vector3f(0,0,0), lastAccel = new Vector3f(0,0,0);
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(1,1,1,1);
	public float squaredDistanceFromCam;

	public RigidBody body;
	public ArrayList<Entity> collisions = new ArrayList<Entity>();

	public Mesh mesh;
	
	public int maxHealth = 100;
	public int health = maxHealth;
	public float flashSpeed = 0;
	public boolean isAlive;
	
	public Entity(String model) {
		if(!model.equals("null") && model.endsWith(".mesh")) {
			this.mesh = MeshParser.getModel(model);
		}
		flashSpeed = 10;
	}

	public Entity(ModelData[] modelData) {
		this.mesh = MeshParser.buildModel("MODEL DATA", modelData);
		flashSpeed = 10;
	}
	
	public Entity copy() {
		try {
			return (Entity) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public abstract void onCollide(Entity e, ManifoldPoint[] contactPoints);
	
	public abstract void update();
	
	public int compareTo(Entity entity) {
		return (this.squaredDistanceFromCam > entity.squaredDistanceFromCam) ? -1: 1;
	}
}