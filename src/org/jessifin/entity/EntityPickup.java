package org.jessifin.entity;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.jessifin.audio.Audio;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityPickup extends Entity {
	
	public int healthBump = 1;

	public EntityPickup() {
		super("sphere", new Vector3f(1,1,1));
		this.scale = new Vector3f(10,10,10);
		this.colorFill = new Color4f(1,0,0,1);
		this.health = 5;
	}

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		Audio.playAtEntity("hit.wav", this, 1);
		if(e.health + healthBump < e.maxHealth && e.health > healthBump) {
			e.health += healthBump;
		}
	}

	public void update() {
		
	}

}
