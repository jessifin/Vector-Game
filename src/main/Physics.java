package main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import model.Model;
import model.ModelData;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import entity.Entity;
import game.Game;

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
			ModelData data = entity.model[0].data;
			
			ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(data.vertices.length * 4).order(ByteOrder.nativeOrder());
			for(int i = 0; i < data.vertices.length; i++) {
				vertexBuffer.putFloat(data.vertices[i]);
			}
			vertexBuffer.flip();
			
			ByteBuffer indexBuffer = ByteBuffer.allocateDirect(data.indices.length * 4).order(ByteOrder.nativeOrder());
			for(int i = 0; i < data.indices.length; i++) {
				indexBuffer.putInt((int)data.indices[i]);
			}
			indexBuffer.flip();
			
			TriangleIndexVertexArray triangleInfo = new TriangleIndexVertexArray(
					data.indices.length/3, indexBuffer, 4 * 3, data.vertices.length, vertexBuffer, 4 * 3);
			
			CollisionShape shape = new SphereShape(5);//new BvhTriangleMeshShape(triangleInfo, true);
			
			MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0,0,0,1),new Vector3f(0,0,0), 1)));
			RigidBodyConstructionInfo construction = new RigidBodyConstructionInfo(5, motionState, shape, new Vector3f(0,0,0));
			construction.restitution = .5f;
			shape.calculateLocalInertia(5, new Vector3f(10,0,0));
			
			RigidBody body = new RigidBody(construction);
			
			world.addRigidBody(body);
			
			entity.body = body;
	}
	
	public static void update(int timePassed) {
		world.stepSimulation(timePassed);
		
		for(Entity e: Game.entities) {
		//	e.pos = e.body.getWorldTransform(new Transform()).origin;
		}
	}
	
	public static void destroy() {
		world.destroy();
	}

}
