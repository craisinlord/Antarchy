package com.craisinlord.antarchy.forge.mixins.compat;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Protects virtual position calls made before the drone constructor has finished. */
@Pseudo
@Mixin(targets = "me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer", remap = false)
public abstract class DroneFakePlayerConstructionMixin extends FakePlayer {
    @Unique
    private boolean antarchy$droneConstructionComplete;

    protected DroneFakePlayerConstructionMixin(ServerLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void antarchy$finishDroneConstruction(CallbackInfo ci) {
        antarchy$droneConstructionComplete = true;
    }

    // Explicit Mojmap/SRG selectors: the optional target is unavailable to the
    // annotation processor, but its Minecraft overrides are reobfuscated by Forge.
    @Inject(method = {"position()Lnet/minecraft/world/phys/Vec3;", "m_20182_()Lnet/minecraft/world/phys/Vec3;"},
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void antarchy$positionDuringConstruction(CallbackInfoReturnable<Vec3> cir) {
        if (!antarchy$droneConstructionComplete) {
            cir.setReturnValue(super.position());
        }
    }

    @Inject(method = {"blockPosition()Lnet/minecraft/core/BlockPos;", "m_20183_()Lnet/minecraft/core/BlockPos;"},
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void antarchy$blockPositionDuringConstruction(CallbackInfoReturnable<BlockPos> cir) {
        if (!antarchy$droneConstructionComplete) {
            cir.setReturnValue(super.blockPosition());
        }
    }
}
