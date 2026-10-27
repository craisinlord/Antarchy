package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.mixins.client.ElytraModelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class BrutalflyElytraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("antarchy", "textures/models/armor/brutalfly_wings.png");
    private static final RenderType RENDER_TYPE = RenderTypes.armorCutoutNoCull(TEXTURE);
    private final ElytraModel model;

    public BrutalflyElytraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
        this.model = new ElytraModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        ItemStack chestStack = state.chestEquipment;
        if (!(chestStack.getItem() instanceof BrutalflyElytraItem) || state.isInvisible) {
            return;
        }

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        BrutalflyElytraClientState.AnimationState animationState = BrutalflyElytraClientState.get(state.id);
        float progress = animationState != null ? animationState.progress(partialTick) : 0.0F;
        float strength = animationState != null ? animationState.strength() : 0.0F;
        float flap = Mth.sin(progress * Mth.PI);
        float liftOffset = flap * (2.8F + strength * 0.8F);
        float pitchOffset = flap * (0.35F + strength * 0.12F);
        float tipCurl = flap * (0.30F + strength * 0.10F);
        boolean foil = chestStack.hasFoil();

        poseStack.pushPose();
        if (state.isCrouching) {
            poseStack.translate(0.0F, 0.0F, 0.125F);
        }
        this.submitWings(poseStack, submitNodeCollector, RENDER_TYPE, lightCoords, state, liftOffset, pitchOffset, tipCurl);
        if (foil) {
            this.submitWings(poseStack, submitNodeCollector, RenderTypes.trimmedArmorGlint(), lightCoords, state, liftOffset, pitchOffset, tipCurl);
        }
        poseStack.popPose();
    }

    private void submitWings(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int lightCoords,
                             AvatarRenderState state, float liftOffset, float pitchOffset, float tipCurl) {
        ElytraModel wings = this.model;
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            wings.setupAnim(state);
            ElytraModelAccessor accessor = (ElytraModelAccessor) (Object) wings;
            ModelPart leftWing = accessor.antarchy$getLeftWing();
            ModelPart rightWing = accessor.antarchy$getRightWing();
            leftWing.y -= liftOffset;
            rightWing.y -= liftOffset;
            leftWing.xRot -= pitchOffset;
            rightWing.xRot -= pitchOffset;
            leftWing.zRot -= tipCurl;
            rightWing.zRot += tipCurl;
            PoseStack localPose = new PoseStack();
            localPose.last().set(pose);
            wings.renderToBuffer(localPose, buffer, lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
    }
}
