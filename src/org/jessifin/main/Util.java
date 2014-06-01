package org.jessifin.main;

import org.jessifin.graphics.Graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	
	public static void calculateMatrix(Matrix4f matrix, Vector3f pos, Vector3f rot, Vector3f scale) {
		float cosX = (float)Math.cos(rot.x);
		float sinX = (float)Math.sin(rot.x);
		float cosY = (float)Math.cos(rot.y);
		float sinY = (float)Math.sin(rot.y);
		float cosZ = (float)Math.cos(rot.z);
		float sinZ = (float)Math.sin(rot.z);
		
		float cosXsinY = cosX * sinY;
		float sinXsinY = sinX * sinY;
		
		matrix.set(new float[] {
			cosY * cosZ * scale.x, -cosY * sinZ * scale.y, sinY * scale.z, pos.x,
			(sinXsinY * cosZ + cosX * sinZ) * scale.x, (-sinXsinY * sinZ + cosX * cosZ) * scale.y, -sinX * cosY * scale.z, pos.y,
			(-cosXsinY * cosZ + sinX * sinZ) * scale.x, (cosXsinY * sinZ + sinX * cosZ) * scale.y, cosX * cosY * scale.z, pos.z,
			0, 0, 0, 1
		});
	}
	
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
		if(stride == 0) {
			System.err.println("Who do you think you are? Oh wait, I'm the only one writing this :P");
			return null;
		}
		String[] splitString = s.split(" ");
		short[] array = new short[splitString.length/stride];
		for(int i = begin; i < splitString.length; i+=stride) {
			array[i/stride] = Short.valueOf(splitString[i]);
		}
		return array;
	}
	
	public static Vector3f getRotation(Matrix3f mat) {
		Vector3f rot = new Vector3f();
		rot.y = (float)Math.asin(mat.getElement(2,0));
		float c = (float) Math.cos(rot.y);
		if(Math.abs(c) > .005f) {
			float TRX = mat.getElement(2,2)/c;
			float TRY = mat.getElement(2,1)/c;
			rot.x = (float)Math.atan2(TRY,TRX);
			
			TRX = mat.getElement(0,0)/c;
			TRY = mat.getElement(1,0)/c;
			rot.z = (float)Math.atan2(TRY,TRX);
		} else {
			rot.x = 0;
			float TRX = mat.getElement(1,1);
			float TRY = mat.getElement(1,0);
			rot.z = (float)Math.atan2(TRY,TRX);
		}
		return rot;
	}
	
	public static ArrayList<?> ensureSize(ArrayList<?> list, int size) {
		while(list.size() < size) {
			list.add(null);
		}
		return list;
	}
	
	public static void saveScreenshot(final ByteBuffer data) {
		System.out.println("Taking screenshot!");
		new Thread() {
			BufferedImage dst;
			public void run() {
				BufferedImage image = new BufferedImage((int)Graphics.WIDTH, (int)Graphics.HEIGHT, BufferedImage.TYPE_INT_RGB);
				int[] array = new int[(int) (Graphics.WIDTH * Graphics.HEIGHT)];
				for(int i = 0; i < array.length; i++) {
					int pootis = i*3;
					array[i] = (data.get(pootis) << 16) + (data.get(pootis+1) << 8) + (data.get(pootis+2) << 0);
				}
				image.setRGB(0, 0, (int)Graphics.WIDTH, (int)Graphics.HEIGHT, array, 0, (int)Graphics.WIDTH);
				AffineTransform trans = AffineTransform.getScaleInstance(1, -1);
				trans.translate(0, -image.getHeight());
				AffineTransformOp op = new AffineTransformOp(trans, AffineTransformOp.TYPE_BILINEAR);
				dst = op.filter(image, null);
				
				try {
					ImageIO.write(dst, "PNG", new File("screenshots/" + System.currentTimeMillis() + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public static ByteBuffer getBuffer(BufferedImage image) {
		int width = image.getWidth(); int height = image.getHeight();
		byte[] data = new byte[width*height*4];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int argb = image.getRGB(x,y);
				int a = (argb>>24)&0xff;
				int r = (argb>>16)&0xff;
				int g = (argb>>8)&0xff;
				int b = argb&0xff;
				//System.out.println("("+x+","+y+") "+(byte)r+" "+(byte)g+" "+(byte)b+" "+(byte)a);
				data[(x+y*width)*4] = 0;
				data[(x+y*width)*4+1] = 127;
				data[(x+y*width)*4+2] = 0;
				data[(x+y*width)*4+3] = 127;
			}
		}
		for(byte b: data) {
			b = 0;
		}
		
		return toBuffer(data);
	}
	
	public static ByteBuffer getBuffer(String loc) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(Main.resourceLoc,loc));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return getBuffer(image);
	}
	
	public static ByteBuffer[] getIcon(byte[] powers) {
		ByteBuffer[] buffers = new ByteBuffer[powers.length];
		for(int i = 0; i < powers.length; i++) {
			int power = 1 << powers[i];
			buffers[i] = getBuffer("icns/"+power+".png");
		}
		return buffers;
	}
}
