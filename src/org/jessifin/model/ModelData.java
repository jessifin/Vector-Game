package org.jessifin.model;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ModelData {
	
	public final String name;
	public final float[] vertices;
	public final short[] indices;
	public Vector3f pos = new Vector3f(0,0,0), rot = new Vector3f(0,0,0), scale = new Vector3f(1,1,1);
	public Color4f color = new Color4f(1,1,1,1);
	public Armature armature;
		
	public ModelData(String name, float[] vertices, short[] indices) {
		this.name = name;
		this.vertices = vertices;
		this.indices = indices;
	}
	
	public ModelData(String name, float[] vertices, short[] indices, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
		this.name = name;
		this.vertices = vertices;
		this.indices = indices;
		this.pos = pos;
		this.rot = rot;
		this.scale = scale;
		this.color = color;
	}
	
	public ModelData(String name, float[] vertices, short[] indices, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color, Armature armature) {
		this.name = name;
		this.vertices = vertices;
		this.indices = indices;
		this.pos = pos;
		this.rot = rot;
		this.scale = scale;
		this.color = color;
		this.armature = armature;
	}
}
