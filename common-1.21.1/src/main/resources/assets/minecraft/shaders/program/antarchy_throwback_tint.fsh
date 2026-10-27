#version 150

uniform sampler2D DiffuseSampler;

uniform float Baseline;
uniform float Wake;
uniform float Prism;

in vec2 texCoord;

out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 WARM = vec3(1.04, 1.01, 0.95);

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    if (Prism > 0.001) {
        vec2 split = vec2(Prism * 0.012, Prism * 0.004);
        color.r = texture(DiffuseSampler, texCoord + split).r;
        color.b = texture(DiffuseSampler, texCoord - split).b;
    }
    color = mix(color, color * WARM, Baseline * 0.5);
    float spread = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
    float luma = dot(color, LUMA);
    color = mix(vec3(luma), color, 1.0 + Baseline * 0.12 * (1.0 - spread));
    color = mix(vec3(dot(color, LUMA)), color, Wake);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
