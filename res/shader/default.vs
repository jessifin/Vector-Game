#version 150 core

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
in vec4 in_Pos;

in vec4 in_Color;
out vec4 out_Color;


void main() {
	//gl_Position = modelMatrix * projectionMatrix * viewMatrix * pos;
    gl_Position = in_Pos;//ftransform();
    out_Color = in_Color;
}