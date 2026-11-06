package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class TigerEyeVisibilityMixin {
    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void antarchy$applyTigerEyeStealth(Entity observer, CallbackInfoReturnable<Double> cir) {
        if (!((Object) this instanceof Player player)) {
            return;
        }
        if (!(observer instanceof Mob mob)) {
            return;
        }
        if (!TigerEyeArmorUtil.shouldReduceDetection(mob, player)) {
            return;
        }
        cir.setReturnValue(cir.getReturnValueD() * (1.0D - TigerEyeArmorUtil.getDetectionReduction(player)));
    }
}
