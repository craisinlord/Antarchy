package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class AntTransitInputMixin {
    @Shadow
    public Input input;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
    private void antarchy$descentFreeze(CallbackInfo ci) {
        if (!AntTransitClientState.frozen()) {
            return;
        }
        this.input.up = false;
        this.input.down = false;
        this.input.left = false;
        this.input.right = false;
        this.input.jumping = false;
        this.input.shiftKeyDown = false;
        this.input.forwardImpulse = 0.0F;
        this.input.leftImpulse = 0.0F;
        LocalPlayer player = (LocalPlayer) (Object) this;
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);
    }
}
