package game;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import audio.Audio;
import main.Input;
import main.Main;
import main.Physics;
import main.Timer;
import model.Model;
import model.ModelData;
import model.ModelParser;
import entity.Entity;
import entity.EntityPizzard;
import entity.EntityPlayer;
import entity.EntityPlayer;
import entity.EntityVirus;
import entity.Terrain;
import graphics.GUI;
import graphics.GUIHUD;
import graphics.Graphics;

public class Game {

	//Player vars
	public static EntityPlayer player;

	//Current Level
	public static Level currentLevel;
	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float camDist = 2;
	public static float Z_NEAR = 0.1f, Z_FAR = 1000;
	public static Vector3f camPos = new Vector3f(10,10,10), camUp = new Vector3f(0,1,0);
	public static GUI gui;
		
	public static void init() {
		
		entities = new ArrayList<Entity>();
		
		player = new EntityPlayer();
		player.model = ModelParser.getModel("poogeon2.dae");
		player.scale = new Vector3f(10,10,10);
		player.pos = new Vector3f(10,20,0);
		for(Model m: player.model) {
			m.colorFill = new Color4f(.5f,1,.5f,1);
		}
		entities.add(player);
		
		Physics.addSphere(player, 10, 0, 3, 10);

		for(int i = 0; i < 5; i++) {
			EntityPizzard pizzard = new EntityPizzard();
			pizzard.pos.y = i*10;
			pizzard.scale = new Vector3f(5,5,5);
			pizzard.model = ModelParser.getModel("sphere.dae");
			entities.add(pizzard);
			Physics.addSphere(pizzard, 1, 5, 1, 10);
		}
		
		Terrain terrain = new Terrain();
		terrain.pos = new Vector3f(0,-10,0);
		terrain.scale = new Vector3f(100,100,100);
		entities.add(terrain);
		//Physics.addBox(terrain, 0, 0, 10);
		Physics.addPlane(0, 10, new Vector3f(0,-100,0));
		
		gui = new GUIHUD();
		
		//I like this color: 0.2f,0.5f,1,1

		
		//Audio.play("elephante_mono.wav", new Vector3f(0,0,0), new Vector3f(0,0,0), 5);

	}
	
	public static void setLevel(Level level) {
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
		player.health = 100-(int)(Main.numLoops % 100);
		gui.update(millisPassed);
		
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);

		for(Entity e: entities) {
			e.update();
			
			e.distanceFromCam = (float)Math.sqrt(Math.pow(e.pos.x - camPos.x, 2)
					+ Math.pow(e.pos.y - camPos.y, 2)
					+ Math.pow(e.pos.z - camPos.z, 2));
			
			e.vel.x = (e.pos.x - e.lastPos.x) / millisPassed;
			e.vel.y = (e.pos.y - e.lastPos.y) / millisPassed;
			e.vel.z = (e.pos.z - e.lastPos.z) / millisPassed;
			e.lastPos = e.pos;
		}
	}
}