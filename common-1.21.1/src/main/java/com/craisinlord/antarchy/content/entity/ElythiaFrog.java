package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class ElythiaFrog {
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final ResourceKey<FrogVariant> VARIANT = ResourceKey.create(
            net.minecraft.core.registries.Registries.FROG_VARIANT,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/frog/frog_elythia.png");

    private ElythiaFrog() {
    }

    public static boolean shouldBecomeElythiaFrog(ServerLevelAccessor level) {
        return level.getLevel().dimension().equals(ELYTHIA);
    }

    public static void applyVariant(Frog frog) {
        BuiltInRegistries.FROG_VARIANT.getHolder(VARIANT).ifPresent(frog::setVariant);
    }

    public static boolean isElythiaFrog(Frog frog) {
        return frog.getVariant().unwrapKey().filter(VARIANT::equals).isPresent();
    }
}
