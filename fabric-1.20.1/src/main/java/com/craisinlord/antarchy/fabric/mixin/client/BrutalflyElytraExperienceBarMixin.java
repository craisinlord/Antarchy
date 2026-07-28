package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class BrutalflyElytraExperienceBarMixin {
    @Inject(method = "isExperienceBarVisible", at = @At("RETURN"), cancellable = true)
    private void antarchy$hideExperienceBarForBrutalfly(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && BrutalflyElytraItem.isWearingBrutalflyElytra(player) && player.isFallFlying()) {
            cir.setReturnValue(false);
        }
    }
}
