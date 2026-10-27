package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GiantFryingPanRenderer extends GeoItemRenderer<GiantFryingPanItem> {
    private static final DataTicket<List<PanItem>> PAN_ITEMS =
            DataTicket.create("antarchy_giant_frying_pan_items", new TypeToken<List<PanItem>>() {});

    public GiantFryingPanRenderer(GeoModel<GiantFryingPanItem> model) {
        super(model);
        this.withRenderLayer(new PanContentsLayer(this));
    }

    private static final class PanContentsLayer extends GeoRenderLayer<GiantFryingPanItem, GeoItemRenderer.RenderData, GeoRenderState> {
        private PanContentsLayer(GeoRenderer<GiantFryingPanItem, GeoItemRenderer.RenderData, GeoRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void addRenderData(GiantFryingPanItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
            List<PanItem> items = new ArrayList<>();
            renderState.addGeckolibData(PAN_ITEMS, items);
            if (relatedObject == null || relatedObject.renderPerspective() == ItemDisplayContext.GUI) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player instanceof Player player)) {
                return;
            }
            GiantFryingPanStorage storage = new GiantFryingPanStorage(relatedObject.itemStack(), player);
            for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
                ItemStack stack = storage.getItem(slot);
                if (stack.isEmpty()) continue;
                ItemStackRenderState itemState = new ItemStackRenderState();
                minecraft.getItemModelResolver().updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, minecraft.level, null, 0);
                items.add(new PanItem(slot, itemState));
            }
        }

        @Override
        public void submitRenderTask(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI) {
                return;
            }
            List<PanItem> items = renderPassInfo.getOrDefaultGeckolibData(PAN_ITEMS, List.of());
            PoseStack poseStack = renderPassInfo.poseStack();
            int packedLight = renderPassInfo.packedLight();
            int packedOverlay = renderPassInfo.packedOverlay();
            for (PanItem item : items) {
                if (item.state().isEmpty()) continue;
                int row = item.slot() / 3;
                int column = item.slot() % 3;
                poseStack.pushPose();
                poseStack.translate((column - 1) * 0.55D, 2.5D - (row - 1) * 0.55D, 0.03125D);
                poseStack.scale(0.24F, 0.24F, 0.24F);
                item.state().submit(poseStack, renderTasks, packedLight, packedOverlay, 0);
                poseStack.popPose();
            }
        }
    }

    private record PanItem(int slot, ItemStackRenderState state) {
    }
}
