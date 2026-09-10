package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.portalgun.PortalGunCollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PortalGunEntityCollisionMixin {
    @Unique
    private AABB antarchy$portalGunStartBox;

    @Inject(method = "collide", at = @At("HEAD"))
    private void antarchy$capturePortalGunStartBox(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        this.antarchy$portalGunStartBox = ((Entity) (Object) this).getBoundingBox();
    }

    @Inject(method = "collide", at = @At("RETURN"), cancellable = true)
    private void antarchy$restorePortalCrossingMotion(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (this.antarchy$portalGunStartBox == null || AntarchyGravityApi.isGravityInverted(entity)) {
            return;
        }
        Vec3 adjusted = PortalGunCollisionHelper.resolveCollision(entity, this.antarchy$portalGunStartBox, movement, cir.getReturnValue());
        cir.setReturnValue(adjusted);
    }
}
