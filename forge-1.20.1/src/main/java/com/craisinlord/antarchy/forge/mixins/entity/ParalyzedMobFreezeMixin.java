package com.craisinlord.antarchy.forge.mixins.entity;

import com.craisinlord.antarchy.forge.registry.AntarchyForgeMisc;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
/*
 * Stops mob AI movement while paralyzed.
 */
public abstract class ParalyzedMobFreezeMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"))
    private void antarchy$freezeAiWhileParalyzed(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mob.hasEffect(AntarchyForgeMisc.PARALYZED.get())) {
            return;
        }

        mob.getNavigation().stop();
        mob.setJumping(false);
    }
}
