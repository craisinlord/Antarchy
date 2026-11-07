package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class AntTransitInputMixin {
    @Shadow public ClientInput input;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void antarchy$descentFreeze(CallbackInfo ci) {
        if (AntTransitClientState.frozen()) {
            Input keys = input.keyPresses;
            input.keyPresses = new Input(false, false, false, false, false, false, keys.sprint());
            LocalPlayer player = (LocalPlayer) (Object) this;
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);
        }
    }
}
