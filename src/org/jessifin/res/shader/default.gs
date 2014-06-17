#version 400

layout (triangles) in;
layout (triangle_strip, max_vertices = 6) out;

uniform float time = 0.0;
uniform float freq = 1.0;
uniform float lineWidth = 1.0;
uniform int stride = 10;
uniform int offset = 0;
uniform bool deformColor;
uniform vec4[2] color;

uniform float width;
uniform float height;
uniform bool useTex;
uniform sampler2D tex;

out vec4 face_color;

vec4 wave(vec4 pos) {
    pos.x += sin(pos.z*6.28+time*freq)*0.05;
    pos.y += sin(pos.x*6.28+time*freq)*sin(pos.z*6.28+time*freq)*0.05;
    return pos;
}

void main() {
    gl_PrimitiveID = gl_PrimitiveIDIn;
    face_color = (useTex ? texture(tex, vec2((gl_PrimitiveIDIn % int(width)) / width, height - (float(gl_PrimitiveIDIn + 1) / (width * height)))) : vec4(1.0)) * vec4(vec3(deformColor ? 1.0 : 0.5 + 0.5 * ((gl_PrimitiveID + offset) % stride) / float(stride)), 1.0) * color[0];
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();
}