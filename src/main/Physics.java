package main;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import entity.Entity;

public class Physics {
	
	private static DiscreteDynamicsWorld world;
	
	private static CollisionConfiguration collisionConfig;
	private static BroadphaseInterface broadphaseInterface;
	private static Dispatcher dispatcher;
	private static ConstraintSolver constraintSolver;
	public static Vector3f gravity = new Vector3f(0,-9.8f,0);

	public static void init() {
		collisionConfig = new DefaultCollisionConfiguration();
		broadphaseInterface = new DbvtBroadphase();
		dispatcher = new CollisionDispatcher(collisionConfig);
		constraintSolver = new SequentialImpulseConstraintSolver();
		world = new DiscreteDynamicsWorld(dispatcher, broadphaseInterface, constraintSolver, collisionConfig);
		world.setGravity(gravity);
	}
	
	public static void addEntity(Entity entity) {
		
	}

}
