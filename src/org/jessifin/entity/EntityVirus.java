package org.jessifin.entity;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.jessifin.main.Util;
import org.jessifin.model.Bone;
import org.jessifin.model.Model;

public class EntityVirus extends Entity {

	public EntityVirus() {
		super("virus.dae");
		/*
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
		*/
	}

	public void onCollide(Entity e) {
		
	}
	
	public void update() {
		
	}

}
