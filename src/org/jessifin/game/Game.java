package org.jessifin.game;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.jessifin.main.Input;
import org.jessifin.main.Main;
import org.jessifin.model.Model;
import org.jessifin.model.MeshParser;
import org.jessifin.physics.Physics;
import org.jessifin.audio.Audio;
import org.jessifin.entity.Entity;
import org.jessifin.entity.EntityPickup;
import org.jessifin.entity.EntityPizzard;
import org.jessifin.entity.EntityPlayer;
import org.jessifin.entity.EntityVirus;
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
	public static float Z_NEAR = 1f, Z_FAR = 2500;
	public static Vector3f camPos = new Vector3f(10,10,10), camUp = new Vector3f(0,1,0);
	public static GUI gui;
		
	public static void init() {
		entities = new ArrayList<Entity>();
		
		player = new EntityPlayer();
		player.mesh = MeshParser.getModel("arepo/arepo.mesh");
		player.scale = new Vector3f(20,20,20);
		player.pos = new Vector3f(100,200,0);
		player.flashSpeed = 2;
		entities.add(player);
		
		//Physics.addEntity(player, 5, 1);
		Physics.addSphere(player, 10, 0, 3, 5);
		//Physics.addBox(player, 5, 0, 2);
		/*
		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 5; y++) {
				for(int z = 0; z < 5; z++) {
					EntityVirus virus = new EntityVirus();
					virus.pos = new Vector3f(x*10,200+y*10,z*10);
					virus.colorFill = new Color4f(Main.rng.nextFloat(),Main.rng.nextFloat(),Main.rng.nextFloat(),1);
					entities.add(virus);
					virus.scale = new Vector3f(5,5,5);
					virus.flashSpeed = (int) (speed*10);
					Physics.addBox(virus, 5, 1, 0.5f);
				}
			}
		}
		*/
		for(int thetaX = 0; thetaX < 30; thetaX++) {
			for(int thetaY = 0; thetaY < 30; thetaY++) {
				EntityVirus virus = new EntityVirus();
				virus.mesh = MeshParser.getModel("rock.mesh");
				virus.colorFill = new Color4f(Main.rng.nextFloat(),Main.rng.nextFloat(),Main.rng.nextFloat(),1);
				virus.pos = new Vector3f((float)((150*Math.cos(Math.toRadians(thetaX * 12))) * Math.sin(Math.toRadians(thetaY * 12))),(float)(500 + 150 * Math.cos(Math.toRadians(thetaY * 12))), (float)(150*Math.sin(Math.toRadians(thetaY * 12)) * Math.sin(Math.toRadians(thetaX * 12))));
				virus.scale = new Vector3f(5,5,5);
				//virus.flashSpeed = 3;
				//entities.add(virus);
				//Physics.addSphere(virus, 50, 2, 1, 2.5f);
			}		
		}
		/*
		Terrain terrain = new Terrain();
		terrain.pos = new Vector3f(0,0,0);
		terrain.scale = new Vector3f(1000,1000,1000);
		entities.add(terrain);
		Physics.addEntity(terrain, 0, 1);
		*/
		Physics.addPlane(0, 10, new Vector3f(0,-500,0), new Quat4f(0,0,0,1));
		//Physics.addPlane(0, 10, new Vector3f(0,100,0), new Quat4f(0,1,0,1));
		
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				EntityVirus virus = new EntityVirus();
				virus.pos = new Vector3f(x * 300, 0, y * 300);
				virus.colorFill = new Color4f(1-x/10f,1-y/10f,0,1);
				virus.mesh = MeshParser.getModel("bawks.mesh");
				virus.scale = new Vector3f(300,300,300);
				virus.isAlive = false;
				virus.flashSpeed = 6.5f;
				entities.add(virus);
				Physics.addBox(virus, 0, 1, 1);
			}
		}

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
		Physics.init(true);
		init();
		Audio.play("death.wav", camPos, new Vector3f(0,0,0), 1);
	}
	
	public static void update(int millisPassed) {
		gui.update(millisPassed);
				
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);
		
		for(Entity e: entities) {
			e.update();
			
			e.squaredDistanceFromCam = (float)(e.pos.x - camPos.x)*(e.pos.x - camPos.x)
					+ (e.pos.y - camPos.y)*(e.pos.y - camPos.y)
					+ (e.pos.z - camPos.z)*(e.pos.z - camPos.z);
		}
	}
}