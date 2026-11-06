package com.craisinlord.antarchy.content.client.renderer;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;

final class PrimordialGrassSodiumVisibility {
    private PrimordialGrassSodiumVisibility() {
    }

    static boolean visible(int sectionX, int sectionY, int sectionZ) {
        SodiumWorldRenderer renderer = SodiumWorldRenderer.instanceNullable();
        if (renderer == null) return true;
        double x = sectionX * 16.0D;
        double y = sectionY * 16.0D;
        double z = sectionZ * 16.0D;
        return renderer.isBoxVisible(x, y, z, x + 15.9D, y + 15.9D, z + 15.9D);
    }
}
