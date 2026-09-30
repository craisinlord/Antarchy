package com.craisinlord.antarchy.mixins.client;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityPortalLerpAccessor {
    @Mutable
    @Accessor("lerpSteps")
    void antarchy$setLerpSteps(int steps);

    @Mutable
    @Accessor("lerpHeadSteps")
    void antarchy$setLerpHeadSteps(int steps);
}
