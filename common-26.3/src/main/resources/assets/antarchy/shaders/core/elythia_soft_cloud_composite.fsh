#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform ElythiaSoftComposite {
    vec4 CompositeInfo;
    vec4 LitColor;
    vec4 ShadowColor;
    vec4 CompositeFog;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texel = CompositeInfo.xy;
    vec4 c = texture(InSampler, texCoord) * 0.4
            + texture(InSampler, texCoord + vec2(texel.x, 0.0)) * 0.15
            + texture(InSampler, texCoord - vec2(texel.x, 0.0)) * 0.15
            + texture(InSampler, texCoord + vec2(0.0, texel.y)) * 0.15
            + texture(InSampler, texCoord - vec2(0.0, texel.y)) * 0.15;
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
