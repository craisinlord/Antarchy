package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantBlock.class)
public abstract class GlowVinesUnderLeavesMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowGlowVinesUnderLeaves(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!AntarchySettings.glowVinesUnderLeaves()) {
            return;
        }
        if (!state.is(Blocks.CAVE_VINES) && !state.is(Blocks.CAVE_VINES_PLANT)) {
            return;
        }

        if (level.getBlockState(pos.above()).is(BlockTags.LEAVES)) {
            cir.setReturnValue(true);
        }
    }
}
