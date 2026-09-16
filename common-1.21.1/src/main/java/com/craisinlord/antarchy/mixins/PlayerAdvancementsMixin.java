package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.antmail.AntmailEventData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
    @Shadow @Final private ServerPlayer player;

    @Inject(method = "award", at = @At("RETURN"))
    private void antarchy$onAward(AdvancementHolder advancement, String criterion, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && player.getAdvancements().getOrStartProgress(advancement).isDone()) AntmailEventData.onAdvancement(player, advancement);
    }
}
