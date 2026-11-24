package com.craisinlord.antarchy.content.entity.royal.decree;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class StandYourGroundDecree implements RoyalDecree {
    private final Map<UUID, Vec3> anchors = new HashMap<>();

    public String translationKey() {
        return "decree.antarchy.stand_your_ground";
    }

    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        this.anchors.computeIfAbsent(target.getUUID(), ignored -> target.position());
    }

    @Override
    public Evaluation evaluate(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target instanceof ServerPlayer player && player.isFallFlying()) {
            this.anchors.put(target.getUUID(), target.position());
            return Evaluation.COMPLIANT;
        }
        Vec3 anchor = this.anchors.computeIfAbsent(target.getUUID(), ignored -> target.position());
        double drift = Math.max(2.0D, AntarchySettings.royalStandYourGroundDrift());
        return target.position().distanceToSqr(anchor) > drift * drift
                ? Evaluation.VIOLATED : Evaluation.COMPLIANT;
    }

    @Override
    public int durationTicks() {
        return 200;
    }

    @Override
    public boolean suppressesAttack(String attackId) {
        return "stomp".equals(attackId) || "wing_gust".equals(attackId) || "ice_spikes".equals(attackId);
    }

    @Override
    public void onKingDisplaced(LivingEntity target) {
        this.anchors.put(target.getUUID(), target.position());
    }

    @Override
    public void onEnded() {
        this.anchors.clear();
    }
}
