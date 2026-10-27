package com.craisinlord.antarchy.content.client.screen;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.menu.DorrieInventoryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DorrieInventoryScreen extends AbstractContainerScreen<DorrieInventoryMenu> {
    private static final Identifier HORSE_INVENTORY_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/horse.png");
    private static final Identifier CHEST_SLOTS_SPRITE =
            Identifier.withDefaultNamespace("container/horse/chest_slots");

    private static final int CHEST_SLOTS_TEX_WIDTH = 90;
    private static final int CHEST_SLOTS_TEX_HEIGHT = 54;

    private float xMouse;
    private float yMouse;

    public DorrieInventoryScreen(DorrieInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int left = this.leftPos;
        int top = this.topPos;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, HORSE_INVENTORY_TEXTURE, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        int storageWidth = DorrieInventoryMenu.STORAGE_COLUMNS * 18;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, CHEST_SLOTS_TEX_WIDTH, CHEST_SLOTS_TEX_HEIGHT, 0, 0,
                left + 79, top + 17, storageWidth, CHEST_SLOTS_TEX_HEIGHT);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHEST_SLOTS_SPRITE, CHEST_SLOTS_TEX_WIDTH, CHEST_SLOTS_TEX_HEIGHT, 0, 0,
                left + 7, top + 17, 18, 18);

        DorrieEntity dorrie = this.menu.dorrie();
        if (dorrie != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }
}
