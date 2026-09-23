package com.craisinlord.antarchy.content.menu;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.GiantFryingPanStorage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class GiantFryingPanMenu extends AbstractContainerMenu {
    private final GiantFryingPanStorage panStorage;

    public GiantFryingPanMenu(int containerId, Inventory inventory) {
        super(AntarchyObjects.GIANT_FRYING_PAN_MENU.get(), containerId);
        ItemStack pan = GiantFryingPanStorage.findPan(inventory.player);
        this.panStorage = new GiantFryingPanStorage(pan, inventory.player);
        for (int slot = 0; slot < GiantFryingPanStorage.SLOT_COUNT; slot++) {
            this.addSlot(new Slot(panStorage, slot, 8 + slot * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return GiantFryingPanStorage.isCampfireInput(inventory.player, stack);
                }

                @Override
                public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col, 8 + col * 18, 108));
    }

    @Override
    public boolean stillValid(Player player) { return player.isAlive() && !panStorage.isEmpty() || GiantFryingPanStorage.findPan(player).getItem() instanceof GiantFryingPanItem; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        if (index < GiantFryingPanStorage.SLOT_COUNT) {
            if (!moveItemStackTo(slot.getItem(), GiantFryingPanStorage.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(slot.getItem(), 0, GiantFryingPanStorage.SLOT_COUNT, false)) return ItemStack.EMPTY;
        if (slot.getItem().isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
}
