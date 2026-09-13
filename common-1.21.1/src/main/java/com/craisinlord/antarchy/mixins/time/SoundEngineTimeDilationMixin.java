package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import com.craisinlord.antarchy.content.effect.ContractedMobEffect;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.mixins.client.EntityBoundSoundInstanceAccessor;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineTimeDilationMixin {
    @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Inject(method = "tickNonPaused", at = @At("TAIL"))
    private void antarchy$refreshActiveSoundPitches(CallbackInfo ci) {
        if (!ClientTimeDilationTicker.hasActiveDilation()) {
            return;
        }
        this.instanceToChannel.forEach((sound, channel) ->
                channel.execute(audioChannel -> audioChannel.setPitch(this.antarchy$dilatePitch(sound.getPitch(), sound))));
    }

    @ModifyReturnValue(method = "calculatePitch", at = @At("RETURN"))
    private float antarchy$dilatePitch(float pitch, SoundInstance sound) {
        double rate = ClientTimeDilationTicker.rateAt(sound.getX(), sound.getY(), sound.getZ());
        if (sound instanceof EntityBoundSoundInstanceAccessor accessor) {
            double entityRate = rateForEntity(accessor.antarchy$getEntity());
            rate = combineRates(rate, entityRate);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            double listenerRate = rateForEntity(minecraft.player);
            rate = combineRates(rate, listenerRate);
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
            if (RoyalEffectHooks.dilatedHolder() != null && living.hasEffect(RoyalEffectHooks.dilatedHolder())) {
                rate = Math.min(rate, DilatedMobEffect.rateForAmplifier(
                        living.getEffect(RoyalEffectHooks.dilatedHolder()).getAmplifier()));
            }
            if (RoyalEffectHooks.contractedHolder() != null && living.hasEffect(RoyalEffectHooks.contractedHolder())) {
                rate = combineRates(rate, ContractedMobEffect.rateForAmplifier(
                        living.getEffect(RoyalEffectHooks.contractedHolder()).getAmplifier()));
            }
        }
        return rate;
    }
}
