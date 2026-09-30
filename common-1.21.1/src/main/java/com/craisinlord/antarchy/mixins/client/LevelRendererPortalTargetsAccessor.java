package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererPortalTargetsAccessor {
    @Accessor("entityEffect")
    PostChain antarchy$getEntityEffect();

    @Accessor("transparencyChain")
    PostChain antarchy$getTransparencyChain();
}
