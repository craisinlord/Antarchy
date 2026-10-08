package com.craisinlord.antarchy.mixins.compat.sodium;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public interface SodiumWorldRendererAccessor {
    @Accessor(value = "renderSectionManager", remap = false)
    RenderSectionManager antarchy$getRenderSectionManager();

    @Accessor(value = "useEntityCulling", remap = false)
    boolean antarchy$getUseEntityCulling();

    @Accessor(value = "useEntityCulling", remap = false)
    void antarchy$setUseEntityCulling(boolean useEntityCulling);
}
