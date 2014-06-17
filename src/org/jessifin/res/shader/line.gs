#version 400

layout (triangles) in;
layout (line_strip, max_vertices = 6) out;

uniform float lineWidth = 1.0;
uniform float alpha = 1.0;
uniform int stride = 10;
uniform int offset = 0;
uniform bool deformColor;
uniform vec4[2] color;
out vec4 face_color;

void main() {
    gl_PrimitiveID = gl_PrimitiveIDIn;
    face_color = color[0] * vec4(vec3(deformColor ? 1.0 : 0.5 + 0.5 * ((gl_PrimitiveID + offset) % stride) / float(stride)), alpha);
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    EndPrimitive();
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();
}