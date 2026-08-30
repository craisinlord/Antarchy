package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LookControl.class)
public abstract class LookControlTimeDilationMixin {
    @Shadow
    protected Mob mob;

    @ModifyReturnValue(method = "rotateTowards", at = @At("RETURN"))
    private float antarchy$slowLookTurn(float result, float from, float to, float maxAngle) {
        double rate = TimeDilationApi.getRate(this.mob);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return result;
        }
        return from + (float) (Mth.wrapDegrees(result - from) * rate);
    }
}
