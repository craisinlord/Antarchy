package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class RoyalMountSpitGoal extends Goal {
    private final RoyalMountEntity mount;
    private int cooldown;

    public RoyalMountSpitGoal(RoyalMountEntity mount) {
        this.mount = mount;
    }

    @Override
    public boolean canUse() {
        if (this.mount.isBaby() || this.mount.isVehicle() || this.mount.isOrderedToSit()) {
            return false;
        }
        LivingEntity target = this.mount.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distance = this.mount.distanceTo(target);
        return distance > 5.0D && distance < 20.0D && this.mount.hasLineOfSight(target);
    }

    @Override
    public void start() {
        this.cooldown = 20;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mount.getTarget();
        if (target == null) {
            return;
        }
        this.mount.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.cooldown-- <= 0) {
            this.mount.startSpitVolley(target);
            this.cooldown = 60;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
