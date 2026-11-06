#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ThrowbackTintConfig {
    float Baseline;
    float Wake;
    float Prism;
};

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 WARM = vec3(1.04, 1.01, 0.95);

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    if (Prism > 0.001) {
        vec2 split = vec2(Prism * 0.012, Prism * 0.004);
        color.r = texture(InSampler, texCoord + split).r;
        color.b = texture(InSampler, texCoord - split).b;
    }
    color = mix(color, color * WARM, Baseline * 0.5);
    float spread = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
    float luma = dot(color, LUMA);
    color = mix(vec3(luma), color, 1.0 + Baseline * 0.12 * (1.0 - spread));
    color = mix(vec3(dot(color, LUMA)), color, Wake);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
