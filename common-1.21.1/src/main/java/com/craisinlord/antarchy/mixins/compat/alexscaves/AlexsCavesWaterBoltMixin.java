package com.craisinlord.antarchy.mixins.compat.alexscaves;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Corrects Water Bolt's explicit -0.07 Y gravity adjustment. */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.item.WaterBoltEntity")
public abstract class AlexsCavesWaterBoltMixin {
    @ModifyArg(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 1),
            index = 1
    )
    private double antarchy$invertManualGravity(double y) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        return !self.isNoGravity() && (ThoraxisUndersideManager.isInUnderside(self)
                || (owner != null && AntarchyGravityApi.isGravityInverted(owner))) ? -y : y;
    }
}
