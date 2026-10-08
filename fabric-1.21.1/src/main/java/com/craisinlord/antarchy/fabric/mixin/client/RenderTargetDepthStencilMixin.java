package com.craisinlord.antarchy.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderTarget.class)
public abstract class RenderTargetDepthStencilMixin {
    @WrapOperation(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
    private void antarchy$allocateDepthStencil(int target, int level, int internalFormat, int width, int height, int border, int format, int type,
                                               IntBuffer pixels, Operation<Void> original) {
        if (internalFormat == GL11.GL_DEPTH_COMPONENT) {
            original.call(target, level, GL30.GL_DEPTH24_STENCIL8, width, height, border, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, pixels);
            return;
        }
        original.call(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    @WrapOperation(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"))
    private void antarchy$attachDepthStencil(int target, int attachment, int textureTarget, int texture, int level, Operation<Void> original) {
        original.call(target, attachment == GL30.GL_DEPTH_ATTACHMENT ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment, textureTarget, texture, level);
    }
}
