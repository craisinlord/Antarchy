#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    float fogFade = linear_fog_value(vertexDistance, ModelOffset.x, ModelOffset.y);
    color.rgb = mix(color.rgb, FogColor.rgb, fogFade);
    color.a *= 1.0 - linear_fog_value(vertexDistance, 0.0, ModelOffset.y);
    fragColor = color;
}
