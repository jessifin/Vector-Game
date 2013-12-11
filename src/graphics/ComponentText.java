package graphics;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

public class ComponentText extends Component {
	
	String text;
	Vector3f pos, rot, scale;
	Color4f color;

	public ComponentText(String text, Vector3f pos, Vector3f rot, Vector3f scale, Color4f color) {
		this.text = text;
		this.pos = pos;
		this.rot = rot;
		this.scale = scale;
		this.color = color;
	}
	
	public void render() {
		Graphics.renderText(text, pos, rot, scale, color);
	}

}
