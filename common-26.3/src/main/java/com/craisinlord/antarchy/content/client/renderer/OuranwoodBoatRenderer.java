package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Quaternionf;

public class OuranwoodBoatRenderer<T extends AbstractBoat> extends EntityRenderer<AbstractBoat, BoatRenderState> {
    private static final String MAIN_LAYER = "main";
    private final Identifier texture;
    private final EntityModel<BoatRenderState> model;
    private final Model.Simple waterPatchModel;

    public OuranwoodBoatRenderer(EntityRendererProvider.Context context, Identifier texture, boolean chest) {
        super(context);
        this.shadowRadius = 0.8F;
        this.texture = texture;
        this.model = new BoatModel(context.bakeLayer(chest ? chestBoatLayer() : boatLayer()));
        this.waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), t -> RenderTypes.waterMask());
    }

    public static ModelLayerLocation boatLayer() {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("antarchy", "boat/ouranwood"), MAIN_LAYER);
    }

    public static ModelLayerLocation chestBoatLayer() {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("antarchy", "chest_boat/ouranwood"), MAIN_LAYER);
    }

    @Override
    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat boat, BoatRenderState state, float partialTick) {
        super.extractRenderState(boat, state, partialTick);
        state.yRot = boat.getYRot(partialTick);
        state.hurtTime = boat.getHurtTime() - partialTick;
        state.hurtDir = boat.getHurtDir();
        state.damageTime = Math.max(boat.getDamage() - partialTick, 0.0F);
        state.bubbleAngle = boat.getBubbleAngle(partialTick);
        state.isUnderWater = boat.isUnderWater();
        state.rowingTimeLeft = boat.getRowingTime(0, partialTick);
        state.rowingTimeRight = boat.getRowingTime(1, partialTick);
    }

    @Override
    public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F - state.yRot));

        float hurtTime = state.hurtTime;
        if (hurtTime > 0.0F) {
            poseStack.rotate(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * state.damageTime / 10.0F * state.hurtDir));
        }

        if (!Mth.equal(state.bubbleAngle, 0.0F)) {
            poseStack.rotate(new Quaternionf().setAngleAxis(state.bubbleAngle * ((float) Math.PI / 180.0F), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(90.0F));
        collector.submitModel(this.model, state, poseStack, this.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        if (!state.isUnderWater) {
            collector.submitModel(this.waterPatchModel, Unit.INSTANCE, poseStack, this.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
