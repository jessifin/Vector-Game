package model;

import static org.lwjgl.opengl.GL15.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import main.Util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModelParser {
	
	private static Hashtable<String,Model[]> loadedModels = new Hashtable<String,Model[]>();

	public static Model[] getModel(String loc) {
		return (loadedModels.containsKey(loc)) ? (loadedModels.get(loc)) : parseModel(loc);
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
			Node meshNode = nodes.item(i).getFirstChild().getNextSibling();

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
			short[] cookedIndices = new short[halfBakedIndices.length/2]; //Because normals are included with the indices
			for(int j = 0; j < cookedIndices.length; j+=2) {
				cookedIndices[j/2] = Short.valueOf(halfBakedIndices[j]);
			}

			ModelData currentData = new ModelData(cookedVertices, cookedIndices);
			modelData[i] = currentData;
		}
		
		return buildModel(modelData);
	}
	
	public static Model[] buildModel(ModelData[] modelData) {
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
			
			Model model = new Model(vertexID, indexID, modelData[i].indices.length);
			models[i] = model;
		}
		
		return models;
	}
}
