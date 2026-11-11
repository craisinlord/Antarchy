#version 330

uniform sampler2D InSampler;
uniform sampler2D Sampler0;

layout(std140) uniform ElythiaSoftComposite {
    vec4 CompositeInfo;
    vec4 LitColor;
    vec4 ShadowColor;
    vec4 CompositeFog;
};

in vec2 texCoord;

out vec4 fragColor;

float viewDepth(float depth) {
    return 1.0 / max(1.0 - depth, 1.0e-7);
}

vec4 gatherCoverage() {
    ivec2 lowSize = textureSize(InSampler, 0);
    ivec2 fullSize = textureSize(Sampler0, 0);
    vec2 scale = vec2(fullSize) / vec2(lowSize);
    ivec2 pixel = clamp(ivec2(gl_FragCoord.xy), ivec2(0), fullSize - ivec2(1));
    float center = viewDepth(texelFetch(Sampler0, pixel, 0).r);
    vec2 lowPosition = gl_FragCoord.xy / scale - 0.5;
    ivec2 base = ivec2(floor(lowPosition));
    vec2 fraction = lowPosition - vec2(base);
    vec4 sum = vec4(0.0);
    float total = 0.0;
    for (int y = -1; y <= 2; y++) {
        for (int x = -1; x <= 2; x++) {
            vec2 offset = vec2(float(x), float(y)) - fraction;
            float spatial = max(0.0, 1.0 - length(offset) / 1.8);
            if (spatial <= 0.0) {
                continue;
            }
            ivec2 texel = clamp(base + ivec2(x, y), ivec2(0), lowSize - ivec2(1));
            ivec2 depthTexel = clamp(ivec2((vec2(texel) + 0.5) * scale), ivec2(0), fullSize - ivec2(1));
            float tap = viewDepth(texelFetch(Sampler0, depthTexel, 0).r);
            float weight = spatial * exp(-abs(tap - center) / (center * 0.08));
            sum += texelFetch(InSampler, texel, 0) * weight;
            total += weight;
        }
    }
    return sum / max(total, 0.25);
}

void main() {
    vec4 c = gatherCoverage();
    float count = c.b * LitColor.a;
    if (count < 0.02) {
        discard;
    }
    float weight = max(c.g, 1.0e-6);
    float shade = clamp(c.r / weight, 0.0, 1.0);
    float haze = clamp(c.a / weight, 0.0, 1.0);
    vec3 color = mix(ShadowColor.rgb, LitColor.rgb, shade);
    color = mix(color, CompositeFog.rgb, haze * ShadowColor.a);
    float alpha = (1.0 - pow(1.0 - CompositeInfo.z, count)) * CompositeInfo.w;
    fragColor = vec4(color, alpha);
}
