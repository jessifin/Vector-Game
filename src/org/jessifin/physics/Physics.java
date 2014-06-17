package org.jessifin.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.jessifin.main.Util;
import org.jessifin.model.Model;
import org.jessifin.model.ModelData;
import org.jessifin.model.ModelParser;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.IDebugDraw;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import org.jessifin.audio.Audio;
import org.jessifin.entity.Entity;
import org.jessifin.game.Game;
import org.jessifin.graphics.gui.GUIHUD;

public class Physics {
	
	private static DiscreteDynamicsWorld world;
	
	private static CollisionConfiguration collisionConfig;
	private static BroadphaseInterface broadphaseInterface;
	private static Dispatcher dispatcher;
	private static ConstraintSolver constraintSolver;
	public static Vector3f gravity = new Vector3f(0,-9.8f,0);
	private static Vector3f worldCorner1 = new Vector3f(-5000,-5000,-5000);
	private static Vector3f worldCorner2 = new Vector3f(5000, 5000, 5000);
		
	private static HashMap<Model,HashMap<SimpleVector,CollisionShape>> collisionShapes = new HashMap<Model,HashMap<SimpleVector,CollisionShape>>();

	private static HashMap<RigidBody,Entity> matches = new HashMap<RigidBody,Entity>();

	public static void init(boolean infiniteWorld) {
		collisionConfig = new DefaultCollisionConfiguration();
		broadphaseInterface = infiniteWorld ? new DbvtBroadphase() : new AxisSweep3(worldCorner1,worldCorner2);
		dispatcher = new CollisionDispatcher(collisionConfig);
		constraintSolver = new SequentialImpulseConstraintSolver();
		world = new DiscreteDynamicsWorld(dispatcher, broadphaseInterface, constraintSolver, collisionConfig);
		world.setGravity(gravity);
		
		collisionShapes = new HashMap<Model,HashMap<SimpleVector,CollisionShape>>();
	}
	
	
	public static RigidBody createRigidBody(Entity entity) {
		Transform transform = new Transform();
		transform.origin.set(entity.pos);
				
		CollisionShape shape = getShape(entity.model, entity.scale);
				
		RigidBodyData data = entity.model.data.rigidBodyData;
		RigidBody body = createRigidBody(data.mass, data.restitution, data.friction, transform, shape);
		
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		
		matches.put(body,entity);
		
		return body;
	}

	private static RigidBody createRigidBody(float mass, float restitution, float friction, Transform transform, CollisionShape shape) {		
		Vector3f localInertia = new Vector3f(0,0,0);
		if(mass != 0) {
			shape.calculateLocalInertia(mass, localInertia);
		}
		
		DefaultMotionState motionState = new DefaultMotionState(transform);
		
		RigidBodyConstructionInfo constructInfo = new RigidBodyConstructionInfo(mass, motionState, shape, localInertia);
		constructInfo.restitution = restitution;
		constructInfo.friction = friction;

		RigidBody body = new RigidBody(constructInfo);
		
		return body;
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
	
	private static CollisionShape getShape(Model model, Vector3f scale) {
		if(collisionShapes.containsKey(model)) {
			if(collisionShapes.get(model).containsKey(new SimpleVector(scale.x,scale.y,scale.z))) {
				return collisionShapes.get(model).get(new SimpleVector(scale.x,scale.y,scale.z));
			} else {
				CollisionShape shape = createShape(model);
				shape.setLocalScaling(scale);
				collisionShapes.get(model).put(new SimpleVector(scale.x,scale.y,scale.z), shape);
				return shape;
			}
		} else {
			CollisionShape shape = createShape(model);
			shape.setLocalScaling(scale);
			HashMap<SimpleVector,CollisionShape> hash = new HashMap<SimpleVector,CollisionShape>();
			hash.put(new SimpleVector(scale.x,scale.y,scale.z), shape);
			collisionShapes.put(model, hash);
			return shape;
		}
	}
	
	private static CollisionShape createShape(Model model) {
		CollisionShape shape = null;
		if(model.data != null && model.data.rigidBodyData != null) {
			if(model.data.rigidBodyData.collisionShape.equals("MESH")) {
				shape = createMesh(model);
			} else if(model.data.rigidBodyData.collisionShape.equals("CONE")) {
				shape = new ConeShape(Math.max(model.data.dimensions.x, model.data.dimensions.z), model.data.dimensions.y);
			} else if(model.data.rigidBodyData.collisionShape.equals("CYLINDER")) {
				shape = new CylinderShape(new Vector3f(model.data.dimensions.x/2f,model.data.dimensions.y/2f,model.data.dimensions.z/2f));
			} else if(model.data.rigidBodyData.collisionShape.equals("CAPSULE")) {
				shape = new CapsuleShape(Math.max(model.data.dimensions.x, model.data.dimensions.z), model.data.dimensions.y);
			} else if(model.data.rigidBodyData.collisionShape.equals("SPHERE")) {
				shape = new SphereShape(Math.max(Math.max(model.data.dimensions.x, model.data.dimensions.y), model.data.dimensions.z));
			} else if(model.data.rigidBodyData.collisionShape.equals("BOX")) {
				shape = new BoxShape(new Vector3f(model.data.dimensions.x/2f,model.data.dimensions.y/2f,model.data.dimensions.z/2f));
			} else if(model.data.rigidBodyData.collisionShape.equals("CONVEX_HULL")) {
				ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();
				for(int i = 0; i < model.data.vertices.length / 3; i++) {
					Vector3f point = new Vector3f(model.data.vertices[3 * i], model.data.vertices[3 * i + 1], model.data.vertices[3 * i + 2]);
					points.add(point);
				}
				shape = new ConvexHullShape(points);
			}
		}

		return shape;
	}
	
	public static void applyImpulse(Entity entity, Vector3f impulse) {
		if(entity.body != null) {
			entity.body.applyCentralImpulse(impulse);
		}
	}
	
	public static Entity conductRaycast(Vector3f begin, Vector3f end) {
		CollisionWorld.ClosestRayResultCallback result = new CollisionWorld.ClosestRayResultCallback(begin, end);
		if(result.hasHit()) {
			Object collisionObject = result.collisionObject;
			return matches.get((RigidBody)collisionObject);
		} else {
			return null;
		}
	}

	public static void update(int timePassed) {
		world.stepSimulation((Game.speed<=0)?0:timePassed);

		for(Entity e: Game.entities) {
			if(e.body != null ) {
				e.collisions.clear();
				Transform transform = new Transform();
				e.pos = e.body.getWorldTransform(transform).origin;
				e.body.getInterpolationLinearVelocity(e.vel);
				Matrix3f rotMat = transform.basis;
				e.rot = Util.getRotation(rotMat);
			}
		}
		
		ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>();
		
		OverlappingPairCache cache = broadphaseInterface.getOverlappingPairCache();
		ObjectArrayList<BroadphasePair> pairs = cache.getOverlappingPairArray();
		
		for(BroadphasePair pair: pairs) {
			RigidBody r0 = (RigidBody) pair.pProxy0.clientObject;
			RigidBody r1 = (RigidBody) pair.pProxy1.clientObject;
			Entity p0 = matches.get(r0);
			Entity p1 = matches.get(r1);
			if(p0 != null && p1 != null) {
				p0.collisions.add(p1);
				p1.collisions.add(p0);
				ObjectArrayList<PersistentManifold> manifolds = new ObjectArrayList<PersistentManifold>();
				pair.algorithm.getAllContactManifolds(manifolds);
				for(PersistentManifold manifold: manifolds) {
					ManifoldPoint[] contactPoints = new ManifoldPoint[manifold.getNumContacts()];
					for(int i = 0; i < contactPoints.length; i++) {
						contactPoints[i] = manifold.getContactPoint(i);
						ManifoldPoint contactPoint = manifold.getContactPoint(i);
						//This is temporary
						if(contactPoint.appliedImpulse > 100) {
							Audio.playAtEntity("hit.wav", p0, 1);
							if(p0.health > 0) {
								p0.health-=5;
							} else {
								if(!p0.isAlive) {
									p0.isAlive = false;
									entitiesToRemove.add(p0);
								}
							}
							if(p1.health > 5) {
								p1.health-=5;
							} else {
								if(p1.isAlive) {
									p1.isAlive = false;
									entitiesToRemove.add(p1);
								}
							}
						}
					}
					p0.onCollide(p1,contactPoints);
					p1.onCollide(p0,contactPoints);
				}
			}
		}
		for(Entity e: entitiesToRemove) {
			matches.remove(e.body);
			world.removeRigidBody(e.body);
			Game.entities.remove(e);
		}
	}
	
	public static void destroy() {
		world.destroy();
		Iterator<Entry<Model,HashMap<SimpleVector,CollisionShape>>> iter0 = collisionShapes.entrySet().iterator();
		while(iter0.hasNext()) {
			Entry<Model,HashMap<SimpleVector,CollisionShape>> entry0 = iter0.next();
			Iterator<Entry<SimpleVector,CollisionShape>> iter1 = entry0.getValue().entrySet().iterator();
			while(iter1.hasNext()) {
				Entry<SimpleVector,CollisionShape> entry1 = iter1.next();
				System.out.println(entry0.getKey().name + " " + entry1.getKey() + " " + entry1.getValue());
			}
		}
	}
	
	private static class SimpleVector {
		private float x,y,z;
		public SimpleVector(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public boolean equals(Object o) {
			SimpleVector v = (SimpleVector)o;
			return v.x == this.x && v.y == this.y && v.z == this.z;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode(); 
		}
		
		@Override
		public String toString() {
			return "(" + x + ", " + y + ", " + z + ")";
		}
	}
}
