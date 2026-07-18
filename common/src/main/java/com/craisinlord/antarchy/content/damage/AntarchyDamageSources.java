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
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.KRAKEN_MAULING),
                attacker
        );
    }

    public static DamageSource krakenLightning(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.KRAKEN_LIGHTNING),
                attacker
        );
    }

    public static DamageSource bedBugBite(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.BED_BUG_BITE),
                attacker
        );
    }

    public static DamageSource nightmareMauling(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.NIGHTMARE_MAULING),
                attacker
        );
    }

    public static DamageSource basiliskBite(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.BASILISK_BITE),
                attacker
        );
    }

    public static DamageSource emperorScorpionSting(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.EMPEROR_SCORPION_STING),
                attacker
        );
    }

    public static DamageSource triffidMauling(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.TRIFFID_MAULING),
                attacker
        );
    }

    public static DamageSource triffidSwallow(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.TRIFFID_SWALLOW),
                attacker
        );
    }

    public static DamageSource corneaStalkPrick(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.CORNEA_STALK_PRICK)
        );
    }

    public static DamageSource toreterrorSpin(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.TORETERROR_SPIN),
                attacker
        );
    }

    public static DamageSource toreterrorJump(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.TORETERROR_JUMP),
                attacker
        );
    }

    public static DamageSource rpoLauncherBlast(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.RPO_LAUNCHER_BLAST),
                attacker
        );
    }

    public static DamageSource spitBugDisrespect(ServerLevel level, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.SPIT_BUG_DISRESPECT),
                directEntity,
                causingEntity
        );
    }

    public static DamageSource jumpyBugJump(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.JUMPY_BUG_JUMP),
                attacker
        );
    }

    public static DamageSource herculesBeetleObliteration(ServerLevel level, Entity attacker) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.HERCULES_BEETLE_OBLITERATION),
                attacker
        );
    }

    public static DamageSource waterSoaked(Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(AntarchyDamageTypes.WATER_SOAKED),
                directEntity,
                causingEntity
        );
    }

    private AntarchyDamageSources() {
    }
}
