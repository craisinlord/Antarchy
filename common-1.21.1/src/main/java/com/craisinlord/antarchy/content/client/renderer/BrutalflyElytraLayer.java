package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.mixins.client.ElytraModelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;

public final class BrutalflyElytraLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "textures/models/armor/brutalfly_wings.png");
    private final ElytraModel<AbstractClientPlayer> model;

    public BrutalflyElytraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.model = new ElytraModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!BrutalflyElytraItem.isWearingBrutalflyElytra(player) || player.isInvisible()) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        BrutalflyElytraClientState.AnimationState animationState = BrutalflyElytraClientState.get(player.getId());
        float progress = animationState != null ? animationState.progress(partialTick) : 0.0F;

        poseStack.pushPose();
        this.getParentModel().copyPropertiesTo(this.model);
        if (player.isCrouching()) {
            poseStack.translate(0.0F, 0.0F, 0.125F);
        }
        this.model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ElytraModelAccessor accessor = (ElytraModelAccessor) (Object) this.model;
        ModelPart leftWing = accessor.antarchy$getLeftWing();
        ModelPart rightWing = accessor.antarchy$getRightWing();
        float strength = animationState != null ? animationState.strength() : 0.0F;
        float flap = net.minecraft.util.Mth.sin(progress * net.minecraft.util.Mth.PI);
        float liftOffset = flap * (2.8F + strength * 0.8F);
        float pitchOffset = flap * (0.35F + strength * 0.12F);
        float tipCurl = flap * (0.30F + strength * 0.10F);
        leftWing.y -= liftOffset;
        rightWing.y -= liftOffset;
        leftWing.xRot -= pitchOffset;
        rightWing.xRot -= pitchOffset;
        leftWing.zRot -= tipCurl;
        rightWing.zRot += tipCurl;

        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, chestStack.hasFoil());
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
