package com.craisinlord.antarchy.content.menu;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class DorrieInventoryMenu extends AbstractContainerMenu {
    public static final int SADDLE_SLOT = 0;
    public static final int STORAGE_COLUMNS = 5;
    public static final int STORAGE_ROWS = 3;
    public static final int DORRIE_SLOT_COUNT = 1 + STORAGE_COLUMNS * STORAGE_ROWS;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

    private final Container dorrieInventory;
    @Nullable
    private final DorrieEntity dorrie;

    public DorrieInventoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(DORRIE_SLOT_COUNT), null);
    }

    public DorrieInventoryMenu(int containerId, Inventory playerInventory, Container dorrieInventory, @Nullable DorrieEntity dorrie) {
        super(AntarchyObjects.DORRIE_INVENTORY_MENU.get(), containerId);
        checkContainerSize(dorrieInventory, DORRIE_SLOT_COUNT);
        this.dorrieInventory = dorrieInventory;
        this.dorrie = dorrie;
        dorrieInventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(dorrieInventory, SADDLE_SLOT, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.SADDLE) && !this.hasItem();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLUMNS; col++) {
                this.addSlot(new Slot(dorrieInventory, 1 + col + row * STORAGE_COLUMNS, 80 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, PLAYER_HOTBAR_Y));
        }
    }

    public Container dorrieInventory() {
        return this.dorrieInventory;
    }

    @Nullable
    public DorrieEntity dorrie() {
        return this.dorrie;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dorrieInventory.stillValid(player)
                && (this.dorrie == null || this.dorrie.isAlive() && this.dorrie.distanceTo(player) < 8.0F);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        quickMoved = stack.copy();
        int dorrieEnd = DORRIE_SLOT_COUNT;
        int playerStart = dorrieEnd;
        int playerEnd = this.slots.size();

        if (index < dorrieEnd) {
            if (!this.moveItemStackTo(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.SADDLE)) {
            if (!this.moveItemStackTo(stack, SADDLE_SLOT, SADDLE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, dorrieEnd, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return quickMoved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.dorrieInventory.stopOpen(player);
    }
}
