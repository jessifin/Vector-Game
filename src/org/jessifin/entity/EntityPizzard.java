package org.jessifin.entity;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityPizzard extends Entity {

	public EntityPizzard() {
		super("pizzard.mesh");
	}

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		//Audio.playAtEntity("hit.wav", this, 1);
	}

	public void update() {
		
	}

}
