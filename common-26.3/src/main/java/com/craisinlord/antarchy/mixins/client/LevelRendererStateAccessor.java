package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererStateAccessor {
    @Accessor("levelRenderState")
    LevelRenderState antarchy$getLevelRenderState();
}
