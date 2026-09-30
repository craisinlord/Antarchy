package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class PortalGunZombieMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$firePortalGun(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        if (!(zombie.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack stack = zombie.getMainHandItem();
        if (stack.getItem() instanceof PortalGunItem portalGun && zombie.getRandom().nextFloat() < 0.008F) {
            PortalGunPortalEntity.PortalSide side = zombie.getRandom().nextBoolean()
                    ? PortalGunPortalEntity.PortalSide.BLUE
                    : PortalGunPortalEntity.PortalSide.ORANGE;
            portalGun.fireMobPortal(serverLevel, zombie, stack, side);
        }
    }
}
