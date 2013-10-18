package model;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
			short[] cookedIndices = new short[halfBakedIndices.length/2];
			for(int j = 0; j < cookedIndices.length; j+=2) {
				cookedIndices[j/2] = Short.valueOf(halfBakedIndices[j]);
			}

			ModelData currentData = new ModelData(cookedVertices, cookedIndices);
			modelData[i] = currentData;
		}
		
		return buildModel(modelData);
	}
	
	public static Model[] buildModel(ModelData[] modelData) {
		
	}
}
