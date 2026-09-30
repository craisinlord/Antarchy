#version 150

in vec3 Position;
in vec2 UV0;

noperspective out vec2 portalUv;

void main() {
    gl_Position = vec4(Position, 1.0);
    portalUv = UV0;
}
