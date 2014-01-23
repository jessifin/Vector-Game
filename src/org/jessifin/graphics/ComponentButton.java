package org.jessifin.graphics;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ComponentButton extends Component {

	ComponentBox body;
	ComponentText title;
	float x1, y1, x2, y2;

	public ComponentButton(String text, float x1, float y1, float x2, float y2, Color4f color) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		
		body = new ComponentBox(x1,y1,x2,y2,0,color);
		
		float textLength = text.length() * Graphics.charWidth - Graphics.charWidth/2f;
		
		float centerX = ((x1>x2) ? x2 : x1) + Math.abs(x2-x1)/2f;
		float centerY = ((y1>y2) ? y2 : y1) + Math.abs(y2-y1)/2f;
				
		float scaleX = (x2-x1)/textLength*Graphics.charWidth;
		float scaleY = (y2-y1)/Graphics.charHeight;
		
		
		title = new ComponentText(text, new Vector3f(x1,y2,0), new Vector3f(0,0,0), new Vector3f(scaleX,scaleY,1), new Color4f(1-color.x,1-color.y,1-color.z,color.w),false);
	}

	public void render() {
		body.render();
		title.render();
	}

}
