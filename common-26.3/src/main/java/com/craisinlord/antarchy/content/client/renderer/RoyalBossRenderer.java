package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.anthology.beam.client.BeamRenderTypes;
import com.craisinlord.antarchy.content.client.model.RoyalBossModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalHead;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

public class RoyalBossRenderer extends GeoEntityRenderer<RoyalBossEntity, LivingEntityRenderState> {
    private static final Identifier QUEEN_PURPLE_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_outer.png");
    private static final Identifier QUEEN_PURPLE_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_inner.png");
    private static final Identifier QUEEN_PURPLE_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_1.png");
    private static final Identifier QUEEN_PURPLE_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_2.png");
    private static final Identifier QUEEN_RED_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_outer.png");
    private static final Identifier QUEEN_RED_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_inner.png");
    private static final Identifier QUEEN_RED_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_end_1.png");
    private static final Identifier QUEEN_RED_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_red_beam_end_2.png");
    private static final Identifier QUEEN_BLACK_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_outer.png");
    private static final Identifier QUEEN_BLACK_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_inner.png");
    private static final Identifier QUEEN_BLACK_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_end_1.png");
    private static final Identifier QUEEN_BLACK_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_black_beam_end_2.png");
    private static final Identifier KING_FIRE_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final Identifier KING_FIRE_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final Identifier KING_FIRE_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final Identifier KING_FIRE_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_2.png");
    private static final Identifier KING_LIGHTNING_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final Identifier KING_LIGHTNING_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final Identifier KING_LIGHTNING_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final Identifier KING_LIGHTNING_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_2.png");
    private static final Identifier KING_ICE_BEAM_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final Identifier KING_ICE_BEAM_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final Identifier KING_ICE_BEAM_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    private static final Identifier KING_ICE_BEAM_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_2.png");
    private static final DataTicket<BeamData> BEAM_DATA = DataTicket.create("antarchy_royal_boss_beam_data", BeamData.class);
    private static final DataTicket<AfterimageData> AFTERIMAGE_DATA =
            DataTicket.create("antarchy_royal_boss_afterimage_data", AfterimageData.class);

    public RoyalBossRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBossModel());
        this.shadowRadius = 8.0F * RoyalBossEntity.MODEL_RENDER_SCALE;
        this.withRenderLayer(new RoyalAfterimageLayer(this));
        this.withRenderLayer(new TemporalContractionGeoLayer<>(this));
    }

    private record AfterimageData(boolean active, Vec3 movement, boolean queen) {
    }

    private static final class RoyalAfterimageLayer extends GeoRenderLayer<RoyalBossEntity, Void, LivingEntityRenderState> {
        private RoyalAfterimageLayer(GeoRenderer<RoyalBossEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void addRenderData(RoyalBossEntity animatable, Void relatedObject, LivingEntityRenderState renderState,
                                  float partialTick) {
            boolean active = animatable.isRoyalAccelerated() || (RoyalEffectHooks.contractedHolder() != null
                    && animatable.hasEffect(RoyalEffectHooks.contractedHolder()));
            renderState.addGeckolibData(AFTERIMAGE_DATA,
                    new AfterimageData(active, animatable.getDeltaMovement(), animatable instanceof QueenEntity));
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            AfterimageData data = renderPassInfo.getGeckolibData(AFTERIMAGE_DATA);
            if (data == null || !data.active()) return;
            Vec3 movement = data.movement();
            if (movement.lengthSqr() < 1.0E-5D) return;
            RenderType afterimageType = RenderTypes.entityTranslucent(this.getRenderer().getTextureLocation(renderPassInfo.renderState()));
            int[] colors = data.queen()
                    ? new int[]{0x66FF5F6F, 0x4DFF4D60, 0x33FF3B52, 0x1FFF2944}
                    : new int[]{0x66FFD966, 0x4DFFC94D, 0x33FFB52E, 0x1FFF8C14};
            float scale = RoyalBossEntity.MODEL_RENDER_SCALE;
            Matrix4f scaledObjectPose = new Matrix4f(renderPassInfo.getPreRenderMatrixState()).scale(scale);
            Matrix4f modelRelativePose = new Matrix4f(scaledObjectPose).invert().mul(renderPassInfo.getModelRenderMatrixState());
            for (int i = colors.length - 1; i >= 0; i--) {
                PoseStack afterimagePose = new PoseStack();
                afterimagePose.last().set(renderPassInfo.getPreRenderMatrixPose());
                afterimagePose.scale(scale, scale, scale);
                Vec3 offset = movement.scale(-(i + 1) * 3.0D / RoyalBossEntity.MODEL_RENDER_SCALE);
                afterimagePose.translate(offset.x, offset.y, offset.z);
                afterimagePose.scale(scale, scale, scale);
                afterimagePose.mulPose(modelRelativePose);
                int colour = colors[i];
                renderTasks.submitCustomGeometry(afterimagePose, afterimageType, (pose, buffer) -> {
                    PoseStack modelPoseStack = renderPassInfo.poseStack();
                    modelPoseStack.pushPose();
                    modelPoseStack.last().set(pose);
                    renderPassInfo.renderPosed(() -> renderPassInfo.model().render(
                            renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, colour));
                    modelPoseStack.popPose();
                });
            }
        }
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public int getRenderColor(RoyalBossEntity animatable, Void relatedObject, float partialTick) {
        if (!animatable.isRoyalAccelerated()) {
            return ARGB.color(255, 255, 255, 255);
        }
        float pulse = 0.5F + 0.5F * (float) Math.sin((animatable.tickCount + partialTick) * 0.4F);
        int green = 60 + Math.round(60.0F * pulse);
        return ARGB.color(255, 255, green, green);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        renderPassInfo.poseStack().scale(RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE, RoyalBossEntity.MODEL_RENDER_SCALE);
        BeamData beams = renderPassInfo.getGeckolibData(BEAM_DATA);
        if (beams == null) return;
        for (int i = 0; i < beams.slots.length; i++) {
            if (beams.slots[i] == null) continue;
            BeamAnchor anchor = beams.slots[i].anchor;
            renderPassInfo.addBonePositionListener(RoyalBossModel.BEAM_ANCHORS[i],
                    (worldPos, modelPos, localPos) -> anchor.offer(worldPos));
        }
    }

    private static final float BEAM_OUTER_RADIUS = 1.75F;
    private static final float BEAM_INNER_RADIUS = 1.5F;

    @Override
    public void addRenderData(RoyalBossEntity entity, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(entity, relatedObject, renderState, partialTick);
        BeamData beams = new BeamData();
        boolean any = false;
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            Vec3 end = entity.getRoyalBeamEndPosition(slot);
            if (entity.isFiringRoyalBeam(slot) && end != null) {
                beams.slots[slot.ordinal()] = extractBeam(entity, slot, end, partialTick);
                any = true;
            }
        }
        renderState.addGeckolibData(BEAM_DATA, any ? beams : null);
    }

    private static BeamSlot extractBeam(RoyalBossEntity entity, RoyalHead.Slot beamHead, Vec3 end, float partialTick) {
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        Identifier outer = QUEEN_PURPLE_BEAM_OUTER;
        Identifier inner = QUEEN_PURPLE_BEAM_INNER;
        Identifier[] endTextures = {QUEEN_PURPLE_BEAM_END_1, QUEEN_PURPLE_BEAM_END_2};
        if (entity instanceof KingEntity) {
            switch (entity.getRoyalBeamElement()) {
                case FIRE -> {
                    outer = KING_FIRE_BEAM_OUTER;
                    inner = KING_FIRE_BEAM_INNER;
                    endTextures = new Identifier[]{KING_FIRE_BEAM_END_1, KING_FIRE_BEAM_END_2};
                }
                case LIGHTNING -> {
                    outer = KING_LIGHTNING_BEAM_OUTER;
                    inner = KING_LIGHTNING_BEAM_INNER;
                    endTextures = new Identifier[]{KING_LIGHTNING_BEAM_END_1, KING_LIGHTNING_BEAM_END_2};
                }
                case ICE -> {
                    outer = KING_ICE_BEAM_OUTER;
                    inner = KING_ICE_BEAM_INNER;
                    endTextures = new Identifier[]{KING_ICE_BEAM_END_1, KING_ICE_BEAM_END_2};
                }
                case GENERIC -> {
                }
            }
        } else {
            switch (entity.getRoyalBeamElement(beamHead)) {
                case QUEEN_RED -> {
                    outer = QUEEN_RED_BEAM_OUTER;
                    inner = QUEEN_RED_BEAM_INNER;
                    endTextures = new Identifier[]{QUEEN_RED_BEAM_END_1, QUEEN_RED_BEAM_END_2};
                }
                case QUEEN_BLACK -> {
                    outer = QUEEN_BLACK_BEAM_OUTER;
                    inner = QUEEN_BLACK_BEAM_INNER;
                    endTextures = new Identifier[]{QUEEN_BLACK_BEAM_END_1, QUEEN_BLACK_BEAM_END_2};
                }
                default -> {
                }
            }
        }
        float time = entity.tickCount + partialTick;
        Identifier endTexture = endTextures[(entity.tickCount / 2) % endTextures.length];
        return new BeamSlot(new BeamAnchor(), entity.getRoyalBeamShootFrom(beamHead, partialTick), end,
                new Vec3(x, y, z), inner, outer, endTexture, -time * 0.25F * 0.5F, -time * 0.25F);
    }

    @Override
    public void submit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks,
                       CameraRenderState cameraState) {
        super.submit(renderState, poseStack, renderTasks, cameraState);
        BeamData beams = renderState.getGeckolibData(BEAM_DATA);
        if (beams == null) return;
        for (BeamSlot slot : beams.slots) {
            if (slot != null) {
                submitBeam(poseStack, renderTasks, slot, BEAM_INNER_RADIUS, BEAM_OUTER_RADIUS, BEAM_INNER_RADIUS);
            }
        }
    }

    static final class BeamAnchor {
        @Nullable
        private Vec3 position;

        void offer(@Nullable Vec3 worldPosition) {
            if (this.position == null && worldPosition != null) {
                this.position = worldPosition;
            }
        }
    }

    static final class BeamData {
        final BeamSlot[] slots = new BeamSlot[RoyalHead.Slot.values().length];
    }

    record BeamSlot(BeamAnchor anchor, Vec3 fallbackStart, Vec3 end, Vec3 origin, Identifier inner, Identifier outer,
                    Identifier endTexture, float innerV, float outerV) {
    }

    static void submitBeam(PoseStack poseStack, SubmitNodeCollector renderTasks, BeamSlot slot,
                           float innerRadius, float outerRadius, float impactSize) {
        renderTasks.submitCustomGeometry(poseStack, BeamRenderTypes.beam(slot.inner()), (pose, buffer) -> {
            BeamFrame frame = beamFrame(pose, slot);
            if (frame != null) drawLocalBeam(buffer, frame.pose(), frame.length(), innerRadius, 4, slot.innerV(), 0.5F);
        });
        renderTasks.submitCustomGeometry(poseStack, BeamRenderTypes.beam(slot.outer()), (pose, buffer) -> {
            BeamFrame frame = beamFrame(pose, slot);
            if (frame != null) drawLocalBeam(buffer, frame.pose(), frame.length(), outerRadius, 8, slot.outerV(), 0.15F);
        });
        renderTasks.submitCustomGeometry(poseStack, BeamRenderTypes.beam(slot.endTexture()), (pose, buffer) -> {
            BeamFrame frame = beamFrame(pose, slot);
            if (frame != null) drawBeamImpact(buffer, frame.pose(), (float) frame.length() - 1.5F, impactSize);
        });
    }

    private record BeamFrame(Matrix4f pose, double length) {
    }

    @Nullable
    private static BeamFrame beamFrame(PoseStack.Pose pose, BeamSlot slot) {
        Vec3 anchor = slot.anchor().position;
        Vec3 origin = slot.origin();
        Vec3 start = (anchor != null ? anchor : slot.fallbackStart()).subtract(origin);
        Vec3 finish = slot.end().subtract(origin);
        Vec3 axis = finish.subtract(start);
        double length = axis.length();
        if (length < 0.05D) {
            return null;
        }
        Vec3 dir = axis.scale(1.0D / length);
        float yaw = (float) Math.atan2(dir.z, dir.x);
        float pitch = (float) Math.acos(dir.y);
        PoseStack beamPose = new PoseStack();
        beamPose.last().set(pose);
        beamPose.translate(start.x, start.y, start.z);
        beamPose.rotate(Axis.YP.rotation((float) (Math.PI * 0.5D - yaw)));
        beamPose.rotate(Axis.XP.rotation((float) (-Math.PI * 0.5D + pitch)));
        beamPose.rotate(Axis.ZP.rotationDegrees(45.0F));
        return new BeamFrame(new Matrix4f(beamPose.last().pose()), length);
    }

    private static void drawLocalBeam(VertexConsumer vertices, Matrix4f pose, double length,
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

    private static void emitBeamQuad(VertexConsumer vertices, Matrix4f pose,
                                     Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                     float minU, float minV, float maxU, float maxV, int endAlpha) {
        beamVertex(vertices, pose, a, minU, minV, 255);
        beamVertex(vertices, pose, b, maxU, minV, endAlpha);
        beamVertex(vertices, pose, c, maxU, maxV, endAlpha);
        beamVertex(vertices, pose, d, minU, maxV, 255);
    }

    static void drawBeamImpact(VertexConsumer vertices, Matrix4f pose, float z, float size) {
        PoseStack endpoint = new PoseStack();
        endpoint.last().pose().set(pose);
        endpoint.translate(0.0D, 0.0D, z);
        endpoint.rotate(Axis.ZP.rotationDegrees(45.0F));
        endpoint.rotate(Axis.XP.rotationDegrees(90.0F));
        endpoint.scale(size / 16.0F, size / 16.0F, size / 16.0F);
        endpoint.translate(0.0D, 24.0D, 0.0D);

        Matrix4f modelPose = endpoint.last().pose();
        emitQuadUv(vertices, modelPose,
                new Vec3(-11.0D, 0.0D, -11.0D), new Vec3(11.0D, 0.0D, -11.0D),
                new Vec3(11.0D, 0.0D, 11.0D), new Vec3(-11.0D, 0.0D, 11.0D),
                0.0F, 0.0F, 22.0F / 128.0F, 22.0F / 128.0F);

        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, -11.0D);
        endpoint.rotate(Axis.XP.rotation(0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), -12, -24, 0, 24, 24, 3, 38);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, -11.0D);
        endpoint.rotate(Axis.XP.rotation(0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), -15, -16, 0, 30, 16, 0, 22);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, 11.0D);
        endpoint.rotate(Axis.XP.rotation(-0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), -12, -24, 0, 24, 24, 3, 38);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(0.0D, 0.0D, 11.0D);
        endpoint.rotate(Axis.XP.rotation(-0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), -15, -16, 0, 30, 16, 0, 22);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(11.0D, 0.0D, 0.0D);
        endpoint.rotate(Axis.ZP.rotation(0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -24, -13, 24, 24, 3, 14);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(11.0D, 0.0D, 0.0D);
        endpoint.rotate(Axis.ZP.rotation(0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -16, -15, 16, 30, 0, -8);
        endpoint.popPose();

        endpoint.pushPose();
        endpoint.translate(-11.0D, 0.0D, 0.0D);
        endpoint.rotate(Axis.ZP.rotation(-0.3927F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -24, -12, 24, 24, 3, 14);
        endpoint.popPose();
        endpoint.pushPose();
        endpoint.translate(-11.0D, 0.0D, 0.0D);
        endpoint.rotate(Axis.ZP.rotation(-0.7854F));
        drawModelPlane(vertices, endpoint.last().pose(), 0, -16, -15, 16, 30, 0, -8);
        endpoint.popPose();
    }

    private static void drawModelPlane(VertexConsumer vertices, Matrix4f pose,
                                       int x, int y, int z, int width, int height, int u, int v) {
        emitQuadUv(vertices, pose,
                new Vec3(x + width, y, z), new Vec3(x, y, z),
                new Vec3(x, y + height, z), new Vec3(x + width, y + height, z),
                (u + width) / 128.0F, v / 128.0F, u / 128.0F, (v + height) / 128.0F);
    }

    private static void emitQuadUv(VertexConsumer vertices, Matrix4f pose,
                                   Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                   float minU, float minV, float maxU, float maxV) {
        vertex(vertices, pose, a, minU, minV);
        vertex(vertices, pose, b, maxU, minV);
        vertex(vertices, pose, c, maxU, maxV);
        vertex(vertices, pose, d, minU, maxV);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f pose, Vec3 pos, float u, float v) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void beamVertex(VertexConsumer vertices, Matrix4f pose, Vec3 pos, float u, float v, int alpha) {
        vertices.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(1.0F, 1.0F, 1.0F, alpha / 255.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
