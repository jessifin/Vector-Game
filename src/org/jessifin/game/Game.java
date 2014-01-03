package org.jessifin.game;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.jessifin.main.Input;
import org.jessifin.main.Main;
import org.jessifin.main.Physics;
import org.jessifin.model.Model;
import org.jessifin.model.ModelParser;
import org.jessifin.entity.Entity;
import org.jessifin.entity.EntityPizzard;
import org.jessifin.entity.EntityPlayer;
import org.jessifin.entity.Terrain;
import org.jessifin.graphics.GUI;
import org.jessifin.graphics.GUIHUD;
import org.jessifin.graphics.Graphics;

public class Game {

	//Player vars
	public static EntityPlayer player;

	//Current Level
	public static Level currentLevel;
	public static float speed = 1;
	
	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float camDist = 2;
	public static float Z_NEAR = 0.3f, Z_FAR = 4000;
	public static Vector3f camPos = new Vector3f(10,10,10), camUp = new Vector3f(0,1,0);
	public static GUI gui;
		
	public static void init() {
		entities = new ArrayList<Entity>();
		
		player = new EntityPlayer();
		player.model = ModelParser.getModel("poogeon.dae");
		player.scale = new Vector3f(20,20,20);
		player.pos = new Vector3f(100,0,0);
		player.flashSpeed = 2;
		for(Model m: player.model) {
			m.colorFill = new Color4f(.5f,1,.5f,1);
		}
		entities.add(player);
		
		//Physics.addEntity(player, 5, 1);
		Physics.addSphere(player, 2, 0, 3, 10);
		//Physics.addBox(player, 5, 0, 2);

		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 5; y++) {
				for(int z = 0; z < 5; z++) {
					EntityPizzard pizzard = new EntityPizzard();
					pizzard.pos = new Vector3f(x*10,y*10,z*10);
					pizzard.scale = new Vector3f(5,5,5);
					pizzard.model = ModelParser.getModel("rock.dae");
					pizzard.colorFill = new Color4f(Main.rng.nextFloat(),Main.rng.nextFloat(),Main.rng.nextFloat(),1);
					entities.add(pizzard);
					Physics.addSphere(pizzard, 1, z*3, 1, 10);
				}
			}
		}
		
		for(int x = 0; x < 4; x++) {
			for(int z = 0; z < 4; z++) {
				Terrain terrain = new Terrain();
				terrain.pos = new Vector3f((x-2)*1000,-200,(z-2)*1000);
				terrain.scale = new Vector3f(1000,100,1000);
				entities.add(terrain);
				Physics.addBox(terrain, 0, 0, 4);
			}
		}
		
		Physics.addPlane(0, 10, new Vector3f(0,-500,0), new Quat4f(0,0,0,1));
		//Physics.addPlane(0, 10, new Vector3f(0,100,0), new Quat4f(0,1,0,1));
		
		//setLevel("test");
		gui = new GUIHUD();
		
		//I like this color: 0.2f,0.5f,1,1

		
		//Audio.play("elephante_mono.wav", new Vector3f(0,0,0), new Vector3f(0,0,0), 5);

	}
	
	public static void setLevel(String loc) {
		Level level = LevelIO.getLevel(loc);
		currentLevel = level;
		entities = level.entities;
		Graphics.clearColor = level.color;
	}
	
	public static void reboot() {
		Physics.destroy();
		Physics.init();
		init();
	}
	
	public static void update(int millisPassed) {
		gui.update(millisPassed);
		
		//player.health = (int)((Main.numLoops%100)/100f*player.maxHealth);
		
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);

		for(Entity e: entities) {
			e.update();
						
			e.distanceFromCam = (float)Math.sqrt((e.pos.x - camPos.x)*(e.pos.x - camPos.x)
					+ (e.pos.y - camPos.y)*(e.pos.y - camPos.y))
					+ (e.pos.z - camPos.z)*(e.pos.z - camPos.z);
			/*
			e.vel.x = (e.pos.x - e.lastPos.x);
			e.vel.y = (e.pos.y - e.lastPos.y);
			e.vel.z = (e.pos.z - e.lastPos.z);
			e.lastPos = new Vector3f(e.pos.x,e.pos.y,e.pos.z);
			
			e.accel.x = (e.vel.x - e.lastVel.x);
			e.accel.y = (e.vel.y - e.lastVel.y);
			e.accel.z = (e.vel.z - e.lastVel.z);
			e.lastVel = new Vector3f(e.vel.x,e.vel.y,e.vel.z);

			if(e.accel.y < 5 && e.vel.y < -3) {
				e.health-=(e.accel.y-e.lastAccel.y);
			}
			
			e.lastAccel = new Vector3f(e.accel.x,e.accel.y,e.accel.z);
			*/
		}
	}
}