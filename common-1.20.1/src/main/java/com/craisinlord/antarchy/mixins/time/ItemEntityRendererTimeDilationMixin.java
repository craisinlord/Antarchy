package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererTimeDilationMixin {
    @Unique
    private static final ThreadLocal<ItemEntity> antarchy$renderingItem = new ThreadLocal<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void antarchy$beginItemRender(ItemEntity item, float entityYaw, float partialTick,
                                          com.mojang.blaze3d.vertex.PoseStack poseStack,
                                          net.minecraft.client.renderer.MultiBufferSource buffer,
                                          int packedLight, CallbackInfo ci) {
        antarchy$renderingItem.set(item);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void antarchy$endItemRender(ItemEntity item, float entityYaw, float partialTick,
                                        com.mojang.blaze3d.vertex.PoseStack poseStack,
                                        net.minecraft.client.renderer.MultiBufferSource buffer,
                                        int packedLight, CallbackInfo ci) {
        antarchy$renderingItem.remove();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"), index = 0)
    private float antarchy$slowItemBob(float phase) {
        ItemEntity item = antarchy$renderingItem.get();
        if (item == null) {
            return phase;
        }
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(item);
        return rate >= 1.0D ? phase : phase * (float) rate;
    }
}
