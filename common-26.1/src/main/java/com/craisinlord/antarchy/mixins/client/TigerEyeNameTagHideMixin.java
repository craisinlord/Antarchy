package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class TigerEyeNameTagHideMixin {
    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$hideNameWhileCamouflaged(LivingEntity entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isCamouflaged(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void antarchy$captureCamouflage(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        ((AntarchyLivingRenderStateAccess) state).antarchy$setTigerEyeCamouflaged(antarchy$isCamouflaged(entity));
    }

    @Unique
    private static boolean antarchy$isCamouflaged(LivingEntity entity) {
        if (!(entity instanceof Player player) || !TigerEyeArmorUtil.hasFullSet(player)) {
            return false;
        }

        TigerEyeCamouflageClientState.CamouflageState state = TigerEyeCamouflageClientState.get(player.getId());
        return state != null && state.active();
    }
}
