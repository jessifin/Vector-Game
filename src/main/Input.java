package main;

import static org.lwjgl.input.Keyboard.*;

import javax.vecmath.Vector3f;

import entity.EntityPizzard;
import game.Game;
import game.Level;
import game.LevelIO;
import graphics.GUIHUD;
import graphics.GUIMenu;
import graphics.Graphics;
import model.ModelParser;

import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import audio.Audio;

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
			Audio.playAtEntity("pew.wav",Game.player);
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
			keys[keyID].nanosPressed = (int) getEventNanoseconds();
			
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
		
		if(keys[KEY_S].state && keys[KEY_RETURN].state) {
			LevelIO.writeLevel("test", new Level(Game.entities));
		}
		if(keys[KEY_W].state) {
			Vector3f forward = new Vector3f(
					Game.player.pos.x - Game.camPos.x,
					Game.player.pos.y - Game.camPos.y,
					Game.player.pos.z - Game.camPos.z
					);
			
			forward.normalize();
			forward.scale(Game.speed*60);
			Physics.applyImpulse(Game.player, forward);
			if(Game.speed<=0) {
				Game.reboot();
				Game.speed = 1;
			} else {
				Game.speed-=.0025f;
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
			backward.scale(Game.speed*60);
			Physics.applyImpulse(Game.player, backward);
			if(Game.speed<=0) {
				Game.reboot();
				Game.speed = 1;
			} else {
				Game.speed-=.0025f;
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
			Game.FoV--;;
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
		if(keys[KEY_R].pressed) {
			Audio.playAtEntity("pew.wav", Game.player);
		}
		if(keys[KEY_BACK].state) {
			Game.reboot();
		}
		if(keys[KEY_RETURN].state) {
			Vector3f forward = new Vector3f(
					Game.player.pos.x - Game.camPos.x,
					Game.player.pos.y - Game.camPos.y,
					Game.player.pos.z - Game.camPos.z
					);
			
			forward.normalize();
			forward.scale(Game.speed*60);
			
			EntityPizzard pizzard = new EntityPizzard();
			pizzard.model = ModelParser.getModel("sphere.dae");
			Vector3f pizzardPos = new Vector3f(Game.player.pos.x + forward.x*5,Game.player.pos.y + forward.y*5,Game.player.pos.z + forward.z*5);
			pizzard.pos = pizzardPos;
			pizzard.scale = new Vector3f(5,5,5);
			Game.entities.add(pizzard);
			Physics.addSphere(pizzard, 5, 1, 1, 5);
			Physics.applyImpulse(pizzard, forward);
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
		public int nanosPressed;
		public boolean state, released, pressed;
	}
}
