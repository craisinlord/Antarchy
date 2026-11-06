package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class AntTransitCameraMixin {
    @Shadow public abstract Vec3 position();
    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow public abstract Entity entity();

    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void antarchy$descentSink(float partialTick, CallbackInfo ci) {
        if (this.entity() == Minecraft.getInstance().player) {
            float sink = AntTransitClientState.sink(partialTick);
            if (sink > 0.0F) {
                this.setPosition(this.position().subtract(0.0D, sink, 0.0D));
            }
        }
    }
}
