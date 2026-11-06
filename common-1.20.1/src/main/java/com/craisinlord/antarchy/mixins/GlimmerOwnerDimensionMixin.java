package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.ant.AntTeleportHelper;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerAntBehavior;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerCompanionSavedData;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
abstract class GlimmerOwnerDimensionMixin {
    @Unique
    private GlimmerEntity antarchy$travellingGlimmer;

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), remap = false)
    private void antarchy$captureGlimmer(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        this.antarchy$travellingGlimmer = GlimmerCompanionSavedData.findNearbyFollower((ServerPlayer) (Object) this);
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"), remap = false)
    private void antarchy$bringGlimmer(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        this.antarchy$finishTravel();
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"))
    private void antarchy$captureGlimmerOnTeleport(ServerLevel destination, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        this.antarchy$travellingGlimmer = destination == player.level() ? null : GlimmerCompanionSavedData.findNearbyFollower(player);
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("RETURN"))
    private void antarchy$bringGlimmerOnTeleport(ServerLevel destination, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        this.antarchy$finishTravel();
    }

    @Unique
    private void antarchy$finishTravel() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        GlimmerEntity glimmer = this.antarchy$travellingGlimmer;
        this.antarchy$travellingGlimmer = null;
        GlimmerAntBehavior.clearReach(player);
        if (glimmer != null && !glimmer.isRemoved() && glimmer.level() != player.level()) {
            AntTeleportHelper.moveCompanion(glimmer, player.serverLevel(), player.position());
        }
    }
}
