package com.craisinlord.antarchy.fabric.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class AntarchyFabricRenderTypes {
    public static final RenderType TRANSLUCENT_QUADS_NO_CULL = RenderType.create(
            "antarchy_fabric_translucent_quads_no_cull",
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS).createRenderSetup()
    );

    private AntarchyFabricRenderTypes() {
    }
}
