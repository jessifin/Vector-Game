#version 400

layout (triangles) in;
layout (triangle_strip, max_vertices = 6) out;

uniform float lineWidth = 1.0;

void main() {
    gl_PrimitiveID = gl_PrimitiveIDIn;
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();
}