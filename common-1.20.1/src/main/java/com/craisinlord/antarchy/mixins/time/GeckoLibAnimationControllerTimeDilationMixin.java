package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;

@Mixin(AnimationController.class)
public abstract class GeckoLibAnimationControllerTimeDilationMixin<T extends GeoAnimatable> {
    @Shadow(remap = false)
    protected T animatable;

    @ModifyReturnValue(method = "adjustTick", at = @At("RETURN"), remap = false)
    private double antarchy$scaleAnimationTick(double tick) {
        if (this.animatable instanceof Entity entity) {
            return tick * TimeDilationApi.getRate(entity);
        }
        return tick;
    }
}
