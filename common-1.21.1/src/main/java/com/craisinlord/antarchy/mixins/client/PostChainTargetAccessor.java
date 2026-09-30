package com.craisinlord.antarchy.mixins.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostChain.class)
public interface PostChainTargetAccessor {
    @Accessor("screenTarget")
    RenderTarget antarchy$getScreenTarget();

    @Mutable
    @Accessor("screenTarget")
    void antarchy$setScreenTarget(RenderTarget renderTarget);
}
