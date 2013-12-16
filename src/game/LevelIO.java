package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;

import main.Physics;
import model.Model;
import model.ModelParser;
import entity.Entity;
import entity.EntityPlayer;
import entity.EntityVirus;
import entity.Terrain;

public class LevelIO {
	
	public static void writeLevel(String loc, Level level) {
		PrintStream outputStream = null;
		try {
			outputStream = new PrintStream(new File("res/level/"+loc+".lvl"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(Entity e: level.entities) {
			outputStream.println(e.getClass().getSimpleName());
			outputStream.println("pos = " + e.pos.x + " " + e.pos.y + " " + e.pos.z);
			outputStream.println("rot = " + e.rot.x + " " + e.rot.y + " " + e.rot.z);
			outputStream.println("scale = " + e.scale.x + " " + e.scale.y + " " + e.scale.z);
			outputStream.println("colorf = " + e.colorFill.x + " " + e.colorFill.y + " " + e.colorFill.z + " " + e.colorFill.w);
			outputStream.println("colorl = " + e.colorLine.x + " " + e.colorLine.y + " " + e.colorLine.z + " " + e.colorLine.w);
			if(e.body != null) {
				String collisionShape =  e.body.getCollisionShape().getName();
				float mass = 1f/e.body.getInvMass();
				if(mass==Float.POSITIVE_INFINITY) {
					mass = 0;
				}
				float restitution = e.body.getRestitution();
				float friction = e.body.getFriction();
				String phys = "phys = " + collisionShape + " " + mass + " " + restitution + " " + friction + " ";
				if(collisionShape.equals("SPHERE")) {
					float radius = ((SphereShape)(e.body.getCollisionShape())).getRadius();
					outputStream.println(phys + radius);
				} else if(collisionShape.equals("BOX")) {
					Vector3f lengths = new Vector3f();
					((BoxShape)(e.body.getCollisionShape())).getHalfExtentsWithMargin(lengths);
					outputStream.println(phys + lengths.x + " " + lengths.y + " " + lengths.z);
				} else if(collisionShape.equals("BVHTRIANGLEMESH")) {
					outputStream.println(phys);
				}
			}
			outputStream.println("Model Count: " + e.model.length);
			for(int i = 0; i < e.model.length; i++) {
				String model = "model%" + i;
				outputStream.println(model +" = " + e.model[i].name);
				outputStream.println(model + ".pos = " + e.model[i].pos.x + " " + e.model[i].pos.y + " " + e.model[i].pos.z);
				outputStream.println(model + ".rot = " + e.model[i].rot.x + " " + e.model[i].rot.y + " " + e.model[i].rot.z);
				outputStream.println(model + ".scale = " + e.model[i].scale.x + " " + e.model[i].scale.y + " " + e.model[i].scale.z);
				outputStream.println(model + ".colorf = " + e.model[i].colorFill.x + " " + e.model[i].colorFill.y + " " + e.model[i].colorFill.z + " " + e.model[i].colorFill.w);
				outputStream.println(model + ".colorl = " + e.model[i].colorLine.x + " " + e.model[i].colorLine.y + " " + e.model[i].colorLine.z + " " + e.model[i].colorLine.w);
			}
			outputStream.println("end\n");
		}
		outputStream.close();
	}

	public static Level getLevel(String loc) {
		Scanner scan = null;
		try {
			scan = new Scanner(new File("res/level/"+loc+".lvl"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = "";
		ArrayList<Entity> entities = new ArrayList<Entity>();
		Entity currentEntity = null;
		boolean createTriangleMesh = false; float meshMass = 5; float meshRest = 0.1f; float meshFrict = 0;
		Model[] currentModels = new Model[1];
		while(scan.hasNext()) {
			line = scan.nextLine();
			if(line.equals("EntityPlayer")) {
				currentEntity = new EntityPlayer();
				Game.player = (EntityPlayer)currentEntity;
				continue;
			}
			if(line.equals("EntityVirus")) {
				currentEntity = new EntityVirus();
				continue;
			}
			if(line.equals("Terrain")) {
				currentEntity = new Terrain();
				continue;
			}
			if(line.equals("end")) {
				currentEntity.model = currentModels.clone();
				if(createTriangleMesh) {
					Physics.addEntity(currentEntity, meshMass, meshRest);
					createTriangleMesh = false;
				}
				entities.add(currentEntity);
				continue;
			}
			if(line.contains("Count")) {
				int count = Integer.valueOf(line.split(":")[1].replaceAll(" ", ""));
				currentModels = new Model[count];
				continue;
			}
			if(line.startsWith("model")) {
				String[] parts = line.split("=");
				
				if(parts[0].contains("%")) {
					
					String uberRawIndex = parts[0].split("%")[1];
					String rawIndex = uberRawIndex.split("\\.")[0];
					int index = Integer.valueOf(rawIndex.replaceAll(" ", ""));
					
					if(!parts[0].contains(".")) {
						String fileName = parts[1].split("%")[0];
						String modelName = parts[1].split("%")[1];
						fileName = fileName.replace(" ", "");
						Model[] model = ModelParser.getModel(fileName);
						currentModels[index] = model[0];
						for(Model tempModel: model) {
							if(tempModel.name.equals(fileName + "%" + modelName)) {
								currentModels[index] = new Model(tempModel);
								break;
							}
						}
						continue;
					} else {
						String type = parts[0].split("\\.")[1];
						if(type.startsWith("pos")) {
							String[] rawPos = parts[1].split(" ");
							if(rawPos[0].equals("")) {
								String[] refinedColor = new String[3];
								System.arraycopy(rawPos, 1, refinedColor, 0, 3);
								rawPos = refinedColor;
							}
							float[] pos = new float[3];
							for(int i = 0; i < 3; i++) {
								pos[i] = Float.valueOf(rawPos[i]);
							}
							currentModels[index].pos = new Vector3f(pos);
							continue;
						}
						if(type.startsWith("rot")) {
							String[] rawRot = parts[1].split(" ");
							if(rawRot[0].equals("")) {
								String[] refinedColor = new String[3];
								System.arraycopy(rawRot, 1, refinedColor, 0, 3);
								rawRot = refinedColor;
							}
							float[] rot = new float[3];
							for(int i = 0; i < 3; i++) {
								rot[i] = Float.valueOf(rawRot[i]);
							}
							currentModels[index].rot = new Vector3f(rot);
							continue;
						}
						if(type.startsWith("scale")) {
							String[] rawScale = parts[1].split(" ");
							if(rawScale[0].equals("")) {
								String[] refinedColor = new String[3];
								System.arraycopy(rawScale, 1, refinedColor, 0, 3);
								rawScale = refinedColor;
							}
							float[] scale = new float[3];
							for(int i = 0; i < 3; i++) {
								scale[i] = Float.valueOf(rawScale[i]);
							}
							currentModels[index].scale = new Vector3f(scale);
							continue;
						}
						if(type.startsWith("colorf")) {
							String[] rawColor = parts[1].split(" ");
							if(rawColor[0].equals("")) {
								String[] refinedColor = new String[4];
								System.arraycopy(rawColor, 1, refinedColor, 0, 4);
								rawColor = refinedColor;
							}
							float[] color = new float[4];
							for(int i = 0; i < 4; i++) {
								color[i] = Float.valueOf(rawColor[i]);
							}
							currentModels[index].colorFill = new Color4f(color);
							continue;
						}
						if(type.startsWith("colorl")) {
							String[] rawColor = parts[1].split(" ");
							if(rawColor[0].equals("")) {
								String[] refinedColor = new String[4];
								System.arraycopy(rawColor, 1, refinedColor, 0, 4);
								rawColor = refinedColor;
							}
							float[] color = new float[4];
							for(int i = 0; i < 4; i++) {
								color[i] = Float.valueOf(rawColor[i]);
							}
							currentModels[index].colorLine = new Color4f(color);
							continue;
						}
					}
				} else {
					currentEntity.model = ModelParser.getModel(parts[parts.length-1].replaceAll(" ",""));
					continue;
				}
			}
			if(line.startsWith("pos")) {
				String[] parts = line.split("=");
				String[] rawPos = parts[1].split(" ");
				if(rawPos[0].equals("")) {
					String[] refinedColor = new String[3];
					System.arraycopy(rawPos, 1, refinedColor, 0, 3);
					rawPos = refinedColor;
				}
				float[] pos = new float[3];
				for(int i = 0; i < 3; i++) {
					pos[i] = Float.valueOf(rawPos[i]);
				}
				currentEntity.pos = new Vector3f(pos);
				continue;
			}
			if(line.startsWith("rot")) {
				String[] parts = line.split("=");
				String[] rawRot = parts[1].split(" ");
				if(rawRot[0].equals("")) {
					String[] refinedColor = new String[3];
					System.arraycopy(rawRot, 1, refinedColor, 0, 3);
					rawRot = refinedColor;
				}
				float[] rot = new float[3];
				for(int i = 0; i < 3; i++) {
					rot[i] = Float.valueOf(rawRot[i]);
				}
				currentEntity.rot = new Vector3f(rot);
				continue;
			}
			if(line.startsWith("scale")) {
				String[] parts = line.split("=");
				String[] rawScale = parts[1].split(" ");
				if(rawScale[0].equals("")) {
					String[] refinedColor = new String[3];
					System.arraycopy(rawScale, 1, refinedColor, 0, 3);
					rawScale = refinedColor;
				}
				float[] scale = new float[3];
				for(int i = 0; i < 3; i++) {
					scale[i] = Float.valueOf(rawScale[i]);
				}
				currentEntity.scale = new Vector3f(scale);
				continue;
			}
			if(line.startsWith("colorf")) {
				String[] parts = line.split("=");
				String[] rawColor = parts[1].split(" ");
				if(rawColor[0].equals("")) {
					String[] refinedColor = new String[4];
					System.arraycopy(rawColor, 1, refinedColor, 0, 4);
					rawColor = refinedColor;
				}
				float[] color = new float[4];
				for(int i = 0; i < 4; i++) {
					color[i] = Float.valueOf(rawColor[i]);
				}
				currentEntity.colorFill = new Color4f(color);
				continue;
			}
			if(line.startsWith("colorl")) {
				String[] parts = line.split("=");
				String[] rawColor = parts[1].split(" ");
				if(rawColor[0].equals("")) {
					String[] refinedColor = new String[4];
					System.arraycopy(rawColor, 1, refinedColor, 0, 4);
					rawColor = refinedColor;
				}
				float[] color = new float[4];
				for(int i = 0; i < 4; i++) {
					color[i] = Float.valueOf(rawColor[i]);
				}
				currentEntity.colorLine = new Color4f(color);
				continue;
			}
			if(line.startsWith("phys")) {
				String type = "";
				if(line.split("=")[1].split(" ")[0].equals("")) {
					type = line.split("=")[1].split(" ")[1].replaceAll(" ", "");
				} else {
					type = line.split("=")[1].split(" ")[1].replaceAll(" ", "");
				}
				if(type.equals("SPHERE")) {
					String[] attributes = line.split("=")[1].split(" ");
					int start = 1;
					if(attributes[0].equals("")) {
						start = 2;
					}
					float mass = Float.valueOf(attributes[start].replaceAll(" ", ""));
					float restitution = Float.valueOf(attributes[start+1].replaceAll(" ",""));
					float friction = Float.valueOf(attributes[start+2].replaceAll(" ",""));
					float radius = Float.valueOf(attributes[start+3].replaceAll(" ",""));
					Physics.addSphere(currentEntity, mass, restitution, friction, radius);
				} else if(type.equals("BOX")) {
					String[] attributes = line.split("=")[1].split(" ");
					int start = 1;
					if(attributes[0].equals("")) {
						start = 2;
					}
					float mass = Float.valueOf(attributes[start].replaceAll(" ", ""));
					float restitution = Float.valueOf(attributes[start+1].replaceAll(" ",""));
					float friction = Float.valueOf(attributes[start+2].replaceAll(" ",""));
					float width = Float.valueOf(attributes[start+3].replaceAll(" ",""));
					float height = Float.valueOf(attributes[start+4].replaceAll(" ",""));
					float depth = Float.valueOf(attributes[start+5].replaceAll(" ",""));
					Vector3f lengths = new Vector3f(width,height,depth);
					currentEntity.scale = lengths;
					Physics.addBox(currentEntity, mass, restitution, friction);
				} else if(type.equals("BVHTRIANGLEMESH")) {
					createTriangleMesh = true;
					String[] attributes = line.split("=")[1].split(" ");
					int start = 1;
					if(attributes[0].equals("")) {
						start = 2;
					}
					meshMass = Float.valueOf(attributes[start].replaceAll(" ", ""));
					meshRest = Float.valueOf(attributes[start+1].replaceAll(" ",""));
					meshFrict = Float.valueOf(attributes[start+2].replaceAll(" ", ""));
				}
			}
		}
		Level level = new Level(entities);
		return level;
	}

}
