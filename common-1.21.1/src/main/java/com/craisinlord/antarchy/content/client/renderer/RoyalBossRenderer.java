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
import com.mojang.math.Axis;
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
        float innerV = -time * 0.25F * 0.5F;

        // Tremorzilla renders the beam in local +Z space. Keeping the mesh in that
        // space is important: the beam textures are authored for these exact face
        // coordinates and are not interchangeable with a world-space billboard.
        float yaw = (float) Math.atan2(dir.z, dir.x);
        float pitch = (float) Math.acos(dir.y);
        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);
        poseStack.mulPose(Axis.YP.rotation((float) (Math.PI * 0.5D - yaw)));
        poseStack.mulPose(Axis.XP.rotation((float) (-Math.PI * 0.5D + pitch)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        org.joml.Matrix4f pose = poseStack.last().pose();

        VertexConsumer innerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(inner));
        drawLocalBeam(innerBuffer, pose, length, BEAM_INNER_RADIUS, 4, innerV, 0.5F);
        VertexConsumer outerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(outer));
        drawLocalBeam(outerBuffer, pose, length, BEAM_OUTER_RADIUS, 8, outerV, 0.15F);

        ResourceLocation endTexture = endTextures[(entity.tickCount / 2) % endTextures.length];
        VertexConsumer endBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(endTexture));
        drawBeamImpact(endBuffer, pose, (float) length - 1.5F, BEAM_INNER_RADIUS);
        poseStack.popPose();
    }

    private static void drawLocalBeam(VertexConsumer vertices, org.joml.Matrix4f pose, double length,
                                      float radius, int sections, float scroll, float uvLengthScale) {
        float endV = scroll + (float) length * uvLengthScale;
        float previousX = -radius;
        float previousY = 0.0F;
        float previousU = 0.0F;
        for (int i = 0; i <= sections; i++) {
            float angle = (float) (Math.PI + Math.PI * 2.0D * i / sections);
            float currentX = Mth.cos(angle) * radius;
            float currentY = Mth.sin(angle) * radius;
            Vec3 previousStart = new Vec3(previousX * 0.55D, previousY * 0.55D, 0.0D);
            Vec3 previousEnd = new Vec3(previousX, previousY, length);
            Vec3 currentEnd = new Vec3(currentX, currentY, length);
            Vec3 currentStart = new Vec3(currentX * 0.55D, currentY * 0.55D, 0.0D);
            emitBeamQuad(vertices, pose, previousStart, previousEnd, currentEnd, currentStart,
                    previousU, scroll, i + 1.0F, endV, 255);
            previousX = currentX;
            previousY = currentY;
            previousU = i + 1.0F;
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

    static void drawBeamImpact(VertexConsumer vertices, org.joml.Matrix4f pose, float z, float size) {
        PoseStack endpoint = new PoseStack();
        endpoint.last().pose().set(pose);
        endpoint.translate(0.0D, 0.0D, z);
        endpoint.mulPose(Axis.ZP.rotationDegrees(45.0F));
        endpoint.mulPose(Axis.XP.rotationDegrees(90.0F));
        endpoint.scale(size / 16.0F, size / 16.0F, size / 16.0F);
        endpoint.translate(0.0D, 24.0D, 0.0D);

        org.joml.Matrix4f modelPose = endpoint.last().pose();
        emitQuadUv(vertices, modelPose,
                new Vec3(-11.0D, 0.0D, -11.0D), new Vec3(11.0D, 0.0D, -11.0D),
                new Vec3(11.0D, 0.0D, 11.0D), new Vec3(-11.0D, 0.0D, 11.0D),
                0.0F, 0.0F, 22.0F / 128.0F, 22.0F / 128.0F);

        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, -11.0D);
        endpoint.mulPose(Axis.XP.rotation(0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), -12, -24, 0, 24, 24, 3, 38);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, -11.0D);
        endpoint.mulPose(Axis.XP.rotation(0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), -15, -16, 0, 30, 16, 0, 22);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, 11.0D);
        endpoint.mulPose(Axis.XP.rotation(-0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), -12, -24, 0, 24, 24, 3, 38);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, 11.0D);
        endpoint.mulPose(Axis.XP.rotation(-0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), -15, -16, 0, 30, 16, 0, 22);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(11.0D, 0.0D, 0.0D);
        endpoint.mulPose(Axis.ZP.rotation(0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -24, -13, 24, 24, 3, 14);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(11.0D, 0.0D, 0.0D);
        endpoint.mulPose(Axis.ZP.rotation(0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -16, -15, 16, 30, 0, -8);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(-11.0D, 0.0D, 0.0D);
        endpoint.mulPose(Axis.ZP.rotation(-0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -24, -12, 24, 24, 3, 14);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(-11.0D, 0.0D, 0.0D);
        endpoint.mulPose(Axis.ZP.rotation(-0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -16, -15, 16, 30, 0, -8);
        endpoint.popPose();
    }

    private static void drawModelPlane(VertexConsumer vertices, org.joml.Matrix4f pose,
                                       int x, int y, int z, int width, int height, int u, int v) {
        emitQuadUv(vertices, pose,
                new Vec3(x + width, y, z), new Vec3(x, y, z),
                new Vec3(x, y + height, z), new Vec3(x + width, y + height, z),
                (u + width) / 128.0F, v / 128.0F, u / 128.0F, (v + height) / 128.0F);
    }

    private static void emitQuadUv(VertexConsumer vertices, org.joml.Matrix4f pose,
                                   Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                   float minU, float minV, float maxU, float maxV) {
        vertex(vertices, pose, a, minU, minV);
        vertex(vertices, pose, b, maxU, minV);
        vertex(vertices, pose, c, maxU, maxV);
        vertex(vertices, pose, d, minU, maxV);
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void beamVertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v, int alpha) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(1.0F, 1.0F, 1.0F, alpha / 255.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
