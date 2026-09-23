package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GiantFryingPanScreen extends AbstractContainerScreen<GiantFryingPanMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int PAN_SECTION_HEIGHT = 35;
    private static final int INVENTORY_SECTION_HEIGHT = 96;
    private static final int PROGRESS_COLOR = 0xC0FF8A2A;

    public GiantFryingPanScreen(GiantFryingPanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 132;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(CONTAINER_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, PAN_SECTION_HEIGHT);
        graphics.blit(CONTAINER_TEXTURE, this.leftPos, this.topPos + PAN_SECTION_HEIGHT, 0, 126, this.imageWidth, INVENTORY_SECTION_HEIGHT);
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
