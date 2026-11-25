package com.craisinlord.antarchy.fabric.mixin.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.ai.RelicheirusNibblePewensGoal", remap = false)
public abstract class AlexsCavesRelicheirusChunkLoadMixin extends MoveToBlockGoal {
    private AlexsCavesRelicheirusChunkLoadMixin(PathfinderMob mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, searchRange);
    }

    @Inject(method = {"getMoveToTarget", "method_30953"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void antarchy$skipUnloadedTree(CallbackInfoReturnable<BlockPos> cir) {
        if (!antarchy$chunkReady(this.mob.level(), this.blockPos)) {
            cir.setReturnValue(this.blockPos);
        }
    }

    @Inject(method = {"isValidTarget", "method_6296"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void antarchy$rejectUnloadedTarget(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!antarchy$chunkReady(level, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean antarchy$chunkReady(LevelReader level, BlockPos pos) {
        return !(level instanceof ServerLevel serverLevel)
                || serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }
}
