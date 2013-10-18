package game;

import java.util.ArrayList;

import entity.Entity;
import entity.EntityPlayer;

public class GameInfo {

	//Player vars
	public static EntityPlayer player = new EntityPlayer();
	
	//Entities
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float Z_NEAR = 0.5f, Z_FAR = 1000;
	
	public static void init() {
		entities.add(player);
	}
}
