package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.PortalGunPortalModel;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalGunPortalRenderer extends GeoEntityRenderer<PortalGunPortalEntity> {
    public PortalGunPortalRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalGunPortalModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(PortalGunPortalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        Matrix4f pose = poseStack.last().pose();
        putPortalVertex(consumer, pose, entity, -0.5F, -1.0F, 0.0F, 1.0F, false);
        putPortalVertex(consumer, pose, entity, -0.5F, 1.0F, 0.0F, 0.0F, false);
        putPortalVertex(consumer, pose, entity, 0.5F, 1.0F, 1.0F, 0.0F, false);
        putPortalVertex(consumer, pose, entity, 0.5F, -1.0F, 1.0F, 1.0F, false);
        VertexConsumer edge = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        putPortalVertex(edge, pose, entity, -0.56F, -1.07F, 0.0F, 1.0F, true);
        putPortalVertex(edge, pose, entity, -0.56F, 1.07F, 0.0F, 0.0F, true);
        putPortalVertex(edge, pose, entity, 0.56F, 1.07F, 1.0F, 0.0F, true);
        putPortalVertex(edge, pose, entity, 0.56F, -1.07F, 1.0F, 1.0F, true);
        poseStack.popPose();
    }

    private static void putPortalVertex(VertexConsumer consumer, Matrix4f pose, PortalGunPortalEntity entity, float horizontal, float vertical, float u, float v, boolean channelTint) {
        Vec3 position = entity.getWidthVec().normalize().scale(horizontal)
                .add(entity.getUpVec().normalize().scale(vertical))
                .add(entity.getNormalVec().normalize().scale(0.03125D));
        int red = channelTint ? entity.getChannelRed() : 255;
        int green = channelTint ? entity.getChannelGreen() : 255;
        int blue = channelTint ? entity.getChannelBlue() : 255;
        int alpha = channelTint ? 150 : 210;
        consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal((float) entity.getNormalVec().x, (float) entity.getNormalVec().y, (float) entity.getNormalVec().z);
    }

    @Override
    protected void applyRotations(PortalGunPortalEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
    }

    @Override
    public @Nullable RenderType getRenderType(PortalGunPortalEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
