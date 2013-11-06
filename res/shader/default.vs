#version 400

layout (location = 0) in vec4 vertPos;

uniform mat4 projectMat, viewMat, modelMat;

void main() {
	gl_Position = projectMat * modelMat * vertPos;
}