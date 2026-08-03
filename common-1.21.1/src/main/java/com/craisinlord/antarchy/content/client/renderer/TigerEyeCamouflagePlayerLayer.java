package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public final class TigerEyeCamouflagePlayerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public TigerEyeCamouflagePlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                       float headPitch) {
        if (player.isSpectator() || player.isInvisible() || player.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get())) {
            return;
        }
        if (!TigerEyeArmorUtil.hasFullSet(player)) {
            return;
        }

        TigerEyeCamouflageClientState.CamouflageState state = TigerEyeCamouflageClientState.get(player.getId());
        if (state == null || !state.active()) {
            return;
        }

        var blockState = Block.stateById(state.blockStateId());
        if (!TigerEyeArmorUtil.isValidCamouflageBlock(blockState)) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.1F, 1.1F, 1.1F);
        var resolved = TigerEyeCamouflageTextureResolver.resolve(blockState, BlockPos.containing(player.position()));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(resolved.texture()));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                resolved.argbTint()
        );
        poseStack.popPose();
    }
}
