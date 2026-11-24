package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.royal.RoyalLootDropGlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class BossLootDropGlowMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void antarchy$glowBossLoot(CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity itemEntity = cir.getReturnValue();
        if (itemEntity != null
                && (Object) this instanceof LivingEntity living
                && living.isDeadOrDying()
                && living.getType().is(AntarchyTags.Entities.GLOWING_BOSS_DROPS)) {
            RoyalLootDropGlow.start(itemEntity);
        }
    }
}
