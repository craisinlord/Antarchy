package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public final class GoopedLivingLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public static final DataTicket<Identifier> GOOPED_TEXTURE = DataTicket.create("antarchy_living_gooped_texture", Identifier.class);
    private static final int GOOPED_TINT = 0x6632FF55;

    public GoopedLivingLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    public static <S extends LivingEntityRenderState> void extract(LivingEntityRenderer<?, S, ?> renderer, LivingEntity entity, S state) {
        state.getDataMap().remove(GOOPED_TEXTURE);
        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (gooped == null || !entity.hasEffect(gooped)) {
            return;
        }
        state.addGeckolibData(GOOPED_TEXTURE, renderer.getTextureLocation(state));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        Identifier texture = state.getGeckolibData(GOOPED_TEXTURE);
        if (texture == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.018F, 1.018F, 1.018F);
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityTranslucent(texture),
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                GOOPED_TINT,
                null,
                state.outlineColor
        );
        poseStack.popPose();
    }
}
