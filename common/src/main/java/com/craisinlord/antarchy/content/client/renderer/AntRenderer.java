package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AntModel;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AntRenderer extends GeoEntityRenderer<BaseAntEntity> {
    private static final float ADULT_SHADOW_RADIUS = 0.3F;
    private static final float ADULT_SCALE = 0.85F;
    private static final float BABY_SCALE = 0.5F;

    public AntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.addRenderLayer(new AntCarriedFoodGeoLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, BaseAntEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable.isBaby()) {
            poseStack.scale(ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS * BABY_SCALE;
        } else {
            poseStack.scale(ADULT_SCALE, ADULT_SCALE, ADULT_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS;
        }
    }
}
