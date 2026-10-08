package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunClientTransit;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerPortalTransitMixin {
    @Unique
    private AABB antarchy$portalTransitStartBox;
    @Unique
    private Vec3 antarchy$portalTransitStartVelocity;

    @Inject(method = "move", at = @At("HEAD"))
    private void antarchy$capturePortalTransitStart(MoverType type, Vec3 movement, CallbackInfo ci) {
        this.antarchy$portalTransitStartBox = ((LocalPlayer) (Object) this).getBoundingBox();
        this.antarchy$portalTransitStartVelocity = ((LocalPlayer) (Object) this).getDeltaMovement();
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void antarchy$applyPortalTransit(MoverType type, Vec3 movement, CallbackInfo ci) {
        AABB startBox = this.antarchy$portalTransitStartBox;
        Vec3 startVelocity = this.antarchy$portalTransitStartVelocity;
        this.antarchy$portalTransitStartBox = null;
        this.antarchy$portalTransitStartVelocity = null;
        if (startBox != null && startVelocity != null && type == MoverType.SELF) {
            PortalGunClientTransit.afterLocalMove((LocalPlayer) (Object) this, startBox, startVelocity);
        }
    }
}
