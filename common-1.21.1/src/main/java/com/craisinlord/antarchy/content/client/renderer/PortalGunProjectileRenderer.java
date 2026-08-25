package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PortalGunProjectileRenderer extends EntityRenderer<PortalGunProjectileEntity> {
    private static final ResourceLocation BLUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final ResourceLocation ORANGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");

    public PortalGunProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PortalGunProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float age = entity.tickCount + partialTick;
        float syncedDistance = entity.getSyncedDistance() + partialTick;
        float pulse = 0.8F + 0.2F * (float) Math.sin(age * 0.9F);
        float travelPulse = 0.82F + 0.18F * (float) Math.sin(syncedDistance * 0.55F);
        float trailStretch = 1.1F + (float) Math.min(entity.getSyncedVelocity().length() * 0.42D, 1.8D);
        float baseScale = entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? 0.44F : 0.48F;
        ResourceLocation texture = entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? BLUE_TEXTURE : ORANGE_TEXTURE;
        float red = entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? 0.55F : 1.0F;
        float green = entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? 0.82F : 0.56F;
        float blue = entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? 1.0F : 0.18F;

        poseStack.scale(baseScale * trailStretch, baseScale * travelPulse, baseScale * travelPulse);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer core = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        addQuad(core, pose, matrix, red, green, blue, 0.92F, packedLight, -0.9F, -0.5F, 0.9F, 0.5F);
        VertexConsumer glow = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(texture));
        addQuad(glow, pose, matrix, 1.0F, 1.0F, 1.0F, 0.8F * pulse, 0xF000F0, -1.15F, -0.7F, 1.15F, 0.7F);

        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
        pose = poseStack.last();
        matrix = pose.pose();
        core = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        addQuad(core, pose, matrix, red, green, blue, 0.74F, packedLight, -0.45F, -0.95F, 0.45F, 0.95F);
        glow = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(texture));
        addQuad(glow, pose, matrix, 1.0F, 1.0F, 1.0F, 0.52F * pulse, 0xF000F0, -0.62F, -1.15F, 0.62F, 1.15F);
        poseStack.popPose();
    }

    @Override
    protected int getBlockLightLevel(PortalGunProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(PortalGunProjectileEntity entity) {
        return entity.getPortalSide() == com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity.PortalSide.BLUE ? BLUE_TEXTURE : ORANGE_TEXTURE;
    }

    private static void addQuad(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, float red, float green, float blue, float alpha, int packedLight, float minX, float minY, float maxX, float maxY) {
        addVertex(consumer, pose, matrix, minX, minY, 0.0F, 1.0F, red, green, blue, alpha, packedLight);
        addVertex(consumer, pose, matrix, maxX, minY, 1.0F, 1.0F, red, green, blue, alpha, packedLight);
        addVertex(consumer, pose, matrix, maxX, maxY, 1.0F, 0.0F, red, green, blue, alpha, packedLight);
        addVertex(consumer, pose, matrix, minX, maxY, 0.0F, 0.0F, red, green, blue, alpha, packedLight);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, float x, float y, float u, float v, float red, float green, float blue, float alpha, int packedLight) {
        consumer.addVertex(matrix, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
