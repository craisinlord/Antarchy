package com.craisinlord.antarchy.content.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class AntarchyDamageSources {
    public static DamageSource krakenMauling(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.KRAKEN_MAULING),
                attacker
        );
    }

    public static DamageSource krakenLightning(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.KRAKEN_LIGHTNING),
                attacker
        );
    }

    public static DamageSource bedBugBite(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.BED_BUG_BITE),
                attacker
        );
    }

    public static DamageSource nightmareMauling(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.NIGHTMARE_MAULING),
                attacker
        );
    }

    public static DamageSource basiliskBite(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.BASILISK_BITE),
                attacker
        );
    }

    public static DamageSource emperorScorpionSting(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.EMPEROR_SCORPION_STING),
                attacker
        );
    }

    public static DamageSource flytrapMauling(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.FLYTRAP_MAULING),
                attacker
        );
    }

    public static DamageSource flytrapSwallow(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.FLYTRAP_SWALLOW),
                attacker
        );
    }

    public static DamageSource corneaStalkPrick(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.CORNEA_STALK_PRICK)
        );
    }

    public static DamageSource toreterrorSpin(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.TORETERROR_SPIN),
                attacker
        );
    }

    public static DamageSource toreterrorJump(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.TORETERROR_JUMP),
                attacker
        );
    }

    public static DamageSource rpoLauncherBlast(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.RPO_LAUNCHER_BLAST),
                attacker
        );
    }

    public static DamageSource spitBugDisrespect(ServerLevel level, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.SPIT_BUG_DISRESPECT),
                directEntity,
                causingEntity
        );
    }

    public static DamageSource springbugJump(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.SPRINGBUG_JUMP),
                attacker
        );
    }

    public static DamageSource herculesBeetleObliteration(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.HERCULES_BEETLE_OBLITERATION),
                attacker
        );
    }

    public static DamageSource waterSoaked(Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.WATER_SOAKED),
                directEntity,
                causingEntity
        );
    }

    public static DamageSource moonVacuum(ServerLevel level, @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(AntarchyDamageTypes.MOON_VACUUM),
                null,
                causingEntity
        );
    }

    private AntarchyDamageSources() {
    }
}
