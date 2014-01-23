package org.jessifin.graphics;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ComponentBar extends Component {
	
	ComponentBox outsideBox, insideBox;
	ComponentText title;
	float width;
	float percent = 1;
	float x1,x2,y1,y2;

	public ComponentBar(String text, float x1, float y1, float x2, float y2, float border, Color4f insideColor, Color4f outsideColor) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		insideBox = new ComponentBox(x1,y1,x2,y2,0,insideColor);
		
		outsideBox = new ComponentBox(x1-border,y1-border,x2+border,y2+border,0,outsideColor);
		
		this.width = border;
				
		float centerX = x1+(x2-x1)/2f;
		float lengthOfString = text.length()*Graphics.charWidth;
		float leftBoundText = centerX - lengthOfString/2f;
		
		title = new ComponentText(text,
		new Vector3f(leftBoundText+Graphics.charWidth/2f,y2+Graphics.charHeight+.1f,0),
		new Vector3f(0,0,0),
		new Vector3f(Graphics.charWidth,1,1),
		outsideColor,
		false);
	}
	
	public void setColor(Color4f insideColor, Color4f outsideColor) {
		insideBox.color = insideColor;
		outsideBox.color = outsideColor;
	}
	
	public void render() {
		outsideBox.x1 = x1 - width;
		outsideBox.y1 = y1 - width;
		outsideBox.x2 = x2 + width;
		outsideBox.y2 = y2 + width;
		
		insideBox.x2 = ((outsideBox.x2 - outsideBox.x1) - 2*width) * percent + insideBox.x1;
		
		insideBox.render();
		outsideBox.render();
		title.render();
	}

}
