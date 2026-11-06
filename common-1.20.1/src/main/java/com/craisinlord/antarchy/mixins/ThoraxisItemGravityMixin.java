package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
public abstract class ThoraxisItemGravityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04D))
    private double antarchy$thoraxisGravity(double gravity) {
        return ThoraxisEventMath.scaleGravity((Entity) (Object) this, gravity);
    }
}
