package model;

public class ModelData {
	
	public final String name;
	public final float[] vertices;
	public final short[] indices;
	
	public ModelData(String name, float[] vertices, short[] indices) {
		this.name = name;
		this.vertices = vertices;
		this.indices = indices;
	}
}
