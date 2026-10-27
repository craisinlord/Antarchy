package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public final class ParalyzedStoneLivingLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public static final DataTicket<Boolean> PARALYZED = DataTicket.create("antarchy_living_paralyzed", Boolean.class);
    static final Identifier STONE_TEXTURE = Identifier.withDefaultNamespace("textures/block/stone.png");
    static final RenderType STONE_RENDER_TYPE = RenderTypes.entityCutout(STONE_TEXTURE);
    static final int STONE_TINT = 0xFFB1ADA4;

    public ParalyzedStoneLivingLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
        state.addGeckolibData(PARALYZED, entity.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get()));
    }

    static boolean isParalyzed(LivingEntityRenderState state) {
        return state.getOrDefaultGeckolibData(PARALYZED, false);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        if (!isParalyzed(state)) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(1.02f, 1.02f, 1.02f);
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                STONE_RENDER_TYPE,
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                STONE_TINT,
                null,
                state.outlineColor,
                null
        );
        poseStack.popPose();
    }
}
