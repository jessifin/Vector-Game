package model;

import javax.vecmath.Color4f;

public class Model {

	public final int vertexID, indexID, indexCount;
	public int indicesToRender;
	
	public Color4f colorFill, colorLine;
	
	public Model(int vertexID, int indexID, int indexCount) {
		this.vertexID = vertexID;
		this.indexID = indexID;
		this.indexCount = indexCount;
		this.indicesToRender = indexCount;
	}
}
