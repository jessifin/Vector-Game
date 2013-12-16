package main;

import static org.lwjgl.input.Keyboard.*;

import javax.vecmath.Vector3f;

import game.Game;
import game.Level;
import game.LevelIO;
import graphics.Graphics;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import audio.Audio;

public class Input {
	
	public static float dX = 0, dY = 0;
	public static float absX = 0, absY = 0;
	public static float x = 0, y = 0;
	public static float speed = 1;
	
	static final float mouseSensitivity = 100;
	
	public static KeyInfo[] keyboardInfo = new KeyInfo[KEYBOARD_SIZE];
	
	public static void init() {
		Mouse.setGrabbed(true);
		enableRepeatEvents(true);
		for(int i = 0; i < keyboardInfo.length; i++) {
			if(Keyboard.getKeyName(i) != null) {
				keyboardInfo[i] = KeyInfo.getKeyInfo();
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
			Audio.playAtPlayer("pootis.wav");
			//Main.entities.add(new EntityProjectile(new Vector3f(GameInfo.player.pos.x, GameInfo.player.pos.y, GameInfo.player.pos.z)));
		}
		if(Mouse.isButtonDown(1)) {
			//Audio.playMusic("le_elephante.wav");
			Graphics.takeScreenShot();
		}
	}
	
	public static void keyboardUpdate(float millisPassed) {
		poll();
		while(next()) {
			int keyID = getEventKey();
			keyboardInfo[keyID].nanosPressed = (int) getEventNanoseconds();
			keyboardInfo[keyID].state = getEventKeyState();
		}
		if(keyboardInfo[Keyboard.KEY_S].state && keyboardInfo[Keyboard.KEY_RETURN].state) {
			LevelIO.writeLevel("test2", new Level(Game.entities));
		}
		if(keyboardInfo[KEY_W].state) {
			Vector3f forward = new Vector3f(
					Game.player.pos.x - Game.camPos.x,
					Game.player.pos.y - Game.camPos.y,
					Game.player.pos.z - Game.camPos.z
					);
			
			forward.normalize();
			forward.scale(speed*3);
			Physics.applyImpulse(Game.player, forward);
			/*
			Game.player.pos.x -= (float) (Math.cos(x) * Math.sin(y))*speed;
			Game.player.pos.y -= (float) (Math.cos(y))*speed;
			Game.player.pos.z -= (float) (Math.sin(x) * Math.sin(y))*speed;
			*/
		}
		if(keyboardInfo[KEY_S].state) {
			Vector3f backward = new Vector3f(
					Game.camPos.x - Game.player.pos.x,
					Game.camPos.y - Game.player.pos.y,
					Game.camPos.z - Game.player.pos.z
					);
						
			backward.normalize();
			backward.scale(speed*3);
			Physics.applyImpulse(Game.player, backward);
			/*
			Game.player.pos.x += (float) (Math.cos(x) * Math.sin(y))*speed;
			Game.player.pos.y += (float) (Math.cos(y))*speed;
			Game.player.pos.z += (float) (Math.sin(x) * Math.sin(y))*speed;
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
		if(keyboardInfo[KEY_C].state && speed < 10) {
			speed+=.01f;
		}
		if(keyboardInfo[KEY_Z].state && speed > .02f) {
			speed-=.01f;
		}
		if(keyboardInfo[KEY_Z].state && keyboardInfo[KEY_C].state) {
			speed = 5;
		}
		if(keyboardInfo[KEY_1].state) {
			Game.camDist-=speed;
		}
		if(keyboardInfo[KEY_3].state) {
			Game.camDist+=speed;
		}
		if(keyboardInfo[KEY_LEFT].state) {
			Game.FoV-=speed;
		}
		if(keyboardInfo[KEY_RIGHT].state) {
			Game.FoV+=speed;
		}
		if(keyboardInfo[KEY_ESCAPE].state) {
			Main.RUNNING = false;
		}
		if(keyboardInfo[KEY_F1].state) {
		//	Graphics.toggleFullScreen();
		//	Mouse.setCursorPosition(Graphics.WIDTH/2, Graphics.HEIGHT/2);
			x = .01f; y = 3.14f;
			Mouse.setGrabbed(true);
		}
		if(keyboardInfo[KEY_BACK].state) {
			Game.reboot();
		}
		if(keyboardInfo[KEY_RETURN].state) {
		}
		
	}

	public static class KeyInfo {
		
		public int nanosPressed;
		public boolean state;
		
		public static KeyInfo getKeyInfo() {
			return new KeyInfo();
		}
		
	}
}
