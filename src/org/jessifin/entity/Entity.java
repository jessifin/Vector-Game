package org.jessifin.entity;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;

import org.jessifin.model.Model;
import org.jessifin.model.ModelData;
import org.jessifin.model.ModelParser;
import org.jessifin.physics.Physics;

public abstract class Entity implements Comparable<Entity> {
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Vector3f vel = new Vector3f(0,0,0);
	public Color4f colorFill = new Color4f(1,1,1,1), colorLine = new Color4f(1,1,1,1);
	public float squaredDistanceFromCam;

	public RigidBody body;
	public ArrayList<Entity> collisions = new ArrayList<Entity>();

	public Model model;
	
	public int maxHealth = 100;
	public int health = maxHealth;
	public float flashSpeed = 0;
	public boolean isAlive;
	
	public Entity(String modelName, Vector3f scale) {
		if(!modelName.equals("null")) {
			model = ModelParser.getModel(modelName)[0];
		}
		this.scale = scale;
		body = Physics.createRigidBody(this);
	}

	public Entity(ModelData[] modelData) {
		model = ModelParser.buildModel("MODEL DATA", modelData)[0];
	}
	
	public void updatePos() {
		//body.getWorldTransform(new Transform()).origin.set(pos);
		body.translate(pos);
	}

	public abstract void onCollide(Entity e, ManifoldPoint[] contactPoints);
	
	public abstract void update();
	
	public int compareTo(Entity entity) {
		return this.squaredDistanceFromCam > entity.squaredDistanceFromCam ? -1: 1;
	}
}