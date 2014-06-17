#version 400

layout (location = 0) in vec4 vertPos;

uniform float time = 0.0;
uniform float freq = 1.0;

uniform mat4 modelViewProjectionMatrix;

void main() {
    gl_Position = modelViewProjectionMatrix * vertPos;
    /*
    if (time == 0.0) {
        gl_Position = projectViewModelMat * vertPos;
    } else {
        vec4 pos = vertPos;
        pos.x += sin(pos.z*6.28+time*freq)*0.05;
        pos.y += sin(pos.x*6.28+time*freq)*sin(pos.z*6.28+time*freq)*0.05;
        pos.z += sin(pos.z*6.28+time*freq)*0.05;
        gl_Position = projectViewModelMat * pos;
    }
    */
}