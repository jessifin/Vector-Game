package model;

import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Iterator;

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

import main.Util;

public class ModelParser {
	
	private static HashMap<String,Model[]> loadedModels = new HashMap<String,Model[]>();

	public static Model[] getModel(String loc) {
		return (loadedModels.containsKey(loc)) ? (loadedModels.get(loc)) : parseModel(loc);
	}
	
	private static Model[] parseModel(String loc) {
		System.out.println("Loading model: " + loc);
		
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
		
		NodeList nodes = document.getElementsByTagName("geometry");
		ModelData[] modelData = new ModelData[nodes.getLength()];
	
		for(int i = 0; i < nodes.getLength(); i++) {
			//Why aren't these enumerations? It's just annoying this way.
			Node geometryNode = nodes.item(i);
			String name = geometryNode.getAttributes().item(0).getNodeValue();
			
			Node meshNode = geometryNode.getFirstChild().getNextSibling();

			Node vertexNode = meshNode.getChildNodes().item(1).getChildNodes().item(1);
			Node indexNode = meshNode.getChildNodes().item(7).getChildNodes().item(7);
			
			String rawVertices = vertexNode.getTextContent();
			String[] mediumRareVertices = rawVertices.split(" ");
			float[] cookedVertices = new float[mediumRareVertices.length];
			for(int j = 0; j < cookedVertices.length; j++) {
				cookedVertices[j] = Float.valueOf(mediumRareVertices[j]);
			}
			
			String rawIndices = indexNode.getTextContent();
			String[] halfBakedIndices = rawIndices.split(" ");
			short[] cookedIndices = new short[halfBakedIndices.length/2]; //Because normals are paired with the indices
			for(int j = 0; j < cookedIndices.length; j+=2) {
				cookedIndices[j/2] = Short.valueOf(halfBakedIndices[j]);
			}

			ModelData currentData = new ModelData(name, cookedVertices, cookedIndices);
			modelData[i] = currentData;
		}
		
		NodeList transforms = document.getElementsByTagName("visual_scene").item(0).getChildNodes();
		
		for(int i = 1; i < transforms.getLength(); i++) {
			Node pos = transforms.item(i).getFirstChild();

			System.out.println(pos.getNodeName());
		}
		return buildModel(loc, modelData);
	}
	
	public static Model[] buildModel(String id, ModelData[] modelData) {
		Model[] models = new Model[modelData.length];
		
		for(int i = 0; i < models.length; i++) {
			FloatBuffer vertexData = Util.toBuffer(modelData[i].vertices);
			ShortBuffer indexData = Util.toBuffer(modelData[i].indices);
			
			int vertexID = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vertexID);
			glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
			
			int indexID = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexID);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);			
			
			Model model = new Model(modelData[i].name, vertexID, indexID, modelData[i].indices.length);
			models[i] = model;
		}
		
		loadedModels.put(id, models);
		return models;
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
			}
		}
	}
}
