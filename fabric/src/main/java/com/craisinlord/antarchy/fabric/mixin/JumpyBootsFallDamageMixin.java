package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.fabric.util.JumpyBootsFabricHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class JumpyBootsFallDamageMixin {
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float antarchy$reduceJumpyBootsFallDamage(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!JumpyBootsItem.isWearingJumpyBoots(self)) return fallDistance;
        long protectionUntil = JumpyBootsFabricHelper.getProtectionUntil(self);
        if (self.level().getGameTime() < protectionUntil) {
            return fallDistance * 0.25F;
        }
        return fallDistance;
    }
}
