package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.block.UpperBlock;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class UpperBlockEntity extends RandomizableContainerBlockEntity {
    private static final int CONTAINER_SIZE = 5;
    private static final int MOVE_ITEM_SPEED = 8;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int cooldownTime = -1;

    public UpperBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<UpperBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UpperBlockEntity blockEntity) {
        blockEntity.cooldownTime--;
        if (!blockEntity.isOnCooldown()) {
            blockEntity.setCooldown(0);
            tryMoveItems(level, pos, state, blockEntity);
        }
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private void setCooldown(int cooldown) {
        this.cooldownTime = cooldown;
    }

    private static boolean tryMoveItems(ServerLevel level, BlockPos pos, BlockState state, UpperBlockEntity upper) {
        if (!state.getValue(UpperBlock.ENABLED)) {
            return false;
        }

        boolean moved = false;
        if (!upper.isEmpty()) {
            moved = pushToDestination(level, pos, state, upper);
        }
        if (!moved) {
            moved = pullFromBelow(level, pos, upper);
        }
        moved |= suckLooseItems(level, pos, upper);

        if (moved) {
            upper.setCooldown(MOVE_ITEM_SPEED);
            upper.setChanged();
            return true;
        }
        return false;
    }

    private static boolean pullFromBelow(ServerLevel level, BlockPos pos, UpperBlockEntity upper) {
        Container source = getContainerAt(level, pos.below());
        if (source == null) {
            return false;
        }
        if (source instanceof UpperBlockEntity sourceUpper && sourceUpper.isOnCooldown()) {
            return false;
        }
        return transferOneItem(source, upper);
    }

    private static boolean pushToDestination(ServerLevel level, BlockPos pos, BlockState state, UpperBlockEntity upper) {
        Direction facing = state.getValue(UpperBlock.FACING);
        Container destination = getContainerAt(level, pos.relative(facing));
        if (destination == null) {
            return false;
        }
        boolean pushed = transferOneItem(upper, destination);
        if (pushed && destination instanceof UpperBlockEntity destinationUpper && !destinationUpper.isOnCooldown()) {
            destinationUpper.setCooldown(MOVE_ITEM_SPEED);
            destinationUpper.setChanged();
        }
        return pushed;
    }

    private static boolean suckLooseItems(ServerLevel level, BlockPos pos, UpperBlockEntity upper) {
        AABB suckArea = new AABB(pos.below()).inflate(-0.05D, 0.0D, -0.05D);
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, suckArea, ItemEntity::isAlive);
        boolean moved = false;
        for (ItemEntity itemEntity : entities) {
            ItemStack stack = itemEntity.getItem();
            ItemStack remainder = addItem(upper, stack.copy());
            if (remainder.isEmpty()) {
                itemEntity.discard();
                moved = true;
            } else if (remainder.getCount() != stack.getCount()) {
                itemEntity.setItem(remainder);
                moved = true;
            }
        }
        return moved;
    }

    private static Container getContainerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    private static boolean transferOneItem(Container source, Container destination) {
        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            ItemStack stack = source.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack single = stack.copyWithCount(1);
            ItemStack remainder = addItem(destination, single);
            if (remainder.isEmpty()) {
                stack.shrink(1);
                source.setItem(slot, stack);
                destination.setChanged();
                source.setChanged();
                return true;
            }
        }
        return false;
    }

    private static ItemStack addItem(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize() && !stack.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int limit = Math.min(existing.getMaxStackSize(), container.getMaxStackSize());
                int room = limit - existing.getCount();
                if (room > 0) {
                    int moveCount = Math.min(room, stack.getCount());
                    existing.grow(moveCount);
                    stack.shrink(moveCount);
                }
            }
        }
        for (int slot = 0; slot < container.getContainerSize() && !stack.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty() && container.canPlaceItem(slot, stack)) {
                int moveCount = Math.min(stack.getMaxStackSize(), stack.getCount());
                container.setItem(slot, stack.split(moveCount));
            }
        }
        return stack;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.antarchy.upper");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new HopperMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        tag.putInt("Cooldown", this.cooldownTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        this.cooldownTime = tag.getInt("Cooldown");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
