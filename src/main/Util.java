package main;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	
	public static FloatBuffer convertToBuffer(Vector3f vec) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		buffer.put(new float[] {vec.x, vec.y, vec.z});
		buffer.flip();
		return buffer;
	}
}
