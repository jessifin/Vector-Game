package org.jessifin.model;

import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

import org.jessifin.main.Util;

public class ModelParser {
	
	private static HashMap<String,Model[]> loadedModels = new HashMap<String,Model[]>();

	public static Model[] getModel(String loc) {
		return (loadedModels.containsKey(loc)) ? loadedModels.get(loc) : parseModel(loc);
	}
	
	private static Model[] parseModel(String loc) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch(ParserConfigurationException exception) { 
			exception.printStackTrace();
		}
		
		Document document = null;
		try {
			document = builder.parse(new File("res/model/daes/" + loc));
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

				modelData[(i-1)/2].rot = new Vector3f(xRot,zRot,-yRot);
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
			document = builder.parse(new File("res/model/" + loc));
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
				
				Model model = new Model(id+"%"+modelData[i].name, vaoID, vertexID, indexID, modelData[i].indices.length, modelData[i]);
				model.pos = modelData[i].pos;
				model.rot = modelData[i].rot;
				model.scale = modelData[i].scale;
				model.calculateMatrix();
				models[i] = model;
			}
			
			loadedModels.put(id, models);
			return models;
		}
	}

	public static void clearModelMap() {
		//Why you gotta use set and collection? Make up your mind, Oracle.
		Iterator<String> keys = loadedModels.keySet().iterator();
		Iterator<Model[]> values = loadedModels.values().iterator();
		while(keys.hasNext()) {
			System.out.println("Deleting model " + keys.next());
			for(Model m: values.next()) {
				System.out.println('\t' + m.name);
				glDeleteBuffers(m.vertexID);
				glDeleteBuffers(m.indexID);
				GL30.glDeleteVertexArrays(m.vaoID);
			}
		}
	}
}
