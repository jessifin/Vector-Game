package org.jessifin.model;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class Bone {

	public String name;
	public Bone parent;
	public Bone[] children;
	public Vector3f head, tail;
	public Matrix4f matrix, tempMatrix = new Matrix4f();
	
	public Bone(String name, Vector3f head, Vector3f tail, Matrix4f matrix) {
		this.name = name;
		this.head = head;
		this.tail = tail;
		this.matrix = matrix;
	}

}
