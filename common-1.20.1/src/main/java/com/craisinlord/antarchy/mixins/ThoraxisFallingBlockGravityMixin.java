package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FallingBlockEntity.class)
public abstract class ThoraxisFallingBlockGravityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04D))
    private double antarchy$thoraxisGravity(double gravity) {
        return ThoraxisEventMath.scaleGravity((Entity) (Object) this, gravity);
    }
}
