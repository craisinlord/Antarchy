package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.portalgun.PortalGunCollisionHelper;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 1100)
public abstract class PortalGunEntityCollisionMixin {
    @Unique
    private AABB antarchy$portalGunStartBox;
    @Unique
    private AABB antarchy$portalGunMoveStartBox;
    @Unique
    private Vec3 antarchy$portalGunMoveStartVelocity;

    @Inject(method = "move", at = @At("HEAD"))
    private void antarchy$capturePortalGunMovementStart(MoverType type, Vec3 movement, CallbackInfo ci) {
        this.antarchy$portalGunMoveStartBox = ((Entity) (Object) this).getBoundingBox();
        this.antarchy$portalGunMoveStartVelocity = ((Entity) (Object) this).getDeltaMovement();
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void antarchy$teleportMobOnPortalCrossing(MoverType type, Vec3 movement, CallbackInfo ci) {
        AABB startBox = this.antarchy$portalGunMoveStartBox;
        Vec3 startVelocity = this.antarchy$portalGunMoveStartVelocity;
        this.antarchy$portalGunMoveStartBox = null;
        this.antarchy$portalGunMoveStartVelocity = null;
        if (startBox != null && startVelocity != null) {
            PortalGunPortalEntity.teleportEntityAfterMovement((Entity) (Object) this, startBox, startVelocity);
        }
    }

    @Inject(method = "collide", at = @At("HEAD"))
    private void antarchy$capturePortalGunStartBox(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        this.antarchy$portalGunStartBox = ((Entity) (Object) this).getBoundingBox();
    }

    @Inject(method = "collide", at = @At("RETURN"), cancellable = true)
    private void antarchy$restorePortalCrossingMotion(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (this.antarchy$portalGunStartBox == null) {
            return;
        }
        Vec3 collided = cir.getReturnValue();
        Vec3 adjusted = PortalGunCollisionHelper.resolveCollision(entity, this.antarchy$portalGunStartBox, movement, collided);
        if (adjusted != collided) {
            cir.setReturnValue(adjusted);
        }
    }

}
