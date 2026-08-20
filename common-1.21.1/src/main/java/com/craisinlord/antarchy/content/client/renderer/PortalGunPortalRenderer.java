package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.PortalGunPortalModel;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalGunPortalRenderer extends GeoEntityRenderer<PortalGunPortalEntity> {
    public PortalGunPortalRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalGunPortalModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PortalGunPortalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Direction facing = entity.getFacingDirection();
        poseStack.pushPose();
        if (facing.getAxis() == Direction.Axis.Y) {
            poseStack.mulPose(Axis.XP.rotationDegrees(facing == Direction.UP ? -90.0F : 90.0F));
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 0xF000F0);
        poseStack.popPose();
    }

    @Override
    public @Nullable RenderType getRenderType(PortalGunPortalEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
