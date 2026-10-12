package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketGravityMixin {

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 antarchy$fixFireworkThrustDirection(LivingEntity entity, Operation<Vec3> original) {
        Vec3 look = original.call(entity);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return look;
        }
        return AntarchyGravityRotationUtil.vecWorldToPlayer(look, direction);
    }

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getHandHoldingItemAngle(Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 antarchy$rotateAttachedRocketOffset(LivingEntity entity, Item item, Operation<Vec3> original) {
        Vec3 offset = original.call(entity, item);
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted()) {
            return offset;
        }
        return AntarchyGravityRotationUtil.vecPlayerToWorld(offset, direction);
    }
}
