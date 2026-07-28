package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingBlockEntity.class)
/*
 * Preserves full block data on carried falling blocks.
 */
public abstract class FallingBlockEntityBlockDataMixin {
    @Shadow public CompoundTag blockData;
    @Shadow public abstract BlockState getBlockState();

    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean antarchy$placeDoublePlantCarriedBlock(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        FallingBlockEntity entity = (FallingBlockEntity) (Object) this;
        if (!entity.getTags().contains(GravityGunItem.GRAVITY_GUN_CARRIED_BLOCK_TAG) || !this.antarchy$isCarriedDoublePlant(state)) {
            return original.call(level, pos, state, flags);
        }

        return this.antarchy$tryPlaceExact(level, pos, state);
    }

    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity antarchy$preserveBlockEntityDataOnDrop(FallingBlockEntity entity, ItemLike itemLike, Operation<ItemEntity> original) {
        if (entity.getTags().contains(GravityGunItem.GRAVITY_GUN_CARRIED_BLOCK_TAG) && this.antarchy$tryPlaceNearby(entity)) {
            return null;
        }

        if (this.blockData == null || this.blockData.isEmpty()) {
            return original.call(entity, itemLike);
        }

        Entity self = (Entity) (Object) this;
        ItemStack stack = new ItemStack(itemLike);
        if (!(stack.getItem() instanceof BlockItem)) {
            return original.call(entity, itemLike);
        }

        String blockEntityId = this.blockData.getString("id");
        ResourceLocation blockEntityKey = ResourceLocation.tryParse(blockEntityId);
        if (blockEntityKey == null) {
            return original.call(entity, itemLike);
        }

        BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(blockEntityKey);
        if (blockEntityType == null) {
            return original.call(entity, itemLike);
        }

        BlockItem.setBlockEntityData(stack, blockEntityType, this.blockData.copy());
        return self.spawnAtLocation(stack);
    }

    private boolean antarchy$tryPlaceNearby(FallingBlockEntity entity) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        BlockState fallingState = this.getBlockState();
        if (fallingState.isAir()) {
            return false;
        }

        List<BlockPos> candidates = new ArrayList<>();
        BlockPos basePos = self.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                candidates.add(basePos.offset(dx, 0, dz));
                candidates.add(basePos.offset(dx, -1, dz));
            }
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(self.position())));

        for (BlockPos pos : candidates) {
            if (!this.antarchy$canPlaceAt(level, pos, fallingState)) {
                continue;
            }

            BlockState placeState = fallingState;
            if (this.antarchy$isCarriedDoublePlant(placeState)) {
                if (this.antarchy$tryPlaceExact(level, pos, placeState)) {
                    self.discard();
                    return true;
                }
                continue;
            }

            if (placeState.hasProperty(BlockStateProperties.WATERLOGGED) && level.getFluidState(pos).getType() == Fluids.WATER) {
                placeState = placeState.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (!level.setBlock(pos, placeState, 3)) {
                continue;
            }

            this.antarchy$restoreBlockEntityData(level, pos, placeState);
            self.discard();
            return true;
        }

        return false;
    }

    private boolean antarchy$canPlaceAt(Level level, BlockPos pos, BlockState fallingState) {
        if (this.antarchy$isCarriedDoublePlant(fallingState)) {
            BlockState existingLower = level.getBlockState(pos);
            BlockState existingUpper = level.getBlockState(pos.above());
            boolean canReplaceLower = existingLower.canBeReplaced(
                    new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
            );
            boolean canReplaceUpper = existingUpper.canBeReplaced(
                    new DirectionalPlaceContext(level, pos.above(), Direction.DOWN, ItemStack.EMPTY, Direction.UP)
            );
            boolean hasSupport = !FallingBlock.isFree(level.getBlockState(pos.below()));
            return canReplaceLower && canReplaceUpper && hasSupport && fallingState.canSurvive(level, pos);
        }

        BlockState existingState = level.getBlockState(pos);
        boolean canReplace = existingState.canBeReplaced(
                new DirectionalPlaceContext(level, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
        );
        boolean hasSupport = !FallingBlock.isFree(level.getBlockState(pos.below()));
        return canReplace && hasSupport && fallingState.canSurvive(level, pos);
    }

    private boolean antarchy$tryPlaceExact(Level level, BlockPos pos, BlockState fallingState) {
        if (!this.antarchy$canPlaceAt(level, pos, fallingState)) {
            return false;
        }

        DoublePlantBlock.placeAt(level, fallingState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), pos, 3);
        return true;
    }

    private boolean antarchy$isCarriedDoublePlant(BlockState state) {
        return state.getBlock() instanceof DoublePlantBlock
                && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
    }

    private void antarchy$restoreBlockEntityData(Level level, BlockPos pos, BlockState placedState) {
        if (this.blockData == null || this.blockData.isEmpty() || !placedState.hasBlockEntity()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        CompoundTag mergedTag = blockEntity.saveWithoutMetadata(level.registryAccess());
        for (String key : this.blockData.getAllKeys()) {
            mergedTag.put(key, this.blockData.get(key).copy());
        }

        blockEntity.loadWithComponents(mergedTag, level.registryAccess());
        blockEntity.setChanged();
    }
}
