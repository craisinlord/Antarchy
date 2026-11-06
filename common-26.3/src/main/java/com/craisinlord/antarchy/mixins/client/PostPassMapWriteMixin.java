package com.craisinlord.antarchy.mixins.client;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PostPass.class)
public abstract class PostPassMapWriteMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDevice;createBuffer(Ljava/util/function/Supplier;ILjava/nio/ByteBuffer;)Lcom/mojang/renderpearl/api/buffers/GpuBuffer;"), index = 1)
    private int antarchy$enableUniformBufferMapping(int usage) {
        return usage | GpuBuffer.USAGE_MAP_WRITE;
    }
}
