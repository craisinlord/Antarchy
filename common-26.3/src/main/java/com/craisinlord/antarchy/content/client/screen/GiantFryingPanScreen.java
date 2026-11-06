package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class GiantFryingPanScreen extends AbstractContainerScreen<GiantFryingPanMenu> {
    private static final Identifier CONTAINER_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int PAN_SECTION_HEIGHT = 35;
    private static final int INVENTORY_SECTION_HEIGHT = 96;
    private static final int PROGRESS_COLOR = 0xC0FF8A2A;

    public GiantFryingPanScreen(GiantFryingPanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 132);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, PAN_SECTION_HEIGHT, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos + PAN_SECTION_HEIGHT, 0.0F, 126.0F, this.imageWidth, INVENTORY_SECTION_HEIGHT, 256, 256);
        GiantFryingPanStorage storage = this.menu.storage();
        for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
            float progress = storage.cookProgress(slot);
            if (progress <= 0.0F) continue;
            int x = this.leftPos + 8 + slot * 18;
            int y = this.topPos + GiantFryingPanMenu.PAN_ROW_Y + 16;
            graphics.fill(x, y, x + Math.round(16 * progress), y + 1, PROGRESS_COLOR);
        }
    }
}
