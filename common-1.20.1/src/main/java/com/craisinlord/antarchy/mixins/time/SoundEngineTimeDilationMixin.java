package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.effect.ContractedMobEffect;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.mixins.client.EntityBoundSoundInstanceAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public abstract class SoundEngineTimeDilationMixin {
    @ModifyReturnValue(method = "calculatePitch", at = @At("RETURN"))
    private float antarchy$dilatePitch(float pitch, SoundInstance sound) {
        double rate = ClientTimeDilationTicker.rateAt(sound.getX(), sound.getY(), sound.getZ());
        if (sound instanceof EntityBoundSoundInstanceAccessor accessor) {
            rate = combineRates(rate, rateForEntity(accessor.antarchy$getEntity()));
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            rate = combineRates(rate, rateForEntity(minecraft.player));
        }
        if (Math.abs(rate - TimeDilationMath.NORMAL_RATE) < 0.001D) {
            return pitch;
        }
        return (float) (pitch * Math.max(TimeDilationMath.MIN_RATE, rate));
    }

    private static double combineRates(double first, double second) {
        if (first < TimeDilationMath.NORMAL_RATE || second < TimeDilationMath.NORMAL_RATE) {
            return Math.min(first, second);
        }
        return Math.max(first, second);
    }

    private static double rateForEntity(Entity entity) {
        double rate = TimeDilationApi.getRate(entity);
        if (entity instanceof LivingEntity living) {
            if (living.hasEffect(AntarchyObjects.DILATED_EFFECT.get())) {
                rate = Math.min(rate, DilatedMobEffect.rateForAmplifier(
                        living.getEffect(AntarchyObjects.DILATED_EFFECT.get()).getAmplifier()));
            }
            if (living.hasEffect(AntarchyObjects.CONTRACTED_EFFECT.get())) {
                rate = combineRates(rate, ContractedMobEffect.rateForAmplifier(
                        living.getEffect(AntarchyObjects.CONTRACTED_EFFECT.get()).getAmplifier()));
            }
        }
        return rate;
    }
}
