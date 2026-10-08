package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PortalGunItemEntityRemovalMixin {
    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void antarchy$clearPortalsOfDestroyedGun(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof ItemEntity itemEntity
                && (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)
                && !itemEntity.isRemoved()
                && itemEntity.level() instanceof ServerLevel level
                && itemEntity.getItem().getItem() instanceof PortalGunItem) {
            PortalGunItem.clearPortalsOf(level, itemEntity.getItem());
        }
    }
}
