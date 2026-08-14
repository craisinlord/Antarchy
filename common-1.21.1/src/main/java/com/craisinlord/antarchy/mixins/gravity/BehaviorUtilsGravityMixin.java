package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BehaviorUtils.class)
public abstract class BehaviorUtilsGravityMixin {
    @WrapOperation(
            method = "throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)V",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private static ItemEntity antarchy$fixThrownItemSpawn(
            Level level,
            double x,
            double y,
            double z,
            ItemStack stack,
            Operation<ItemEntity> original,
            @Local float yOffset,
            @Local LivingEntity entity
    ) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return original.call(level, x, y, z, stack);
        }

        Vec3 eyeOffset = AntarchyGravityRotationUtil.getEyeOffset(entity, entity.getEyeHeight());
        Vec3 offset = eyeOffset.normalize().scale(yOffset);
        Vec3 itemPos = entity.position().add(eyeOffset).subtract(offset);
        ItemEntity itemEntity = original.call(level, itemPos.x(), itemPos.y(), itemPos.z(), stack);
        AntarchyGravityApi.setGravityDirection(itemEntity, AntarchyGravityApi.getGravityDirection(entity));
        return itemEntity;
    }

    @WrapOperation(
            method = "throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private static void antarchy$fixThrownItemVelocity(
            ItemEntity itemEntity,
            Vec3 deltaMovement,
            Operation<Void> original,
            @Local LivingEntity entity
    ) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            original.call(itemEntity, deltaMovement);
            return;
        }

        AntarchyGravityApi.setWorldVelocity(itemEntity, deltaMovement);
    }
}
