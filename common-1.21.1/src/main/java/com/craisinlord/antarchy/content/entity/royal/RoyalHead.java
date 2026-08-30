package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class RoyalHead {
    public enum Slot {
        LEFT(1, "head_left", "bite_1", "shoot_1"),
        CENTER(2, "head_center", "bite_2", "shoot_2"),
        RIGHT(3, "head_right", "bite_3", "shoot_3");

        private final int partIndex;
        private final String controllerName;
        private final String biteAnimation;
        private final String shootAnimation;

        Slot(int partIndex, String controllerName, String biteAnimation, String shootAnimation) {
            this.partIndex = partIndex;
            this.controllerName = controllerName;
            this.biteAnimation = biteAnimation;
            this.shootAnimation = shootAnimation;
        }

        public int partIndex() {
            return this.partIndex;
        }

        public String controllerName() {
            return this.controllerName;
        }

        public String biteAnimation() {
            return this.biteAnimation;
        }

        public String shootAnimation() {
            return this.shootAnimation;
        }
    }

    private final Slot slot;
    private int targetId = -1;
    private int attackCooldown;
    private int biteTicks;
    private int biteHitTick = -1;
    private int shootTicks;
    private boolean beamActive;

    public RoyalHead(Slot slot) {
        this.slot = slot;
    }

    public Slot slot() {
        return this.slot;
    }

    @Nullable
    public LivingEntity target(Level level) {
        if (this.targetId < 0) {
            return null;
        }
        return level.getEntity(this.targetId) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.targetId = target == null ? -1 : target.getId();
    }

    public boolean busy() {
        return this.biteTicks > 0 || this.shootTicks > 0 || this.beamActive;
    }

    public boolean readyToAttack() {
        return !this.busy() && this.attackCooldown <= 0;
    }

    public void startBite(int durationTicks, int hitTick, int cooldownTicks) {
        this.biteTicks = durationTicks;
        this.biteHitTick = hitTick;
        this.attackCooldown = cooldownTicks;
    }

    public void startShoot(int durationTicks, int cooldownTicks) {
        this.shootTicks = durationTicks;
        this.attackCooldown = cooldownTicks;
    }

    public void setBeamActive(boolean beamActive) {
        this.beamActive = beamActive;
    }

    public boolean beamActive() {
        return this.beamActive;
    }

    public boolean shooting() {
        return this.shootTicks > 0;
    }

    public boolean tickBiteHit() {
        if (this.biteTicks <= 0) {
            return false;
        }
        this.biteTicks--;
        boolean hit = this.biteTicks == this.biteHitTick;
        if (hit) {
            this.biteHitTick = -1;
        }
        return hit;
    }

    public void tick() {
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.shootTicks > 0) {
            this.shootTicks--;
        }
    }

    public void reset() {
        this.targetId = -1;
        this.attackCooldown = 0;
        this.biteTicks = 0;
        this.biteHitTick = -1;
        this.shootTicks = 0;
        this.beamActive = false;
    }
}
