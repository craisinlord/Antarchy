package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Supplier;

public class UpwardFallingBlockEntity extends Entity {

    /** Set by the platform registration before any block tick fires. */
    public static Supplier<EntityType<UpwardFallingBlockEntity>> TYPE;

    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(UpwardFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    private static final double RISE_SPEED = 0.5;
    private static final int    MAX_TICKS  = 200;

    public int time = 0;

    public UpwardFallingBlockEntity(EntityType<UpwardFallingBlockEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    public static void fallUp(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        // Prevent stacking multiple entities from the same block position
        if (!level.getEntitiesOfClass(UpwardFallingBlockEntity.class, new AABB(pos)).isEmpty()) return;

        level.removeBlock(pos, false);
        UpwardFallingBlockEntity entity = new UpwardFallingBlockEntity(TYPE.get(), level);
        entity.entityData.set(DATA_BLOCK_STATE, state);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        BlockState blockState = getBlockState();
        if (blockState.isAir()) {
            discard();
            return;
        }

        time++;
        if (time > MAX_TICKS || getY() >= level().getMaxBuildHeight()) {
            dropAndRemove(blockState);
            return;
        }

        // Move upward directly — noPhysics=true means move() just calls setPos()
        setPos(getX(), getY() + RISE_SPEED, getZ());

        BlockPos headPos  = BlockPos.containing(getX(), getY() + getBbHeight(), getZ());
        BlockState above  = level().getBlockState(headPos);

        // Leaves block the entity but can't hold scaffolding — drop as item
        if (above.is(BlockTags.LEAVES)) {
            dropAndRemove(blockState);
            return;
        }

        boolean hitCeiling     = above.isFaceSturdy(level(), headPos, Direction.DOWN);
        boolean hitScaffolding = above.getBlock() instanceof AntimetalScaffoldingBlock;

        if (hitCeiling || hitScaffolding) {
            BlockPos  placePos = headPos.below();
            BlockState atPlace = level().getBlockState(placePos);
            if (atPlace.isAir() || atPlace.canBeReplaced()) {
                level().setBlock(placePos, blockState, 3);
            } else {
                dropAndRemove(blockState);
                return;
            }
            discard();
        }
    }

    private void dropAndRemove(BlockState state) {
        if (!level().isClientSide && !state.isAir()) {
            ItemStack drop = new ItemStack(state.getBlock().asItem());
            if (!drop.isEmpty()) spawnAtLocation(drop);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            BlockState state = NbtUtils.readBlockState(
                    level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),
                    tag.getCompound("BlockState")
            );
            entityData.set(DATA_BLOCK_STATE, state);
        }
        time = tag.getInt("Time");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
        tag.putInt("Time", time);
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable()  { return false; }
}
