package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class FightMeCowardDecree implements RoyalDecree {
    public String name() { return "FIGHT ME, COWARD!"; }
    public String translationKey() { return "decree.antarchy.fight_me_coward"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) {
        if (target.distanceTo(king) > 28.0D) {
            Vec3 pull = king.position().subtract(target.position()).normalize().scale(0.35D);
            target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.05D, pull.z));
            target.hasImpulse = true;
        }
    }
}
