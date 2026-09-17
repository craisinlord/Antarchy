package com.craisinlord.antarchy.mixins.compat.scguns;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Corrects Scorched Guns arrow-derived projectiles with custom tick gravity. */
@Mixin(targets = "top.ribs.scguns.entity.projectile.BrassBoltEntity")
public abstract class ScgunsArrowGravityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$invertScgunsArrowGravity(CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.isNoGravity() || (!AntarchyGravityApi.isGravityInverted(self)
                && !ThoraxisUndersideManager.isInUnderside(self))) {
            return;
        }

        // Brass Bolt adds +0.01 after AbstractArrow.tick(); invert that custom
        // correction to -0.01, without touching normal arrows.
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, -0.02D, 0.0D));
    }
}
