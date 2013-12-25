package graphics;

import javax.vecmath.Color4f;

public class ComponentBox extends Component {
	
	public float x1, y1, x2, y2, rotation;
	public Color4f color = new Color4f(1,1,1,1);

	public ComponentBox(float x1, float y1, float x2, float y2, float rotation, Color4f color) {
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
		this.rotation = rotation;
		this.color = color;
	}
	
	public void render() {
		Graphics.renderBox(x1, y1, x2, y2, rotation, color);
	}

}
