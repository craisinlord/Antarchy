package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ComputerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final String DISKS_TAG = "Disks";
    private static final String ACTIVE_UNTIL_TAG = "ActiveUntil";
    private final Set<ResourceLocation> diskIds = new LinkedHashSet<>();
    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ON = RawAnimation.begin().thenLoop("on_state");
    private static final RawAnimation OFF = RawAnimation.begin().thenLoop("off_state");
    private static final RawAnimation TURN_ON = RawAnimation.begin().thenPlay("turn_on");
    private static final RawAnimation TURN_OFF = RawAnimation.begin().thenPlay("turn_off");
    private long activeUntil;
    private boolean lastActive;

    public ComputerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<ComputerBlockEntity>> type) {
        super(type.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ComputerBlockEntity computer) {
        boolean active = computer.isActive();
        if (active != computer.lastActive) {
            computer.lastActive = active;
            computer.triggerAnim("transitions", active ? "turn_on" : "turn_off");
        }
        if (!level.isClientSide && computer.activeUntil > 0L && level.getGameTime() >= computer.activeUntil && !level.hasNeighborSignal(pos)) {
            computer.activeUntil = 0L;
            computer.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean insert(ItemStack stack, Player player) {
        if (!stack.is(AntarchyObjects.FLOPPY_DISK.get())) {
            return false;
        }
        ResourceLocation diskId = stack.get(AntarchyObjects.FLOPPY_DISK_COMPONENT.get());
        if (diskId == null || this.diskIds.contains(diskId)) {
            stack.shrink(1);
            activate();
            return true;
        }
        this.diskIds.add(diskId);
        stack.shrink(1);
        activate();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public void activate() {
        if (level != null && !level.isClientSide) {
            activeUntil = Math.max(activeUntil, level.getGameTime() + 40L);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isActive() {
        return level != null && (level.hasNeighborSignal(worldPosition) || level.getGameTime() < activeUntil);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "state", state -> state.setAndContinue(isActive() ? ON : OFF)));
        controllers.add(new AnimationController<>(this, "transitions", state -> PlayState.STOP)
                .triggerableAnim("turn_on", TURN_ON)
                .triggerableAnim("turn_off", TURN_OFF));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }

    public List<ResourceLocation> diskIds() {
        return List.copyOf(diskIds);
    }

    public boolean ejectOne(Player player) {
        if (level == null || level.isClientSide || diskIds.isEmpty()) {
            return false;
        }
        ResourceLocation diskId = diskIds.iterator().next();
        diskIds.remove(diskId);
        ItemStack disk = diskStack(diskId);
        if (!player.addItem(disk)) {
            player.drop(disk, false);
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public void dropDisks(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (ResourceLocation diskId : diskIds) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, diskStack(diskId));
        }
        diskIds.clear();
    }

    private static ItemStack diskStack(ResourceLocation diskId) {
        ItemStack stack = new ItemStack(AntarchyObjects.FLOPPY_DISK.get());
        stack.set(AntarchyObjects.FLOPPY_DISK_COMPONENT.get(), diskId);
        return stack;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        diskIds.clear();
        ListTag disks = tag.getList(DISKS_TAG, 8);
        for (int index = 0; index < disks.size(); index++) {
            try {
                diskIds.add(ResourceLocation.parse(disks.getString(index)));
            } catch (Exception ignored) {
            }
        }
        activeUntil = tag.getLong(ACTIVE_UNTIL_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag disks = new ListTag();
        for (ResourceLocation diskId : diskIds) {
            disks.add(StringTag.valueOf(diskId.toString()));
        }
        tag.put(DISKS_TAG, disks);
        tag.putLong(ACTIVE_UNTIL_TAG, activeUntil);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
