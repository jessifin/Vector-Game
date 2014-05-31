#version 400

layout (location = 0) in vec4 vertPos;
//layout (location = 1) in int[] groups;
//layout (location = 2) in float[] weights;

uniform float time = 0.0;
uniform float freq = 1.0;

uniform mat4 modelViewProjectionMatrix;

uniform int num_bones;
uniform mat4[] bones;

void main() {
    vec4 pos = (num_bones == 0) ? vertPos : bones[0] * vertPos;
    gl_Position = modelViewProjectionMatrix * pos;
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