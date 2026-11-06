package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.ant.AntTeleportHelper;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerAntBehavior;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerCompanionSavedData;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class GlimmerOwnerDimensionMixin {
    @Unique
    private GlimmerEntity antarchy$travellingGlimmer;

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    private void antarchy$captureGlimmer(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        this.antarchy$travellingGlimmer = GlimmerCompanionSavedData.findNearbyFollower((ServerPlayer) (Object) this);
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"))
    private void antarchy$bringGlimmer(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        GlimmerEntity glimmer = this.antarchy$travellingGlimmer;
        this.antarchy$travellingGlimmer = null;
        GlimmerAntBehavior.clearReach(player);
        if (glimmer != null && !glimmer.isRemoved() && glimmer.level() != player.level()) {
            AntTeleportHelper.moveCompanion(glimmer, player.level(), player.position());
        }
    }
}
