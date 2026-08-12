package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Supplier;

public class UpwardFallingBlockEntity extends Entity {

    public static Supplier<EntityType<UpwardFallingBlockEntity>> TYPE;

    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE =
            SynchedEntityData.defineId(UpwardFallingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    private static final double RISE_ACCEL = 0.04;
    private static final double DRAG       = 0.98;
    private static final int    MAX_TICKS  = 200;

    public int time = 0;
    private boolean breaksOnLeaves = true;
    private double distanceTraveled = 0.0;

    public UpwardFallingBlockEntity(EntityType<UpwardFallingBlockEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    public static void fallUp(Level level, BlockPos pos, BlockState state) {
        fallUp(level, pos, state, true);
    }

    public static void fallUp(Level level, BlockPos pos, BlockState state, boolean breaksOnLeaves) {
        if (level.isClientSide) return;
        // Prevent stacking multiple entities from the same block position
        if (!level.getEntitiesOfClass(UpwardFallingBlockEntity.class, new AABB(pos)).isEmpty()) return;

        level.removeBlock(pos, false);
        UpwardFallingBlockEntity entity = new UpwardFallingBlockEntity(TYPE.get(), level);
        entity.entityData.set(DATA_BLOCK_STATE, state);
        entity.breaksOnLeaves = breaksOnLeaves;
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        this.setOldPosAndRot();

        BlockState blockState = getBlockState();
        if (blockState.isAir()) {
            if (!level().isClientSide) {
                discard();
            }
            return;
        }

        double beforeY = getY();
        setDeltaMovement(getDeltaMovement().add(0.0, RISE_ACCEL, 0.0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(DRAG));
        distanceTraveled += Math.max(0.0, getY() - beforeY);

        if (level().isClientSide) return;

        time++;
        if (time > MAX_TICKS || getY() >= level().getMaxBuildHeight()) {
            dropAndRemove(blockState);
            return;
        }

        damageEntitiesAlongPath(blockState);

        BlockPos headPos  = BlockPos.containing(getX(), getBoundingBox().maxY, getZ());
        BlockState above  = level().getBlockState(headPos);

        // Leaves block the entity but can't hold scaffolding — drop as item, unless this
        // instance is allowed to land on leaves like a normal falling block would
        if (above.is(BlockTags.LEAVES) && this.breaksOnLeaves) {
            dropAndRemove(blockState);
            return;
        }

        boolean hitScaffolding = above.getBlock() instanceof AntimetalScaffoldingBlock;

        if (this.verticalCollision || hitScaffolding) {
            placeAtLanding(blockState, headPos.below());
            return;
        }
    }

    private void placeAtLanding(BlockState state, BlockPos landingPos) {
        if (!level().isClientSide) {
            BlockState existing = level().getBlockState(landingPos);
            if (existing.canBeReplaced()) {
                level().setBlock(landingPos, state, Block.UPDATE_ALL);
            } else {
                dropAndRemove(state);
                return;
            }
        }
        discard();
    }

    private void damageEntitiesAlongPath(BlockState state) {
        float damage = getImpactDamage(state);
        if (damage <= 0.0F) {
            return;
        }

        AABB hitBox = getBoundingBox().inflate(0.15D);
        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, hitBox, LivingEntity::isAlive)) {
            living.hurt(level().damageSources().fallingBlock(this), damage);
        }
    }

    private float getImpactDamage(BlockState state) {
        float baseDamage = Math.max(0.0F, (float) this.distanceTraveled - 1.5F);
        if (baseDamage <= 0.0F) {
            return 0.0F;
        }

        float thicknessMultiplier = 1.0F;
        if (state.hasProperty(PointedDripstoneBlock.THICKNESS)) {
            thicknessMultiplier = switch (state.getValue(PointedDripstoneBlock.THICKNESS)) {
                case TIP, TIP_MERGE -> 2.0F;
                case FRUSTUM -> 1.5F;
                case MIDDLE -> 1.25F;
                case BASE -> 1.0F;
            };
        }

        return Math.min(baseDamage * thicknessMultiplier, 40.0F);
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
