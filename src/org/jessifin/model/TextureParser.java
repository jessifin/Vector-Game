package org.jessifin.model;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.jessifin.main.Main;
import org.jessifin.main.Util;

public class TextureParser {
	
	private static HashMap<String,Texture> parsedTextures = new HashMap<String,Texture>(); 

	public static Texture getTexture(String loc) {
		return parsedTextures.containsKey(loc) ? parsedTextures.get(loc) : parseTexture(loc);
	}
	
	private static Texture parseTexture(String loc) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(Main.resourceLoc, "model/" + loc + ".png"));
		} catch(IOException exception) {
			//exception.printStackTrace();
			return null;
		}
		
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		for(int i = 0; i < data.length / 4; i++) {
			byte[] pixel = new byte[] {data[4 * i], data[4 * i + 1], data[4 * i + 2], data[4 * i + 3]};
			for(int j = 0; j < 4; j++) {
				data[4 * i + j] = pixel[3 - j];
			}
		}
		
		ByteBuffer buffer = Util.toBuffer(data);
		
		int texID = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, texID);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		Texture texture = new Texture(texID, image.getWidth(), image.getHeight());
		
		parsedTextures.put(loc, texture);
		
		return texture;
	}
	
	public static void clearTextureMap() {
		Iterator<String> keys = parsedTextures.keySet().iterator();
		Iterator<Texture> values = parsedTextures.values().iterator();
		while(keys.hasNext()) {
			System.out.println("Deleting model " + keys.next());
			Texture t = values.next();
			glDeleteTextures(t.texID);
		}
	}
}
