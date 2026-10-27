package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntDescentClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class AntDescentCameraMixin {
    @Shadow
    public abstract Vec3 getPosition();

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(method = "setup", at = @At("TAIL"))
    private void antarchy$descentSink(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (entity != Minecraft.getInstance().player) {
            return;
        }
        float sink = AntDescentClientState.sink(partialTick);
        if (sink > 0.0F) {
            this.setPosition(this.getPosition().subtract(0.0D, sink, 0.0D));
        }
    }
}
