package org.jessifin.model;

import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import org.jessifin.main.Main;
import org.jessifin.main.Util;
import org.jessifin.physics.RigidBodyData;

public class ModelParser {
	
	private static Scanner scanner;
	
	private static HashMap<String,Model[]> parsedModels = new HashMap<String,Model[]>();

	public static Model[] getModel(String loc) {
		return parsedModels.containsKey(loc) ? parsedModels.get(loc) : parseModel(loc);
	}
	
	private static Model[] parseModel(String loc) {
		try {
			scanner = new Scanner(new File(Main.resourceLoc, "model/" + loc + ".mesh"));
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
		
		ArrayList<ModelData> models = new ArrayList<ModelData>();
		
		while(scanner.hasNext()) {
			String[] data = new String[8];
			
			for(int i = 0; i < data.length; i++) {
				data[i] = scanner.nextLine();
			}
	
			String name = data[0];
			Vector3f pos = new Vector3f(Util.toArray(data[1]));
			Vector3f rot = new Vector3f(Util.toArray(data[2]));
			Vector3f scale = new Vector3f(Util.toArray(data[3]));
			Vector3f dimensions = new Vector3f(Util.toArray(data[4]));
			Color4f color = new Color4f(Util.toArray(data[5]));
			float[] vertices = Util.toArray(data[6]);
			short[] indices = Util.toArray(data[7], 0, 1);
			
			RigidBodyData rigidBodyData = null;
			
			if(name.contains("RIGIDBODY")) {
				String[] rigidBodyInfo = new String[4];
				for(int i = 0; i < rigidBodyInfo.length; i++) {
					rigidBodyInfo[i] = scanner.nextLine();
				}
				float friction = Float.parseFloat(rigidBodyInfo[0]);
				float restitution = Float.parseFloat(rigidBodyInfo[1]);
				float mass = Float.parseFloat(rigidBodyInfo[2]);
				String collisionShape = rigidBodyInfo[3];
				rigidBodyData = new RigidBodyData(friction, restitution, mass, collisionShape);
			}
			
			Armature armature = null;

			if(name.contains("ARMATURE")) {
				String[] armInfo = new String[4];
				for(int i = 0; i < armInfo.length; i++) {
					armInfo[i] = scanner.nextLine();
				}
				short[] groupCounts = Util.toArray(armInfo[0], 0, 1);
				short[] groups = Util.toArray(armInfo[1], 0, 1);
				float[] weights = Util.toArray(armInfo[2]);
				int numBones = Integer.parseInt(armInfo[3]);
								
				Bone[] bones = new Bone[numBones];
				String[] rawParents = new String[numBones];
				String[] rawChildren = new String[numBones];
				
				for(int i = 0; i < numBones; i++) {
					String[] info = new String[6];
					for(int j = 0; j < info.length; j++) {
						info[j] = scanner.nextLine();
					}
					rawParents[i] = info[1];
					rawChildren[i] = info[2];
			
					Vector3f head = new Vector3f(Util.toArray(info[3]));
					Vector3f tail = new Vector3f(Util.toArray(info[4]));
					Matrix4f matrix = new Matrix4f(Util.toArray(info[5]));
					bones[i] = new Bone(info[0], head, tail, matrix);
				}
				
				ArrayList<Bone> rawRootBones = new ArrayList<Bone>();
				
				for(int i = 0; i < numBones; i++) {
					if(!rawParents[i].equals("NULL")) {
						for(int j = 0; j < numBones; j++) {
							if(rawParents[i].equals(bones[j].name)) {
								bones[i].parent = bones[j];
								break;
							}
						}
					} else {
						rawRootBones.add(bones[i]);
					}
					if(!rawChildren.equals("NULL")) {
						String[] children = rawChildren[i].split(" ");
						bones[i].children = new Bone[children.length];
						for(int j = 0; j < children.length; j++) {
							for(int k = 0; k < numBones; k++) {
								if(children[j].equals(bones[k].name)) {
									bones[i].children[j] = bones[k];
									break;
								}
							}
						}
					}
				}
				
				Bone[] rootBones = new Bone[rawRootBones.size()];
				for(int i = 0; i < rootBones.length; i++) {
					rootBones[i] = rawRootBones.get(i);
				}
				
				armature = new Armature(groupCounts, groups, weights, bones, rootBones);
			}
			
			ModelData modelData = new ModelData(name, vertices, indices, pos, rot, scale, dimensions, color, rigidBodyData, armature);
			
			models.add(modelData);
		}
		
		ModelData[] modelDatae = new ModelData[models.size()];
		for(int i = 0; i < modelDatae.length; i++) {
			modelDatae[i] = models.get(i);
		}
		
		return buildModel(loc, modelDatae);
	}
	
	public static Model[] buildModel(String id, ModelData... modelData) {
		if(parsedModels.containsKey(id)) {
			return parsedModels.get(id);
		} else {
			System.out.println("Loading model: " + id);
						
			Model[] models = new Model[modelData.length];
			
			for(int i = 0; i < models.length; i++) {
				FloatBuffer vertexData = Util.toBuffer(modelData[i].vertices);
				ShortBuffer indexData = Util.toBuffer(modelData[i].indices);
				
				int vaoID = GL30.glGenVertexArrays();
				GL30.glBindVertexArray(vaoID);
				
				int vertexID = glGenBuffers();
				glBindBuffer(GL_ARRAY_BUFFER, vertexID);
				glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
				//Attribute 0 is for the position input to the shader
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				GL30.glBindVertexArray(0);
								
				int indexID = glGenBuffers();
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID);
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);	
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
				
				Model model = new Model(id + "%" + modelData[i].name, vaoID, vertexID, indexID, modelData[i].indices.length, modelData[i]);
				model.pos = modelData[i].pos;
				model.rot = modelData[i].rot;
				model.scale = modelData[i].scale;
				model.colorFill = modelData[i].color;
				Util.calculateMatrix(model.matrix, model.pos, model.rot, model.scale);
				models[i] = model;
			}
									
			parsedModels.put(id, models);
			return models;
		}
	}

	public static void clearModelMap() {
		Iterator<String> keys = parsedModels.keySet().iterator();
		Iterator<Model[]> values = parsedModels.values().iterator();
		while(keys.hasNext()) {
			System.out.println("Deleting model " + keys.next());
			for(Model m: values.next()) {
				glDeleteBuffers(m.vertexID);
				glDeleteBuffers(m.indexID);
				GL30.glDeleteVertexArrays(m.vaoID);
			}
		}
	}
}