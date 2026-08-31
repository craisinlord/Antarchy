package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationEntityAccess;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityTimeDilationStateMixin implements TimeDilationEntityAccess {
    @Unique
    private double antarchy$timeDilationRate = TimeDilationMath.NORMAL_RATE;
    @Unique
    private final Map<String, Double> antarchy$timeDilationTimerProgress = new HashMap<>();
    @Unique
    private boolean antarchy$inTimeDilationMove;
    @Unique
    private boolean antarchy$applyingExternalImpulse;

    @Override
    public double antarchy$getTimeDilationRate() {
        return this.antarchy$timeDilationRate;
    }

    @Override
    public void antarchy$setTimeDilationRate(double rate) {
        this.antarchy$timeDilationRate = TimeDilationMath.clampRate(rate);
        if (this.antarchy$timeDilationRate >= TimeDilationMath.NORMAL_RATE) {
            this.antarchy$timeDilationTimerProgress.clear();
        }
    }

    @Override
    public boolean antarchy$consumeTimeDilationTick(String timerKey, double rate) {
        double clampedRate = TimeDilationMath.clampRate(rate);
        if (clampedRate >= TimeDilationMath.NORMAL_RATE) {
            return true;
        }

        double progress = this.antarchy$timeDilationTimerProgress.getOrDefault(timerKey, 0.0D) + clampedRate;
        int wholeTicks = (int) Math.floor(progress + 1.0E-9D);
        if (wholeTicks <= 0) {
            this.antarchy$timeDilationTimerProgress.put(timerKey, progress);
            return false;
        }

        this.antarchy$timeDilationTimerProgress.put(timerKey, progress - wholeTicks);
        return true;
    }

    @Override
    public boolean antarchy$isInTimeDilationMove() {
        return this.antarchy$inTimeDilationMove;
    }

    @Override
    public void antarchy$setInTimeDilationMove(boolean inMove) {
        this.antarchy$inTimeDilationMove = inMove;
    }

    @Override
    public boolean antarchy$isApplyingExternalImpulse() {
        return this.antarchy$applyingExternalImpulse;
    }

    @Override
    public void antarchy$setApplyingExternalImpulse(boolean applying) {
        this.antarchy$applyingExternalImpulse = applying;
    }
}
