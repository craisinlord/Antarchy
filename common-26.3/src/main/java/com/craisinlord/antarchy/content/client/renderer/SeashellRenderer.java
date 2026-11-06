package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.craisinlord.antarchy.content.client.model.SeashellModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class SeashellRenderer extends GeoBlockRenderer<SeashellBlockEntity, BlockEntityRenderState> {
    private static final DataTicket<List<DisplayedItem>> DISPLAYED_ITEMS =
            DataTicket.create("antarchy_seashell_displayed_items", new TypeToken<List<DisplayedItem>>() {});

    public SeashellRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new SeashellModel());
        this.withRenderLayer(new DisplayedItemsLayer(this, context.itemModelResolver()));
    }

    private static final class DisplayedItemsLayer extends GeoRenderLayer<SeashellBlockEntity, Void, BlockEntityRenderState> {
        private final ItemModelResolver itemModelResolver;

        private DisplayedItemsLayer(GeoRenderer<SeashellBlockEntity, Void, BlockEntityRenderState> renderer, ItemModelResolver itemModelResolver) {
            super(renderer);
            this.itemModelResolver = itemModelResolver;
        }

        @Override
        public void addRenderData(SeashellBlockEntity animatable, @Nullable Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
            List<SeashellBlockEntity.DisplayedStack> displayedStacks = animatable.getDisplayedStacks();
            List<DisplayedItem> items = new ArrayList<>(displayedStacks.size());
            for (SeashellBlockEntity.DisplayedStack displayedStack : displayedStacks) {
                ItemStackRenderState itemState = new ItemStackRenderState();
                this.itemModelResolver.updateForTopItem(itemState, displayedStack.stack(), ItemDisplayContext.FIXED, animatable.getLevel(), null, 0);
                items.add(new DisplayedItem(itemState, displayedStack.offset()));
            }
            renderState.addGeckolibData(DISPLAYED_ITEMS, items);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<BlockEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            List<DisplayedItem> items = renderPassInfo.getOrDefaultGeckolibData(DISPLAYED_ITEMS, List.of());
            PoseStack poseStack = renderPassInfo.poseStack();
            int packedLight = renderPassInfo.packedLight();
            int packedOverlay = renderPassInfo.packedOverlay();
            for (DisplayedItem item : items) {
                renderDisplayItem(poseStack, renderTasks, item.state(), item.offset(), packedLight, packedOverlay);
            }
        }
    }

    private static void renderDisplayItem(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            ItemStackRenderState itemState,
            Vec3 localOffset,
            int packedLight,
            int packedOverlay
    ) {
        if (itemState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(localOffset.x(), localOffset.y(), localOffset.z());
        poseStack.rotate(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.22F, 0.22F, 0.22F);
        itemState.submit(poseStack, collector, packedLight, packedOverlay, 0);
        poseStack.popPose();
    }

    private record DisplayedItem(ItemStackRenderState state, Vec3 offset) {
    }
}
