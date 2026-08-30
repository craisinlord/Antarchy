package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBossModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalBossRenderer extends GeoEntityRenderer<RoyalBossEntity> {
    public RoyalBossRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBossModel());
        this.shadowRadius = 8.0F;
    }

    @Override
    public RenderType getRenderType(RoyalBossEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, RoyalBossEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.scale(RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE);
        this.shadowRadius = 8.0F * RoyalBossEntity.MODEL_RENDER_SCALE;
    }

    @Override
    public void render(RoyalBossEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        Vec3 end = entity.getRoyalBeamEndPosition();
        if (!entity.isFiringRoyalBeam() || end == null) {
            return;
        }
        double x = net.minecraft.util.Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = net.minecraft.util.Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = net.minecraft.util.Mth.lerp(partialTick, entity.zo, entity.getZ());
        Vec3 start = entity.getRoyalBeamShootFrom(partialTick).subtract(x, y, z);
        Vec3 finish = end.subtract(x, y, z);
        Vec3 direction = finish.subtract(start).normalize();
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) {
            side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side = side.normalize().scale(0.22D);
        VertexConsumer vertices = bufferSource.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        drawBeamQuad(vertices, poseStack, start, finish, side);
        drawBeamQuad(vertices, poseStack, start, finish, new Vec3(-side.z, side.y, side.x));
        poseStack.popPose();
    }

    private static void drawBeamQuad(VertexConsumer vertices, PoseStack poseStack, Vec3 start, Vec3 end, Vec3 side) {
        var pose = poseStack.last().pose();
        vertex(vertices, pose, start.subtract(side));
        vertex(vertices, pose, start.add(side));
        vertex(vertices, pose, end.add(side));
        vertex(vertices, pose, end.subtract(side));
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setUv2(15728880 & 65535, 15728880 >> 16)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
