uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

uniform vec4 pos;

void main() {
	gl_Position = modelMatrix * projectionMatrix * viewMatrix * pos;
}