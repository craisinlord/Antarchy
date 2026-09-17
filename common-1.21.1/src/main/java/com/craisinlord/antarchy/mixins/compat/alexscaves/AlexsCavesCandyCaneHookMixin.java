package com.craisinlord.antarchy.mixins.compat.alexscaves;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Handles Alex's Caves' manually-launched Candy Cane Hook. */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.item.CandyCaneHookEntity")
public abstract class AlexsCavesCandyCaneHookMixin {
    private boolean antarchy$launchAdjusted;

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$rotateManualLaunch(CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (this.antarchy$launchAdjusted) {
            return;
        }
        this.antarchy$launchAdjusted = true;

        Entity owner = self.getOwner();
        boolean ownerInverted = owner != null && AntarchyGravityApi.isGravityInverted(owner);
        if (ownerInverted || ThoraxisUndersideManager.isInUnderside(self)) {
            AntarchyGravityDirection direction = ownerInverted
                    ? AntarchyGravityApi.getGravityDirection(owner)
                    : AntarchyGravityDirection.UP;
            Vec3 launch = AntarchyGravityRotationUtil.vecPlayerToWorld(
                    self.getDeltaMovement(), direction);
            self.setDeltaMovement(launch);
        }
    }

    @ModifyArg(
            method = "fling",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"),
            index = 1
    )
    private double antarchy$invertFlingBoost(double y) {
        Projectile self = (Projectile) (Object) this;
        Entity owner = self.getOwner();
        return y != 0.0D && (ThoraxisUndersideManager.isInUnderside(self)
                || (owner != null && AntarchyGravityApi.isGravityInverted(owner))) ? -y : y;
    }
}
