package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class RoyalJudgmentScreen extends AbstractContainerScreen<RoyalJudgmentMenu> {
    private static final ResourceLocation INVENTORY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public RoyalJudgmentScreen(RoyalJudgmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 84;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(INVENTORY_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int gold = 0xD8E6B84A;
        graphics.fill(this.leftPos - 2, this.topPos - 2, this.leftPos + this.imageWidth + 2, this.topPos, gold);
        graphics.fill(this.leftPos - 2, this.topPos + this.imageHeight, this.leftPos + this.imageWidth + 2,
                this.topPos + this.imageHeight + 2, gold);
        graphics.fill(this.leftPos - 2, this.topPos - 2, this.leftPos, this.topPos + this.imageHeight + 2, gold);
        graphics.fill(this.leftPos + this.imageWidth, this.topPos - 2, this.leftPos + this.imageWidth + 2,
                this.topPos + this.imageHeight + 2, gold);
    }
}
