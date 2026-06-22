package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class AntCarriedFoodGeoLayer extends BlockAndItemGeoLayer<BaseAntEntity> {
    private static final String FOOD_BONE = "head";

    public AntCarriedFoodGeoLayer(GeoRenderer<BaseAntEntity> renderer) {
        super(renderer);
    }

    @Override
    protected @Nullable ItemStack getStackForBone(GeoBone bone, BaseAntEntity animatable) {
        if (!FOOD_BONE.equals(bone.getName())) {
            return super.getStackForBone(bone, animatable);
        }

        ItemStack carriedFood = animatable.getCarriedFood();
        return carriedFood.isEmpty() ? super.getStackForBone(bone, animatable) : carriedFood.copy();
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, BaseAntEntity animatable) {
        return ItemDisplayContext.FIXED;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, BaseAntEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        double yOffset = animatable.isDancing() ? -0.055D : -0.04D;
        poseStack.translate(0.0D, yOffset, -0.42D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.4F, 0.4F, 0.4F);
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
