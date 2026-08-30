package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class AdvanceDecree implements RoyalDecree {
    public String name() { return "ADVANCE"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { Vec3 p = king.position().subtract(target.position()).normalize().scale(0.35D); target.setDeltaMovement(target.getDeltaMovement().add(p.x, 0.05D, p.z)); target.hasImpulse = true; }
}
