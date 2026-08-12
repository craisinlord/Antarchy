package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class GoopedGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final int GOOPED_TINT = 0x6632FF55;

    public GoopedGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                       MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!(animatable instanceof Entity entity) || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (gooped == null || !livingEntity.hasEffect(gooped.value())) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.018F, 1.018F, 1.018F);
        RenderType goopedRenderType = RenderType.entityTranslucent(this.getRenderer().getTextureLocation(animatable));
        VertexConsumer goopedBuffer = bufferSource.getBuffer(goopedRenderType);
        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                goopedRenderType,
                goopedBuffer,
                partialTick,
                packedLight,
                packedOverlay,
                0.196F,
                1.0F,
                0.333F,
                0.4F
        );
        poseStack.popPose();
    }
}
