package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class DreadMobEffect extends MobEffect {
    public DreadMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x1A0A1A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (AntarchySettings.dreadHallucinationMobsEnabled()
                && entity instanceof Player player
                && entity.level() instanceof ServerLevel serverLevel) {
            int minTicks = Math.max(1, (int) Math.round(AntarchySettings.dreadHallucinationMobMinInterval() * 20.0D));
            int maxTicks = Math.max(minTicks, (int) Math.round(AntarchySettings.dreadHallucinationMobMaxInterval() * 20.0D));
            int averageTicks = (minTicks + maxTicks) / 2;
            if (player.getRandom().nextInt(averageTicks) == 0) {
                Vec3 spawnPos = player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
                NightmareBiteEntity.spawnAt(serverLevel, spawnPos, player.getYRot(), false);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
