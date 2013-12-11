package graphics;

import game.Game;

import javax.vecmath.Color4f;

import main.Main;

public class GUIHUD extends GUI {
	
	float healthBarWidth, healthBarLeftBound;
	ComponentBar healthBar;

	public GUIHUD() {
		healthBarWidth = (Graphics.RIGHT - Graphics.LEFT) / 3f;
		healthBarLeftBound = ((Graphics.RIGHT - Graphics.LEFT) / 2f) - (healthBarWidth / 2f);

		healthBar = new ComponentBar("HP",healthBarLeftBound-.1f,0.4f,healthBarLeftBound+healthBarWidth+.1f,0.8f,.05f,new Color4f(1,1,.1f,1),new Color4f(1,1,1,1));
		components.add(healthBar);
	}

	public void update(int millisPassed) {
		float percent = (float)Game.player.health / (float)Game.player.maxHealth;
		healthBar.percent = percent;
		healthBar.setColor(new Color4f(1-percent,percent,0,1), new Color4f(1,1,1,1));
	}
	
	public void render() {		
		for(Component component: components) {
			component.render();
		}
	}

}
