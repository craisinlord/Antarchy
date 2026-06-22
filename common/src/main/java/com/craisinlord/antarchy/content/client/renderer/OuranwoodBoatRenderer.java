package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

public class OuranwoodBoatRenderer<T extends Boat> extends EntityRenderer<T> {
    private static final String MAIN_LAYER = "main";
    private final ResourceLocation texture;
    private final ListModel<Boat> model;

    public OuranwoodBoatRenderer(EntityRendererProvider.Context context, ResourceLocation texture, boolean chest) {
        super(context);
        this.shadowRadius = 0.8F;
        this.texture = texture;
        this.model = chest
                ? new ChestBoatModel(context.bakeLayer(chestBoatLayer()))
                : new BoatModel(context.bakeLayer(boatLayer()));
    }

    public static ModelLayerLocation boatLayer() {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("antarchy", "boat/ouranwood"), MAIN_LAYER);
    }

    public static ModelLayerLocation chestBoatLayer() {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("antarchy", "chest_boat/ouranwood"), MAIN_LAYER);
    }

    @Override
    public void render(T boat, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        float hurtTime = boat.getHurtTime() - partialTick;
        float damage = Math.max(boat.getDamage() - partialTick, 0.0F);
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * boat.getHurtDir()));
        }

        float bubbleAngle = boat.getBubbleAngle(partialTick);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(boat.getBubbleAngle(partialTick) * ((float) Math.PI / 180.0F), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        this.model.setupAnim(boat, partialTick, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        if (!boat.isUnderWater() && this.model instanceof WaterPatchModel waterPatchModel) {
            VertexConsumer waterConsumer = buffer.getBuffer(RenderType.waterMask());
            waterPatchModel.waterPatch().render(poseStack, waterConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
        super.render(boat, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T boat) {
        return this.texture;
    }
}
