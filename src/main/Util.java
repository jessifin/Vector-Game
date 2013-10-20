package main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	
	public static FloatBuffer toBuffer(Vector3f vec) {
		float[] data = new float[3];
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
}
