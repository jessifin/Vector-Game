package main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import model.Model;
import model.ModelData;
import model.ModelParser;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
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
import graphics.GUIHUD;

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
	
	private static BvhTriangleMeshShape createMesh(Model model) {
		ModelData data = model.data;
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
		
		BvhTriangleMeshShape shape = new BvhTriangleMeshShape(triangleInfo, true);
		
		return shape;
	}
	
	private static BoxShape createBox(Vector3f lengths) {
		BoxShape shape = new BoxShape(lengths);
		return shape;
	}
	
	private static SphereShape createSphere(float radius) {
		SphereShape shape = new SphereShape(radius);
		return shape;
	}
	
	private static RigidBody createRigidBody(float mass, float restitution, Transform transform, CollisionShape shape) {		
		Vector3f localInertia = new Vector3f(0,0,0);
		if(mass != 0) {
			shape.calculateLocalInertia(mass, localInertia);
		}
		
		DefaultMotionState motionState = new DefaultMotionState(transform);
		
		RigidBodyConstructionInfo constructInfo = new RigidBodyConstructionInfo(mass, motionState, shape, localInertia);
		constructInfo.restitution = restitution;

		RigidBody body = new RigidBody(constructInfo);
		
		return body;
	}
	
	public static void addSphere(Entity entity, float mass, float rest, float radius) {
		CollisionShape shape = createSphere(radius);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(10);

		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void addBox(Entity entity, float mass, float rest, Vector3f lengths) {
		CollisionShape shape = createBox(lengths);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(10);

		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void addBox(Vector3f pos, float mass, float rest, Vector3f lengths) {
		CollisionShape shape = createBox(lengths);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(10);

		world.addRigidBody(body);
	}
	
	public static void addEntity(Entity entity, float mass, float rest) {
		
		CollisionShape shape = createMesh(entity.model[0]);
		/*
		MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0,0,0,1),new Vector3f(0,0,0), 1)));
		RigidBodyConstructionInfo construction = new RigidBodyConstructionInfo(5, motionState, shape, new Vector3f(0,0,0));
		construction.restitution = .5f;
		shape.calculateLocalInertia(5, new Vector3f(10,0,0));
		*/
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(1,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		//body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void applyImpulse(Entity entity, Vector3f impulse) {
		if(entity.body != null) {
			entity.body.applyCentralImpulse(impulse);
		}
	}
	
	public static void update(int timePassed) {
		world.stepSimulation(timePassed);
		
		for(Entity e: Game.entities) {
			if(e.body != null ) {
				Transform transform = new Transform();
				e.pos = e.body.getWorldTransform(transform).origin;
				Quat4f quat = new Quat4f();
				e.body.getWorldTransform(transform).getRotation(quat);
				e.rot.x = quat.x;
				e.rot.y = quat.y;
				e.rot.z = quat.z;
			}
		}
	}
	
	public static void destroy() {
		world.destroy();
	}

}
