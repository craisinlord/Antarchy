package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.fabric.BloodglassManager;
import java.util.Collection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class BloodglassEffectMixin {

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void antarchy$onBloodglassWardAdded(MobEffectInstance effectInstance, @Nullable Entity source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!effectInstance.is(AntarchyObjects.BLOODGLASS_WARD.get())) return;
        if (!(entity instanceof Player player)) return;
        BloodglassManager.handleWardApplied(player, effectInstance.getAmplifier());
    }

    @Inject(method = "onEffectsRemoved", at = @At("HEAD"))
    private void antarchy$onBloodglassWardRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player player)) return;
        for (MobEffectInstance effectInstance : effects) {
            if (!effectInstance.is(AntarchyObjects.BLOODGLASS_WARD.get())) continue;
            BloodglassManager.handleWardRemoved(player);
        }
    }
}
