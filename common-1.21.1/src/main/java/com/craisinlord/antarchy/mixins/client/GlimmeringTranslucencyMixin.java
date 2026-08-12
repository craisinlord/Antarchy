package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class GlimmeringTranslucencyMixin<T extends LivingEntity> {
    // Vertex-color alpha applied to the base skin render while glimmering, on top of the recolor
    // overlay, so the body itself reads as see-through rather than just tinted.
    private static final int GLIMMERING_ALPHA = 0x40;

    @Unique
    private boolean antarchy$glimmeringActive;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void antarchy$captureGlimmering(LivingEntity entity, float limbSwing, float partialTick,
                                             com.mojang.blaze3d.vertex.PoseStack poseStack,
                                             net.minecraft.client.renderer.MultiBufferSource buffer,
                                             int packedLight, CallbackInfo ci) {
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        this.antarchy$glimmeringActive = glimmering != null && entity.hasEffect(glimmering);
    }

    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType antarchy$forceTranslucentRenderType(LivingEntityRenderer<T, ?> self, LivingEntity entity,
                                                             boolean bodyVisible, boolean translucent, boolean glowingEffect) {
        if (this.antarchy$glimmeringActive) {
            var texture = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity);
            return RenderType.entityTranslucent(texture);
        }
        return this.getRenderType((T) entity, bodyVisible, translucent, glowingEffect);
    }

    @Shadow
    protected abstract RenderType getRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowingEffect);

    @ModifyArg(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            index = 4
    )
    private int antarchy$applyGlimmeringAlpha(int color) {
        if (!this.antarchy$glimmeringActive) {
            return color;
        }
        return (color & 0x00FFFFFF) | (GLIMMERING_ALPHA << 24);
    }
}
