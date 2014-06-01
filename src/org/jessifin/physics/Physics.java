package org.jessifin.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.jessifin.main.Util;
import org.jessifin.model.Model;
import org.jessifin.model.ModelData;
import org.jessifin.model.MeshParser;

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
import org.jessifin.graphics.GUIHUD;

public class Physics {
	
	private static DiscreteDynamicsWorld world;
	
	private static CollisionConfiguration collisionConfig;
	private static BroadphaseInterface broadphaseInterface;
	private static Dispatcher dispatcher;
	private static ConstraintSolver constraintSolver;
	public static Vector3f gravity = new Vector3f(0,-9.8f,0);
	private static Vector3f worldCorner1 = new Vector3f(-5000,-5000,-5000);
	private static Vector3f worldCorner2 = new Vector3f(5000, 5000, 5000);
	
	private static HashMap<Float,SphereShape> sphereShapes;
	private static HashMap<Vector3f,BoxShape> boxShapes;
	private static HashMap<Vector3f,StaticPlaneShape> planeShapes;
	private static HashMap<Model,CollisionShape> meshShapes;

	private static HashMap<RigidBody,Entity> matches = new HashMap<RigidBody,Entity>();

	public static void init(boolean infiniteWorld) {
		collisionConfig = new DefaultCollisionConfiguration();
		//I should consider using an Axis Sweep instead of the default broadphase
		broadphaseInterface = infiniteWorld ? new DbvtBroadphase() : new AxisSweep3(worldCorner1,worldCorner2);
		dispatcher = new CollisionDispatcher(collisionConfig);
		constraintSolver = new SequentialImpulseConstraintSolver();
		world = new DiscreteDynamicsWorld(dispatcher, broadphaseInterface, constraintSolver, collisionConfig);
		world.setGravity(gravity);
		
		sphereShapes = new HashMap<Float,SphereShape>();
		boxShapes = new HashMap<Vector3f,BoxShape>();
		planeShapes = new HashMap<Vector3f,StaticPlaneShape>();
		meshShapes = new HashMap<Model,CollisionShape>();
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

	public static CompoundShape combineShapes(Transform[] transforms, CollisionShape[] shapes) {
		CompoundShape compoundShape = new CompoundShape();
		for(int i = 0; i < shapes.length; i++) {
			compoundShape.addChildShape(transforms[i], shapes[i]);
		}
		return compoundShape;
	}
	
	public static CollisionShape createShape(Model[] model) {
		ArrayList<CollisionShape> shapes = new ArrayList<CollisionShape>();
		ArrayList<Transform> trans = new ArrayList<Transform>();
		for(int m = 0; m < model.length; m++) {
			CollisionShape shape = null;
			if(model[m].data.rigidBodyData.collisionShape.equals("MESH")) {
				shape = createMesh(model[m]);
			} else if(model[m].data.rigidBodyData.collisionShape.equals("CONE")) {
				shape = new ConeShape(Math.max(model[m].data.dimensions.x, model[m].data.dimensions.z), model[m].data.dimensions.y);
			} else if(model[m].data.rigidBodyData.collisionShape.equals("CYLINDER")) {
				shape = new CylinderShape(new Vector3f(model[m].data.dimensions.x/2f,model[m].data.dimensions.y/2f,model[m].data.dimensions.z/2f));
			} else if(model[m].data.rigidBodyData.collisionShape.equals("CAPSULE")) {
				shape = new CapsuleShape(Math.max(model[m].data.dimensions.x, model[m].data.dimensions.z), model[m].data.dimensions.y);
			} else if(model[m].data.rigidBodyData.collisionShape.equals("SPHERE")) {
				shape = new SphereShape(Math.max(Math.max(model[m].data.dimensions.x, model[m].data.dimensions.y), model[m].data.dimensions.z));
			} else if(model[m].data.rigidBodyData.collisionShape.equals("BOX")) {
				shape = new BoxShape(new Vector3f(model[m].data.dimensions.x/2f,model[m].data.dimensions.y/2f,model[m].data.dimensions.z/2f));
			} else if(model[m].data.rigidBodyData.collisionShape.equals("CONVEX_HULL")) {
				shape = new ConvexHullShape(null);
			}
			
			if(shape != null) {
				shapes.add(shape);
				Transform transform = new Transform();
				Matrix4f mat = new Matrix4f();
				Util.calculateMatrix(mat, model[m].pos, model[m].rot, new Vector3f(1,1,1));
				trans.add(transform);
			}
		}
		
		if(shapes.size() == 0) {
			return null;
		} else if(shapes.size() == 1) {
			return shapes.get(0);
		} else {
			CollisionShape[] shapesArray = (CollisionShape[]) shapes.toArray();
			Transform[] transArray = (Transform[]) trans.toArray();
			return combineShapes(transArray, shapesArray);
		}
		
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
		SphereShape shape;
		if(sphereShapes.containsKey(radius)) {
			shape = sphereShapes.get(radius);
		} else {
			System.out.println("Creating new Sphere Shape: " + radius);
			shape = new SphereShape(radius);
			sphereShapes.put(radius, shape);
		}
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(frict);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		entity.body = body;
		matches.put(body,entity);
	}
	
	public static void addPlane(float rest, float frict, Vector3f pos, Quat4f rot) {
		CollisionShape shape = new StaticPlaneShape(new Vector3f(0,1,0),1);
		Transform transform = new Transform();
		transform.setRotation(rot);
		
		RigidBody body = createRigidBody(0,rest,transform,shape);
		body.setFriction(frict);
		body.translate(pos);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		world.addRigidBody(body);
	}
	
	public static void addPlane(Entity entity, float rest, float frict, float height) {
		CollisionShape shape = new StaticPlaneShape(new Vector3f(0,1,0),1);
		Transform transform = new Transform();
		transform.setIdentity();
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(0,rest,transform,shape);
		body.setFriction(frict);
		body.translate(entity.pos);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		entity.body = body;
		matches.put(body,entity);
	}
	
	public static void addBox(Entity entity, float mass, float rest, float frict) {
		Vector3f halfExtents = new Vector3f(
				entity.scale.x/2f,
				entity.scale.y/2f,
				entity.scale.z/2f);
		BoxShape shape;
		if(boxShapes.containsKey(halfExtents)) {
			shape = boxShapes.get(halfExtents);
		} else {
			System.out.println("Creating new Box Shape: " + halfExtents);
			shape = new BoxShape(halfExtents);
			boxShapes.put(halfExtents, shape);
		}
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setFriction(frict);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		entity.body = body;
		matches.put(body,entity);
	}
	
	public static void addBox(Vector3f pos, float mass, float rest, float frict, Vector3f halfExtents) {
		BoxShape shape;
		if(boxShapes.containsKey(halfExtents)) {
			shape = boxShapes.get(halfExtents);
		} else {
			System.out.println("Creating new Box Shape: " + halfExtents);
			shape = new BoxShape(halfExtents);
			boxShapes.put(halfExtents, shape);
		}
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
		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(entity.pos.x,entity.pos.y,entity.pos.z);
		transform.setRotation(new Quat4f(0,0,0,1));
		
		CollisionShape[] shapes = new CollisionShape[entity.mesh.model.length];
		Transform[] positions = new Transform[entity.mesh.model.length];
		float[] masses = new float[entity.mesh.model.length];
		for(int i = 0; i < shapes.length; i++) {
			shapes[i] = createMesh(entity.mesh.model[i]);
			Transform tempTransform = new Transform();
			tempTransform.origin.set(entity.mesh.model[i].pos);
			positions[i] = new Transform();
			masses[i] = 5;
		}
		
		
		CompoundShape pootis = combineShapes(positions,shapes);
		pootis.calculatePrincipalAxisTransform(masses, transform, new Vector3f(0,0,0));
		
		CollisionShape shape;
		if(meshShapes.containsKey(entity.mesh.model[0])) {
			shape = meshShapes.get(entity.mesh.model[0]);
		} else {
			System.out.println("Creating Mesh Shape " + entity.mesh.model[0].name);
			shape = createMesh(entity.mesh.model[0]);
			meshShapes.put(entity.mesh.model[0], shape);
		}
		
		shape.setLocalScaling(entity.scale);
		shape.calculateLocalInertia(mass, new Vector3f(0,0,0));

		RigidBody body = createRigidBody(mass,rest,transform,shape);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);		
		world.addRigidBody(body);
		
		entity.body = body;
		matches.put(body,entity);
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
		world.stepSimulation((Game.speed<=0)?0:timePassed*10);

		for(Entity e: Game.entities) {
			if(e.body != null ) {
				e.collisions.clear();
				Transform transform = new Transform();
				e.pos = e.body.getWorldTransform(transform).origin;
				e.body.getInterpolationLinearVelocity(e.vel);
				Matrix3f rotMat = transform.basis;
				e.rot = Util.getRotation(rotMat);
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
						if(contactPoint.appliedImpulse > 200) {
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
}
