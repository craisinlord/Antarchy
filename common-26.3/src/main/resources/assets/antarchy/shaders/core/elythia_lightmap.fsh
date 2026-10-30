#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform LightmapInfo {
    float SkyFactor;
    float BlockFactor;
    float NightVisionFactor;
    float DarknessScale;
    float BossOverlayWorldDarkeningFactor;
    float BrightnessFactor;
    vec3 BlockLightTint;
    vec3 SkyLightColor;
    vec3 AmbientColor;
    vec3 NightVisionColor;
} lightmapInfo;

layout(std140) uniform AntarchyDreamInfo {
    float Strength;
} antarchyDreamInfo;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float get_brightness(float level) {
    return level / (4.0 - 3.0 * level);
}

vec3 notGamma(vec3 color) {
    float maxComponent = max(max(color.x, color.y), color.z);
    float maxInverted = 1.0f - maxComponent;
    float maxScaled = 1.0f - maxInverted * maxInverted * maxInverted * maxInverted;
    return color * (maxScaled / maxComponent);
}

float parabolicMixFactor(float level) {
    return (2.0 * level - 1.0) * (2.0 * level - 1.0);
}

void main() {
    
    float block_level = floor(texCoord.x * 16) / 15;
    float sky_level = floor(texCoord.y * 16) / 15;

    float block_brightness = get_brightness(block_level) * lightmapInfo.BlockFactor;
    float sky_brightness = get_brightness(sky_level) * lightmapInfo.SkyFactor;

    
    vec3 nightVisionColor = lightmapInfo.NightVisionColor * lightmapInfo.NightVisionFactor;
    vec3 color = max(lightmapInfo.AmbientColor, nightVisionColor);

    
    color += lightmapInfo.SkyLightColor * sky_brightness;

    
    vec3 BlockLightColor = mix(lightmapInfo.BlockLightTint, vec3(1.0), 0.9 * parabolicMixFactor(block_level));
    color += BlockLightColor * block_brightness;

    
    color = mix(color, color * vec3(0.7, 0.6, 0.6), lightmapInfo.BossOverlayWorldDarkeningFactor);

    
    color = color - vec3(lightmapInfo.DarknessScale);

    
    color = clamp(color, 0.0, 1.0);
    vec3 notGamma = notGamma(color);
    color = mix(color, notGamma, lightmapInfo.BrightnessFactor);

    vec3 dreamRgb = floor(clamp(color, 0.0, 1.0) * 255.0 + 0.5) / 255.0;
    float dreamLuminance = dot(dreamRgb, vec3(0.2126, 0.7152, 0.0722));
    float dreamDarkness = 1.0 - dreamLuminance;
    float dreamTorchlight = 1.0 - block_level;
    float dreamHue = min(1.0, dreamDarkness * dreamDarkness * 1.0) * dreamTorchlight * antarchyDreamInfo.Strength;
    float dreamFloor = dreamDarkness * dreamDarkness * dreamDarkness * antarchyDreamInfo.Strength;
    vec3 dreamShifted = mix(dreamRgb, dreamLuminance * vec3(1.30, 0.78, 2.05), dreamHue);
    color = clamp(dreamShifted + vec3(0.070, 0.035, 0.140) * dreamFloor, 0.0, 1.0);
    color = floor(color * 255.0 + 0.5) / 255.0;

    fragColor = vec4(color, 1.0);
}
