#version 400

layout (triangles) in;
layout (triangle_strip, max_vertices = 6) out;

uniform vec3 camPos = vec3(0.0, 0.0, 0.0);

uniform float lineWidth = 1.0;

struct Line {
    int primitiveID;
    vec4[2] points;
};

struct Triangle {
    int primitiveID;
    vec4[3] points;
};

struct Quad {
    int primitiveID;
    vec4[4] points;
};

void drawTriangle(Triangle t) {
    gl_PrimitiveID = t.primitiveID;
    gl_Position = t.points[0];
    EmitVertex();
    gl_Position = t.points[1];
    EmitVertex();
    gl_Position = t.points[2];
    EmitVertex();
    EndPrimitive();
}

Triangle[2] decompose(Quad q) {
    Triangle[2] tris;
    tris[0] = Triangle(q.primitiveID, vec4[3] (q.points[0], q.points[1], q.points[2]));
    tris[1] = Triangle(q.primitiveID, vec4[3] (q.points[1], q.points[3], q.points[2]));
    return tris;
}

Quad compose(Line line, vec4 up, float width) {
    vec4 l0 = line.points[0]; vec4 l1 = line.points[1];
    
    vec4 q0 = l0 + up * width / 2.0; vec4 q1 = l0 - up * width / 2.0;
    vec4 q2 = l1 + up * width / 2.0; vec4 q3 = l1 - up * width / 2.0;
    
    Quad quad = Quad(line.primitiveID, vec4[4] (q0, q1, q2, q3));
    return quad;
}

void drawQuad(Quad q) {
    Triangle[2] tris = decompose(q);
    for(int i = 0; i < tris.length(); i++) {
        drawTriangle(tris[0]);
    }
}

void main() {
    gl_PrimitiveID = gl_PrimitiveIDIn;
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();
    /*
    if(lineWidth == 0.0) {
        gl_PrimitiveID = gl_PrimitiveIDIn;
        gl_Position = gl_in[0].gl_Position;
        EmitVertex();
        gl_Position = gl_in[1].gl_Position;
        EmitVertex();
        gl_Position = gl_in[2].gl_Position;
        EmitVertex();
        EndPrimitive();
    } else {
        Line line0 = Line(gl_PrimitiveIDIn, vec4[2] (gl_in[0].gl_Position, gl_in[1].gl_Position));
        Quad quad0 = compose(line0, vec4(0.0, 1.0, 0.0, 1.0), 1.0);
        drawQuad(quad0);
        
        Line line1 = Line(gl_PrimitiveIDIn, vec4[2] (gl_in[1].gl_Position, gl_in[2].gl_Position));
        Quad quad1 = compose(line1, vec4(0.0, 1.0, 0.0, 1.0), 1.0);
        drawQuad(quad1);
        
        Line line2 = Line(gl_PrimitiveIDIn, vec4[2] (gl_in[0].gl_Position, gl_in[2].gl_Position));
        Quad quad2 = compose(line2, vec4(0.0, 1.0, 0.0, 1.0), 1.0);
        drawQuad(quad2);
    }
    
    vec3 u = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
    vec3 v = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
    
    vec3 normal = cross(u,v);
    */
}