#version 150

uniform sampler2D DiffuseSampler;

uniform float Baseline;
uniform float Wake;

in vec2 texCoord;

out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 WARM = vec3(1.04, 1.01, 0.95);

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    color = mix(color, color * WARM, Baseline * 0.5);
    float spread = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
    float luma = dot(color, LUMA);
    color = mix(vec3(luma), color, 1.0 + Baseline * 0.12 * (1.0 - spread));
    color = mix(vec3(dot(color, LUMA)), color, Wake);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
