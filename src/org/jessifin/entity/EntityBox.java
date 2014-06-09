package org.jessifin.entity;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityBox extends Entity {

	public EntityBox(Vector3f scaling) {
		super("bawks", scaling);
	}

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		//Audio.playAtEntity("hit.wav", this, 1);
		if(health > 0) {
			health--;
		} else {
			this.isAlive = false;
		}
	}
	
	public void update() {
		
	}

}
