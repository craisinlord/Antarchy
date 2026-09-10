package com.craisinlord.antarchy.mixins;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("inGround")
    void antarchy$setInGround(boolean inGround);

    @Accessor("inGroundTime")
    void antarchy$setInGroundTime(int inGroundTime);

    @Accessor("shakeTime")
    void antarchy$setShakeTime(int shakeTime);
}
