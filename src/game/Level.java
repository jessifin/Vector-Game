package game;

import java.util.ArrayList;

import entity.Entity;

public class Level {
	
	ArrayList<Entity> entities = new ArrayList<Entity>();

	public Level(ArrayList<Entity> entities) {
		this.entities = entities;
	}

}
