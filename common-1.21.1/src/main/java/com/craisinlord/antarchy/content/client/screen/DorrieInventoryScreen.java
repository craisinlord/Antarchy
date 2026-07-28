package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.menu.DorrieInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DorrieInventoryScreen extends AbstractContainerScreen<DorrieInventoryMenu> {
    private static final ResourceLocation HORSE_INVENTORY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
    private static final ResourceLocation CHEST_SLOTS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/horse/chest_slots");

    private static final int CHEST_SLOTS_TEX_WIDTH = 90;
    private static final int CHEST_SLOTS_TEX_HEIGHT = 54;

    private float xMouse;
    private float yMouse;

    public DorrieInventoryScreen(DorrieInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.blit(HORSE_INVENTORY_TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight);

        int storageWidth = DorrieInventoryMenu.STORAGE_COLUMNS * 18;
        guiGraphics.blitSprite(CHEST_SLOTS_SPRITE, CHEST_SLOTS_TEX_WIDTH, CHEST_SLOTS_TEX_HEIGHT, 0, 0,
                left + 79, top + 17, storageWidth, CHEST_SLOTS_TEX_HEIGHT);
        guiGraphics.blitSprite(CHEST_SLOTS_SPRITE, CHEST_SLOTS_TEX_WIDTH, CHEST_SLOTS_TEX_HEIGHT, 0, 0,
                left + 7, top + 17, 18, 18);

        DorrieEntity dorrie = this.menu.dorrie();
        if (dorrie != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    left + 8,
                    top + 18,
                    left + 71,
                    top + 70,
                    24,
                    0.25F,
                    this.xMouse,
                    this.yMouse,
                    dorrie
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
