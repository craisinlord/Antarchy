package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
/*
 * Spawns crossbow shots with gravity-aware aim.
 */
public abstract class CrossbowItemGravityMixin {

    private static final double BOLT_OFFSET = 0.15000000596046448D;

    @WrapOperation(method = "createProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0))
    private static double antarchy$fixCrossbowSpawnX(LivingEntity shooter, Operation<Double> original) {
        if (!AntarchySettings.invertProjectilesFromInvertedPlayers() || !(shooter instanceof Player)) return original.call(shooter);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(shooter);
        if (!direction.isInverted()) return original.call(shooter);
        Vec3 offset = AntarchyGravityRotationUtil.vecPlayerToWorld(0.0D, BOLT_OFFSET, 0.0D, direction);
        return shooter.getEyePosition().subtract(offset).x;
    }

    @WrapOperation(method = "createProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D", ordinal = 0))
    private static double antarchy$fixCrossbowSpawnY(LivingEntity shooter, Operation<Double> original) {
        if (!AntarchySettings.invertProjectilesFromInvertedPlayers() || !(shooter instanceof Player)) return original.call(shooter);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(shooter);
        if (!direction.isInverted()) return original.call(shooter);
        Vec3 offset = AntarchyGravityRotationUtil.vecPlayerToWorld(0.0D, BOLT_OFFSET, 0.0D, direction);
        return shooter.getEyePosition().subtract(offset).y + BOLT_OFFSET;
    }

    @WrapOperation(method = "createProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 0))
    private static double antarchy$fixCrossbowSpawnZ(LivingEntity shooter, Operation<Double> original) {
        if (!AntarchySettings.invertProjectilesFromInvertedPlayers() || !(shooter instanceof Player)) return original.call(shooter);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(shooter);
        if (!direction.isInverted()) return original.call(shooter);
        Vec3 offset = AntarchyGravityRotationUtil.vecPlayerToWorld(0.0D, BOLT_OFFSET, 0.0D, direction);
        return shooter.getEyePosition().subtract(offset).z;
    }
}
