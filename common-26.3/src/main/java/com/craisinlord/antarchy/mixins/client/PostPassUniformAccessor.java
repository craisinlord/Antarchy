package com.craisinlord.antarchy.mixins.client;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PostPass.class)
public interface PostPassUniformAccessor {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> antarchy$customUniforms();
}
