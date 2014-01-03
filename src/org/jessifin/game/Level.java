package org.jessifin.game;

import java.util.ArrayList;

import javax.vecmath.Color4f;

import org.jessifin.entity.Entity;

public class Level {
	
	Color4f color = new Color4f(0,0,0,0);
	
	ArrayList<Entity> entities = new ArrayList<Entity>();

	public Level(ArrayList<Entity> entities) {
		this.entities = entities;
	}
	
	public Level() {
		
	}

}
