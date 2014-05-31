package org.jessifin.model;

import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.jessifin.main.Main;
import org.jessifin.main.Util;

public class ModelParser {
	
	private static Scanner scanner;
	
	private static HashMap<String,Model[]> loadedModels = new HashMap<String,Model[]>();

	public static Model[] getModel(String loc) {
		return (loadedModels.containsKey(loc)) ? loadedModels.get(loc) : parseModel(loc);
	}
	
	private static Model[] parseModel(String loc) {
		if(loc.endsWith(".dae")) {
			return parseCOLLADA(loc);
		} else if(loc.endsWith(".mesh")) {
			return parseMesh(loc);
		} else {
			System.err.println("Model format not supported.");
			return null;
		}
	}
	
	private static Model[] parseMesh(String loc) {
		try {
			scanner = new Scanner(new File(Main.resourceLoc, "model/meshes/" + loc));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ArrayList<ModelData> models = new ArrayList<ModelData>();
		
		while(scanner.hasNext()) {
			String[] data = new String[7];
			
			for(int i = 0; i < data.length; i++) {
				data[i] = scanner.nextLine();
			}
	
			String name = data[0];
			Vector3f pos = new Vector3f(Util.toArray(data[1]));
			Vector3f rot = new Vector3f(Util.toArray(data[2]));
			Vector3f scale = new Vector3f(Util.toArray(data[3]));
			Color4f color = new Color4f(Util.toArray(data[4]));
			float[] vertices = Util.toArray(data[5]);
			short[] indices = Util.toArray(data[6], 0, 1);
			
			Armature armature = null;
			
			if(name.startsWith("ARMATURE")) {
				String[] armData = new String[4];
				for(int i = 0; i < armData.length; i++) {
					armData[i] = scanner.nextLine();
				}
				short[] groupCounts = Util.toArray(armData[0], 0, 1);
				short[] groups = Util.toArray(armData[1], 0, 1);
				float[] weights = Util.toArray(armData[2]);
				int numBones = Integer.parseInt(armData[3]);
								
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
			
			ModelData modelData = new ModelData(name, vertices, indices, pos, rot, scale, color, armature);
			
			models.add(modelData);
		}
		
		ModelData[] modelDatae = new ModelData[models.size()];
		for(int i = 0; i < modelDatae.length; i++) {
			modelDatae[i] = models.get(i);
		}
		
		return buildModel(loc, modelDatae);
	}
	
	private static Model[] parseCOLLADA(String loc) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch(ParserConfigurationException exception) { 
			exception.printStackTrace();
		}
		
		Document document = null;
		try {
			document = builder.parse(new File(Main.resourceLoc, "model/daes/" + loc));
		} catch(IOException exception) {
			exception.printStackTrace();
		} catch(SAXException exception) {
			exception.printStackTrace();
		}
		
		document.getDocumentElement().normalize();
		
		NodeList nodes = document.getElementsByTagName("geometry");
		ModelData[] modelData = new ModelData[nodes.getLength()];
	
		for(int i = 0; i < nodes.getLength(); i++) {
			//Why aren't these enumerations? It's just annoying this way.
			Node geometryNode = nodes.item(i);

			String name = geometryNode.getAttributes().item(0).getNodeValue();
			
			Node meshNode = geometryNode.getFirstChild().getNextSibling();

			if(meshNode != null) {
				Node vertexNode = meshNode.getChildNodes().item(1).getChildNodes().item(1);
				Node indexNode = meshNode.getChildNodes().item(7).getChildNodes().item(7);
				
				float[] vertices = Util.toArray(vertexNode.getTextContent());
				short[] indices = Util.toArray(indexNode.getTextContent(), 0, 2);
				float[] fixedVerts = new float[vertices.length];
				for(int j = 0; j < vertices.length; j++) {
					if(j%3==0) {
						fixedVerts[j] = vertices[j];
					} else if(j%3==1) {
						fixedVerts[j] = vertices[j+1];
					} else {
						fixedVerts[j] = -vertices[j-1];
 					}
				}
				
				ModelData currentData = new ModelData(name, fixedVerts, indices);
				modelData[i] = currentData;
			} else {
				float[] verts = {-.5f,-.5f,0,-.5f,.5f,0,.5f,-.5f,0,.5f,.5f,0};
				short[] inds = {0,1,2,1,2,3};
				ModelData currentData = new ModelData("missingno%missingno",verts,inds);
				modelData[i] = currentData;
			}
		}
		
		if(modelData.length == 0) {
			modelData = new ModelData[1];
			float[] verts = {-.5f,-.5f,0,-.5f,.5f,0,.5f,-.5f,0,.5f,.5f,0};
			short[] inds = {0,1,2,1,2,3};
			ModelData currentData = new ModelData("missingno%missingno",verts,inds);
			modelData[0] = currentData;
		}
		
		NodeList transforms = document.getElementsByTagName("visual_scene").item(0).getChildNodes();
	
		for(int i = 1; i < transforms.getLength(); i+=2) {
			if(!transforms.item(i).getAttributes().item(0).getNodeValue().equals("Armature")) {
				NodeList children = transforms.item(i).getChildNodes();
				float[] rawPos = Util.toArray(children.item(1).getTextContent());
				float[] pos = {rawPos[0], rawPos[2], -rawPos[1]};
				modelData[(i-1)/2].pos = new Vector3f(pos);
				
				float[] rawScale = Util.toArray(children.item(9).getTextContent());
				float[] scale = {rawScale[0],rawScale[2],rawScale[1]};
				modelData[(i-1)/2].scale = new Vector3f(scale);
				
				float xRot = Float.valueOf(children.item(3).getTextContent().split(" ")[3]) * 0.0174533f;
				float yRot = Float.valueOf(children.item(5).getTextContent().split(" ")[3]) * 0.0174533f;
				float zRot = Float.valueOf(children.item(7).getTextContent().split(" ")[3]) * 0.0174533f;

				modelData[(i-1)/2].rot = new Vector3f(xRot,-zRot,-yRot);
			}
		}

		return buildModel(loc, modelData);
	}
	
	public static Matrix4f[] getArmature(String loc) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch(ParserConfigurationException exception) { 
			exception.printStackTrace();
		}
		
		Document document = null;
		try {
			document = builder.parse(new File(Main.resourceLoc,"model/" + loc));
		} catch(IOException exception) {
			exception.printStackTrace();
		} catch(SAXException exception) {
			exception.printStackTrace();
		}
		
		document.getDocumentElement().normalize();
		
		NodeList matrices = document.getElementsByTagName("matrix");
		
		Matrix4f[] data = new Matrix4f[matrices.getLength()];
		for(int i = 0; i < matrices.getLength(); i++) {
			Matrix4f mat = new Matrix4f(Util.toArray(matrices.item(i).getTextContent()));
			mat.transpose();
			data[i] = mat;
		}
		
		return data;
	}
	
	public static Model[] buildModel(String id, ModelData[] modelData) {
		if(loadedModels.containsKey(id)) {
			return loadedModels.get(id);
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
				model.armature = modelData[i].armature;
				model.calculateMatrix();
				models[i] = model;
			}
			
			loadedModels.put(id, models);
			return models;
		}
	}

	public static void clearModelMap() {
		Iterator<String> keys = loadedModels.keySet().iterator();
		Iterator<Model[]> values = loadedModels.values().iterator();
		while(keys.hasNext()) {
			System.out.println("Deleting model " + keys.next());
			for(Model m: values.next()) {
				//System.out.println('\t' + m.name);
				glDeleteBuffers(m.vertexID);
				glDeleteBuffers(m.indexID);
				GL30.glDeleteVertexArrays(m.vaoID);
			}
		}
	}
}