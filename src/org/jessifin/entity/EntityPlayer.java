package org.jessifin.entity;

import org.jessifin.audio.Audio;
import org.jessifin.game.Game;
import org.jessifin.model.ModelData;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;

public class EntityPlayer extends Entity {
	
	public EntityPlayer() {
		super("poogeon.dae");
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

	public void onCollide(Entity e, ManifoldPoint[] contactPoints) {
		boolean hitsound = false;
		for(ManifoldPoint contactPoint: contactPoints) {
			if(contactPoint.appliedImpulse > 500) {
				hitsound = true;
				break;
			}
		}
		if(hitsound) {
			Audio.playAtEntity("hit.wav", this, 1);
		}
	}
	
	public void update() {
		if(health == 0) {
			Game.reboot();
		}
	}

}