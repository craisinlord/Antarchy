package com.craisinlord.antarchy.mixins.compat.scguns;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bridges Scorched Guns' Entity-based projectile physics into Antarchy gravity. */
@Mixin(targets = {
        "top.ribs.scguns.entity.projectile.ProjectileEntity",
        "top.ribs.scguns.entity.projectile.BearPackShellProjectileEntity",
        "top.ribs.scguns.entity.projectile.KrahgRoundProjectileEntity",
        "top.ribs.scguns.entity.projectile.LightningProjectileEntity",
        "top.ribs.scguns.entity.projectile.OsborneSlugProjectileEntity",
        "top.ribs.scguns.entity.projectile.ShotballProjectileEntity"
})
public abstract class ScgunsProjectileEntityGravityMixin {
    @Shadow @Nullable protected LivingEntity shooter;
    @Shadow protected double modifiedGravity;
    @Shadow public abstract Vec3 getDeltaMovement();
    @Shadow public abstract void setDeltaMovement(Vec3 movement);

    @Unique private boolean antarchy$launchDirectionAdjusted;

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$prepareScgunsProjectile(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        boolean inverted = ThoraxisUndersideManager.isInUnderside(self)
                || (this.shooter != null && AntarchyGravityApi.isGravityInverted(this.shooter));
        if (!inverted) {
            if (AntarchyGravityApi.isGravityInverted(self) && !AntarchyGravityApi.isGravityForced(self)) {
                AntarchyGravityApi.setAirborneGravityDirection(
                        self,
                        AntarchyGravityDirection.DOWN,
                        false,
                        AntarchyGravityTransition.INSTANT
                );
            }
            return;
        }

        AntarchyGravityApi.setAirborneGravityDirection(
                self,
                AntarchyGravityDirection.UP,
                false,
                AntarchyGravityTransition.INSTANT
        );
        if (!this.antarchy$launchDirectionAdjusted && self.tickCount <= 1) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.lengthSqr() > 1.0E-8D) {
                this.setDeltaMovement(AntarchyGravityRotationUtil.vecPlayerToWorld(
                        motion,
                        AntarchyGravityDirection.UP
                ));
            }
            this.antarchy$launchDirectionAdjusted = true;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$invertScgunsGravity(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!AntarchyGravityApi.isGravityInverted(self) || this.modifiedGravity == 0.0D) {
            return;
        }

        // Scorched Guns already adds modifiedGravity once near tick end. Replace
        // that downward world-Y acceleration with its upward counterpart.
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -2.0D * this.modifiedGravity, 0.0D));
    }
}
