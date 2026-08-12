package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
public abstract class SpongeAntiwaterMixin {
    private static final int MAX_DEPTH = 6;
    private static final int MAX_COUNT = 65;
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    @Inject(method = "tryAbsorbWater", at = @At("TAIL"))
    private void antarchy$tryAbsorbAntiwater(Level level, BlockPos pos, CallbackInfo ci) {
        if (!level.getBlockState(pos).is(Blocks.SPONGE)) {
            return;
        }

        if (antarchy$removeAntiwaterBreadthFirstSearch(level, pos) > 1) {
            level.setBlock(pos, Blocks.WET_SPONGE.defaultBlockState(), 2);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static int antarchy$removeAntiwaterBreadthFirstSearch(Level level, BlockPos origin) {
        return BlockPos.breadthFirstTraversal(
                origin,
                MAX_DEPTH,
                MAX_COUNT,
                (pos, consumer) -> {
                    for (Direction direction : ALL_DIRECTIONS) {
                        consumer.accept(pos.relative(direction));
                    }
                },
                pos -> antarchy$tryRemoveAntiwaterAt(level, origin, pos)
        );
    }

    private static boolean antarchy$tryRemoveAntiwaterAt(Level level, BlockPos origin, BlockPos pos) {
        if (pos.equals(origin)) {
            return true;
        }

        BlockState state = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.is(AntarchyTags.Fluids.ANTIWATER)) {
            return false;
        }

        Block block = state.getBlock();
        if (block instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(level, pos, state).isEmpty()) {
            return true;
        }

        if (block instanceof LiquidBlock) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return true;
        }

        return false;
    }
}
