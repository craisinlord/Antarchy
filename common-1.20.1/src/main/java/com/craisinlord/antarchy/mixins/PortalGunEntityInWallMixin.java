package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PortalGunEntityInWallMixin {
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void antarchy$ignorePortalInWall(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity.level() == null) {
            return;
        }
        for (PortalGunPortalEntity portal : entity.level().getEntitiesOfClass(PortalGunPortalEntity.class, entity.getBoundingBox().inflate(2.0D), PortalGunPortalEntity::isAlive)) {
            if (portal.intersectsEntityBounds(entity) || portal.getCollisionRemovalAabbForEntity(entity).intersects(entity.getBoundingBox())) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
