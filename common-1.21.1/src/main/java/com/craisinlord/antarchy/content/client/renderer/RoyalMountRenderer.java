package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalMountModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.craisinlord.antarchy.content.entity.royal.PrinceEntity;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalMountRenderer extends GeoEntityRenderer<RoyalMountEntity> {
    private static final ResourceLocation QUEEN_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_outer.png");
    private static final ResourceLocation QUEEN_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_inner.png");
    private static final ResourceLocation QUEEN_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_1.png");
    private static final ResourceLocation KING_FIRE_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final ResourceLocation KING_FIRE_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final ResourceLocation KING_FIRE_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final ResourceLocation KING_LIGHTNING_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final ResourceLocation KING_LIGHTNING_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final ResourceLocation KING_LIGHTNING_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final ResourceLocation KING_ICE_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final ResourceLocation KING_ICE_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final ResourceLocation KING_ICE_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    public RoyalMountRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalMountModel());
        this.shadowRadius = 1.1F;
        this.addRenderLayer(new TemporalContractionGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(RoyalMountEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, RoyalMountEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        float scale = animatable.getGrowthScale();
        poseStack.scale(scale, scale, scale);
        this.shadowRadius = 1.1F * scale;
    }

    @Override
    public void render(RoyalMountEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        Vec3 end = entity.getBeamEndPosition();
        if (!entity.isFiringBeam() || end == null) return;
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        Vec3 start = entity.beamShootFrom().subtract(x, y, z);
        Vec3 finish = end.subtract(x, y, z);
        Vec3 axis = finish.subtract(start);
        double length = axis.length();
        if (length < 0.05D) return;
        Vec3 dir = axis.scale(1.0D / length);
        Vec3 side = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) side = dir.cross(new Vec3(1.0D, 0.0D, 0.0D));
        side = side.normalize();
        ResourceLocation outer = QUEEN_OUTER;
        ResourceLocation inner = QUEEN_INNER;
        ResourceLocation endTexture = QUEEN_END;
        if (entity instanceof PrinceEntity) {
            switch (entity.getBeamElement()) {
                case FIRE -> { outer = KING_FIRE_OUTER; inner = KING_FIRE_INNER; endTexture = KING_FIRE_END; }
                case ICE -> { outer = KING_ICE_OUTER; inner = KING_ICE_INNER; endTexture = KING_ICE_END; }
                default -> { outer = KING_LIGHTNING_OUTER; inner = KING_LIGHTNING_INNER; endTexture = KING_LIGHTNING_END; }
            }
        }
        float time = entity.tickCount + partialTick;
        float tiles = (float) (length / 3.0D);
        org.joml.Matrix4f pose = poseStack.last().pose();
        drawBeam(bufferSource.getBuffer(RenderType.entityTranslucentEmissive(outer)), pose, start, finish, side.scale(0.24D), -time * 0.32F, -time * 0.32F + tiles);
        drawBeam(bufferSource.getBuffer(RenderType.entityTranslucentEmissive(inner)), pose, start, finish, side.scale(0.13D), -time * 0.5F, -time * 0.5F + tiles);
        Vec3 up = new Vec3(this.entityRenderDispatcher.camera.getUpVector()).scale(0.55D);
        Vec3 left = new Vec3(this.entityRenderDispatcher.camera.getLeftVector()).scale(0.55D);
        drawQuad(bufferSource.getBuffer(RenderType.entityTranslucentEmissive(endTexture)), pose, finish.add(left).add(up), finish.subtract(left).add(up), finish.subtract(left).subtract(up), finish.add(left).subtract(up));
    }

    private static void drawBeam(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 start, Vec3 end, Vec3 half, float startV, float endV) {
        drawQuad(vertices, pose, start.subtract(half), start.add(half), end.add(half), end.subtract(half), startV, endV);
        drawQuad(vertices, pose, start.add(half), start.subtract(half), end.subtract(half), end.add(half), startV, endV);
    }

    private static void drawQuad(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        drawQuad(vertices, pose, a, b, c, d, 0.0F, 1.0F);
    }

    private static void drawQuad(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float startV, float endV) {
        vertex(vertices, pose, a, 0.0F, startV); vertex(vertices, pose, b, 1.0F, startV); vertex(vertices, pose, c, 1.0F, endV); vertex(vertices, pose, d, 0.0F, endV);
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0x00F000F0).setNormal(0.0F, 1.0F, 0.0F);
    }
}
