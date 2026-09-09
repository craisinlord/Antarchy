package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class RoyalHead {
    public enum Slot {
        LEFT(1, "head_left", "bite_3", "shoot_3"),
        CENTER(2, "head_center", "bite_1", "shoot_1"),
        RIGHT(3, "head_right", "bite_2", "shoot_2");

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
    private boolean shooting;
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
        return this.shooting || this.beamActive;
    }

    public boolean readyToAttack() {
        return !this.busy();
    }

    public void startShoot() {
        this.shooting = true;
    }

    public void stopShoot() {
        this.shooting = false;
    }

    public void setBeamActive(boolean beamActive) {
        this.beamActive = beamActive;
    }

    public boolean beamActive() {
        return this.beamActive;
    }

    public boolean shooting() {
        return this.shooting;
    }

    public void reset() {
        this.targetId = -1;
        this.shooting = false;
        this.beamActive = false;
    }
}
