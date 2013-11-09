package game;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import main.Input;
import model.ModelData;
import entity.Entity;
import entity.EntityPlayer;

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
		player = new EntityPlayer("rollboscis.dae");
		entities.add(player);
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				for(int z = 0; z < 10; z++) {
					EntityPlayer e = new EntityPlayer(new ModelData[] {modelData});
					e.pos.x = x + .5f;
					e.pos.y = y + .5f;
					e.pos.z = z + .5f;
					entities.add(e);
				}
			}
		}
	}
	
	public static void update() {
		camPos.x = (float) (camDist * Math.cos(Input.x) * Math.sin(Input.y) + player.pos.x);
		camPos.y = (float) (camDist * Math.cos(Input.y) + player.pos.y);
		camPos.z = (float) (camDist * Math.sin(Input.x) * Math.sin(Input.y) + player.pos.z);
		for(Entity e: entities) {
			e.rot.z += Input.dX;
			e.scale.x += Input.dY;
		}
	}
}