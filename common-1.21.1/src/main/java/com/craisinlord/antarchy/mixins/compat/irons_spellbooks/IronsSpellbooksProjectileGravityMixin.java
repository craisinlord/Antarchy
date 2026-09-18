package com.craisinlord.antarchy.mixins.compat.irons_spellbooks;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Handles Iron's Spellbooks' custom travel() gravity path. */
@Mixin(targets = "io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile")
@Pseudo
public abstract class IronsSpellbooksProjectileGravityMixin {
    @Shadow public abstract float getSpeed();
    @Shadow protected abstract double getDefaultGravity();

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$prepareMagicProjectile(CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        boolean inverted = ThoraxisUndersideManager.isInUnderside(self)
                || (owner != null && AntarchyGravityApi.isGravityInverted(owner));
        if (!inverted) {
            return;
        }
        AntarchyGravityApi.setAirborneGravityDirection(
                self,
                AntarchyGravityDirection.UP,
                false,
                AntarchyGravityTransition.INSTANT
        );
    }

    @Inject(method = "shoot", at = @At("HEAD"), cancellable = true)
    private void antarchy$rotateMagicProjectileLaunch(Vec3 rotation, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        if (owner == null || (!ThoraxisUndersideManager.isInUnderside(self)
                && !AntarchyGravityApi.isGravityInverted(owner))) {
            return;
        }
        self.setDeltaMovement(AntarchyGravityRotationUtil.vecPlayerToWorld(
                rotation,
                AntarchyGravityApi.getGravityDirection(owner)
        ).scale(this.getSpeed()));
        ci.cancel();
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void antarchy$invertMagicProjectileGravity(CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(self) || self.isNoGravity()) {
            return;
        }

        // AbstractMagicProjectile.travel() already subtracted default gravity.
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, 2.0D * this.getDefaultGravity(), 0.0D));
    }
}
