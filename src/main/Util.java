package main;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	
	public static FloatBuffer convertToBuffer(Vector3f vec) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		buffer.put(new float[] {vec.x, vec.y, vec.z});
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
}
