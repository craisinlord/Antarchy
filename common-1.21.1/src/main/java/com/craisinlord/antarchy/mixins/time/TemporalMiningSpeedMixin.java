package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.effect.ContractedMobEffect;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class TemporalMiningSpeedMixin {
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void antarchy$applyTemporalMiningSpeed(
            net.minecraft.world.level.block.state.BlockState state,
            CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        double rate = 1.0D;

        if (RoyalEffectHooks.dilatedHolder() != null) {
            MobEffectInstance dilation = player.getEffect(RoyalEffectHooks.dilatedHolder());
            if (dilation != null) {
                rate = Math.min(rate, DilatedMobEffect.rateForAmplifier(dilation.getAmplifier()));
            }
        }
        if (RoyalEffectHooks.contractedHolder() != null) {
            MobEffectInstance contraction = player.getEffect(RoyalEffectHooks.contractedHolder());
            if (contraction != null) {
                rate = Math.max(rate, ContractedMobEffect.rateForAmplifier(contraction.getAmplifier()));
            }
        }

        if (rate != 1.0D) {
            cir.setReturnValue((float) (cir.getReturnValue() * rate));
        }
    }
}
