#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

uniform float Baseline;
uniform float Wake;
uniform float Zone;
uniform float Distortion;
uniform float DreamTime;
uniform float Ghost;
uniform float Saturation;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

const float TAU = 6.28318530718;
const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 WARM = vec3(1.04, 1.01, 0.95);
const vec2 GLOW_RING[8] = vec2[8](
    vec2(1.0, 0.0), vec2(0.7071, 0.7071), vec2(0.0, 1.0), vec2(-0.7071, 0.7071),
    vec2(-1.0, 0.0), vec2(-0.7071, -0.7071), vec2(0.0, -1.0), vec2(0.7071, -0.7071)
);

vec3 hueShift(vec3 color, float angle) {
    vec3 axis = vec3(0.57735026919);
    float c = cos(angle);
    return color * c + cross(axis, color) * sin(angle) + axis * dot(axis, color) * (1.0 - c);
}

vec3 glowSample(vec2 uv) {
    vec3 sampleColor = texture(DiffuseSampler, uv).rgb;
    return sampleColor * smoothstep(0.7, 1.0, dot(sampleColor, LUMA));
}

void main() {
    vec2 centered = texCoord - vec2(0.5);
    float radial = length(centered) * 1.41421356;
    float edge = smoothstep(0.35, 1.0, radial);
    float wobble = Zone * Distortion * edge;

    vec3 color;
    vec2 uv = texCoord;
    if (wobble > 0.0005) {
        uv = clamp(texCoord + vec2(
            sin(texCoord.y * 17.0 + DreamTime * 1.3),
            cos(texCoord.x * 13.0 + DreamTime * 1.1)
        ) * 0.003 * wobble, vec2(0.0), vec2(1.0));
        vec2 fringe = centered / max(length(centered), 0.0001) * 0.002 * wobble;
        color = vec3(
            texture(DiffuseSampler, clamp(uv + fringe, vec2(0.0), vec2(1.0))).r,
            texture(DiffuseSampler, uv).g,
            texture(DiffuseSampler, clamp(uv - fringe, vec2(0.0), vec2(1.0))).b
        );
    } else {
        color = texture(DiffuseSampler, uv).rgb;
    }

    if (Zone > 0.001) {
        vec3 glow = vec3(0.0);
        vec2 reach = oneTexel * 6.5;
        for (int i = 0; i < 8; i++) {
            glow += glowSample(uv + GLOW_RING[i] * reach);
        }
        color += glow * (0.07 * Zone);
    }

    color = mix(color, color * WARM, Baseline * 0.5 + Zone * 0.25);

    float spread = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
    float luma = dot(color, LUMA);
    color = mix(vec3(luma), color, 1.0 + (Saturation - 1.0) * (1.0 - spread));

    color = hueShift(color, sin(DreamTime * (TAU / 10.0)) * 0.06 * Zone);

    float breath = 0.5 + 0.5 * sin(DreamTime * 0.9);
    color *= 1.0 - smoothstep(0.45, 1.1, radial) * (0.14 + 0.06 * breath) * Zone;

    if (Ghost > 0.0005) {
        color = mix(color, texture(PrevSampler, texCoord).rgb, min(Ghost, 0.15));
    }

    color = mix(vec3(dot(color, LUMA)), color, Wake);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
