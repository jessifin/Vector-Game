package org.jessifin.model;

import com.bulletphysics.dynamics.RigidBody;

public class Mesh {

	public Model[] model;
	public RigidBody body;
	
	public Mesh(Model[] model, RigidBody body) {
		this.model = model;
		this.body = body;
	}

}
