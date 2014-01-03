package org.jessifin.graphics;

import java.util.ArrayList;

public abstract class GUI {
		
	public boolean pausesGame = false;
	
	public ArrayList<Component> components = new ArrayList<Component>();

	public GUI() {
		
	}
	
	public abstract void update(int millisPassed);
	
	public abstract void render();

}