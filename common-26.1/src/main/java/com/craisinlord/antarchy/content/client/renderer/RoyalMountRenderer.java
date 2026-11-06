package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalMountModel;
import com.craisinlord.antarchy.content.entity.royal.PrinceEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RoyalMountRenderer extends GeoEntityRenderer<RoyalMountEntity, LivingEntityRenderState> {
    private static final Identifier QUEEN_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_outer.png");
    private static final Identifier QUEEN_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_inner.png");
    private static final Identifier QUEEN_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_1.png");
    private static final Identifier QUEEN_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/queen/queen_purple_beam_end_2.png");
    private static final Identifier KING_FIRE_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_outer.png");
    private static final Identifier KING_FIRE_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_inner.png");
    private static final Identifier KING_FIRE_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_1.png");
    private static final Identifier KING_FIRE_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/fire_beam_end_2.png");
    private static final Identifier KING_LIGHTNING_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_outer.png");
    private static final Identifier KING_LIGHTNING_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_inner.png");
    private static final Identifier KING_LIGHTNING_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_1.png");
    private static final Identifier KING_LIGHTNING_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/lightning_beam_end_2.png");
    private static final Identifier KING_ICE_OUTER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_outer.png");
    private static final Identifier KING_ICE_INNER = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_inner.png");
    private static final Identifier KING_ICE_END_1 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_1.png");
    private static final Identifier KING_ICE_END_2 = Identifier.fromNamespaceAndPath("antarchy", "textures/entity/king/ice_beam_end_2.png");
    private static final DataTicket<Float> GROWTH_SCALE = DataTicket.create("antarchy_royal_mount_growth_scale", Float.class);
    private static final DataTicket<Integer> BEAM_HEAD = DataTicket.create("antarchy_royal_mount_beam_head", Integer.class);
    private static final DataTicket<RoyalBossRenderer.BeamSlot> BEAM =
            DataTicket.create("antarchy_royal_mount_beam", RoyalBossRenderer.BeamSlot.class);

    public RoyalMountRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalMountModel());
        this.shadowRadius = 1.1F;
        this.withRenderLayer(new TemporalContractionGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public void extractRenderState(RoyalMountEntity entity, LivingEntityRenderState state, float partialTick) {
        this.shadowRadius = 1.1F * entity.getGrowthScale();
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void addRenderData(RoyalMountEntity entity, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(entity, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(GROWTH_SCALE, entity.getGrowthScale());
        renderState.addGeckolibData(BEAM_HEAD, entity.getBeamHead());
        renderState.addGeckolibData(BEAM, extractBeam(entity, partialTick));
    }

    private static RoyalBossRenderer.BeamSlot extractBeam(RoyalMountEntity entity, float partialTick) {
        Vec3 end = entity.getBeamEndPosition();
        if (!entity.isFiringBeam() || end == null) return null;
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY());
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        Identifier outer = QUEEN_OUTER;
        Identifier inner = QUEEN_INNER;
        Identifier[] endTextures = {QUEEN_END_1, QUEEN_END_2};
        if (entity instanceof PrinceEntity) {
            switch (entity.getBeamElement()) {
                case FIRE -> { outer = KING_FIRE_OUTER; inner = KING_FIRE_INNER; endTextures = new Identifier[]{KING_FIRE_END_1, KING_FIRE_END_2}; }
                case ICE -> { outer = KING_ICE_OUTER; inner = KING_ICE_INNER; endTextures = new Identifier[]{KING_ICE_END_1, KING_ICE_END_2}; }
                default -> { outer = KING_LIGHTNING_OUTER; inner = KING_LIGHTNING_INNER; endTextures = new Identifier[]{KING_LIGHTNING_END_1, KING_LIGHTNING_END_2}; }
            }
        }
        float time = entity.tickCount + partialTick;
        Identifier endTexture = endTextures[(entity.tickCount / 2) % endTextures.length];
        return new RoyalBossRenderer.BeamSlot(new RoyalBossRenderer.BeamAnchor(), entity.beamShootFrom(), end,
                new Vec3(x, y, z), inner, outer, endTexture, -time * 0.25F * 0.5F, -time * 0.25F);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        float scale = renderPassInfo.getOrDefaultGeckolibData(GROWTH_SCALE, 1.0F);
        renderPassInfo.poseStack().scale(scale, scale, scale);
        RoyalBossRenderer.BeamSlot beam = renderPassInfo.getGeckolibData(BEAM);
        int head = renderPassInfo.getOrDefaultGeckolibData(BEAM_HEAD, -1);
        if (beam != null && head >= 0 && head < RoyalMountModel.HEADS.length) {
            RoyalBossRenderer.BeamAnchor anchor = beam.anchor();
            renderPassInfo.addBonePositionListener(RoyalMountModel.HEADS[head],
                    (worldPos, modelPos, localPos) -> anchor.offer(worldPos));
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        RoyalMountModel.applyRiddenNeckPose(renderPassInfo.renderState(), snapshots);
    }

    @Override
    public void submit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks,
                       CameraRenderState cameraState) {
        super.submit(renderState, poseStack, renderTasks, cameraState);
        RoyalBossRenderer.BeamSlot beam = renderState.getGeckolibData(BEAM);
        if (beam != null) {
            RoyalBossRenderer.submitBeam(poseStack, renderTasks, beam, 0.375F, 0.44F, 0.375F);
        }
    }
}
