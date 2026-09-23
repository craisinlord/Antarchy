package com.craisinlord.antarchy.mixins.compat.alexscaves;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Corrects Depth Charge's custom tick, which bypasses ThrowableProjectile.tick. */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.item.DepthChargeEntity")
@Pseudo
public abstract class AlexsCavesDepthChargeMixin {
    @ModifyArg(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"),
            index = 1,
            require = 0
    )
    private double antarchy$invertManualGravity(double y) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        return !self.isNoGravity() && (ThoraxisUndersideManager.isInUnderside(self)
                || (owner != null && AntarchyGravityApi.isGravityInverted(owner))) ? -y : y;
    }
}
