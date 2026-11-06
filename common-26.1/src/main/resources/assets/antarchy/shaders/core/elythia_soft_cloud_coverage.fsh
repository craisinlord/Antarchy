#version 330

uniform sampler2D InSampler;

layout(std140) uniform ElythiaSoftCloud {
    vec4 Clearings[8];
    vec4 FrameInfo;
    vec4 DepthInfo;
};

in float vWeight;
in float vShade;
in float vDistance;
in float vHaze;
in float vStep;

out vec4 fragColor;

void main() {
    ivec2 size = textureSize(InSampler, 0);
    ivec2 coord = clamp(ivec2(gl_FragCoord.xy * DepthInfo.xy), ivec2(0), size - ivec2(1));
    if (gl_FragCoord.z > texelFetch(InSampler, coord, 0).r) {
        discard;
    }
    float coverage = vWeight * vStep;
    float weight = coverage * exp2(-vDistance / 120.0) * 64.0;
    fragColor = vec4(weight * vShade, weight, coverage, weight * vHaze);
}
