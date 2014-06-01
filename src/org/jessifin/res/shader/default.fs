#version 400

out vec4 frag_color;

uniform vec4[2] color;
uniform float alpha = 1.0;
uniform int stride = 10;
uniform int offset = 0;
uniform bool deformColor;

void main() {
    frag_color = color[/*gl_PrimitiveID % 2*/0] * vec4(vec3(deformColor ? 1.0 : 0.5 + 0.5 * ((gl_PrimitiveID + offset) % stride) / float(stride)), alpha);
}