package com.craisinlord.antarchy.fabric.mixin.entity;

import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class ParalyzedMobFreezeMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"))
    private void antarchy$freezeAiWhileParalyzed(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mob.hasEffect(AntarchyFabricMisc.mobEffectHolder(AntarchyFabricMisc.PARALYZED))) {
            return;
        }

        mob.getNavigation().stop();
        mob.setJumping(false);
    }
}
