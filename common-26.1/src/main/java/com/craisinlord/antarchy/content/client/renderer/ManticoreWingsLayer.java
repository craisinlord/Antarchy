package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.item.ManticoreWingsItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ManticoreWingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/models/armor/manticore_wings.png");
    private static final RenderType RENDER_TYPE = RenderTypes.armorCutoutNoCull(TEXTURE);
    private final ElytraModel model;

    public ManticoreWingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
        this.model = new ElytraModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        ItemStack chestStack = state.chestEquipment;
        if (!(chestStack.getItem() instanceof ManticoreWingsItem) || state.isInvisible) {
            return;
        }
        poseStack.pushPose();
        if (state.isCrouching) {
            poseStack.translate(0.0F, 0.0F, 0.125F);
        }
        submitNodeCollector.submitModel(this.model, state, poseStack, RENDER_TYPE, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        if (chestStack.hasFoil()) {
            submitNodeCollector.submitModel(this.model, state, poseStack, RenderTypes.armorEntityGlint(), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
        poseStack.popPose();
    }
}
