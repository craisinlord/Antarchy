package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class RoyalJudgmentScreen extends AbstractContainerScreen<RoyalJudgmentMenu> {
    private static final Identifier INVENTORY_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/inventory.png");

    public RoyalJudgmentScreen(RoyalJudgmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.inventoryLabelY = 84;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        int gold = 0xD8E6B84A;
        graphics.fill(this.leftPos - 2, this.topPos - 2, this.leftPos + this.imageWidth + 2, this.topPos, gold);
        graphics.fill(this.leftPos - 2, this.topPos + this.imageHeight, this.leftPos + this.imageWidth + 2,
                this.topPos + this.imageHeight + 2, gold);
        graphics.fill(this.leftPos - 2, this.topPos - 2, this.leftPos, this.topPos + this.imageHeight + 2, gold);
        graphics.fill(this.leftPos + this.imageWidth, this.topPos - 2, this.leftPos + this.imageWidth + 2,
                this.topPos + this.imageHeight + 2, gold);
    }
}
