package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.menu.GiantFryingPanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GiantFryingPanScreen extends AbstractContainerScreen<GiantFryingPanMenu> {
    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public GiantFryingPanScreen(GiantFryingPanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 132;
        inventoryLabelY = 40;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(INVENTORY_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
