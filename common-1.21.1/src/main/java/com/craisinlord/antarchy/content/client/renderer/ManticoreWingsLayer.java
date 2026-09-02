package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.item.ManticoreWingsItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ManticoreWingsLayer extends RenderLayer<net.minecraft.client.player.AbstractClientPlayer, PlayerModel<net.minecraft.client.player.AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/models/armor/manticore_wings.png");
    private final ElytraModel<net.minecraft.client.player.AbstractClientPlayer> model;

    public ManticoreWingsLayer(RenderLayerParent<net.minecraft.client.player.AbstractClientPlayer, PlayerModel<net.minecraft.client.player.AbstractClientPlayer>> renderer) {
        super(renderer);
        this.model = new ElytraModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       net.minecraft.client.player.AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ManticoreWingsItem.isWearingManticoreWings(player) || player.isInvisible()) {
            return;
        }
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        poseStack.pushPose();
        this.getParentModel().copyPropertiesTo(this.model);
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.0F, 0.125F);
        }
        this.model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, chestStack.hasFoil());
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
