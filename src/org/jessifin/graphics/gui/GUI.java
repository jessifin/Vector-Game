package org.jessifin.graphics.gui;

import java.util.ArrayList;

import org.jessifin.graphics.gui.component.Component;

public abstract class GUI {
		
	public boolean pausesGame = false;
	
	public ArrayList<Component> components = new ArrayList<Component>();

	public GUI() {
		
	}
	
	public abstract void update(int millisPassed);
	
	public abstract void render();

}