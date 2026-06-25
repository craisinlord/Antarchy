package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.StinkBugModel;
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StinkBugRenderer extends GeoEntityRenderer<StinkBugEntity> {
    private static final float ADULT_SCALE = 0.75F;
    private static final float BABY_SCALE = 0.50F;
    private static final float ADULT_SHADOW_RADIUS = 0.6F;

    public StinkBugRenderer(EntityRendererProvider.Context context) {
        super(context, new StinkBugModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withScale(ADULT_SCALE);
    }

    @Override
    public void preRender(PoseStack poseStack, StinkBugEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS * BABY_SCALE;
        } else {
            this.shadowRadius = ADULT_SHADOW_RADIUS;
        }
    }
}
