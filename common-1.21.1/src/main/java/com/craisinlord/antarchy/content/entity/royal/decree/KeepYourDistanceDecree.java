package com.craisinlord.antarchy.content.entity.royal.decree;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class KeepYourDistanceDecree implements RoyalDecree {
    public String name() { return "KEEP YOUR DISTANCE"; }
    public void apply(ServerLevel level, KingEntity king, LivingEntity target) { if (target.distanceTo(king) < 18.0D) { Vec3 p = target.position().subtract(king.position()).normalize().scale(0.8D); target.setDeltaMovement(target.getDeltaMovement().add(p.x, 0.2D, p.z)); target.hasImpulse = true; } }
}
