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
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_mauling")
            );

    public static final ResourceKey<DamageType> KRAKEN_LIGHTNING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_lightning")
            );

    public static final ResourceKey<DamageType> BED_BUG_BITE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bed_bug_bite")
            );

    public static final ResourceKey<DamageType> NIGHTMARE_MAULING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_mauling")
            );

    public static final ResourceKey<DamageType> BASILISK_BITE =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "basilisk_bite")
            );

    public static final ResourceKey<DamageType> EMPEROR_SCORPION_STING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "emperor_scorpion_sting")
            );

    public static final ResourceKey<DamageType> TRIFFID_MAULING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "triffid_mauling")
            );

    public static final ResourceKey<DamageType> TRIFFID_SWALLOW =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "triffid_swallow")
            );

    public static final ResourceKey<DamageType> CORNEA_STALK_PRICK =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cornea_stalk_prick")
            );

    private AntarchyDamageTypes() {
    }
}
