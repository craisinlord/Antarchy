package com.craisinlord.antarchy.neoforge.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class AntarchyWorldOverlayRenderTypes {
    public static final RenderType TRANSLUCENT_QUADS = RenderType.create(
            "antarchy_world_overlay_quads",
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS).createRenderSetup()
    );

    private AntarchyWorldOverlayRenderTypes() {
    }
}
