package graphics;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ComponentButton extends Component {

	ComponentBox body;
	ComponentText title;
	float x1, y1, x2, y2;

	public ComponentButton(String text, float x1, float y1, float x2, float y2, Color4f color) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		
		body = new ComponentBox(x1,y1,x2,y2,0,color);
		
		float centerX = (x2-x1)/2f + x1;
		float centerY = (y2-y1)/2f + y1;
		
		float scaleX = (x2-x1)/(text.length()/Graphics.charWidth);
			
		title = new ComponentText(text, new Vector3f(x1,y1,0), new Vector3f(0,0,0), new Vector3f(scaleX,1,1), new Color4f(1,1,1,1));
	}

	public void render() {
		body.render();
		title.render();
	}

}
