package org.jessifin.model;

public class Armature {
	
	public RiggedVertex[] vertices;
	public Bone[] bones;
	public Bone[] rootBones;
	
	public Armature(short[] groupCounts, short[] groups, float[] weights, Bone[] bones, Bone[] rootBones) {
		short[][] vertexGroups = new short[groupCounts.length][];
		float[][] vertexWeights = new float[groupCounts.length][];
		for(int i = 0; i < groupCounts.length; i++) {
			vertexGroups[i] = new short[groupCounts[i]];
			vertexWeights[i] = new float[groupCounts[i]];
		}
		
		int index = 0;
		for(int i = 0; i < vertexGroups.length; i++) {
			for(int j = 0; j < vertexGroups[i].length; j++) {
				vertexGroups[i][j] = groups[index];
				vertexWeights[i][j] = weights[index];
				index++;
			}
		}
		
		vertices = new RiggedVertex[groupCounts.length];
		for(int i = 0; i < vertices.length; i++) {
			vertices[i] = new RiggedVertex(vertexGroups[i], vertexWeights[i]);
		}
		
		this.bones = bones;
		this.rootBones = rootBones;
	}

	public class RiggedVertex {
		SkinningInfo[] skinningInfo;
		public RiggedVertex(short[] vertexGroups, float[] vertexWeights) {
			skinningInfo = new SkinningInfo[vertexGroups.length];
			for(int i = 0; i < vertexGroups.length; i++) {
				skinningInfo[i] = new SkinningInfo(vertexGroups[i], vertexWeights[i]);
			}
		}
		public class SkinningInfo {
			short vertexGroup;
			float vertexWeight;
			public SkinningInfo(short vertexGroup, float vertexWeight) {
				this.vertexGroup = vertexGroup;
				this.vertexWeight = vertexWeight;
			}
		}
	}
	
}