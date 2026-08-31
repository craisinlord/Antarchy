package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBossModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class RoyalBossRenderer extends GeoEntityRenderer<RoyalBossEntity> {
    private static final ResourceLocation QUEEN_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_beam_outer.png");
    private static final ResourceLocation QUEEN_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_beam_inner.png");
    private static final ResourceLocation QUEEN_BEAM_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_beam_end_1.png");
    private static final ResourceLocation KING_FIRE_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final ResourceLocation KING_FIRE_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final ResourceLocation KING_FIRE_BEAM_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final ResourceLocation KING_ICE_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final ResourceLocation KING_ICE_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final ResourceLocation KING_ICE_BEAM_END = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    public RoyalBossRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBossModel());
        this.shadowRadius = 8.0F;
    }

    @Override
    public RenderType getRenderType(RoyalBossEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public Color getRenderColor(RoyalBossEntity animatable, float partialTick, int packedLight) {
        if (!animatable.isRoyalAccelerated()) {
            return Color.ofRGBA(255, 255, 255, 255);
        }
        float pulse = 0.5F + 0.5F * (float) Math.sin((animatable.tickCount + partialTick) * 0.4F);
        int green = 60 + Math.round(60.0F * pulse);
        return Color.ofRGBA(255, green, green, 255);
    }

    @Override
    public void preRender(PoseStack poseStack, RoyalBossEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                          @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.scale(RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE);
        this.shadowRadius = 8.0F * RoyalBossEntity.MODEL_RENDER_SCALE;
    }

    private static final float BEAM_OUTER_RADIUS = 0.55F;
    private static final float BEAM_INNER_RADIUS = 0.26F;
    private static final float BEAM_TILE_BLOCKS = 3.0F;
    private static final float BEAM_SCROLL_SPEED = 0.32F;

    @Override
    public void render(RoyalBossEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        Vec3 end = entity.getRoyalBeamEndPosition();
        if (!entity.isFiringRoyalBeam() || end == null) {
            return;
        }
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        Vec3 start = entity.getRoyalBeamShootFrom(partialTick).subtract(x, y, z);
        Vec3 finish = end.subtract(x, y, z);
        Vec3 axis = finish.subtract(start);
        double length = axis.length();
        if (length < 0.05D) {
            return;
        }
        Vec3 dir = axis.scale(1.0D / length);
        Vec3 side1 = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side1.lengthSqr() < 1.0E-4D) {
            side1 = dir.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        side1 = side1.normalize();
        Vec3 side2 = dir.cross(side1).normalize();

        ResourceLocation outer = QUEEN_BEAM_OUTER;
        ResourceLocation inner = QUEEN_BEAM_INNER;
        ResourceLocation endTexture = QUEEN_BEAM_END;
        if (entity instanceof KingEntity) {
            switch (entity.getRoyalBeamElement()) {
                case FIRE -> {
                    outer = KING_FIRE_BEAM_OUTER;
                    inner = KING_FIRE_BEAM_INNER;
                    endTexture = KING_FIRE_BEAM_END;
                }
                case LIGHTNING -> {
                    outer = KING_LIGHTNING_BEAM_OUTER;
                    inner = KING_LIGHTNING_BEAM_INNER;
                    endTexture = KING_LIGHTNING_BEAM_END;
                }
                case ICE -> {
                    outer = KING_ICE_BEAM_OUTER;
                    inner = KING_ICE_BEAM_INNER;
                    endTexture = KING_ICE_BEAM_END;
                }
                case GENERIC -> {
                }
            }
        }

        float time = entity.tickCount + partialTick;
        float tiles = (float) (length / BEAM_TILE_BLOCKS);
        float scroll = -time * BEAM_SCROLL_SPEED;
        var pose = poseStack.last().pose();

        VertexConsumer outerBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(outer));
        drawBeamPlane(outerBuffer, pose, start, finish, side1.scale(BEAM_OUTER_RADIUS), scroll, scroll + tiles);
        drawBeamPlane(outerBuffer, pose, start, finish, side2.scale(BEAM_OUTER_RADIUS), scroll, scroll + tiles);

        float innerScroll = -time * BEAM_SCROLL_SPEED * 1.6F;
        VertexConsumer innerBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(inner));
        drawBeamPlane(innerBuffer, pose, start, finish, side1.scale(BEAM_INNER_RADIUS), innerScroll, innerScroll + tiles);
        drawBeamPlane(innerBuffer, pose, start, finish, side2.scale(BEAM_INNER_RADIUS), innerScroll, innerScroll + tiles);

        float impactSize = 1.1F + 0.35F * Mth.sin(time * 0.6F);
        Vec3 camLeft = new Vec3(this.entityRenderDispatcher.camera.getLeftVector());
        Vec3 camUp = new Vec3(this.entityRenderDispatcher.camera.getUpVector());
        VertexConsumer endBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(endTexture));
        drawBillboard(endBuffer, pose, finish, camLeft.scale(impactSize), camUp.scale(impactSize));
    }

    private static void drawBeamPlane(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 start, Vec3 end, Vec3 half,
                                      float startV, float endV) {
        emitQuad(vertices, pose,
                start.subtract(half), start.add(half), end.add(half), end.subtract(half),
                0.0F, startV, 1.0F, endV);
        emitQuad(vertices, pose,
                start.add(half), start.subtract(half), end.subtract(half), end.add(half),
                0.0F, startV, 1.0F, endV);
    }

    private static void drawBillboard(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 center, Vec3 left, Vec3 up) {
        emitQuad(vertices, pose,
                center.add(left).add(up), center.subtract(left).add(up),
                center.subtract(left).subtract(up), center.add(left).subtract(up),
                0.0F, 0.0F, 1.0F, 1.0F);
        emitQuad(vertices, pose,
                center.subtract(left).add(up), center.add(left).add(up),
                center.add(left).subtract(up), center.subtract(left).subtract(up),
                0.0F, 0.0F, 1.0F, 1.0F);
    }

    private static void emitQuad(VertexConsumer vertices, org.joml.Matrix4f pose,
                                 Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                 float minU, float minV, float maxU, float maxV) {
        vertex(vertices, pose, a, minU, minV);
        vertex(vertices, pose, b, maxU, minV);
        vertex(vertices, pose, c, maxU, maxV);
        vertex(vertices, pose, d, minU, maxV);
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
