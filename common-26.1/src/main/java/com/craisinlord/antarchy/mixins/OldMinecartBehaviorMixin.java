package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.craisinlord.antarchy.content.minecart.AntimetalMinecartPhysics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
    private OldMinecartBehaviorMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean antarchy$redirectIsRail(BlockState state) {
        if (this.antarchy$isOnAntimetalRail()) {
            return true;
        }
        return BaseRailBlock.isRail(state);
    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"), cancellable = true)
    private void antarchy$redirectMoveAlongTrack(ServerLevel level, CallbackInfo ci) {
        if (!this.antarchy$isOnAntimetalRail() || !(this.minecart instanceof AntimetalMinecartAccess access)) {
            return;
        }
        BlockPos railPos = access.antarchy$getAntimetalRailPos();
        if (railPos == null) {
            return;
        }
        AntimetalMinecartPhysics.moveAlongTrack(this.minecart, railPos, this.minecart.level().getBlockState(railPos));
        ci.cancel();
    }

    @Inject(method = "getPosOffs", at = @At("HEAD"), cancellable = true)
    private void antarchy$sampleAntimetalPosOffs(double x, double y, double z, double offset, CallbackInfoReturnable<Vec3> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            cir.setReturnValue(AntimetalMinecartPhysics.getPosOffs(this.minecart, x, y, z, offset));
        }
    }

    @Inject(method = "getPos", at = @At("HEAD"), cancellable = true)
    private void antarchy$sampleAntimetalPos(double x, double y, double z, CallbackInfoReturnable<Vec3> cir) {
        if (this.antarchy$isOnAntimetalRail()) {
            cir.setReturnValue(AntimetalMinecartPhysics.getPos(this.minecart, x, y, z));
        }
    }

    @Unique
    private boolean antarchy$isOnAntimetalRail() {
        return this.minecart instanceof AntimetalMinecartAccess access && access.antarchy$isOnAntimetalRail();
    }
}
