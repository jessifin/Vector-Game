package entity;

import javax.vecmath.Matrix4f;

import model.Bone;
import model.Model;
import model.ModelData;
import model.ModelParser;

public class EntityPlayer extends Entity {

	public EntityPlayer() {
		super("hoovy.dae");
		/*
		Matrix4f[] matrices = ModelParser.getArmature("characterWithMesh.dae");
		
		rootBone = new Bone();
		rootBone.model = 																		model[0];
		rootBone.model.matrix = 																matrices[0];
			rootBone.children = new Bone[4];
			rootBone.children[0] = new Bone();
			rootBone.children[0].model = 														model[1];
			rootBone.children[0].model.matrix = 												matrices[1];
				rootBone.children[0].children = new Bone[1];
				rootBone.children[0].children[0] = new Bone();
				rootBone.children[0].children[0].model = 										model[2];
				rootBone.children[0].children[0].model.matrix = 								matrices[2];
					rootBone.children[0].children[0].children = new Bone[1];
					rootBone.children[0].children[0].children[0] = new Bone();
					rootBone.children[0].children[0].children[0].model = 						model[3];
					rootBone.children[0].children[0].children[0].model.matrix = 				matrices[3];
			rootBone.children[1] = new Bone();
			rootBone.children[1].model = 														model[4];
			rootBone.children[1].model.matrix = 												matrices[4];
				rootBone.children[1].children = new Bone[1];
				rootBone.children[1].children[0] = new Bone();
				rootBone.children[1].children[0].model =										model[5];
				rootBone.children[1].children[0].model.matrix = 								matrices[5];
			rootBone.children[2] = new Bone();
			rootBone.children[2].model = 														model[6];
			rootBone.children[2].model.matrix = 												matrices[6];
				rootBone.children[2].children = new Bone[1];
				rootBone.children[2].children[0] = new Bone();
				rootBone.children[2].children[0].model = 										model[7];
				rootBone.children[2].children[0].model.matrix =									matrices[7];
			rootBone.children[3] = new Bone();
			rootBone.children[3].model =														model[8];
			rootBone.children[3].model.matrix = 												matrices[8];
				rootBone.children[3].children = new Bone[1];
				rootBone.children[3].children[0] = new Bone();
				rootBone.children[3].children[0].model =										model[9];
				rootBone.children[3].children[0].model.matrix =									matrices[9];
					//rootBone.children[3].children[0].children = new Bone[1];
					//rootBone.children[3].children[0].children[0] = new Bone();
					//rootBone.children[3].children[0].children[0].model =						model[10];
					//rootBone.children[3].children[0].children[0].model.matrix =					matrices[10];
	
		loopThroughBones(rootBone);
		*/
	}
	
	public EntityPlayer(ModelData[] modelData) {
		super(modelData);
	}
	
	public void update() {
		System.out.println(collisions + " " + physID);
	}

}