package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.ultimate.UltimateToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
/*
 * Keeps custom sapling stage updates synced like vanilla.
 */
public abstract class ServerPlayerGameModeMixin {
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    @Shadow private int gameTicks;
    @Shadow private int lastSentState;

    @Inject(method = "incrementDestroyProgress", at = @At("HEAD"))
    private void antarchy$mirror3x3DestroyProgress(BlockState state, BlockPos pos, int lastDestroyProgressTicks, CallbackInfoReturnable<Float> cir) {
        int elapsed = this.gameTicks - lastDestroyProgressTicks;
        float progress = state.getDestroyProgress(this.player, this.player.level(), pos) * (elapsed + 1);
        int stage = (int) (progress * 10.0F);
        if (stage != this.lastSentState && stage >= 0 && stage < 10) {
            UltimateToolHelper.broadcastAreaMiningProgress(this.player, this.level, pos, stage);
        }
    }
}
