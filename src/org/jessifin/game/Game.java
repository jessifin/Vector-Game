package org.jessifin.game;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.jessifin.main.Input;
import org.jessifin.main.Main;
import org.jessifin.model.Model;
import org.jessifin.model.ModelParser;
import org.jessifin.physics.Physics;
import org.jessifin.audio.Audio;
import org.jessifin.entity.Entity;
import org.jessifin.entity.EntityPickup;
import org.jessifin.entity.EntityPlayer;
import org.jessifin.entity.EntityBox;
import org.jessifin.graphics.Graphics;
import org.jessifin.graphics.gui.GUI;
import org.jessifin.graphics.gui.GUIHUD;

public class Game {

	//Player vars
	public static EntityPlayer player;

	//Current Level
	public static Level currentLevel;
	public static float speed = 1;
	
	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	public static ArrayList<Entity> entitiesToAdd = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float camDist = 2;
	public static final float Z_NEAR = 1, Z_FAR = 2500;
	public static Vector3f camPos = new Vector3f(10,10,10), camUp = new Vector3f(0,1,0);
	public static GUI gui;
		
	public static void init() {
		entities = new ArrayList<Entity>();
		
		player = new EntityPlayer();
		//player.scale = new Vector3f(20,20,20);
		player.pos = new Vector3f(0,50,0);
		player.updatePos();
		player.flashSpeed = 2;
		entities.add(player);
		
		EntityBox box = new EntityBox(new Vector3f(100,10,100));
		box.pos = new Vector3f(0,0,0);
		box.updatePos();
		box.colorFill = new Color4f(1,1,1,1);
		box.flashSpeed = 1;
		entities.add(box);

		//setLevel("test");
		gui = new GUIHUD();
		
		//I like this color: 0.2f,0.5f,1,1

		
		Audio.playMusic("keeping_busy.wav");
	}
	
	public static void setLevel(String loc) {
		Level level = LevelIO.getLevel(loc);
		currentLevel = level;
		entities = level.entities;
		Graphics.clearColor = level.color;
	}
	
	public static void reboot() {
		Physics.destroy();
		Physics.init(true);
		init();
		Audio.play("death.wav", camPos, new Vector3f(0,0,0), 1);
	}
	
	public static void update(int millisPassed) {
		gui.update(millisPassed);
				
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);
		
		if(entitiesToAdd.size() > 0) {
			for(Entity e: entitiesToAdd) {
				entities.add(e);
			}
			entitiesToAdd.clear();
		}
		
		for(Entity e: entities) {
			e.update();
			
			e.squaredDistanceFromCam = (float)(e.pos.x - camPos.x)*(e.pos.x - camPos.x)
					+ (e.pos.y - camPos.y)*(e.pos.y - camPos.y)
					+ (e.pos.z - camPos.z)*(e.pos.z - camPos.z);
		}
	}
}