package com.craisinlord.antarchy.mixins.compat.ars_nouveau;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Handles Ars Nouveau's explicit tickNextPosition gravity adjustment. */
@Mixin(targets = "com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell")
@Pseudo
public abstract class ArsNouveauProjectileGravityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$prepareArsProjectile(CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        if (owner != null && (ThoraxisUndersideManager.isInUnderside(self)
                || AntarchyGravityApi.isGravityInverted(owner))) {
            AntarchyGravityApi.setAirborneGravityDirection(
                    self,
                    AntarchyGravityDirection.UP,
                    false,
                    AntarchyGravityTransition.INSTANT
            );
        }
    }

    @Inject(method = "tickNextPosition", at = @At("TAIL"))
    private void antarchy$invertArsProjectileGravity(CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(self) || self.isNoGravity()) {
            return;
        }

        // Ars applies -0.03 directly in tickNextPosition(). Correct that one
        // application to +0.03 without changing damping or spell effects.
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, 0.06D, 0.0D));
    }
}
