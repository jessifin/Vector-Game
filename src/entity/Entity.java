package entity;

import javax.vecmath.Vector3f;

import model.Model;
import model.ModelData;
import model.ModelParser;

public abstract class Entity {
	
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);

	public Model[] model;
	
	public Entity(String model) {
		this.model = ModelParser.getModel(model);
	}
	
	public Entity(ModelData[] modelData) {
		this.model = ModelParser.buildModel("player", modelData);
	}
}