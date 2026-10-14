package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class GiantFryingPanRenderer extends GeoItemRenderer<GiantFryingPanItem> {
    public GiantFryingPanRenderer(GeoModel<GiantFryingPanItem> model) {
        super(model);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            GiantFryingPanItem animatable,
            BakedGeoModel model,
            net.minecraft.client.renderer.RenderType renderType,
            MultiBufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (isReRender || this.renderPerspective == ItemDisplayContext.GUI) return;
        ItemStack pan = this.getCurrentItemStack();
        net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
        if (player == null) return;
        GiantFryingPanStorage storage = new GiantFryingPanStorage(pan, player);
        for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
            ItemStack stack = storage.getItem(slot);
            if (stack.isEmpty()) continue;
            int row = slot / 3;
            int column = slot % 3;
            poseStack.pushPose();
            poseStack.translate((column - 1) * 0.55D, 2.5D - (row - 1) * 0.55D, 0.03125D);
            poseStack.scale(0.24F, 0.24F, 0.24F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, Minecraft.getInstance().level, 0);
            poseStack.popPose();
        }
    }
}
