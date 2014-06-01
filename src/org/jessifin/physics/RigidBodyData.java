package org.jessifin.physics;

public class RigidBodyData {
	
	public float friction, restitution, mass;
	public String collisionShape;

	public RigidBodyData(float friction, float restitution, float mass, String collisionShape) {
		this.friction = friction;
		this.restitution = restitution;
		this.mass = mass;
		this.collisionShape = collisionShape;
	}

}
