package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.hud.CustomBossBarRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class CustomBossBarMixin {
    @Inject(
            method = "drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void antarchy$drawCustomBossBar(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent, CallbackInfo ci) {
        if (CustomBossBarRenderer.render(guiGraphics, x, y, bossEvent)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;")
    )
    private Component antarchy$hideNameForCustomBossBar(LerpingBossEvent bossEvent) {
        return CustomBossBarRenderer.isCustomBoss(bossEvent) ? Component.empty() : bossEvent.getName();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, ordinal = 1))
    private int antarchy$doubleGapForCustomBossBar(int original, @Local LerpingBossEvent bossEvent) {
        return CustomBossBarRenderer.isCustomBoss(bossEvent) ? original + 19 : original;
    }
}
