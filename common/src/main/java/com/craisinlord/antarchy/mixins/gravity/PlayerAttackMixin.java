package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
/*
 * Fixes player attack checks under inverted gravity.
 */
public abstract class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockGravityGunMelee(Entity target, CallbackInfo ci) {
        if (!AntarchySettings.gravityGunEnabled()) {
            return;
        }

        Player player = (Player) (Object) this;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GravityGunItem) {
            ci.cancel();
        }
    }
}
