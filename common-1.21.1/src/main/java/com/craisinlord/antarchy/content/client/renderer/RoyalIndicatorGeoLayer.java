package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.JudgmentMarkAccess;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class RoyalIndicatorGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    public RoyalIndicatorGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
            @Nullable RenderType renderType, MultiBufferSource bufferSource,
            @Nullable com.mojang.blaze3d.vertex.VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity living)) {
            return;
        }
        RoyalIndicatorRenderer.render(poseStack, bufferSource, living,
                RoyalEffectHooks.commandedHolder() != null && living.hasEffect(RoyalEffectHooks.commandedHolder()),
                living instanceof JudgmentMarkAccess access && access.antarchy$isJudgmentMarked(),
                living.tickCount + partialTick);
    }
}
