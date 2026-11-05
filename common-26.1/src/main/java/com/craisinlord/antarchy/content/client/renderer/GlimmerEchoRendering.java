package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class GlimmerEchoRendering {
    public static final float ALPHA = 0.78F;

    private GlimmerEchoRendering() {
    }

    public static Identifier emissiveTexture(String entity) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/glimmer/" + entity + "_glimmer_emissive.png");
    }

    public static int renderColor(boolean glimmerEcho, int normal) {
        return glimmerEcho ? ARGB.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F) : normal;
    }

    public static int emissiveColor() {
        return ARGB.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F);
    }
}
