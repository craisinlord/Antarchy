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
    private double antarchy$inheritedTimeDilationRate = TimeDilationMath.NORMAL_RATE;
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
        double previousRate = this.antarchy$timeDilationRate;
        this.antarchy$timeDilationRate = TimeDilationMath.clampRate(rate);
        if (previousRate != TimeDilationMath.NORMAL_RATE
                && this.antarchy$timeDilationRate == TimeDilationMath.NORMAL_RATE) {
            this.antarchy$timeDilationTimerProgress.clear();
        }
    }

    @Override
    public double antarchy$getInheritedTimeDilationRate() {
        return this.antarchy$inheritedTimeDilationRate;
    }

    @Override
    public void antarchy$setInheritedTimeDilationRate(double rate) {
        this.antarchy$inheritedTimeDilationRate = TimeDilationMath.clampRate(rate);
    }

    @Override
    public boolean antarchy$consumeTimeDilationTick(String timerKey, double rate) {
        return this.antarchy$consumeTimeDilationTicks(timerKey, rate) > 0;
    }

    @Override
    public int antarchy$consumeTimeDilationTicks(String timerKey, double rate) {
        double clampedRate = TimeDilationMath.clampRate(rate);
        if (Math.abs(clampedRate - TimeDilationMath.NORMAL_RATE) < 0.001D) {
            return 1;
        }

        double progress = this.antarchy$timeDilationTimerProgress.getOrDefault(timerKey, 0.0D) + clampedRate;
        int wholeTicks = (int) Math.floor(progress + 1.0E-9D);
        if (wholeTicks <= 0) {
            this.antarchy$timeDilationTimerProgress.put(timerKey, progress);
            return 0;
        }

        this.antarchy$timeDilationTimerProgress.put(timerKey, progress - wholeTicks);
        return wholeTicks;
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
