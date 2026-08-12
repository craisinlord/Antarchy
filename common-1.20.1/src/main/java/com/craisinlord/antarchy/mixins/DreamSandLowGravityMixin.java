package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
/*
 * Stores per-entity dream sand low-grav state.
 */
public abstract class DreamSandLowGravityMixin implements DreamSandLowGravityAccess {
    @Unique
    private boolean antarchy$dreamSandLowGravityActive;
    @Unique
    private int antarchy$dreamSandLowGravityTicksRemaining;
    @Unique
    private int antarchy$dreamSandLandingGraceTicks;

    @Override
    public boolean antarchy$isDreamSandLowGravityActive() {
        return this.antarchy$dreamSandLowGravityActive;
    }

    @Override
    public void antarchy$setDreamSandLowGravityActive(boolean active) {
        this.antarchy$dreamSandLowGravityActive = active;
    }

    @Override
    public int antarchy$getDreamSandLowGravityTicksRemaining() {
        return this.antarchy$dreamSandLowGravityTicksRemaining;
    }

    @Override
    public void antarchy$setDreamSandLowGravityTicksRemaining(int ticks) {
        this.antarchy$dreamSandLowGravityTicksRemaining = Math.max(0, ticks);
    }

    @Override
    public int antarchy$getDreamSandLandingGraceTicks() {
        return this.antarchy$dreamSandLandingGraceTicks;
    }

    @Override
    public void antarchy$setDreamSandLandingGraceTicks(int ticks) {
        this.antarchy$dreamSandLandingGraceTicks = Math.max(0, ticks);
    }
}
