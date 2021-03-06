package org.jessifin.main;

import static org.lwjgl.input.Keyboard.*;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.jessifin.entity.Entity;
import org.jessifin.entity.EntityBox;
import org.jessifin.game.Game;
import org.jessifin.game.Level;
import org.jessifin.game.LevelIO;
import org.jessifin.graphics.Graphics;
import org.jessifin.graphics.gui.GUIHUD;
import org.jessifin.graphics.gui.GUIMenu;
import org.jessifin.model.ModelParser;
import org.jessifin.physics.Physics;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.jessifin.audio.Audio;

public class Input {
	
	public static float dX = 0, dY = 0;
	public static float absX = 0, absY = 0;
	public static float x = 0, y = 0;
	private static boolean grab = false;
	
	static final float mouseSensitivity = 100;
	
	public static KeyInfo[] keys = new KeyInfo[KEYBOARD_SIZE];
	
	public static void init() {
		Mouse.setGrabbed(true);
		enableRepeatEvents(true);
		for(int i = 0; i < keys.length; i++) {
			if(getKeyName(i) != null) {
				keys[i] = new KeyInfo();
			}
		}
	}
	
	public static void mouseUpdate(float millisPassed) {
		dX = (Mouse.getDX())/mouseSensitivity*16f/millisPassed;
		dY = (Mouse.getDY())/mouseSensitivity*16f/millisPassed;
		absX = Mouse.getX()/Graphics.WIDTH;
		absY = Mouse.getY()/Graphics.HEIGHT;
		x += dX; y += dY;
		
		if(Mouse.isButtonDown(0)) {
			Physics.conductRaycast(Game.camPos, Game.player.pos);
			Audio.play("pootis.wav", Game.player.pos, new Vector3f(0,0,0), 5);
		}
		if(Mouse.isButtonDown(1)) {
			//Audio.playMusic("le_elephante.wav");
			Graphics.takeScreenShot();
		}
		
		if(Game.gui.pausesGame && !grab) {
			Mouse.setGrabbed(true);
			Mouse.setCursorPosition((int)Graphics.WIDTH/2, (int)Graphics.HEIGHT/2);
			grab = true;
		}
		if(!Game.gui.pausesGame && grab) {
			Mouse.setGrabbed(false);
			grab = false;
		}
	}
	
	public static void keyboardUpdate(float millisPassed) {
		poll();
		while(next()) {
			int keyID = getEventKey();
			keys[keyID].nanosPressed = getEventNanoseconds();
			
			boolean state = getEventKeyState();
			keys[keyID].released = false;
			keys[keyID].pressed = false;
			
			if(keys[keyID].state && !state) {
				keys[keyID].released = true;
			}
			if(!keys[keyID].state && state) {
				keys[keyID].pressed = true;
			}
			keys[keyID].state = state;
		}
		
		if(keys[KEY_W].state) {
			Vector3f forward = new Vector3f(
				Game.player.pos.x - Game.camPos.x,
				Game.player.pos.y - Game.camPos.y,
				Game.player.pos.z - Game.camPos.z
			);
			
			forward.normalize();
			forward.scale(20);
			Physics.applyImpulse(Game.player, forward);
			if(Game.speed<=0) {
				Game.reboot();
				Game.speed = 1;
			} else {
			//	Game.speed-=.0025f;
			}
			/*
			Game.player.pos.x -= (float) (Math.cos(x) * Math.sin(y))*Game.speed;
			Game.player.pos.y -= (float) (Math.cos(y))*Game.speed;
			Game.player.pos.z -= (float) (Math.sin(x) * Math.sin(y))*Game.speed;
			*/
		}
		if(keys[KEY_S].state) {
			Vector3f backward = new Vector3f(
				Game.camPos.x - Game.player.pos.x,
				Game.camPos.y - Game.player.pos.y,
				Game.camPos.z - Game.player.pos.z
			);
						
			backward.normalize();
			backward.scale(20);
			Physics.applyImpulse(Game.player, backward);
			if(Game.speed<=0) {
				Game.reboot();
				Game.speed = 1;
			} else {
			//	Game.speed-=.0025f;
			}
			/*
			Game.player.pos.x += (float) (Math.cos(x) * Math.sin(y))*Game.speed;
			Game.player.pos.y += (float) (Math.cos(y))*Game.speed;
			Game.player.pos.z += (float) (Math.sin(x) * Math.sin(y))*Game.speed;
			*/
		}
		/*
		if(keyboardInfo[KEY_Q].state) {
			Graphics.camUp.x-=.1f;
			Graphics.camUp.z-=.1f;
		}
		if(keyboardInfo[KEY_E].state) {
			Graphics.camUp.x+=.1f;
			Graphics.camUp.z+=.1f;
		}
		*/
		if(keys[KEY_Q].pressed) {
			MacUtil.toggleFullscreen();
		}
		if(keys[KEY_Z].state) {
			Game.speed -= .01f;
		}
		if(keys[KEY_C].state) {
			Game.speed += .01f;
		}
		if(keys[KEY_Z].state && keys[KEY_C].state) {
			Game.speed = 1;
		}
		if(keys[KEY_1].state) {
			Game.camDist-=Game.speed*20;
		}
		if(keys[KEY_3].state) {
			Game.camDist+=Game.speed*20;
		}
		if(keys[KEY_LEFT].state) {
			Game.FoV--;
		}
		if(keys[KEY_RIGHT].state) {
			Game.FoV++;
		}
		if(keys[KEY_ESCAPE].pressed) {
			if(Game.gui instanceof GUIMenu) {
				Mouse.setGrabbed(true);
				Game.gui = new GUIHUD();
			} else {
				Mouse.setGrabbed(false);
				Game.gui = new GUIMenu();
			}
		}
		if(keys[KEY_BACK].pressed) {
			Game.reboot();
		}
		if(keys[KEY_TAB].pressed) {
			Sys.alert("Yo dawg", "I heard you like messages");
			Graphics.setIcon("/icns/16.png");
			Sys.openURL("http://www.reddit.com");
		}
		if(keys[KEY_RETURN].pressed) {
			Vector3f forward = new Vector3f(
				Game.player.pos.x - Game.camPos.x,
				Game.player.pos.y - Game.camPos.y,
				Game.player.pos.z - Game.camPos.z
			);
			
			forward.normalize();
			forward.scale(Game.speed*60);
			EntityBox box = new EntityBox(new Vector3f(50,50,50));
			Vector3f pizzardPos = new Vector3f(Game.player.pos.x + forward.x*5,Game.player.pos.y + forward.y*5,Game.player.pos.z + forward.z*5);
			box.pos = pizzardPos;
			box.updatePos();
			box.colorFill = new Color4f(Main.rng.nextFloat(),Main.rng.nextFloat(),Main.rng.nextFloat(),1);
			Game.entities.add(box);
			Physics.applyImpulse(box, forward);
		}
		if(keys[KEY_F1].pressed) {
			Graphics.setDisplayMode(Graphics.getDefaultDisplayMode(), !Graphics.fullscreen);
		}
		
		for(KeyInfo key: keys) {
			if(key != null) {
				key.pressed = false;
				key.released = false;
			}
		}
	}

	public static class KeyInfo {
		public long nanosPressed;
		public boolean state, released, pressed;
	}
}