package graphics;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ComponentBar extends Component {
	
	ComponentBox outsideBox, insideBox;
	ComponentText title;
	float width;
	float percent = 1;
	float x1,x2,y1,y2;

	public ComponentBar(String text, float x1, float y1, float x2, float y2, float width, Color4f insideColor, Color4f outsideColor) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		insideBox = new ComponentBox(x1,y1,x2,y2,0,insideColor);
		
		outsideBox = new ComponentBox(x1-width,y1-width,x2+width,y2+width,0,outsideColor);
		
		this.width = width;
		
		title = new ComponentText(text,new Vector3f(x1+(x2-x1)/2-text.length()/4f,y1+2.12f,0),new Vector3f(0,0,0), new Vector3f(1,0.8f,1),outsideColor);
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
