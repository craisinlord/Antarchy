package com.craisinlord.antarchy.content.entity.royal.beam;

public record RoyalBeamSettings(
        double range,
        double targetTracking,
        double pathStep,
        int durationTicks,
        int travelTicks,
        int cooldownTicks,
        float pathDamageRadius,
        float impactDamageRadius,
        float damage,
        float knockback,
        int terrainIntervalTicks,
        double maxTerrainDistance,
        float pathTerrainRadius,
        float impactTerrainRadius,
        int maxTerrainMutationsPerTick,
        float terrainMutationChance,
        float terrainDropChance,
        float maxTerrainResistance,
        boolean terrainEnabled,
        boolean requireLineOfSightForDamage
) {
    public static RoyalBeamSettings gauntletPower() {
        return new RoyalBeamSettings(64.0D, 0.0D, 2.0D, 18, 4, 50, 0.9F, 1.8F,
                9.0F, 0.8F, 1, 64.0D, 0.8F, 1.8F, 6, 1.0F, 1.0F, 50.0F, true, false);
    }

    public static RoyalBeamSettings gauntletInfinity() {
        return new RoyalBeamSettings(64.0D, 0.0D, 1.5D, 24, 4, 1200, 1.25F, 2.5F,
                28.0F, 1.8F, 1, 64.0D, 1.0F, 2.0F, 0, 0.0F, 0.0F, 0.0F, false, false);
    }

    public static RoyalBeamSettings tremorzillaLike(float damage, boolean terrainEnabled) {
        return new RoyalBeamSettings(
                100.0D,
                0.1D,
                7.5D,
                30,
                12,
                90,
                6.0F,
                6.0F,
                damage,
                1.0F,
                3,
                100.0D,
                5.0F,
                4.0F,
                192,
                1.0F,
                0.08F,
                15.0F,
                terrainEnabled,
                true
        );
    }

    public RoyalBeamSettings {
        range = Math.max(1.0D, range);
        targetTracking = Math.clamp(targetTracking, 0.0D, 1.0D);
        pathStep = Math.max(1.0D, pathStep);
        durationTicks = Math.max(1, durationTicks);
        travelTicks = Math.max(1, travelTicks);
        cooldownTicks = Math.max(1, cooldownTicks);
        pathDamageRadius = Math.max(0.0F, pathDamageRadius);
        impactDamageRadius = Math.max(0.0F, impactDamageRadius);
        damage = Math.max(0.0F, damage);
        knockback = Math.max(0.0F, knockback);
        terrainIntervalTicks = Math.max(1, terrainIntervalTicks);
        maxTerrainDistance = Math.max(0.0D, maxTerrainDistance);
        pathTerrainRadius = Math.max(0.0F, pathTerrainRadius);
        impactTerrainRadius = Math.max(0.0F, impactTerrainRadius);
        maxTerrainMutationsPerTick = Math.max(0, maxTerrainMutationsPerTick);
        terrainMutationChance = Math.clamp(terrainMutationChance, 0.0F, 1.0F);
        terrainDropChance = Math.clamp(terrainDropChance, 0.0F, 1.0F);
        maxTerrainResistance = Math.max(0.0F, maxTerrainResistance);
    }
}
