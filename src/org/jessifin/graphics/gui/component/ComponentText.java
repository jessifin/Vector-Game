package org.jessifin.graphics.gui.component;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.jessifin.graphics.Graphics;

public class ComponentText extends Component {
	
	String text;
	Vector3f pos, rot, scale;
	Color4f color;
	boolean renderBox;

	public ComponentText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color, boolean renderBox) {
		this.text = text;
		this.pos = pos;
		this.rot = rot;
		this.scale = scale;
		this.color = color;
		this.renderBox = renderBox;
	}
	
	public void render() {
		Graphics.renderText(text, pos, rot, scale, color,renderBox);
	}

}
