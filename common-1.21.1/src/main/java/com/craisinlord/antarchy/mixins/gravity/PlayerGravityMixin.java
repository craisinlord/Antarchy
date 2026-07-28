package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
/*
 * Handles player-only gravity sync and movement fixes.
 */
public abstract class PlayerGravityMixin {
    @Redirect(
            method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 antarchy$rotateTravelLookAngle(Player player) {
        Vec3 lookAngle = player.getLookAngle();
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(player);
        if (!direction.isInverted() || player.isFallFlying()) return lookAngle;
        return AntarchyGravityRotationUtil.vecWorldToPlayer(lookAngle, direction);
    }

    @WrapOperation(method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"))
    private BlockPos antarchy$fixTravelBlockPos(double x, double y, double z,
            Operation<BlockPos> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection((Entity) (Object) this);
        if (!direction.isInverted()) return original.call(x, y, z);
        Vec3 rotate = AntarchyGravityRotationUtil.vecPlayerToWorld(
                new Vec3(0.0D, 0.9D, 0.0D), direction);
        return original.call(x - rotate.x, y - rotate.y + 0.9D, z - rotate.z);
    }

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", ordinal = 0))
    private float antarchy$fixAttackYaw0(Player attacker, Operation<Float> original,
            @Local(argsOnly = true) Entity target) {
        AntarchyGravityDirection targetDir = AntarchyGravityApi.getGravityDirection(target);
        AntarchyGravityDirection attackerDir = AntarchyGravityApi.getGravityDirection(attacker);
        if (targetDir == attackerDir) return original.call(attacker);
        Vec2 worldRot = AntarchyGravityRotationUtil.rotPlayerToWorld(
                original.call(attacker), attacker.getXRot(), attackerDir);
        return AntarchyGravityRotationUtil.rotWorldToPlayer(worldRot.x, worldRot.y, targetDir).x;
    }

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", ordinal = 1))
    private float antarchy$fixAttackYaw1(Player attacker, Operation<Float> original,
            @Local(argsOnly = true) Entity target) {
        AntarchyGravityDirection targetDir = AntarchyGravityApi.getGravityDirection(target);
        AntarchyGravityDirection attackerDir = AntarchyGravityApi.getGravityDirection(attacker);
        if (targetDir == attackerDir) return original.call(attacker);
        Vec2 worldRot = AntarchyGravityRotationUtil.rotPlayerToWorld(
                original.call(attacker), attacker.getXRot(), attackerDir);
        return AntarchyGravityRotationUtil.rotWorldToPlayer(worldRot.x, worldRot.y, targetDir).x;
    }

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", ordinal = 2))
    private float antarchy$fixAttackYaw2(Player attacker, Operation<Float> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(attacker);
        if (!direction.isInverted()) return original.call(attacker);
        return AntarchyGravityRotationUtil.rotPlayerToWorld(
                original.call(attacker), attacker.getXRot(), direction).x;
    }

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", ordinal = 3))
    private float antarchy$fixAttackYaw3(Player attacker, Operation<Float> original) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(attacker);
        if (!direction.isInverted()) return original.call(attacker);
        return AntarchyGravityRotationUtil.rotPlayerToWorld(
                original.call(attacker), attacker.getXRot(), direction).x;
    }
}
