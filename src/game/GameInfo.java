package game;

import entity.EntityPlayer;

public class GameInfo {

	//Player vars
	public static EntityPlayer player = new EntityPlayer();
	
	//Audio related vars
	public static float musicVolume = 1, fxVolume = 1;
	
	//Graphics
	public static float FoV = 60;
	public static float Z_NEAR = 0.5f, Z_FAR = 1000;
}
