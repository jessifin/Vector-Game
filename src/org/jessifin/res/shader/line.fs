#version 400

out vec4 frag_color;
in vec4 face_color;

void main() {
    frag_color = face_color;
}