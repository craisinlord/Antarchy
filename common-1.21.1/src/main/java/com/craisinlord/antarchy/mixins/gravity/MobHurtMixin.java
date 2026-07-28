package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
/*
 * Fixes hurt push logic that assumes normal gravity.
 */
public abstract class MobHurtMixin {
    @WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getYRot()F"))
    private float wrapOperation_tryAttack_getYaw_0(Mob attacker, Operation<Float> original, Entity target) {
        AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(target);
        if (!gravityDirection.isInverted()) {
            return original.call(attacker);
        }
        return AntarchyGravityRotationUtil.rotWorldToPlayer(original.call(attacker), attacker.getXRot(), gravityDirection).x;
    }
}
