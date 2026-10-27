package com.craisinlord.antarchy.content.menu;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class RoyalJudgmentMenu extends AbstractContainerMenu {
    public RoyalJudgmentMenu(int containerId, Inventory inventory) {
        super(AntarchyObjects.ROYAL_JUDGMENT_MENU.get(), containerId);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
                this.addSlot(new Slot(inventory, inventorySlot, 8 + column * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return !RoyalJudgmentState.isSealed(player.getUUID(), this.getContainerSlot())
                                && super.mayPickup(player);
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !RoyalJudgmentState.isSealed(inventory.player.getUUID(), this.getContainerSlot())
                                && super.mayPlace(stack);
                    }
                });
            }
        }
        for (int column = 0; column < 9; column++) {
            int inventorySlot = column;
            this.addSlot(new Slot(inventory, inventorySlot, 8 + column * 18, 76) {
                @Override
                public boolean mayPickup(Player player) {
                    return !RoyalJudgmentState.isSealed(player.getUUID(), this.getContainerSlot())
                            && super.mayPickup(player);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !RoyalJudgmentState.isSealed(inventory.player.getUUID(), this.getContainerSlot())
                            && super.mayPlace(stack);
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemStack moved = slot.getItem().copy();
        if (!this.moveItemStackTo(moved, 0, this.slots.size(), true)) return ItemStack.EMPTY;
        slot.setByPlayer(moved.isEmpty() ? ItemStack.EMPTY : moved);
        slot.onTake(player, moved);
        return original;
    }
}
