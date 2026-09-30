#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

noperspective in vec2 portalUv;
out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, portalUv);
    fragColor = vec4(color.rgb, 1.0) * ColorModulator;
}
