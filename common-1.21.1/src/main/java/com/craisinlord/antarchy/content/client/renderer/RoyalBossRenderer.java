package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBossModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalHead;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

public class RoyalBossRenderer extends GeoEntityRenderer<RoyalBossEntity> {
    private static final ResourceLocation QUEEN_PURPLE_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_outer.png");
    private static final ResourceLocation QUEEN_PURPLE_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_inner.png");
    private static final ResourceLocation QUEEN_PURPLE_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_1.png");
    private static final ResourceLocation QUEEN_PURPLE_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_2.png");
    private static final ResourceLocation QUEEN_RED_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_outer.png");
    private static final ResourceLocation QUEEN_RED_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_inner.png");
    private static final ResourceLocation QUEEN_RED_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_end_1.png");
    private static final ResourceLocation QUEEN_RED_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_end_2.png");
    private static final ResourceLocation QUEEN_BLACK_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_outer.png");
    private static final ResourceLocation QUEEN_BLACK_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_inner.png");
    private static final ResourceLocation QUEEN_BLACK_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_end_1.png");
    private static final ResourceLocation QUEEN_BLACK_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_end_2.png");
    private static final ResourceLocation KING_FIRE_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final ResourceLocation KING_FIRE_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final ResourceLocation KING_FIRE_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final ResourceLocation KING_FIRE_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_2.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final ResourceLocation KING_LIGHTNING_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_2.png");
    private static final ResourceLocation KING_ICE_BEAM_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final ResourceLocation KING_ICE_BEAM_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final ResourceLocation KING_ICE_BEAM_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    private static final ResourceLocation KING_ICE_BEAM_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_2.png");
    public RoyalBossRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBossModel());
        this.shadowRadius = 8.0F;
        this.addRenderLayer(new RoyalAfterimageLayer(this));
        this.addRenderLayer(new TemporalContractionGeoLayer<>(this));
    }

    private static final class RoyalAfterimageLayer extends GeoRenderLayer<RoyalBossEntity> {
        private RoyalAfterimageLayer(RoyalBossRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, RoyalBossEntity animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {
            if (!animatable.isRoyalAccelerated() && (RoyalEffectHooks.contractedHolder() == null
                    || !animatable.hasEffect(RoyalEffectHooks.contractedHolder()))) return;
            Vec3 movement = animatable.getDeltaMovement();
            if (movement.lengthSqr() < 1.0E-5D) return;
            RenderType afterimageType = RenderType.entityTranslucent(this.getRenderer().getTextureLocation(animatable));
            VertexConsumer afterimageBuffer = bufferSource.getBuffer(afterimageType);
            int[] colors = animatable instanceof QueenEntity
                    ? new int[]{0x66FF5F6F, 0x4DFF4D60, 0x33FF3B52, 0x1FFF2944}
                    : new int[]{0x66FFD966, 0x4DFFC94D, 0x33FFB52E, 0x1FFF8C14};
            for (int i = colors.length - 1; i >= 0; i--) {
                poseStack.pushPose();
                Vec3 offset = movement.scale(-(i + 1) * 3.0D / RoyalBossEntity.MODEL_RENDER_SCALE);
                poseStack.translate(offset.x, offset.y, offset.z);
                this.getRenderer().reRender(
                        bakedModel,
                        poseStack,
                        bufferSource,
                        animatable,
                        afterimageType,
                        afterimageBuffer,
                        partialTick,
                        0xF000F0,
                        OverlayTexture.NO_OVERLAY,
                        colors[i]
                );
                poseStack.popPose();
            }
        }
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

    private static final float BEAM_OUTER_RADIUS = 1.75F;
    private static final float BEAM_INNER_RADIUS = 1.5F;

    @Override
    public void render(RoyalBossEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            if (entity.isFiringRoyalBeam(slot) && entity.getRoyalBeamEndPosition(slot) != null) {
                renderBeam(entity, slot, partialTick, poseStack, bufferSource);
            }
        }
    }

    private void renderBeam(RoyalBossEntity entity, RoyalHead.Slot beamHead, float partialTick,
                            PoseStack poseStack, MultiBufferSource bufferSource) {
        Vec3 end = entity.getRoyalBeamEndPosition(beamHead);
        if (end == null) return;
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        Vec3 trackedAnchor = ((RoyalBossModel) this.getGeoModel()).getTrackedBeamAnchor(beamHead);
        Vec3 start = (trackedAnchor != null ? trackedAnchor : entity.getRoyalBeamShootFrom(beamHead, partialTick))
                .subtract(x, y, z);
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

        ResourceLocation outer = QUEEN_PURPLE_BEAM_OUTER;
        ResourceLocation inner = QUEEN_PURPLE_BEAM_INNER;
        ResourceLocation[] endTextures = {QUEEN_PURPLE_BEAM_END_1, QUEEN_PURPLE_BEAM_END_2};
        if (entity instanceof KingEntity) {
            switch (entity.getRoyalBeamElement()) {
                case FIRE -> {
                    outer = KING_FIRE_BEAM_OUTER;
                    inner = KING_FIRE_BEAM_INNER;
                    endTextures = new ResourceLocation[]{KING_FIRE_BEAM_END_1, KING_FIRE_BEAM_END_2};
                }
                case LIGHTNING -> {
                    outer = KING_LIGHTNING_BEAM_OUTER;
                    inner = KING_LIGHTNING_BEAM_INNER;
                    endTextures = new ResourceLocation[]{KING_LIGHTNING_BEAM_END_1, KING_LIGHTNING_BEAM_END_2};
                }
                case ICE -> {
                    outer = KING_ICE_BEAM_OUTER;
                    inner = KING_ICE_BEAM_INNER;
                    endTextures = new ResourceLocation[]{KING_ICE_BEAM_END_1, KING_ICE_BEAM_END_2};
                }
                case GENERIC -> {
                }
            }
        } else {
            switch (entity.getRoyalBeamElement(beamHead)) {
                case QUEEN_RED -> {
                    outer = QUEEN_RED_BEAM_OUTER;
                    inner = QUEEN_RED_BEAM_INNER;
                    endTextures = new ResourceLocation[]{QUEEN_RED_BEAM_END_1, QUEEN_RED_BEAM_END_2};
                }
                case QUEEN_BLACK -> {
                    outer = QUEEN_BLACK_BEAM_OUTER;
                    inner = QUEEN_BLACK_BEAM_INNER;
                    endTextures = new ResourceLocation[]{QUEEN_BLACK_BEAM_END_1, QUEEN_BLACK_BEAM_END_2};
                }
                default -> {
                }
            }
        }

        float time = entity.tickCount + partialTick;
        float outerV = -time * 0.25F;
        var pose = poseStack.last().pose();

        float innerV = -time * 0.25F * 0.5F;
        VertexConsumer innerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(inner));
        drawBeamTube(innerBuffer, pose, start, finish, side1, side2, BEAM_INNER_RADIUS, 4, innerV, 0.0F, 0.55F, 0.5F, 255);

        VertexConsumer outerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(outer));
        // Tremorzilla's outer shell stays fully visible all the way to the impact;
        // fading the far ring makes the beam look like a translucent cone.
        drawBeamTube(outerBuffer, pose, start, finish, side1, side2, BEAM_OUTER_RADIUS, 8, outerV, 0.0F, 0.55F, 0.15F, 255);

        float impactSize = 1.5F + 0.35F * Mth.sin(time * 0.6F);
        ResourceLocation endTexture = endTextures[(entity.tickCount / 2) % endTextures.length];
        VertexConsumer endBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(endTexture));
        drawBeamImpact(endBuffer, pose, finish.subtract(dir.scale(1.5D)), dir, side1, side2, impactSize);
    }

    private static void drawBeamTube(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 start, Vec3 end,
                                     Vec3 side1, Vec3 side2, float radius, int sections, float scroll, float phase,
                                     float startScale, float uvLengthScale, int endAlpha) {
        Vec3[] startRing = new Vec3[sections];
        Vec3[] endRing = new Vec3[sections];
        for (int i = 0; i < sections; i++) {
            double angle = Math.PI * 2.0D * i / sections + phase;
            Vec3 offset = side1.scale(Math.cos(angle) * radius).add(side2.scale(Math.sin(angle) * radius));
            startRing[i] = start.add(offset.scale(startScale));
            endRing[i] = end.add(offset);
        }
        float endV = scroll + (float) start.distanceTo(end) * uvLengthScale;
        for (int i = 0; i < sections; i++) {
            int next = (i + 1) % sections;
            // Alex's beam renderer intentionally uses section indices for U rather
            // than normalizing them. This is part of the authored 16x16 texture
            // pattern and keeps the strip continuous around the tube.
            emitBeamQuad(vertices, pose, startRing[i], endRing[i], endRing[next], startRing[next],
                    i, scroll, next, endV, endAlpha);
        }
    }

    private static void emitBeamQuad(VertexConsumer vertices, org.joml.Matrix4f pose,
                                     Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                     float minU, float minV, float maxU, float maxV, int endAlpha) {
        beamVertex(vertices, pose, a, minU, minV, 255);
        beamVertex(vertices, pose, b, maxU, minV, endAlpha);
        beamVertex(vertices, pose, c, maxU, maxV, endAlpha);
        beamVertex(vertices, pose, d, minU, maxV, 255);
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

    private static void drawBeamImpact(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 center, Vec3 direction,
                                       Vec3 side1, Vec3 side2, float size) {
        // The end textures are atlas textures used by Tremorzilla's crossed flame
        // planes, not a texture meant to be repeated on eight radial billboards.
        // Two crossed planes preserve the same readable four-point impact without
        // stretching the atlas into the noisy starburst the old code produced.
        drawBillboard(vertices, pose, center, side1.scale(size), side2.scale(size));
        drawBillboard(vertices, pose, center.subtract(direction.scale(size * 0.35D)),
                side1.scale(size * 0.82D), direction.scale(size * 0.82D));
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

    private static void beamVertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v, int alpha) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
