package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import com.geckolib.constant.dataticket.DataTicket;

public final class GlimmeringLivingLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public static final DataTicket<Boolean> GLIMMERING = DataTicket.create("antarchy_glimmering_living", Boolean.class);
    private static final int GLIMMERING_TINT = 0x4266CCFF;

    @Nullable
    private final LivingEntityRenderer<?, S, M> livingRenderer;

    @SuppressWarnings("unchecked")
    public GlimmeringLivingLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
        this.livingRenderer = renderer instanceof LivingEntityRenderer<?, ?, ?> living ? (LivingEntityRenderer<?, S, M>) living : null;
    }

    public static void extractRenderData(LivingEntity entity, EntityRenderState state) {
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        state.addGeckolibData(GLIMMERING, glimmering != null && entity.hasEffect(glimmering));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        if (!state.getOrDefaultGeckolibData(GLIMMERING, false) || this.livingRenderer == null) {
            return;
        }

        Identifier texture = this.livingRenderer.getTextureLocation(state);
        submitNodeCollector.submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(texture),
                LightCoordsUtil.FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                GLIMMERING_TINT,
                null,
                state.outlineColor
        );
    }
}
