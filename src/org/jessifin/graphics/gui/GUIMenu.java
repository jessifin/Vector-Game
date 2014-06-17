package org.jessifin.graphics.gui;

import javax.vecmath.Color4f;

import org.jessifin.graphics.gui.component.Component;
import org.jessifin.graphics.gui.component.ComponentBox;
import org.jessifin.graphics.gui.component.ComponentButton;

public class GUIMenu extends GUI {
	
	ComponentBox box;
	ComponentButton button;

	public GUIMenu() {
		pausesGame = true;

		button = new ComponentButton("BUTTON",4,3,9,5,new Color4f(0,0,1,1));
		components.add(button);
	}

	public void update(int millisPassed) {
		
	}

	public void render() {
		for(Component component: components) {
			component.render();
		}
	}

}
