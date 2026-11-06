package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TigerEyeCamouflagePlayerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final DataTicket<TigerEyeCamouflageTextureResolver.ResolvedTexture> CAMOUFLAGE =
            DataTicket.create("antarchy_player_tiger_eye_camouflage", TigerEyeCamouflageTextureResolver.ResolvedTexture.class);

    public TigerEyeCamouflagePlayerLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
        state.getDataMap().remove(CAMOUFLAGE);
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.isSpectator() || player.isInvisible() || player.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get())) {
            return;
        }
        if (!TigerEyeArmorUtil.hasFullSet(player)) {
            return;
        }

        TigerEyeCamouflageClientState.CamouflageState camouflage = TigerEyeCamouflageClientState.get(player.getId());
        if (camouflage == null || !camouflage.active()) {
            return;
        }

        BlockState blockState = Block.stateById(camouflage.blockStateId());
        if (!TigerEyeArmorUtil.isValidCamouflageBlock(blockState)) {
            return;
        }

        state.addGeckolibData(CAMOUFLAGE, TigerEyeCamouflageTextureResolver.resolve(blockState, BlockPos.containing(player.position())));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        TigerEyeCamouflageTextureResolver.ResolvedTexture resolved = state.getGeckolibData(CAMOUFLAGE);
        if (resolved == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.1F, 1.1F, 1.1F);
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                RenderTypes.entityCutout(resolved.texture()),
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                resolved.argbTint(),
                null,
                state.outlineColor,
                null
        );
        poseStack.popPose();
    }
}
