package com.craisinlord.antarchy.content.menu;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class GiantFryingPanMenu extends AbstractContainerMenu {
    public static final int PAN_ROW_Y = 18;
    public static final int INVENTORY_Y = 49;
    public static final int HOTBAR_Y = 107;
    private final GiantFryingPanStorage panStorage;
    private final int panSlot;

    public GiantFryingPanMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, GiantFryingPanStorage.findPanSlot(inventory.player));
    }

    public GiantFryingPanMenu(int containerId, Inventory inventory, int panSlot) {
        super(AntarchyObjects.GIANT_FRYING_PAN_MENU.get(), containerId);
        Player player = inventory.player;
        this.panSlot = panSlot;
        this.panStorage = new GiantFryingPanStorage(player, panSlot);
        for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
            this.addSlot(new Slot(this.panStorage, slot, 8 + slot * 18, PAN_ROW_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return GiantFryingPanStorage.isCampfireInput(player, stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(this.inventorySlot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(this.inventorySlot(inventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    private Slot inventorySlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return index != GiantFryingPanMenu.this.panSlot && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return index != GiantFryingPanMenu.this.panSlot && super.mayPlace(stack);
            }
        };
    }

    public GiantFryingPanStorage storage() {
        return this.panStorage;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && button == this.panSlot) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.panStorage.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        if (index < GiantFryingPanStorage.SLOT_COUNT) {
            if (!this.moveItemStackTo(current, GiantFryingPanStorage.SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (!GiantFryingPanStorage.isCampfireInput(player, current)
                || !this.moveItemStackTo(current, 0, GiantFryingPanStorage.SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
}
