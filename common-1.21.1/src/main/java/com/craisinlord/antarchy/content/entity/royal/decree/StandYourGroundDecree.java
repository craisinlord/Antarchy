package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class StandYourGroundDecree implements RoyalDecree {
    private static final double ALLOWED_DRIFT = 6.0D;
    private final Map<UUID, Vec3> anchors = new HashMap<>();
    public String translationKey() { return "decree.antarchy.stand_your_ground"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        Vec3 anchor = this.anchors.computeIfAbsent(target.getUUID(), ignored -> target.position());
        if (target.position().distanceToSqr(anchor) > ALLOWED_DRIFT * ALLOWED_DRIFT) {
            king.invokeJudgment(target);
        }
    }
    @Override
    public void onEnded() {
        this.anchors.clear();
    }
}
