package com.craisinlord.antarchy.content.glimmerecho;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public record GlimmerSigilTarget(
        String bossKey,
        Supplier<? extends EntityType<? extends Mob>> entityType,
        GlimmerSite site,
        ResourceLocation defeatAdvancement,
        SpawnMode spawnMode) {

    public enum SpawnMode {
        GROUND,
        AIR
    }

    public static final GlimmerSigilTarget TORETERROR = new GlimmerSigilTarget(
            "toreterror",
            () -> AntarchyObjects.TORETERROR.get(),
            biomeSite("toreterror"),
            id("kill_toreterror"),
            SpawnMode.GROUND);

    public static final GlimmerSigilTarget ALPHA_MANTIS = new GlimmerSigilTarget(
            "alpha_mantis",
            () -> registeredMob("alpha_mantis"),
            biomeSite("alpha_mantis"),
            id("bugged_out"),
            SpawnMode.GROUND);

    public static final GlimmerSigilTarget EMPEROR_SCORPION = new GlimmerSigilTarget(
            "emperor_scorpion",
            () -> registeredMob("emperor_scorpion"),
            GlimmerSite.thoraxisSurface(),
            id("sting_operation"),
            SpawnMode.GROUND);

    public static final GlimmerSigilTarget BRUTALFLY = new GlimmerSigilTarget(
            "brutalfly",
            () -> AntarchyObjects.BRUTALFLY.get(),
            biomeSite("brutalfly"),
            id("kill_brutalfly"),
            SpawnMode.AIR);

    public static final GlimmerSigilTarget HERCULES_BEETLE = new GlimmerSigilTarget(
            "hercules_beetle",
            () -> AntarchyObjects.HERCULES_BEETLE.get(),
            biomeSite("hercules_beetle"),
            id("herculean_task"),
            SpawnMode.GROUND);

    public static final GlimmerSigilTarget KRAKEN = new GlimmerSigilTarget(
            "kraken",
            () -> AntarchyObjects.KRAKEN.get(),
            biomeSite("kraken"),
            id("davy_jones_locker"),
            SpawnMode.AIR);

    public static final GlimmerSigilTarget KING = new GlimmerSigilTarget(
            "king",
            () -> AntarchyObjects.KING.get(),
            GlimmerSite.anyOf(GlimmerSite.kingsTree(), biomeSite("king")),
            id("down_with_the_king"),
            SpawnMode.AIR);

    public static final GlimmerSigilTarget QUEEN = new GlimmerSigilTarget(
            "queen",
            () -> AntarchyObjects.QUEEN.get(),
            GlimmerSite.anyOf(structureSite("queen"), GlimmerSite.underside(), GlimmerSite.thoraxisSurface()),
            id("down_with_the_queen"),
            SpawnMode.AIR);

    private static final List<GlimmerSigilTarget> ALL = List.of(
            TORETERROR, ALPHA_MANTIS, EMPEROR_SCORPION, BRUTALFLY, HERCULES_BEETLE, KRAKEN, KING, QUEEN);

    public static boolean isSigilBoss(EntityType<?> type) {
        for (GlimmerSigilTarget target : ALL) {
            if (target.entityType().get() == type) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> registeredMob(String path) {
        return (EntityType<? extends Mob>) BuiltInRegistries.ENTITY_TYPE.get(id(path));
    }

    private static GlimmerSite structureSite(String bossKey) {
        return GlimmerSite.structure(TagKey.create(Registries.STRUCTURE, id("glimmer_sigil_sites/" + bossKey)));
    }

    private static GlimmerSite biomeSite(String bossKey) {
        return GlimmerSite.biome(TagKey.create(Registries.BIOME, id("glimmer_sigil_sites/" + bossKey)));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }
}
