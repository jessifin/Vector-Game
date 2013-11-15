package game;

import java.util.ArrayList;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import audio.Audio;
import main.Input;
import main.Main;
import main.Timer;
import model.Model;
import model.ModelData;
import entity.Entity;
import entity.EntityPlayer;
import entity.EntityVirus;

public class Game {

	//Player vars
	public static EntityPlayer player;

	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float camDist = 2;
	public static float Z_NEAR = 0.5f, Z_FAR = 1000;
	public static Vector3f camPos = new Vector3f(10,10,10), camUp = new Vector3f(0,1,0);
		
	public static void init() {
		float[] verts = {-.5f,-.5f,0,-.5f,.5f,0,.5f,-.5f,0,.5f,.5f,0};
		short[] inds = {0,1,2,1,2,3};
		ModelData modelData = new ModelData("triangle",verts,inds);
		//player = new EntityPlayer(new ModelData[] {modelData});
		player = new EntityPlayer("pizzard.dae");
		for(Model m: player.model) {
			m.colorFill = new Color4f(.5f,1,.5f,1);
		}
		entities.add(player);
		EntityVirus virus = new EntityVirus(new Vector3f(10, 10, 10));
		entities.add(virus);
		Entity floor = new EntityPlayer("hoovy.dae");
		floor.colorFill = new Color4f(1,0,0,1);
		//I like this color: 0.2f,0.5f,1,1
		entities.add(floor);
		
		Audio.play("elephante_mono.wav", new Vector3f(0,0,0), new Vector3f(0,0,0), 5);

	}
	
	public static void update() {
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);
		for(Entity e: entities) {
			e.update();
			
			e.distanceFromCam = (float)Math.sqrt(Math.pow(e.pos.x - camPos.x, 2)
					+ Math.pow(e.pos.y - camPos.y, 2)
					+ Math.pow(e.pos.z - camPos.z, 2));
			
			e.vel.x = (e.pos.x - e.lastPos.x) / 2;
			e.vel.y = (e.pos.y - e.lastPos.y) / 2;
			e.vel.z = (e.pos.z - e.lastPos.z) / 2;
			e.lastPos = e.pos;
		}
	}
}