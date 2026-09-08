package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoveControl.class)
public abstract class MoveControlGravityMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    protected double wantedX;

    @Shadow
    protected double wantedY;

    @Shadow
    protected double wantedZ;

    @Shadow
    protected double speedModifier;

    @Inject(method = "setWantedPosition", at = @At("TAIL"))
    private void antarchy$convertInvertedWantedPosition(double x, double y, double z, double speed, CallbackInfo ci) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        Vec3 worldOffset = new Vec3(x - this.mob.getX(), y - this.mob.getY(), z - this.mob.getZ());
        Vec3 localOffset = AntarchyGravityRotationUtil.vecWorldToPlayer(
                worldOffset,
                AntarchyGravityApi.getGravityDirection(this.mob));
        this.wantedX = this.mob.getX() + localOffset.x;
        this.wantedY = this.mob.getY() + localOffset.y;
        this.wantedZ = this.mob.getZ() + localOffset.z;
        this.speedModifier = speed;
    }
}
