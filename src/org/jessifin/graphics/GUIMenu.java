package org.jessifin.graphics;

import javax.vecmath.Color4f;

public class GUIMenu extends GUI {
	
	ComponentBox box;
	ComponentButton button;

	public GUIMenu() {
		pausesGame = true;

		button = new ComponentButton("BBBBB",4,3,9,5,new Color4f(0,0,1,1));
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
