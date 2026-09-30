package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.Camera;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraBasisAccessor {
    @Accessor("forwards")
    Vector3f antarchy$getForwards();

    @Accessor("up")
    Vector3f antarchy$getUp();

    @Accessor("left")
    Vector3f antarchy$getLeft();
}
