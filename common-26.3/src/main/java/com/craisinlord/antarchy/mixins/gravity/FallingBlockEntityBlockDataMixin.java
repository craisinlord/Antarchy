package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityBlockDataMixin {
    @Unique
    private static final Logger ANTARCHY$LOGGER = LogUtils.getLogger();
    @Shadow public CompoundTag blockData;
    @Shadow public abstract BlockState getBlockState();

    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
            )
    )
    private boolean antarchy$placeDoublePlantCarriedBlock(Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        FallingBlockEntity entity = (FallingBlockEntity) (Object) this;
        if (!entity.entityTags().contains(GravityGunItem.GRAVITY_GUN_CARRIED_BLOCK_TAG) || !this.antarchy$isCarriedDoubleBlock(state)) {
            return original.call(level, pos, state);
        }

        return this.antarchy$tryPlaceExact(level, pos, state);
    }

    @WrapOperation(
            method = "tick",
            at = @org.spongepowered.asm.mixin.injection.At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity antarchy$preserveBlockEntityDataOnDrop(FallingBlockEntity entity, ServerLevel serverLevel, ItemLike itemLike, Operation<ItemEntity> original) {
        if (entity.entityTags().contains(GravityGunItem.GRAVITY_GUN_CARRIED_BLOCK_TAG) && this.antarchy$tryPlaceNearby(entity)) {
            return null;
        }

        if (this.blockData == null || this.blockData.isEmpty()) {
            return original.call(entity, serverLevel, itemLike);
        }

        Entity self = (Entity) (Object) this;
        ItemStack stack = new ItemStack(itemLike);
        if (!(stack.getItem() instanceof BlockItem)) {
            return original.call(entity, serverLevel, itemLike);
        }

        String blockEntityId = this.blockData.getStringOr("id", "");
        Identifier blockEntityKey = Identifier.tryParse(blockEntityId);
        if (blockEntityKey == null) {
            return original.call(entity, serverLevel, itemLike);
        }

        BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(blockEntityKey);
        if (blockEntityType == null) {
            return original.call(entity, serverLevel, itemLike);
        }

        CompoundTag stackData = this.blockData.copy();
        stackData.remove("id");
        if (stackData.isEmpty()) {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntityType, stackData));
        }
        return self.spawnAtLocation(serverLevel, stack);
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
            if (this.antarchy$isCarriedDoubleBlock(placeState)) {
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
        if (this.antarchy$isCarriedDoubleBlock(fallingState)) {
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

        BlockState lowerState = fallingState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        BlockState upperState = fallingState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        if (!level.setBlock(pos, lowerState, 3)) {
            return false;
        }
        if (!level.setBlock(pos.above(), upperState, 3)) {
            level.removeBlock(pos, false);
            return false;
        }
        return true;
    }

    private boolean antarchy$isCarriedDoubleBlock(BlockState state) {
        return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
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

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), ANTARCHY$LOGGER)) {
            RegistryAccess registryAccess = level.registryAccess();
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess);
            blockEntity.saveWithoutMetadata(output);
            CompoundTag mergedTag = output.buildResult();
            for (String key : this.blockData.keySet()) {
                mergedTag.put(key, this.blockData.get(key).copy());
            }
            blockEntity.loadWithComponents(TagValueInput.create(reporter, registryAccess, mergedTag));
        }
        blockEntity.setChanged();
    }
}
