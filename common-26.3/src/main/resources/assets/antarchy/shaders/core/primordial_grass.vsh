#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(std140) uniform GrassFrame {
    vec4 GrassWind;
    vec4 GustTable[8];
    vec4 TramplePos[32];
    vec4 TrampleMotion[32];
    vec4 GrassState;
    vec4 VariantUv[3];
    vec4 VariantHeight;
};

uniform sampler2D Sampler2;

layout(location = 0) in vec2 Offset;
layout(location = 1) in vec2 Shape;
layout(location = 2) in vec2 UV0;
layout(location = 3) in float Flippable;
layout(location = 4) in vec3 Root;
layout(location = 5) in vec2 Meta;
layout(location = 6) in uvec2 UV2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec2 texCoord0;

float smoothCurve(float t) {
    return t * t * (3.0 - 2.0 * t);
}

float gustEnvelope(float age) {
    if (age < 0.0 || age >= 136.0) {
        return 0.0;
    }
    if (age < 25.0) {
        return smoothCurve(age / 25.0);
    }
    if (age < 54.0) {
        return 1.0;
    }
    return 1.0 - smoothCurve((age - 54.0) / 82.0);
}

void main() {
    float h = Shape.x;
    float f = Shape.y;
    float metaByte = floor(Meta.x * 255.0 + 0.5);
    float exposure = metaByte > 127.5 ? 1.0 : 0.0;
    int variant = clamp(int(mod(metaByte, 128.0)), 0, 2);
    float height = VariantHeight[variant];
    float tuftHash = Meta.y;
    float mirror = Flippable > 0.5 && mod(floor(tuftHash * 255.0 + 0.5), 2.0) > 0.5 ? -1.0 : 1.0;
    vec3 root = Root + ModelOffset;
    vec3 rel = root + vec3(Offset.x * mirror, h * height, Offset.y);
    vec2 windDir = GrassWind.xy;
    float calm = sin(GrassWind.w + root.x * 0.07 + root.z * 0.09) * 0.04;
    float gustTime = GrassWind.z - dot(root.xz, windDir) / 0.31;
    float idx = floor(gustTime / 800.0);
    vec4 entry = GustTable[clamp(int(idx) + 4, 0, 7)];
    float age = gustTime - idx * 800.0 - entry.x;
    float gust = gustEnvelope(age) * entry.y;
    vec2 windOffset = windDir * (calm * h * h + gust * f * exposure * 0.34) * GrassState.x;
    vec2 push = vec2(0.0);
    int trampleCount = int(GrassState.y);
    if (trampleCount > 0 && h > 0.0 && dot(root, root) < 1444.0) {
        for (int i = 0; i < trampleCount; i++) {
            vec4 p = TramplePos[i];
            float vertical = 1.0 - smoothstep(1.0, 1.6, abs(root.y - p.y));
            if (vertical <= 0.0) {
                continue;
            }
            vec2 d = root.xz - p.xz;
            float dist = length(d);
            vec3 motion = TrampleMotion[i].xyz;
            float w = motion.z * vertical * (1.0 - smoothstep(p.w * 0.45, p.w, dist));
            push += w * (d / max(dist, 0.25) * 0.45 + motion.xy * 0.85);
        }
    }
    float raw = length(push);
    float lean = 0.0;
    if (raw > 0.0) {
        lean = 1.0 - exp(-raw * 1.25);
        push *= lean / raw;
    }
    float bend = h * (0.35 + 0.65 * h);
    float variation = 0.8 + 0.35 * tuftHash;
    float flutter = sin(GrassWind.w * 3.0 + tuftHash * 6.2831855) * 0.1 * bend;
    vec3 pos = rel;
    pos.xz += windOffset * (1.0 - 0.6 * lean) + push * 0.5 * variation * bend + vec2(-push.y, push.x) * flutter;
    pos.y -= h * h * height * 0.14 * lean * lean;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vertexColor = sample_lightmap(Sampler2, ivec2(UV2));
    texCoord0 = VariantUv[variant].xy + UV0 * VariantUv[variant].zw;
}
