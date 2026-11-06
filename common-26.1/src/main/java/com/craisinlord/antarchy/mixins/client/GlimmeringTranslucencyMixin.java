package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class GlimmeringTranslucencyMixin {
    private static final int GLIMMERING_ALPHA = 0x40;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void antarchy$captureGlimmering(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        ((AntarchyLivingRenderStateAccess) state).antarchy$setGlimmering(glimmering != null && entity.hasEffect(glimmering));
    }

    @Redirect(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType antarchy$forceTranslucentRenderType(LivingEntityRenderer<?, ?, ?> self, LivingEntityRenderState state,
                                                             boolean bodyVisible, boolean translucent, boolean glowingEffect) {
        if (((AntarchyLivingRenderStateAccess) state).antarchy$isGlimmering()) {
            return RenderTypes.entityTranslucent(this.getTextureLocation(state));
        }
        return this.getRenderType(state, bodyVisible, translucent, glowingEffect);
    }

    @Shadow
    protected abstract RenderType getRenderType(LivingEntityRenderState state, boolean bodyVisible, boolean translucent, boolean glowingEffect);

    @Shadow
    public abstract net.minecraft.resources.Identifier getTextureLocation(LivingEntityRenderState state);

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            ),
            index = 6
    )
    private int antarchy$applyGlimmeringAlpha(Model<?> model, Object state, PoseStack poseStack, RenderType renderType,
                                              int light, int overlay, int color, TextureAtlasSprite sprite, int outlineColor,
                                              ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!(state instanceof AntarchyLivingRenderStateAccess access) || !access.antarchy$isGlimmering()) {
            return color;
        }
        return (color & 0x00FFFFFF) | (GLIMMERING_ALPHA << 24);
    }
}
