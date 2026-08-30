package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineTimeDilationMixin {
    @ModifyReturnValue(method = "calculatePitch", at = @At("RETURN"))
    private float antarchy$dilatePitch(float pitch, SoundInstance sound) {
        double rate = ClientTimeDilationTicker.rateAt(sound.getX(), sound.getY(), sound.getZ());
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return pitch;
        }
        return (float) (pitch * Math.max(TimeDilationMath.MIN_RATE, rate));
    }
}
