package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.portalgun.PortalGunCollisionHelper;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PortalGunEntityCollisionMixin {
    @Unique
    private AABB antarchy$portalGunStartBox;
    @Unique
    private AABB antarchy$portalGunMoveStartBox;
    @Unique
    private Vec3 antarchy$portalGunMoveStartPosition;

    @Inject(method = "move", at = @At("HEAD"))
    private void antarchy$capturePortalGunMovementStart(net.minecraft.world.entity.MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        this.antarchy$portalGunMoveStartBox = entity.getBoundingBox();
        this.antarchy$portalGunMoveStartPosition = entity.position();
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void antarchy$teleportMobOnPortalCrossing(net.minecraft.world.entity.MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (this.antarchy$portalGunMoveStartBox != null && this.antarchy$portalGunMoveStartPosition != null) {
            PortalGunPortalEntity.teleportEntityAfterMovement(
                    entity,
                    this.antarchy$portalGunMoveStartBox,
                    entity.position().subtract(this.antarchy$portalGunMoveStartPosition)
            );
        }
        this.antarchy$portalGunMoveStartBox = null;
        this.antarchy$portalGunMoveStartPosition = null;
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
        Vec3 adjusted = PortalGunCollisionHelper.resolveCollision(entity, this.antarchy$portalGunStartBox, movement, cir.getReturnValue());
        cir.setReturnValue(adjusted);
    }

}
