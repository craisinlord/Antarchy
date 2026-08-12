package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.SeashellBlock;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class SeashellBlockEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity {
    private static final String MAIN_CONTROLLER = "main_controller";
    private static final String TRANSITION_CONTROLLER = "transition_controller";
    private static final String OPEN_TRIGGER = "close_to_open";
    private static final String CLOSE_TRIGGER = "open_to_close";
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlayAndHold("open");
    private static final RawAnimation CLOSED_ANIM = RawAnimation.begin().thenPlayAndHold("closed");
    private static final Component TITLE = Component.translatable("block.antarchy.seashell");
    private static final int OPEN_TRANSITION_TICKS = 8;
    private static final int CLOSE_TRANSITION_TICKS = 9;

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private boolean visualOpen;
    private boolean initializedVisualState;
    private boolean redstonePowered;
    private boolean manuallyOpened;
    private boolean transitionTargetOpen;
    private int transitionTicksRemaining;

    public SeashellBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.SEASHELL_BLOCK_ENTITY.get(), pos, blockState);
        this.redstonePowered = blockState.getValue(SeashellBlock.POWERED);
        this.visualOpen = this.redstonePowered;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SeashellBlockEntity seashell) {
        seashell.syncVisualState(state.getValue(SeashellBlock.POWERED));
        seashell.tickVisualTransition();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SeashellBlockEntity seashell) {
        seashell.syncVisualState(state.getValue(SeashellBlock.POWERED));
        seashell.tickVisualTransition();
    }

    public boolean canAcceptItem(@Nullable Player player, ItemStack stack) {
        this.unpackLootTable(player);
        return !stack.isEmpty() && this.getFirstEmptySlot() >= 0;
    }

    public boolean tryInsert(@Nullable Player player, InteractionHand hand, ItemStack heldStack) {
        this.unpackLootTable(player);
        int slot = this.getFirstEmptySlot();
        if (slot < 0 || heldStack.isEmpty()) {
            return false;
        }

        ItemStack inserted = heldStack.copyWithCount(1);
        this.items.set(slot, inserted);
        if (player == null || !player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.setChangedAndSync();
        return true;
    }

    public boolean tryRemove(Player player, InteractionHand hand) {
        this.unpackLootTable(player);
        int slot = this.getLastFilledSlot();
        if (slot < 0) {
            return false;
        }

        ItemStack removed = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, removed);
        } else if (!player.addItem(removed)) {
            player.drop(removed, false);
        }

        this.setChangedAndSync();
        return true;
    }

    public boolean hasAnyContents() {
        this.generateLootIfNeeded(null);
        return this.items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public void dropContents(Level level, BlockPos pos) {
        this.unpackLootTable(null);
        Containers.dropContents(level, pos, this.items);
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
        this.setChanged();
    }

    public boolean onPowerStateChanged(boolean powered) {
        boolean wasOpen = this.isEffectivelyOpen();
        this.redstonePowered = powered;
        boolean isOpen = this.isEffectivelyOpen();
        if (wasOpen != isOpen) {
            if (isOpen) {
                this.generateLootIfNeeded(null);
            }
            this.beginTransition(isOpen);
        }

        this.setChangedAndSync();
        return wasOpen != isOpen;
    }

    public boolean openManually() {
        if (this.isEffectivelyOpen()) {
            return false;
        }

        this.manuallyOpened = true;
        this.generateLootIfNeeded(null);
        this.beginTransition(true);
        this.setChangedAndSync();
        return true;
    }

    public boolean closeManually() {
        if (!this.manuallyOpened) {
            return false;
        }

        this.manuallyOpened = false;
        boolean stillOpen = this.isEffectivelyOpen();
        if (!stillOpen) {
            this.beginTransition(false);
        }

        this.setChangedAndSync();
        return true;
    }

    public boolean isEffectivelyOpen() {
        return this.redstonePowered || this.manuallyOpened;
    }

    public void syncVisualState(boolean powered) {
        this.redstonePowered = powered;
        if (!this.initializedVisualState) {
            this.visualOpen = this.isEffectivelyOpen();
            this.initializedVisualState = true;
            return;
        }

        if (this.transitionTicksRemaining <= 0) {
            this.visualOpen = this.isEffectivelyOpen();
        }
    }

    public boolean isVisualOpen() {
        return this.visualOpen;
    }

    public List<DisplayedStack> getDisplayedStacks() {
        List<ItemStack> inserted = new ArrayList<>();
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                inserted.add(stack);
            }
        }

        List<DisplayedStack> displayed = new ArrayList<>(inserted.size());
        for (int i = 0; i < inserted.size(); i++) {
            displayed.add(new DisplayedStack(inserted.get(i), displayOffsetFor(i)));
        }
        return displayed;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, state ->
                state.setAndContinue(this.visualOpen ? OPEN_ANIM : CLOSED_ANIM)));
        controllers.add(new AnimationController<>(this, TRANSITION_CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(OPEN_TRIGGER, RawAnimation.begin().thenPlay(OPEN_TRIGGER))
                .triggerableAnim(CLOSE_TRIGGER, RawAnimation.begin().thenPlay(CLOSE_TRIGGER)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableCache;
    }

    @Override
    protected Component getDefaultName() {
        return TITLE;
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
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && this.items.get(slot).isEmpty();
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            net.minecraft.world.ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.putBoolean("VisualOpen", this.visualOpen);
        tag.putBoolean("VisualInitialized", this.initializedVisualState);
        tag.putBoolean("ManuallyOpened", this.manuallyOpened);
        tag.putBoolean("TransitionTargetOpen", this.transitionTargetOpen);
        tag.putInt("TransitionTicksRemaining", this.transitionTicksRemaining);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items")) {
            net.minecraft.world.ContainerHelper.loadAllItems(tag, this.items);
        }
        this.visualOpen = tag.getBoolean("VisualOpen");
        this.initializedVisualState = tag.getBoolean("VisualInitialized");
        this.manuallyOpened = tag.getBoolean("ManuallyOpened");
        this.transitionTargetOpen = tag.getBoolean("TransitionTargetOpen");
        this.transitionTicksRemaining = tag.getInt("TransitionTicksRemaining");
    }

    private void setChangedAndSync() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void generateLootIfNeeded(@Nullable Player player) {
        if (this.level instanceof ServerLevel) {
            this.unpackLootTable(player);
        }
    }

    private int getFirstEmptySlot() {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private int getLastFilledSlot() {
        for (int i = this.items.size() - 1; i >= 0; i--) {
            if (!this.items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void beginTransition(boolean opening) {
        this.transitionTargetOpen = opening;
        this.transitionTicksRemaining = opening ? OPEN_TRANSITION_TICKS : CLOSE_TRANSITION_TICKS;
        this.triggerAnim(TRANSITION_CONTROLLER, opening ? OPEN_TRIGGER : CLOSE_TRIGGER);
    }

    private void tickVisualTransition() {
        if (this.transitionTicksRemaining <= 0) {
            return;
        }

        this.transitionTicksRemaining--;
        if (this.transitionTicksRemaining == 0) {
            this.visualOpen = this.transitionTargetOpen;
            this.setChangedAndSync();
        }
    }

    private static Vec3 displayOffsetFor(int insertionIndex) {
        return switch (insertionIndex) {
            case 0 -> offset(1, 0);
            case 1 -> offset(0, 0);
            case 2 -> offset(2, 0);
            case 3 -> offset(1, 1);
            case 4 -> offset(0, 1);
            case 5 -> offset(2, 1);
            case 6 -> offset(1, 2);
            case 7 -> offset(0, 2);
            case 8 -> offset(2, 2);
            default -> Vec3.ZERO;
        };
    }

    private static Vec3 offset(int column, int row) {
        double x = -0.22D + column * 0.22D;
        double z = -0.22D + row * 0.22D;
        return new Vec3(x, 0.155D, z);
    }

    public record DisplayedStack(ItemStack stack, Vec3 offset) {
    }
}
