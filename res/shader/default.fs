#version 400

out vec4 fragColor;

uniform vec4 color;
uniform int stride = 10;

void main() {
    float faceColor = 0.5 + (gl_PrimitiveID % stride) / (2.0 * float(stride));
    vec4 damp = vec4(faceColor, faceColor, faceColor, 1.0);
    fragColor = color * damp;
}