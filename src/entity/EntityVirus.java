package entity;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import model.Model;

public class EntityVirus extends Entity {

	public EntityVirus(Vector3f pos) {
		super("virus.dae");
		this.pos = pos;
		for(Model m: model) {
			m.colorFill = new Color4f(0.1f,0.9f,0.05f,1);
		}
	}

}
