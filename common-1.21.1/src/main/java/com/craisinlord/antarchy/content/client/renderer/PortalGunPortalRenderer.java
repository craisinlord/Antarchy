package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.model.PortalGunPortalModel;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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
        Matrix4f pose = poseStack.last().pose();
        float portalScale = entity.getPortalVisualScale(partialTick);
        float halfWidth = entity.getPortalWidth() * 0.5F;
        float halfHeight = entity.getPortalHeight() * 0.5F;
        ResourceLocation texture = portalTexture(entity);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        if (isFancyPortalsEnabled()) {
            float frameV = Math.floorMod((int) ((entity.level().getGameTime() + partialTick) / 5.0F), 4) * 0.25F;
            putAnimatedPortalVertex(consumer, pose, entity, portalScale, frameV, -halfWidth, -halfHeight, 0.0F, 1.0F);
            putAnimatedPortalVertex(consumer, pose, entity, portalScale, frameV, -halfWidth, halfHeight, 0.0F, 0.0F);
            putAnimatedPortalVertex(consumer, pose, entity, portalScale, frameV, halfWidth, halfHeight, 1.0F, 0.0F);
            putAnimatedPortalVertex(consumer, pose, entity, portalScale, frameV, halfWidth, -halfHeight, 1.0F, 1.0F);
        } else {
            putPortalVertex(consumer, pose, entity, portalScale, -halfWidth, -halfHeight, 0.0F, 1.0F, true);
            putPortalVertex(consumer, pose, entity, portalScale, -halfWidth, halfHeight, 0.0F, 0.0F, true);
            putPortalVertex(consumer, pose, entity, portalScale, halfWidth, halfHeight, 1.0F, 0.0F, true);
            putPortalVertex(consumer, pose, entity, portalScale, halfWidth, -halfHeight, 1.0F, 1.0F, true);
        }
        VertexConsumer edge = bufferSource.getBuffer(RenderType.entityTranslucent(PortalGunPortalRim.texture(entity)));
        float rimWidth = halfWidth + PortalGunPortalRim.EXTRA_WIDTH;
        float rimHeight = halfHeight + PortalGunPortalRim.EXTRA_HEIGHT;
        putPortalVertex(edge, pose, entity, portalScale, -rimWidth, -rimHeight, 0.0F, 1.0F, true);
        putPortalVertex(edge, pose, entity, portalScale, -rimWidth, rimHeight, 0.0F, 0.0F, true);
        putPortalVertex(edge, pose, entity, portalScale, rimWidth, rimHeight, 1.0F, 0.0F, true);
        putPortalVertex(edge, pose, entity, portalScale, rimWidth, -rimHeight, 1.0F, 1.0F, true);
        poseStack.popPose();
    }

    private static ResourceLocation portalTexture(PortalGunPortalEntity entity) {
        return isFancyPortalsEnabled()
                ? entity.getVariant().vortexTexture(entity.getPortalSide())
                : PortalGunPortalRim.texture(entity);
    }

    private static boolean isFancyPortalsEnabled() {
        Minecraft minecraft = Minecraft.getInstance();
        return AntarchySettings.portalGunFancyPortals()
                && minecraft.options.graphicsMode().get() == GraphicsStatus.FANCY;
    }

    private static void putAnimatedPortalVertex(VertexConsumer consumer, Matrix4f pose, PortalGunPortalEntity entity, float portalScale, float frameV, float horizontal, float vertical, float u, float v) {
        Vec3 position = entity.getWidthVec().normalize().scale(horizontal * portalScale)
                .add(entity.getUpVec().normalize().scale(vertical * portalScale))
                .add(entity.getNormalVec().normalize().scale(0.03125D));
        consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(255, 255, 255, 210)
                .setUv(u, frameV + v * 0.25F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal((float) entity.getNormalVec().x, (float) entity.getNormalVec().y, (float) entity.getNormalVec().z);
    }

    private static void putPortalVertex(VertexConsumer consumer, Matrix4f pose, PortalGunPortalEntity entity, float portalScale, float horizontal, float vertical, float u, float v, boolean channelTint) {
        Vec3 position = entity.getWidthVec().normalize().scale(horizontal * portalScale)
                .add(entity.getUpVec().normalize().scale(vertical * portalScale))
                .add(entity.getNormalVec().normalize().scale(0.03125D));
        int color = entity.getPortalColor();
        int red = channelTint ? color >> 16 & 0xFF : 255;
        int green = channelTint ? color >> 8 & 0xFF : 255;
        int blue = channelTint ? color & 0xFF : 255;
        int alpha = channelTint ? PortalGunPortalRim.ENTITY_ALPHA : 210;
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
        return RenderType.entityTranslucent(portalTexture(animatable));
    }
}
