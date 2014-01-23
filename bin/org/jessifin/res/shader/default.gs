#version 400

layout (triangles) in;
layout (triangle_strip, max_vertices = 6) out;

uniform float lineWidth = 1.0;

void main() {
        vec3 up = vec3(0.0, lineWidth, 0.0);
        vec3 normal0 = normalize(gl_in[0].gl_Position.xyz - gl_in[1].gl_Position.xyz);
        vec4 tangent0 = vec4(cross(normal0, up), 1.0);
        
        gl_PrimitiveID = gl_PrimitiveIDIn;
        gl_Position = gl_in[0].gl_Position;
        EmitVertex();
        gl_Position = gl_in[1].gl_Position;
        EmitVertex();
        gl_Position = gl_in[0].gl_Position + vec4(0.0, lineWidth/2.0, 0.0, 1.0);
        EmitVertex();
        EndPrimitive();
    
    gl_PrimitiveID = gl_PrimitiveIDIn;
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position + vec4(0.0, lineWidth/2.0, 0.0, 1.0);
    EmitVertex();
    EndPrimitive();
    
        /*
        gl_PrimitiveID = gl_PrimitiveIDIn;
        gl_Position = gl_in[1].gl_Position;
        EmitVertex();
        gl_Position = gl_in[2].gl_Position;
        EmitVertex();
        EndPrimitive();
    
        gl_PrimitiveID = gl_PrimitiveIDIn;
        gl_Position = gl_in[0].gl_Position;
        EmitVertex();
        gl_Position = gl_in[2].gl_Position;
        EmitVertex();
        EndPrimitive();
        */
}