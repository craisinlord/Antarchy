package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationEntityAccess;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMoveTimeDilationMixin {
    @Inject(method = "move", at = @At("HEAD"))
    private void antarchy$beginTimeDilationMove(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        if (this instanceof TimeDilationEntityAccess access) {
            access.antarchy$setInTimeDilationMove(true);
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void antarchy$endTimeDilationMove(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        if (this instanceof TimeDilationEntityAccess access) {
            access.antarchy$setInTimeDilationMove(false);
        }
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 antarchy$scaleTimeDilatedMovement(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        double rate = TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return movement;
        }
        return movement.scale(rate);
    }
}
