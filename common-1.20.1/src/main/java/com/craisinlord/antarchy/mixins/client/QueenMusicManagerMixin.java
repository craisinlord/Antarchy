package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.QueenMusicHandler;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class QueenMusicManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$pauseVanillaMusicWhileQueenMusicIsPlaying(CallbackInfo ci) {
        if (QueenMusicHandler.isPlaying()) {
            ci.cancel();
        }
    }
}
