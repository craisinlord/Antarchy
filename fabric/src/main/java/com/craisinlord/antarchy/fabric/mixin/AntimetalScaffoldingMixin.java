package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScaffoldingBlock.class)
public abstract class AntimetalScaffoldingMixin {
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void antarchy$antimetalPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!antarchy$isAntimetalScaffolding()) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        int distance = antarchy$getDistance(level, pos);
        BlockState state = ((ScaffoldingBlock) (Object) this).defaultBlockState()
                .setValue(ScaffoldingBlock.WATERLOGGED, level.getFluidState(pos).getType().isSame(net.minecraft.world.level.material.Fluids.WATER))
                .setValue(ScaffoldingBlock.DISTANCE, distance)
                .setValue(ScaffoldingBlock.BOTTOM, antarchy$isBottom(level, pos, distance));
        cir.setReturnValue(state);
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void antarchy$antimetalCanSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!antarchy$isAntimetalScaffolding()) {
            return;
        }

        cir.setReturnValue(antarchy$getDistance(level, pos) < 7);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$antimetalTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!antarchy$isAntimetalScaffolding()) {
            return;
        }

        int distance = antarchy$getDistance(level, pos);
        BlockState updated = state.setValue(ScaffoldingBlock.DISTANCE, distance)
                .setValue(ScaffoldingBlock.BOTTOM, antarchy$isBottom(level, pos, distance));

        if (updated.getValue(ScaffoldingBlock.DISTANCE) == 7) {
            if (state.getValue(ScaffoldingBlock.DISTANCE) == 7) {
                FallingBlockEntity.fall(level, pos, updated);
            } else {
                level.destroyBlock(pos, true);
            }
        } else if (state != updated) {
            level.setBlock(pos, updated, 3);
        }

        ci.cancel();
    }

    private boolean antarchy$isAntimetalScaffolding() {
        return ((ScaffoldingBlock) (Object) this).asItem() == AntarchyFabricContent.ANTIMETAL_SCAFFOLDING.get().asItem();
    }

    private int antarchy$getDistance(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable().move(Direction.DOWN);
        BlockState below = level.getBlockState(cursor);
        int distance = 7;
        if (below.is((ScaffoldingBlock) (Object) this)) {
            distance = below.getValue(ScaffoldingBlock.DISTANCE) + 1;
        } else if (below.isFaceSturdy(level, cursor, Direction.UP)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos.MutableBlockPos neighborPos = pos.mutable().move(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!neighbor.is((ScaffoldingBlock) (Object) this)) {
                continue;
            }

            distance = Math.min(distance, neighbor.getValue(ScaffoldingBlock.DISTANCE) + 1);
            if (distance == 1) {
                break;
            }
        }

        return distance;
    }

    private boolean antarchy$isBottom(BlockGetter level, BlockPos pos, int distance) {
        if (distance <= 0) {
            return false;
        }

        return !level.getBlockState(pos.below()).is((ScaffoldingBlock) (Object) this);
    }
}
