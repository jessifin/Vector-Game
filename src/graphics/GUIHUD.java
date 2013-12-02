package graphics;

import game.Game;

import javax.vecmath.Color4f;

public class GUIHUD extends GUI {
	
	float healthBarWidth, healthBarLeftBound;
	ComponentBox health, antiHealth;
	ComponentBox guiBox;

	public GUIHUD() {
		healthBarWidth = (Graphics.RIGHT - Graphics.LEFT) / 3f;
		healthBarLeftBound = ((Graphics.RIGHT - Graphics.LEFT) / 2f) - (healthBarWidth / 2f);
		
		health = new ComponentBox(healthBarLeftBound,14.9f,healthBarLeftBound + healthBarWidth,15.3f,0,new Color4f(1,1,.1f,1));
		components.add(health);
		
		//antiHealth = new ElementBox(healthBarLeftBound,14.9f,healthBarLeftBound + healthBarWidth,15.3f,0,new Color4f(1,1,1,1));
		//elements.add(antiHealth);
		
		guiBox = new ComponentBox(healthBarLeftBound-.1f,14.8f,healthBarLeftBound+healthBarWidth+.1f,15.4f,0,new Color4f(1,1,1,1));
		components.add(guiBox);
	}

	public void update(int millisPassed) {
		health.x1 = healthBarLeftBound;
		health.x2 = healthBarLeftBound + healthBarWidth * (float)(Game.player.health)/(float)(Game.player.maxHealth);
		health.color = new Color4f(1-(float)(Game.player.health)/(float)(Game.player.maxHealth),(float)(Game.player.health)/(float)(Game.player.maxHealth),0,1);
		
		//antiHealth.x1 = health.x2;
		//antiHealth.x2 = healthBarLeftBound + healthBarWidth;
	}
	
	public void render() {		
		for(Component component: components) {
			component.render();
		}
	}

}
