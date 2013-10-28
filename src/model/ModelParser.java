package model;

import static org.lwjgl.opengl.GL15.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
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

import main.Util;

public class ModelParser {
	
	private static HashMap<String,Model[]> loadedModels = new HashMap<String,Model[]>();

	public static Model[] getModel(String loc) {
		return (loadedModels.containsKey(loc)) ? (loadedModels.get(loc)) : (loc.endsWith(".dae") ? parseModel(loc) : parseHeightmap(loc));
	}
	
	private static Model[] parseHeightmap(String loc) {
		BufferedImage map = null;
		try {
			map = ImageIO.read(new File("res/model/" + loc));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		/*
		int width = map.getWidth(); int height = map.getHeight();
		
		float[] vertices = new float[width * height];
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				vertices[width * y + x] = map.getRGB(x,y) / Integer.MAX_VALUE;
			}
		}
		*/
		return null;
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
			
			float[] vertices = Util.toArray(vertexNode.getTextContent());
			short[] indices = Util.toArray(indexNode.getTextContent(), 0, 2);
			
			ModelData currentData = new ModelData(name, vertices, indices);
			modelData[i] = currentData;
		}
		
		NodeList transforms = document.getElementsByTagName("visual_scene").item(0).getChildNodes();
		
		for(int i = 1; i < transforms.getLength(); i+=2) {
			NodeList children = transforms.item(i).getChildNodes();
			
			float[] pos = Util.toArray(children.item(1).getTextContent());
			modelData[(i-1)/2].pos = new Vector3f(pos);
			
			float[] scale = Util.toArray(children.item(9).getTextContent());
			modelData[(i-1)/2].scale = new Vector3f(scale);
			
			float zRot = Float.valueOf(children.item(3).getTextContent().split(" ")[3]);
			float yRot = Float.valueOf(children.item(5).getTextContent().split(" ")[3]);
			float xRot = Float.valueOf(children.item(7).getTextContent().split(" ")[3]);
			modelData[(i-1)/2].rot = new Vector3f(xRot,yRot,zRot);
			
			System.out.println(xRot + " " + yRot + " " + zRot);
			
			/*
			for(int j = 1; j < children.getLength() - 2; j+=2) {
				String scrap = children.item(j).getTextContent();
				
				if(j == 1 || j == 9) {
					Vector3f data = new Vector3f();
					String[] refined = scrap.split(" ");
					float[] reclaimed = new float[refined.length];
					for(int k = 0; k < reclaimed.length; k++) {
						reclaimed[k] = Float.valueOf(refined[k]);
					}
					data = new Vector3f(reclaimed);
					
					if(j == 1) {
						modelData[(i-1)/2].pos = data;
					} else {
						modelData[(i-1)/2].scale = data;
					}
				} else {
					float data = Float.valueOf(scrap.split(" ")[3]) * 3.14159f / 180;
					switch(j) {
					case 3: modelData[(i-1)/2].rot.z = data; break;
					case 5: modelData[(i-1)/2].rot.y = data; break;
					case 7: modelData[(i-1)/2].rot.x = data; break;
					default: break;
					}
				}
			}
			*/
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
			model.pos = modelData[i].pos;
			model.rot = modelData[i].rot;
			model.scale = modelData[i].scale;
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
