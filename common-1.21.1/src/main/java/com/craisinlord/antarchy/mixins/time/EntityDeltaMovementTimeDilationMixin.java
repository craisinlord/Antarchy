package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationEntityAccess;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityDeltaMovementTimeDilationMixin {
    @ModifyVariable(method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 antarchy$scaleTimeDilatedVelocityChange(Vec3 nextMovement) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof TimeDilationEntityAccess access)
                || access.antarchy$isInTimeDilationMove()) {
            return nextMovement;
        }

        double rate = TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return nextMovement;
        }

        Vec3 currentMovement = entity.getDeltaMovement();
        double y = currentMovement.y + (nextMovement.y - currentMovement.y) * rate;
        if (nextMovement.y > currentMovement.y && nextMovement.y > 0.0D && (entity.onGround() || currentMovement.y <= 0.0D)) {
            y = nextMovement.y;
        }
        return new Vec3(
                currentMovement.x + (nextMovement.x - currentMovement.x) * rate,
                y,
                currentMovement.z + (nextMovement.z - currentMovement.z) * rate
        );
    }
}
