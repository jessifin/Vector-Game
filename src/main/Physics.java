package main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import model.Model;
import model.ModelData;
import model.ModelParser;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
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
import com.bulletphysics.util.ObjectArrayList;

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
	
	public static BoxShape createBox(Vector3f lengths) {
		BoxShape shape = new BoxShape(lengths);
		return shape;
	}
	
	public static SphereShape createSphere(float radius) {
		SphereShape shape = new SphereShape(radius);
		return shape;
	}
	
	public static StaticPlaneShape createPlane(Vector3f normal) {
		StaticPlaneShape shape = new StaticPlaneShape(normal, 1);
		return shape;
	}
	
	public static CompoundShape combineShapes(Vector3f[] trans, CollisionShape[] shapes) {
		CompoundShape compoundShape = new CompoundShape();
		Transform t = new Transform();
		for(int i = 0; i < shapes.length; i++) {
			t.origin.set(trans[i]);
			compoundShape.addChildShape(t, shapes[i]);
		}
		return compoundShape;
	}
	
	
	public static RigidBody createRigidBody(float mass, float restitution, Transform transform, CollisionShape shape) {		
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
	
	public static void addSphere(Entity entity, float mass, float rest, float frict, float radius) {
		CollisionShape shape = createSphere(radius);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(frict);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		

		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void addPlane(float rest, float frict, Vector3f pos, Quat4f rot) {
		CollisionShape shape = createPlane(new Vector3f(0,1,0));
		Transform transform = new Transform();
		transform.setRotation(rot);
		
		RigidBody body = createRigidBody(0,rest,transform,shape);
		body.setFriction(frict);
		body.translate(pos);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		

		world.addRigidBody(body);
	}
	
	public static void addPlane(Entity entity, float rest, float frict, float height) {
		CollisionShape shape = createPlane(new Vector3f(0,1,0));
		Transform transform = new Transform();
		transform.setIdentity();
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(0,rest,transform,shape);
		body.setFriction(frict);
		body.translate(entity.pos);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		

		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void addBox(Entity entity, float mass, float rest, float frict) {
		CollisionShape shape = createBox(new Vector3f(
				entity.scale.x,
				entity.scale.y,
				entity.scale.z));
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(frict);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		

		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void addBox(Vector3f pos, float mass, float rest, float frict, Vector3f lengths) {
		CollisionShape shape = createBox(lengths);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(frict);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		

		world.addRigidBody(body);
	}
	
	public static void addEntity(Entity entity, float mass, float rest) {
		CollisionShape shape = createMesh(entity.model[0]);
		shape.setLocalScaling(entity.scale);
		shape.calculateLocalInertia(mass, new Vector3f(0,0,0));
		/*
		MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0,0,0,1),new Vector3f(0,0,0), 1)));
		RigidBodyConstructionInfo construction = new RigidBodyConstructionInfo(5, motionState, shape, new Vector3f(0,0,0));
		construction.restitution = .5f;
		shape.calculateLocalInertia(5, new Vector3f(10,0,0));
		*/
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		
		entity.body = body;
	}
	
	public static void applyImpulse(Entity entity, Vector3f impulse) {
		if(entity.body != null) {
			entity.body.applyCentralImpulse(impulse);
		}
	}
	
	public static void update(int timePassed) {
		world.stepSimulation((Game.speed<=0.05f)?0:timePassed*10);
	//	System.out.println((Game.speed<=0.05f)?0:(timePassed/Game.speed)/2 + " " + Game.speed + " " + timePassed);
		
		OverlappingPairCache cache = broadphaseInterface.getOverlappingPairCache();
		ObjectArrayList<BroadphasePair> pairs = cache.getOverlappingPairArray();
		for(BroadphasePair pair: pairs) {
			
		}
		
		for(Entity e: Game.entities) {
			if(e.body != null ) {
				Transform transform = new Transform();
				e.pos = e.body.getWorldTransform(transform).origin;
				e.body.getInterpolationLinearVelocity(e.vel);
				Matrix3f rotMat = transform.basis;
				Vector3f rot = new Vector3f();
				rot.y = (float)Math.asin(rotMat.getElement(2,0));
				float c = (float) Math.cos(rot.y);
				if(Math.abs(c) > .005f) {
					float TRX = rotMat.getElement(2,2)/c;
					float TRY = rotMat.getElement(2,1)/c;
					rot.x = (float)Math.atan2(TRY,TRX);
					
					TRX = rotMat.getElement(0,0)/c;
					TRY = rotMat.getElement(1,0)/c;
					rot.z = (float)Math.atan2(TRY,TRX);
				} else {
					rot.x = 0;
					float TRX = rotMat.getElement(1,1);
					float TRY = rotMat.getElement(1,0);
					rot.z = (float)Math.atan2(TRY,TRX);
				}
				e.rot = rot;
				//printMatrix(rotMat);
				/*
				Matrix3f xRotMat = new Matrix3f(new float[] {
						1, 0, 0,
						0, (float) Math.cos(rot.x), - (float) Math.sin(rot.x),
						0, (float) Math.sin(rot.x), (float) Math.cos(rot.x),
				});
				
				Matrix3f yRotMat = new Matrix3f(new float[] {
						(float) Math.cos(rot.y), 0, (float) Math.sin(rot.y),
						0, 1, 0,
						- (float) Math.sin(rot.y), 0, (float) Math.cos(rot.y),
				});
				
				Matrix3f zRotMat = new Matrix3f(new float[] {
						(float) Math.cos(rot.z), - (float) Math.sin(rot.z), 0,
						(float) Math.sin(rot.z), (float) Math.cos(rot.z), 0,
						0, 0, 1,
				});
				xRotMat.mul(yRotMat); xRotMat.mul(zRotMat);
				printMatrix(xRotMat);
				*/
			}
		}
	}
	
	public static void printMatrix(Matrix3f mat) {
		for(int y = 0; y < 3; y++) {
			for(int x = 0; x < 3; x++) {
				System.out.print(mat.getElement(x,y) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void destroy() {
		world.destroy();
	}
	
	/**
	 * @param pos is the center of the box
	 */
	public static boolean intersectsBox(Vector3f aabbMin, Vector3f aabbMax, Entity e) {
		Vector3f eaabbMin = new Vector3f(); Vector3f eaabbMax = new Vector3f();
		e.body.getAabb(eaabbMin, eaabbMax);
		
		if((((aabbMin.x < eaabbMin.x && aabbMax.x > eaabbMin.x) || (aabbMin.x < eaabbMax.x && aabbMax.x > eaabbMin.x))) &&
			(((aabbMin.y < eaabbMin.y && aabbMax.y > eaabbMin.y) || (aabbMin.y < eaabbMax.y && aabbMax.y > eaabbMin.y))) &&
			((aabbMin.z < eaabbMin.z && aabbMax.z > eaabbMin.z) || (aabbMin.z < eaabbMax.z && aabbMax.z > eaabbMin.z)))
				return true;
		
		return false;
	}
	
	public static void raytest(Vector3f rayFromWorld, boolean first) {
		
		
	}
}
