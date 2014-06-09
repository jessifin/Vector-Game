package org.jessifin.entity;

import javax.vecmath.Vector3f;

import org.jessifin.audio.Audio;
import org.jessifin.game.Game;
import org.jessifin.model.ModelData;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityPlayer extends Entity {
	
	public EntityPlayer() {
		super("arepo", new Vector3f(1,1,1));
	}

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		boolean hitsound = false;
		for(ManifoldPoint contactPoint: contactPoints) {
			if(contactPoint.appliedImpulse > 500) {
				hitsound = true;
				break;
			}
		}
		if(hitsound) {
			Audio.playAtEntity("hit.wav", this, 1);
		}
	}
	
	public void update() {
		if(health == 0) {
			Game.reboot();
		}
	}

}