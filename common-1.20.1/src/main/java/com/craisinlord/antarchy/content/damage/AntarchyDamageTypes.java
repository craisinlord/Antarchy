package com.craisinlord.antarchy.content.damage;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class AntarchyDamageTypes {
    public static final ResourceKey<DamageType> KRAKEN_MAULING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "kraken_mauling")
            );

    public static final ResourceKey<DamageType> KRAKEN_LIGHTNING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "kraken_lightning")
            );

    public static final ResourceKey<DamageType> BED_BUG_BITE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "bed_bug_bite")
            );

    public static final ResourceKey<DamageType> NIGHTMARE_MAULING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "nightmare_mauling")
            );

    public static final ResourceKey<DamageType> BASILISK_BITE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "basilisk_bite")
            );

    public static final ResourceKey<DamageType> EMPEROR_SCORPION_STING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "emperor_scorpion_sting")
            );

    public static final ResourceKey<DamageType> TRIFFID_MAULING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "triffid_mauling")
            );

    public static final ResourceKey<DamageType> TRIFFID_SWALLOW =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "triffid_swallow")
            );

    public static final ResourceKey<DamageType> CORNEA_STALK_PRICK =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "cornea_stalk_prick")
            );

    public static final ResourceKey<DamageType> TORETERROR_SPIN =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "toreterror_spin")
            );

    public static final ResourceKey<DamageType> TORETERROR_JUMP =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "toreterror_jump")
            );

    public static final ResourceKey<DamageType> WATER_SOAKED =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "water_soaked")
            );

    public static final ResourceKey<DamageType> RPO_LAUNCHER_BLAST =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "rpo_launcher_blast")
            );

    public static final ResourceKey<DamageType> SPIT_BUG_DISRESPECT =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "spit_bug_disrespect")
            );

    public static final ResourceKey<DamageType> JUMPY_BUG_JUMP =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "jumpy_bug_jump")
            );

    public static final ResourceKey<DamageType> HERCULES_BEETLE_OBLITERATION =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation(Antarchy.MODID, "hercules_beetle_obliteration")
            );

    private AntarchyDamageTypes() {
    }
}
