package main;

import graphics.Graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	
	public static FloatBuffer toBuffer(Tuple3f vec) {
		float[] data = new float[3];
		vec.get(data);
		return toBuffer(data);
	}
	
	public static FloatBuffer toBuffer(Tuple4f vec) {
		float[] data = new float[4];
		vec.get(data);
		return toBuffer(data);
	}
	
	public static FloatBuffer toBuffer(Matrix4f mat) {
		float[] data = new float[16];
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 4; y++) {
				data[y * 4 + x] = mat.getElement(x,y);
			}
		}
		return toBuffer(data);
	}
	
	public static ByteBuffer toBuffer(byte[] data) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public static FloatBuffer toBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public static ShortBuffer toBuffer(short[] data) {
		ShortBuffer buffer = BufferUtils.createShortBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public static float[] toArray(String s) {
		String[] splitString = s.split(" ");
		float[] array = new float[splitString.length];
		for(int j = 0; j < array.length; j++) {
			array[j] = Float.valueOf(splitString[j]);
		}
		return array;
	}
	
	public static short[] toArray(String s, int begin, int stride) {
		String[] splitString = s.split(" ");
		short[] array = new short[splitString.length/stride];
		for(int i = begin; i < splitString.length; i+=stride) {
			array[i/stride] = Short.valueOf(splitString[i]);
		}
		return array;
	}
	
	public static void saveScreenshot(ByteBuffer data) {
		BufferedImage image = new BufferedImage((int)Graphics.WIDTH, (int)Graphics.HEIGHT, BufferedImage.TYPE_INT_RGB);
		IntBuffer intBuffer = data.asIntBuffer();
		int[] array = new int[intBuffer.limit()];
		intBuffer.get(array);
		image.setRGB(0, 0, (int)Graphics.WIDTH, (int)Graphics.HEIGHT, array, 0, (int)Graphics.WIDTH);
		try {
			ImageIO.write(image, "PNG", new File("screenshots/screenshot.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
