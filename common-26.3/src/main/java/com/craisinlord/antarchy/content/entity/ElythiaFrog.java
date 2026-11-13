package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class ElythiaFrog {
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final ResourceKey<FrogVariant> VARIANT = ResourceKey.create(
            Registries.FROG_VARIANT,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/frog/frog_elythia.png");

    private ElythiaFrog() {
    }

    public static boolean shouldBecomeElythiaFrog(ServerLevelAccessor level) {
        return level.getLevel().dimension().equals(ELYTHIA);
    }

    public static void applyVariant(Frog frog) {
        frog.registryAccess().lookupOrThrow(Registries.FROG_VARIANT).get(VARIANT)
                .ifPresent(variant -> frog.setComponent(DataComponents.FROG_VARIANT, variant));
    }

    public static boolean isElythiaFrog(Frog frog) {
        return frog.getVariant().unwrapKey().filter(VARIANT::equals).isPresent();
    }
}
