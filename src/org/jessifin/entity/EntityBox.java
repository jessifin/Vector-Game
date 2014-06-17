package org.jessifin.entity;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.jessifin.game.Game;
import org.jessifin.main.Timer;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityBox extends Entity {
	
//	private Timer timer = new Timer(3000);

	public EntityBox(Vector3f scaling) {
		super("bawks", scaling);
	}

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		//Audio.playAtEntity("hit.wav", this, 1);
	}
	
	public void update() {
		/*
		while(timer.poll()) {
			EntityBox childBox = new EntityBox(scale);
			childBox.pos = pos;
			childBox.updatePos();
			childBox.colorFill = new Color4f(colorFill.x * 0.8f, colorFill.y * 0.8f, colorFill.z * 0.8f, 1);
			Game.entitiesToAdd.add(childBox);
		}
		*/
	}

}
