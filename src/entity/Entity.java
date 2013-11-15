package entity;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

import main.Physics;
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
	
	public Entity(String model) {
		this.model = ModelParser.getModel(model);
	}
	
	public Entity(ModelData[] modelData) {
		this.model = ModelParser.buildModel("player", modelData);
	}
	
	public abstract void update();
	
}