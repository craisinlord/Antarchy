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
    private static final ResourceLocation QUEEN_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_1.png");
    private static final ResourceLocation QUEEN_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_2.png");
    private static final ResourceLocation KING_FIRE_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final ResourceLocation KING_FIRE_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final ResourceLocation KING_FIRE_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final ResourceLocation KING_FIRE_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_2.png");
    private static final ResourceLocation KING_LIGHTNING_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final ResourceLocation KING_LIGHTNING_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final ResourceLocation KING_LIGHTNING_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final ResourceLocation KING_LIGHTNING_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_2.png");
    private static final ResourceLocation KING_ICE_OUTER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final ResourceLocation KING_ICE_INNER = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final ResourceLocation KING_ICE_END_1 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    private static final ResourceLocation KING_ICE_END_2 = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_2.png");
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
        int beamHead = entity.getBeamHead();
        Vec3 trackedAnchor = ((RoyalMountModel) this.getGeoModel()).getTrackedBeamAnchor(beamHead);
        Vec3 start = (trackedAnchor != null ? trackedAnchor : entity.beamShootFrom()).subtract(x, y, z);
        Vec3 finish = end.subtract(x, y, z);
        Vec3 axis = finish.subtract(start);
        double length = axis.length();
        if (length < 0.05D) return;
        Vec3 dir = axis.scale(1.0D / length);
        Vec3 side = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-4D) side = dir.cross(new Vec3(1.0D, 0.0D, 0.0D));
        side = side.normalize();
        Vec3 side2 = dir.cross(side).normalize();
        ResourceLocation outer = QUEEN_OUTER;
        ResourceLocation inner = QUEEN_INNER;
        ResourceLocation[] endTextures = {QUEEN_END_1, QUEEN_END_2};
        if (entity instanceof PrinceEntity) {
            switch (entity.getBeamElement()) {
                case FIRE -> { outer = KING_FIRE_OUTER; inner = KING_FIRE_INNER; endTextures = new ResourceLocation[]{KING_FIRE_END_1, KING_FIRE_END_2}; }
                case ICE -> { outer = KING_ICE_OUTER; inner = KING_ICE_INNER; endTextures = new ResourceLocation[]{KING_ICE_END_1, KING_ICE_END_2}; }
                default -> { outer = KING_LIGHTNING_OUTER; inner = KING_LIGHTNING_INNER; endTextures = new ResourceLocation[]{KING_LIGHTNING_END_1, KING_LIGHTNING_END_2}; }
            }
        }
        float time = entity.tickCount + partialTick;
        float outerV = -time * 0.25F;
        float innerV = -time * 0.25F * 0.5F;
        float yaw = (float) Math.atan2(dir.z, dir.x);
        float pitch = (float) Math.acos(dir.y);
        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotation((float) (Math.PI * 0.5D - yaw)));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotation((float) (-Math.PI * 0.5D + pitch)));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(45.0F));
        org.joml.Matrix4f pose = poseStack.last().pose();
        VertexConsumer innerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(inner));
        drawLocalBeam(innerBuffer, pose, length, 0.375F, 4, innerV, 0.5F);
        VertexConsumer outerBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(outer));
        drawLocalBeam(outerBuffer, pose, length, 0.44F, 8, outerV, 0.15F);
        ResourceLocation endTexture = endTextures[(entity.tickCount / 2) % endTextures.length];
        VertexConsumer endBuffer = bufferSource.getBuffer(RoyalBeamRenderTypes.beam(endTexture));
        RoyalBossRenderer.drawBeamImpact(endBuffer, pose, (float) length - 1.5F, 0.375F);
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
            emitBeamQuad(vertices, pose,
                    new Vec3(previousX * 0.55D, previousY * 0.55D, 0.0D),
                    new Vec3(previousX, previousY, length),
                    new Vec3(currentX, currentY, length),
                    new Vec3(currentX * 0.55D, currentY * 0.55D, 0.0D),
                    previousU, scroll, i + 1.0F, endV, 255);
            previousX = currentX;
            previousY = currentY;
            previousU = i + 1.0F;
        }
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
            emitBeamQuad(vertices, pose, startRing[i], endRing[i], endRing[next], startRing[next],
                    i / (float) sections, scroll, next / (float) sections, endV, endAlpha);
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

    private static void drawBeamImpact(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 center, Vec3 direction,
                                       Vec3 side1, Vec3 side2, float size) {
        drawBillboard(vertices, pose, center, side1.scale(size), side2.scale(size));
        for (int i = 0; i < 8; i++) {
            double angle = (i + 0.5D) * Math.PI * 0.25D;
            Vec3 radial = side1.scale(Math.cos(angle)).add(side2.scale(Math.sin(angle))).scale(size);
            Vec3 back = direction.scale(size * (i % 2 == 0 ? 1.5D : 1.0D));
            emitQuad(vertices, pose, center.add(radial), center.subtract(radial),
                    center.subtract(radial).subtract(back), center.add(radial).subtract(back));
        }
    }

    private static void drawBillboard(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 center, Vec3 left, Vec3 up) {
        emitQuad(vertices, pose, center.add(left).add(up), center.subtract(left).add(up),
                center.subtract(left).subtract(up), center.add(left).subtract(up));
        emitQuad(vertices, pose, center.subtract(left).add(up), center.add(left).add(up),
                center.add(left).subtract(up), center.subtract(left).subtract(up));
    }

    private static void emitQuad(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        vertex(vertices, pose, a, 0.0F, 0.0F);
        vertex(vertices, pose, b, 1.0F, 0.0F);
        vertex(vertices, pose, c, 1.0F, 1.0F);
        vertex(vertices, pose, d, 0.0F, 1.0F);
    }

    private static void vertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void beamVertex(VertexConsumer vertices, org.joml.Matrix4f pose, Vec3 pos, float u, float v, int alpha) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).setColor(1.0F, 1.0F, 1.0F, alpha / 255.0F).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0.0F, -1.0F, 0.0F);
    }
}
