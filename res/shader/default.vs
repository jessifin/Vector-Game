#version 400

layout (location = 0) in vec4 vertPos;

uniform vec3 trans, rot, scale;

void main() {
    mat4 transMat = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, trans.x, trans.y, trans.z, 1.0);
    
    mat4 scaleMat = mat4(scale.x, 0.0, 0.0, 0.0, 0.0, scale.y, 0.0, 0.0, 0.0, 0.0, scale.z, 0.0, 0.0, 0.0, 0.0, 1.0);
    
    mat4 xRotMat = mat4(1.0, 0.0, 0.0, 0.0, 0.0, cos(rot.x), sin(rot.x), 0.0, 0.0, -sin(rot.x), cos(rot.x), 0.0, 0.0, 0.0, 0.0, 1.0);
    mat4 yRotMat = mat4(cos(rot.y), 0.0, -sin(rot.y), 0.0, 0.0, 1.0, 0.0, 0.0, sin(rot.y), 0.0, cos(rot.y), 0.0, 0.0, 0.0, 0.0, 1.0);
    mat4 zRotMat = mat4(cos(rot.z), sin(rot.z), 0.0, 0.0, -sin(rot.z), cos(rot.z), 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
    mat4 rotMat = xRotMat * yRotMat * zRotMat;
    
    mat4 modelMatrix = transMat * rotMat * scaleMat;
	gl_Position = modelMatrix * vertPos;
}