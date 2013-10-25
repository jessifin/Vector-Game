package entity;

import model.ModelData;

public class EntityPlayer extends Entity {

	public EntityPlayer() {
		super("pizzard2.dae");
	}
	
	public EntityPlayer(ModelData[] modelData) {
		super(modelData);
	}

}
