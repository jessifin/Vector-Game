package entity;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import main.Util;
import model.Bone;
import model.Model;

public class EntityVirus extends Entity {

	public EntityVirus() {
		super("virus.dae");
		for(Model m: model) {
			m.colorFill = new Color4f(0.1f,0.9f,0.05f,1);
		}
		
		model[0].pos.x = 100;
		model[0].pos.y = 100;
		model[0].pos.z = 100;
		
		rootBone = new Bone();
		rootBone.model = 															model[0];
		System.out.println(rootBone.model.pos.x);
		rootBone.model.calculateMatrix();
			rootBone.children = new Bone[1];
			rootBone.children[0] = new Bone();
			rootBone.children[0].model = 											model[1];
			rootBone.children[0].model.calculateMatrix();
				rootBone.children[0].children = new Bone[1];
				rootBone.children[0].children[0] = new Bone();
				rootBone.children[0].children[0].model = 							model[2];
				rootBone.children[0].children[0].model.calculateMatrix();
					rootBone.children[0].children[0].children = new Bone[1];
					rootBone.children[0].children[0].children[0] = new Bone();
					rootBone.children[0].children[0].children[0].model = 			model[3];
					rootBone.children[0].children[0].children[0].model.calculateMatrix();
		
		loopThroughBones(rootBone);
	}
	
	public void update() {
		
	}

}
