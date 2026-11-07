package com.craisinlord.antarchy.mixins.client;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.Map;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostPass.class)
public interface PostPassUniformAccessor {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> antarchy$customUniforms();
}
