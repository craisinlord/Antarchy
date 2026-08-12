package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class TigerEyeNameTagHideMixin {
    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$hideNameWhileCamouflaged(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Player player) || !TigerEyeArmorUtil.hasFullSet(player)) {
            return;
        }

        TigerEyeCamouflageClientState.CamouflageState state = TigerEyeCamouflageClientState.get(player.getId());
        if (state != null && state.active()) {
            cir.setReturnValue(false);
        }
    }
}
