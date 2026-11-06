package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AntCarriedFoodGeoLayer extends GeoRenderLayer<BaseAntEntity, Void, LivingEntityRenderState> {
    private static final String FOOD_BONE = "head";
    private static final DataTicket<ItemStackRenderState> CARRIED_FOOD =
            DataTicket.create("antarchy_ant_carried_food", ItemStackRenderState.class);
    private static final DataTicket<Boolean> DANCING = DataTicket.create("antarchy_ant_carried_food_dancing", Boolean.class);

    public AntCarriedFoodGeoLayer(GeoRenderer<BaseAntEntity, Void, LivingEntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(BaseAntEntity animatable, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        ItemStack carriedFood = animatable.getCarriedFood();
        ItemStackRenderState itemState = new ItemStackRenderState();
        if (!carriedFood.isEmpty()) {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                    itemState, carriedFood.copy(), ItemDisplayContext.FIXED, animatable.level(), animatable, animatable.getId());
        }
        renderState.addGeckolibData(CARRIED_FOOD, itemState);
        renderState.addGeckolibData(DANCING, animatable.isDancing());
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<LivingEntityRenderState>> consumer) {
        if (!renderPassInfo.willRender()) {
            return;
        }
        ItemStackRenderState itemState = renderPassInfo.getGeckolibData(CARRIED_FOOD);
        if (itemState == null || itemState.isEmpty()) {
            return;
        }
        boolean dancing = renderPassInfo.getOrDefaultGeckolibData(DANCING, false);
        renderPassInfo.model().getBone(FOOD_BONE).ifPresent(bone -> consumer.accept(bone,
                (info, bone2, renderTasks) -> {
                    PoseStack poseStack = info.poseStack();
                    rotateLikeBone(poseStack, bone2);
                    double yOffset = dancing ? 0.58D : 0.15D;
                    poseStack.translate(0.0D, yOffset, -0.50D);
                    poseStack.rotate(Axis.YP.rotationDegrees(90.0F));
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    itemState.submit(poseStack, renderTasks, info.packedLight(), info.packedOverlay(), info.renderState().outlineColor);
                }));
    }

    private static void rotateLikeBone(PoseStack poseStack, GeoBone bone) {
        float xRot = bone.baseRotX();
        float yRot = bone.baseRotY();
        float zRot = bone.baseRotZ();
        if (bone.frameSnapshot != null) {
            xRot += bone.frameSnapshot.getRotX();
            yRot += bone.frameSnapshot.getRotY();
            zRot += bone.frameSnapshot.getRotZ();
        }
        if (zRot != 0.0F) {
            poseStack.rotate(Axis.ZP.rotation(zRot));
        }
        if (yRot != 0.0F) {
            poseStack.rotate(Axis.YP.rotation(yRot));
        }
        if (xRot != 0.0F) {
            poseStack.rotate(Axis.XP.rotation(xRot));
        }
    }
}
