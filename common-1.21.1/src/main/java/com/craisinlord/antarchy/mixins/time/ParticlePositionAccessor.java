package com.craisinlord.antarchy.mixins.time;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticlePositionAccessor {
    @Accessor("x")
    double antarchy$getX();

    @Accessor("y")
    double antarchy$getY();

    @Accessor("z")
    double antarchy$getZ();
}
