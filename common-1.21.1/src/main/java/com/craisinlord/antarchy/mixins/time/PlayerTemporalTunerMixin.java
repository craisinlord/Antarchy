package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationMath;
import com.craisinlord.antarchy.content.time.TemporalTunerAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerTemporalTunerMixin implements TemporalTunerAccess {
    @Unique
    private double antarchy$temporalTunerRate = TimeDilationMath.NORMAL_RATE;

    @Override
    public double antarchy$getTemporalTunerRate() {
        return antarchy$temporalTunerRate;
    }

    @Override
    public void antarchy$setTemporalTunerRate(double rate) {
        antarchy$temporalTunerRate = TimeDilationMath.clampRate(rate);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$saveTemporalTunerRate(CompoundTag tag, CallbackInfo ci) {
        tag.putDouble("AntarchyTemporalTunerRate", antarchy$temporalTunerRate);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void antarchy$loadTemporalTunerRate(CompoundTag tag, CallbackInfo ci) {
        antarchy$temporalTunerRate = TimeDilationMath.clampRate(tag.getDouble("AntarchyTemporalTunerRate"));
    }
}
