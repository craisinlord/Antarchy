#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    color.a *= 1.0 - linear_fog_value(vertexDistance, 0.0, FogCloudsEnd);
    fragColor = color;
}
