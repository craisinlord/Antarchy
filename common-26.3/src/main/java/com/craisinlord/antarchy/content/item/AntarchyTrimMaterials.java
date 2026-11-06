package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class AntarchyTrimMaterials {
    public static final ResourceKey<TrimMaterial> BLOOD_CRYSTAL = create("blood_crystal");
    public static final ResourceKey<TrimMaterial> BLUESTONE = create("bluestone");
    public static final ResourceKey<TrimMaterial> NIGHTMARE_SCALE = create("nightmare_scale");
    public static final ResourceKey<TrimMaterial> TIGERS_EYE = create("tigers_eye");
    public static final ResourceKey<TrimMaterial> TITANIUM = create("titanium");
    public static final ResourceKey<TrimMaterial> URANIUM = create("uranium");

    private AntarchyTrimMaterials() {
    }

    private static ResourceKey<TrimMaterial> create(String path) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
