package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
/*
 * Flips arrow gravity for inverted shooters.
 */
public abstract class AbstractArrowGravityMixin {

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setDeltaMovement(DDD)V"))
    private void antarchy$fixArrowGravityDroop(AbstractArrow instance, double x, double y, double z, Operation<Void> original) {
        if (!AntarchySettings.invertProjectilesFromInvertedPlayers()) {
            original.call(instance, x, y, z);
            return;
        }
        Entity owner = instance.getOwner();
        if (!(owner instanceof Player player)) {
            original.call(instance, x, y, z);
            return;
        }
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(player);
        if (!direction.isInverted()) {
            original.call(instance, x, y, z);
            return;
        }
        Vec3 velocity = new Vec3(x, y + 0.05D, z);
        velocity = AntarchyGravityRotationUtil.vecWorldToPlayer(velocity, direction);
        velocity = new Vec3(velocity.x, velocity.y - 0.05D, velocity.z);
        velocity = AntarchyGravityRotationUtil.vecPlayerToWorld(velocity, direction);
        original.call(instance, velocity.x, velocity.y, velocity.z);
    }
}
