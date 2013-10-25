package game;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import model.ModelData;
import entity.Entity;
import entity.EntityPlayer;

public class GameInfo {

	//Player vars
	public static EntityPlayer player;

	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float Z_NEAR = 0.5f, Z_FAR = 1000;
	public static Vector3f camPos = new Vector3f(10,10,10);
	
	public static void init() {
		float[] verts = {0,0,0,0,1,0,1,0,0,1,1,0};
		short[] inds = {0,1,2,1,2,3};
		ModelData modelData = new ModelData("triangle",verts,inds);
		//player = new EntityPlayer(new ModelData[] {modelData});
		player = new EntityPlayer();
		for(int i = 0; i < 20; i++) {
		//	entities.add(new EntityPlayer(new ModelData[] {modelData}));
		}
		entities.add(player);
	}
}
