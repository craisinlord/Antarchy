package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BedBugModel;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BedBugRenderer extends GeoEntityRenderer<BedBugEntity> {
    private static final float ADULT_SCALE = 0.9F;
    private static final float BABY_SCALE = 0.38F;
    private static final float ADULT_SHADOW_RADIUS = 0.65F;

    public BedBugRenderer(EntityRendererProvider.Context context) {
        super(context, new BedBugModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withScale(ADULT_SCALE);
    }

    @Override
    public void preRender(PoseStack poseStack, BedBugEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
            this.shadowRadius = ADULT_SHADOW_RADIUS * BABY_SCALE;
        } else {
            this.shadowRadius = ADULT_SHADOW_RADIUS;
        }
    }
}
