#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(std140) uniform ElythiaSoftCloud {
    vec4 Clearings[8];
    vec4 FrameInfo;
    vec4 DepthInfo;
};

layout(location = 0) in vec3 Position;
layout(location = 2) in vec4 Color;

layout(location = 0) out float vWeight;
layout(location = 1) out float vShade;
layout(location = 2) out float vDistance;
layout(location = 3) out float vHaze;
layout(location = 4) out float vStep;

void main() {
    vec3 rel = Position + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(rel, 1.0);
    float fogEnd = ColorModulator.x;
    float growthTop = ColorModulator.z;
    float distanceToCamera = length(rel);
    float fogDistance = max(length(rel.xz), abs(rel.y));
    float fogFade = 1.0 - smoothstep(fogEnd * 0.6, fogEnd, fogDistance);
    float nearFade = smoothstep(6.0, 22.0, distanceToCamera);
    float growth = 1.0 - smoothstep(growthTop - 40.0, growthTop, rel.y);
    float clearValue = 1.0;
    if (ColorModulator.w > 0.0) {
        int clearingCount = int(FrameInfo.x);
        for (int i = 0; i < clearingCount; i++) {
            float d = length(rel.xz - Clearings[i].xz);
            clearValue = min(clearValue, smoothstep(Clearings[i].w * 0.7, Clearings[i].w * 1.4, d));
        }
        clearValue = mix(1.0, clearValue, FrameInfo.y);
    }
    vWeight = fogFade * nearFade * growth * clearValue;
    float sunlit = mix(Color.b, Color.g, FrameInfo.z);
    vShade = Color.r * mix(1.0, 0.55 + 0.45 * sunlit, FrameInfo.w);
    vDistance = distanceToCamera;
    vHaze = smoothstep(120.0, fogEnd, fogDistance);
    vStep = ColorModulator.y;
}
