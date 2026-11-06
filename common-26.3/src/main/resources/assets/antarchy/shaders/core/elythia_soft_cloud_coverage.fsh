#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform ElythiaSoftCloud {
    vec4 Clearings[8];
    vec4 FrameInfo;
    vec4 DepthInfo;
};

layout(location = 0) in float vWeight;
layout(location = 1) in float vShade;
layout(location = 2) in float vDistance;
layout(location = 3) in float vHaze;
layout(location = 4) in float vStep;

layout(location = 0) out vec4 fragColor;

void main() {
    ivec2 size = textureSize(InSampler, 0);
    ivec2 coord = clamp(ivec2(gl_FragCoord.xy * DepthInfo.xy), ivec2(0), size - ivec2(1));
    if (gl_FragCoord.z < texelFetch(InSampler, coord, 0).r) {
        discard;
    }
    float coverage = vWeight * vStep;
    float weight = coverage * exp2(-vDistance / 120.0) * 64.0;
    fragColor = vec4(weight * vShade, weight, coverage, weight * vHaze);
}
