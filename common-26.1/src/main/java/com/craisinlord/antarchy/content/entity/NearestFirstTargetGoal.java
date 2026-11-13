package com.craisinlord.antarchy.content.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NearestFirstTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public NearestFirstTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach,
                                  TargetingConditions.Selector targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    protected void findTarget() {
        double x = this.mob.getX();
        double y = this.mob.getEyeY();
        double z = this.mob.getZ();
        List<T> candidates = this.mob.level().getEntitiesOfClass(this.targetType,
                this.getTargetSearchArea(this.getFollowDistance()), candidate -> true);
        ServerLevel level = getServerLevel(this.mob);
        candidates.sort(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(x, y, z)));
        this.target = null;
        for (T candidate : candidates) {
            if (this.targetConditions.test(level, this.mob, candidate)) {
                this.target = candidate;
                return;
            }
        }
    }
}
