#version 400

out vec4 fragColor;

uniform vec4 color;
uniform int stride = 10;
uniform int offset = 0;
uniform int deformColor = 0;

void main() {
    float faceColor = (deformColor==0) ? 0.5 + ((gl_PrimitiveID + offset) % stride) / (2.0 * float(stride)):1;
    vec4 damp = vec4(faceColor, faceColor, faceColor, 1.0);
    fragColor = color * damp;
}