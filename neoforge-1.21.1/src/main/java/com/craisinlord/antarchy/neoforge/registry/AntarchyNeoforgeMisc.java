package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import com.craisinlord.antarchy.content.effect.DreadMobEffect;
import com.craisinlord.antarchy.content.effect.GoopedMobEffect;
import com.craisinlord.antarchy.content.effect.GrowthMobEffect;
import com.craisinlord.antarchy.content.effect.InvertedMobEffect;
import com.craisinlord.antarchy.content.effect.ParalyzedMobEffect;
import com.craisinlord.antarchy.content.effect.ShrinkMobEffect;
import com.craisinlord.antarchy.content.effect.StinkyMobEffect;
import com.craisinlord.antarchy.content.recipe.AmericanizeRecipe;
import com.craisinlord.antarchy.content.worldgen.ants.BrownAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RainbowAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RedAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.TermiteNestFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileCystFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileVeinFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCreepvineFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynEggPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWallAmberMossFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.HangingCreeprootsFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitinSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitinSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.MoltingVinesFeature;
import com.craisinlord.antarchy.content.worldgen.ocean.SeashellFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.*;
import com.craisinlord.antarchy.neoforge.worldgen.CornPatchFeature;
import com.craisinlord.antarchy.neoforge.worldgen.LumenLilyPadFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.*;
import com.craisinlord.antarchy.neoforge.content.fluid.AntiwaterFluid;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;

public final class AntarchyNeoforgeMisc {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Antarchy.MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Antarchy.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Antarchy.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Antarchy.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Antarchy.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Antarchy.MODID);
    private static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Antarchy.MODID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Antarchy.MODID);

    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant>> GLIMMER_VARIANT =
            DATA_COMPONENT_TYPES.register("glimmer_variant",
                    () -> net.minecraft.core.component.DataComponentType.<com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant>builder()
                            .persistent(com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant.CODEC)
                            .networkSynchronized(com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant.STREAM_CODEC)
                            .build());
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<net.minecraft.util.Unit>> AMERICAN =
            DATA_COMPONENT_TYPES.register("american",
                    () -> net.minecraft.core.component.DataComponentType.<net.minecraft.util.Unit>builder()
                            .persistent(net.minecraft.util.Unit.CODEC)
                            .networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(net.minecraft.util.Unit.INSTANCE))
                            .build());
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<net.minecraft.resources.ResourceLocation>> CRITTER_CAGE_ENTITY_TYPE_COMPONENT =
            DATA_COMPONENT_TYPES.register("critter_cage_entity_type",
                    () -> net.minecraft.core.component.DataComponentType.<net.minecraft.resources.ResourceLocation>builder()
                            .persistent(net.minecraft.resources.ResourceLocation.CODEC)
                            .networkSynchronized(net.minecraft.resources.ResourceLocation.STREAM_CODEC)
                            .build());
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<Integer>> CRITTER_CAGE_PRIMARY_COLOR_COMPONENT =
            DATA_COMPONENT_TYPES.register("critter_cage_primary_color",
                    () -> net.minecraft.core.component.DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build());
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<Integer>> CRITTER_CAGE_SECONDARY_COLOR_COMPONENT =
            DATA_COMPONENT_TYPES.register("critter_cage_secondary_color",
                    () -> net.minecraft.core.component.DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build());
    public static final DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>, net.minecraft.world.item.crafting.RecipeSerializer<AmericanizeRecipe>> AMERICANIZE_SERIALIZER =
            RECIPE_SERIALIZERS.register("americanize", () -> AmericanizeRecipe.SERIALIZER);

    public static final DeferredHolder<Attribute, Attribute> DOUBLE_DAMAGE_CHANCE = ATTRIBUTES.register(
            "double_damage_chance",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.double_damage_chance", 0.0, 0.0, 1.0).setSyncable(true)
    );
    public static final DeferredHolder<Attribute, Attribute> BLOODGLASS_MAX_HEARTS = ATTRIBUTES.register(
            "bloodglass_max_hearts",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.bloodglass_max_hearts", 0.0, 0.0, 8.0).setSyncable(true)
    );
    public static final DeferredHolder<MenuType<?>, MenuType<com.craisinlord.antarchy.content.menu.DorrieInventoryMenu>> DORRIE_INVENTORY_MENU = MENU_TYPES.register(
            "dorrie_inventory",
            () -> new MenuType<>(com.craisinlord.antarchy.content.menu.DorrieInventoryMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    // Mob effects
    public static final DeferredHolder<MobEffect, DreadMobEffect> DREAD = MOB_EFFECTS.register("dread", DreadMobEffect::new);
    public static final DeferredHolder<MobEffect, ParalyzedMobEffect> PARALYZED = MOB_EFFECTS.register("paralyzed", ParalyzedMobEffect::new);
    public static final DeferredHolder<MobEffect, InvertedMobEffect> INVERTED = MOB_EFFECTS.register("inverted", InvertedMobEffect::new);
    public static final DeferredHolder<MobEffect, StinkyMobEffect> STINKY = MOB_EFFECTS.register("stinky", StinkyMobEffect::new);
    public static final DeferredHolder<MobEffect, GoopedMobEffect> GOOPED = MOB_EFFECTS.register("gooped", GoopedMobEffect::new);
    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.BloodglassWardEffect> BLOODGLASS_WARD = MOB_EFFECTS.register("bloodglass_ward", com.craisinlord.antarchy.content.effect.BloodglassWardEffect::new);

    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.GlimmeringMobEffect> GLIMMERING = MOB_EFFECTS.register("glimmering", com.craisinlord.antarchy.content.effect.GlimmeringMobEffect::new);
    public static final DeferredHolder<MobEffect, ShrinkMobEffect> SHRINKING_EFFECT = MOB_EFFECTS.register("shrinking", ShrinkMobEffect::new);
    public static final DeferredHolder<MobEffect, GrowthMobEffect> GROWTH_EFFECT = MOB_EFFECTS.register("growth", GrowthMobEffect::new);

    // Potions
    public static final DeferredHolder<Potion, Potion> INVERSION = POTIONS.register("inversion",
            () -> new Potion(new MobEffectInstance(INVERTED, 600)));
    public static final DeferredHolder<Potion, Potion> LONG_INVERSION = POTIONS.register("long_inversion",
            () -> new Potion("inversion", new MobEffectInstance(INVERTED, 2400)));
    public static final DeferredHolder<Potion, Potion> STINKY_POTION = POTIONS.register("stinky",
            () -> new Potion(new MobEffectInstance(STINKY, 1200)));
    public static final DeferredHolder<Potion, Potion> LONG_STINKY = POTIONS.register("long_stinky",
            () -> new Potion("stinky", new MobEffectInstance(STINKY, 2400)));
    public static final DeferredHolder<Potion, Potion> PARALYSIS = POTIONS.register("paralysis",
            () -> new Potion(new MobEffectInstance(PARALYZED, 200)));
    public static final DeferredHolder<Potion, Potion> LONG_PARALYSIS = POTIONS.register("long_paralysis",
            () -> new Potion("paralysis", new MobEffectInstance(PARALYZED, 400)));
    public static final DeferredHolder<Potion, Potion> HASTE = POTIONS.register("haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1)));
    public static final DeferredHolder<Potion, Potion> SHRINKING = POTIONS.register("shrinking",
            () -> new Potion(new MobEffectInstance(SHRINKING_EFFECT, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_SHRINKING = POTIONS.register("strong_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(SHRINKING_EFFECT, 900, 1)));
    public static final DeferredHolder<Potion, Potion> EXTREME_SHRINKING = POTIONS.register("extreme_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(SHRINKING_EFFECT, 600, 2)));
    public static final DeferredHolder<Potion, Potion> GROWING = POTIONS.register("growing",
            () -> new Potion(new MobEffectInstance(GROWTH_EFFECT, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_GROWING = POTIONS.register("strong_growing",
            () -> new Potion("growing", new MobEffectInstance(GROWTH_EFFECT, 900, 1)));
    public static final DeferredHolder<Potion, Potion> EXTREME_GROWING = POTIONS.register("extreme_growing",
            () -> new Potion("growing", new MobEffectInstance(GROWTH_EFFECT, 600, 2)));

    // Fluids
    public static final DeferredHolder<Fluid, Fluid> BILE = FLUIDS.register("bile",
            () -> new BaseFlowingFluid.Source(bileProperties()));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_BILE = FLUIDS.register("flowing_bile",
            () -> new BaseFlowingFluid.Flowing(bileProperties()));
    public static final DeferredHolder<Fluid, Fluid> ICHOR = FLUIDS.register("ichor",
            () -> new BaseFlowingFluid.Source(ichorProperties()));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_ICHOR = FLUIDS.register("flowing_ichor",
            () -> new BaseFlowingFluid.Flowing(ichorProperties()));
    public static final DeferredHolder<Fluid, Fluid> ANTIWATER = FLUIDS.register("antiwater",
            () -> new AntiwaterFluid.Source(antiwaterProperties()));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_ANTIWATER = FLUIDS.register("flowing_antiwater",
            () -> new AntiwaterFluid.Flowing(antiwaterProperties()));
    public static final DeferredHolder<Fluid, Fluid> LUMEN = FLUIDS.register("lumen",
            () -> new BaseFlowingFluid.Source(lumenProperties()));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_LUMEN = FLUIDS.register("flowing_lumen",
            () -> new BaseFlowingFluid.Flowing(lumenProperties()));

    // Particle types
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DREAM_FIRE_FLAME = PARTICLE_TYPES.register("dream_fire_flame",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STINKY_GAS = PARTICLE_TYPES.register("stinky_gas",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STINKY_FLY = PARTICLE_TYPES.register("stinky_fly",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PEACH_LEAVES_PARTICLE = PARTICLE_TYPES.register("peach_leaves_particle",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LOTUS_POLLEN = PARTICLE_TYPES.register("lotus_pollen",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS = PARTICLE_TYPES.register("hypnotic_gas",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_CLOUD = PARTICLE_TYPES.register("hypnotic_gas_cloud",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_DOWN = PARTICLE_TYPES.register("hypnotic_gas_down",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_CLOUD_DOWN = PARTICLE_TYPES.register("hypnotic_gas_cloud_down",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREFLY = PARTICLE_TYPES.register("firefly",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ORANGE_ASH = PARTICLE_TYPES.register("orange_ash",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUCID_BOLT_IMPACT_SMALL = PARTICLE_TYPES.register("lucid_bolt_impact_small",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUCID_BOLT_IMPACT_LARGE = PARTICLE_TYPES.register("lucid_bolt_impact_large",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_BASE = PARTICLE_TYPES.register("inverted_geyser_base",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_PLUME = PARTICLE_TYPES.register("inverted_geyser_plume",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_POOF = PARTICLE_TYPES.register("inverted_geyser_poof",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_ERUPTION = PARTICLE_TYPES.register("inverted_geyser_eruption",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));

    // Features
    public static final DeferredHolder<Feature<?>, RedAntNestFeature> RED_ANT_NEST_FEATURE = FEATURES.register("red_ant_nest",
            () -> new RedAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BrownAntNestFeature> BROWN_ANT_NEST_FEATURE = FEATURES.register("brown_ant_nest",
            () -> new BrownAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, RainbowAntNestFeature> RAINBOW_ANT_NEST_FEATURE = FEATURES.register("rainbow_ant_nest",
            () -> new RainbowAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, TermiteNestFeature> TERMITE_NEST_FEATURE = FEATURES.register("termite_nest",
            () -> new TermiteNestFeature(SimpleBlockConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, OuranwoodTreeFeature> OURANWOOD_LARGE_TREE = FEATURES.register("ouranwood_large_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, OuranwoodTreeFeature> OURANWOOD_YOUNG_TREE = FEATURES.register("ouranwood_young_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, OuranwoodTreeFeature> OURANWOOD_MEDIUM_TREE = FEATURES.register("ouranwood_medium_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, OuranwoodCocoonTreeFeature> OURANWOOD_COCOON_TREE = FEATURES.register("ouranwood_cocoon_tree",
            () -> new OuranwoodCocoonTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PeachTreeFeature> PEACH_TREE_FEATURE = FEATURES.register("peach_tree",
            () -> new PeachTreeFeature(PeachTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PeachTreeFeature> PEACH_LARGE_TREE_FEATURE = FEATURES.register("peach_large_tree",
            () -> new PeachTreeFeature(PeachTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_FOREST_FLORA = FEATURES.register("elythia_forest_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FOREST));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_MEADOW_FLORA = FEATURES.register("elythia_meadow_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.MEADOW));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> PEACH_FOREST_FLORA = FEATURES.register("peach_forest_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.PEACH_FOREST));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> FLOWER_FOREST_MILKWEED = FEATURES.register("flower_forest_milkweed",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FLOWER_FOREST_MILKWEED));
    public static final DeferredHolder<Feature<?>, CornPatchFeature> CORN_PATCH = FEATURES.register("corn_patch",
            () -> new CornPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature> STAR_CORAL_PATCH = FEATURES.register("star_coral_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, SeashellFeature> SEASHELL_PATCH = FEATURES.register("seashell_patch",
            () -> new SeashellFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_BUTTERFLY_FIELDS_FLORA = FEATURES.register("butterfly_fields_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.BUTTERFLY_FIELDS));
    public static final DeferredHolder<Feature<?>, ElythiaSurfaceCoverFeature> ELYTHIA_SURFACE_COVER = FEATURES.register("elythia_surface_cover",
            () -> new ElythiaSurfaceCoverFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaUndergroundFeature> ELYTHIA_UNDERGROUND = FEATURES.register("elythia_underground",
            () -> new ElythiaUndergroundFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BrutalflyCocoonFeature> BRUTALFLY_COCOON = FEATURES.register("brutalfly_cocoon",
            () -> new BrutalflyCocoonFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MolewormTunnelsFeature> ELYTHIA_MOLEWORM_TUNNELS = FEATURES.register("elythia_moleworm_tunnels",
            () -> new MolewormTunnelsFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MolewormCaveEntranceFeature> ELYTHIA_MOLEWORM_CAVE_ENTRANCES = FEATURES.register("elythia_moleworm_cave_entrances",
            () -> new MolewormCaveEntranceFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MolewormWarrensFeature> ELYTHIA_MOLEWORM_WARRENS = FEATURES.register("elythia_moleworm_warrens",
            () -> new MolewormWarrensFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MolewormSurfaceMoundsFeature> ELYTHIA_MOLEWORM_SURFACE_MOUNDS = FEATURES.register("elythia_moleworm_surface_mounds",
            () -> new MolewormSurfaceMoundsFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, TriffidPatchFeature> TRIFFID_PATCH = FEATURES.register("triffid_patch",
            () -> new TriffidPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaPondFeature> ELYTHIA_POND = FEATURES.register("elythia_pond",
            () -> new ElythiaPondFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LumenPoolFeature> LUMEN_POOL = FEATURES.register("lumen_pools",
            () -> new LumenPoolFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LumenLilyPadFeature> LUMEN_LILY_PADS = FEATURES.register("lumen_lily_pads",
            () -> new LumenLilyPadFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature> GIANT_LILY_PAD_PATCH = FEATURES.register("giant_lily_pad_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature> GIANT_LILY_PAD_PATCH_PEACH_FOREST = FEATURES.register("giant_lily_pad_patch_peach_forest",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature(NoneFeatureConfiguration.CODEC, 0.12F));
    public static final DeferredHolder<Feature<?>, LumenStreamFeature> LUMEN_STREAM = FEATURES.register("lumen_streams",
            () -> new LumenStreamFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LumenSpireFeature> LUMEN_SPIRE = FEATURES.register("lumen_spires",
            () -> new LumenSpireFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaTuffBoulderFeature> ELYTHIA_TUFF_BOULDER = FEATURES.register("elythia_tuff_boulder",
            () -> new ElythiaTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaLargeTuffBoulderFeature> ELYTHIA_LARGE_TUFF_BOULDER = FEATURES.register("elythia_large_tuff_boulder",
            () -> new ElythiaLargeTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PeachForestMossyBoulderFeature> PEACH_FOREST_MOSSY_BOULDER = FEATURES.register("peach_forest_mossy_boulder",
            () -> new PeachForestMossyBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PeachForestPondFeature> PEACH_FOREST_POND = FEATURES.register("peach_forest_pond",
            () -> new PeachForestPondFeature(PeachForestPondConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CoralSpikeFeature> ELYTHIA_CORAL_SPIKE = FEATURES.register("elythia_coral_spike",
            () -> new CoralSpikeFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, FallenOuranwoodFeature> FALLEN_OURANWOOD_TREE = FEATURES.register("fallen_ouranwood_tree",
            () -> new FallenOuranwoodFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, FungalMushroomFeature> FUNGAL_MUSHROOM = FEATURES.register("fungal_mushroom",
            () -> new FungalMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisFissureFeature> THORAXIS_FISSURE = FEATURES.register("thoraxis_fissure",
            () -> new ThoraxisFissureFeature(ThoraxisFissureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisRibColumnsFeature> THORAXIS_RIB_COLUMNS = FEATURES.register("thoraxis_rib_columns",
            () -> new ThoraxisRibColumnsFeature(ThoraxisRibColumnsConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisSpikeFeature> THORAXIS_SPIKE = FEATURES.register("thoraxis_spike",
            () -> new ThoraxisSpikeFeature(ThoraxisSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, NyxiteSpikeFeature> NYXITE_SPIKES = FEATURES.register("nyxite_spikes",
            () -> new NyxiteSpikeFeature(NyxiteSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ChitinSpikeFeature> CAVARYN_CHITIN_SPIKES = FEATURES.register("cavaryn_chitin_spikes",
            () -> new ChitinSpikeFeature(ChitinSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CavarynBileVeinFeature> CAVARYN_BILE_VEINS = FEATURES.register("cavaryn_bile_veins",
            () -> new CavarynBileVeinFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CavarynBileCystFeature> CAVARYN_BILE_CYSTS = FEATURES.register("cavaryn_bile_cysts",
            () -> new CavarynBileCystFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CavarynCreepvineFeature> CAVARYN_CREEPVINE = FEATURES.register("cavaryn_creepvine",
            () -> new CavarynCreepvineFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, HangingCreeprootsFeature> HANGING_CREEPROOTS = FEATURES.register("hanging_creeproots",
            () -> new HangingCreeprootsFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, MoltingVinesFeature> MOLTING_VINES = FEATURES.register("molting_vines",
            () -> new MoltingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature> GROUND_MOLTING_VINES = FEATURES.register("ground_molting_vines",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.cavaryn.JumpyBugEggCeilingFeature> JUMPY_BUG_EGG_CEILING = FEATURES.register("jumpy_bug_egg_ceiling",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.JumpyBugEggCeilingFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature> SPIT_BUG_EGG_PATCH = FEATURES.register("spit_bug_egg_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature> ROLLY_CAVES_GIANT_CAVERN = FEATURES.register("rolly_caves_giant_cavern",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CavarynWallAmberMossFeature> CAVARYN_WALL_AMBER_MOSS = FEATURES.register("cavaryn_wall_amber_moss",
            () -> new CavarynWallAmberMossFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, AntiwaterSpringsFeature> ANTIWATER_SPRINGS = FEATURES.register("antiwater_springs",
            () -> new AntiwaterSpringsFeature(AntiwaterSpringsConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PotentNyxiteFeature> POTENT_NYXITE_FEATURE = FEATURES.register("potent_nyxite",
            () -> new PotentNyxiteFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisBloodCrystalFeature> THORAXIS_BLOOD_CRYSTAL_SPIRES = FEATURES.register("thoraxis_blood_crystal_spires",
            () -> new ThoraxisBloodCrystalFeature(ThoraxisBloodCrystalConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CloudSeaCalciteFeature> CLOUD_SEA_CALCITE_BANKS = FEATURES.register("cloud_sea_calcite_banks",
            () -> new CloudSeaCalciteFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BedBugNestFeature> BED_BUG_NEST = FEATURES.register("bed_bug_nest",
            () -> new BedBugNestFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, BedBugSurfaceClusterFeature> BED_BUG_SURFACE_CLUSTER = FEATURES.register("bed_bug_surface_cluster",
            () -> new BedBugSurfaceClusterFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, CavarynEggPatchFeature> CAVARYN_TERROR_EGG_PATCH = FEATURES.register("cavaryn_terror_egg_patch",
            () -> new CavarynEggPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature> HUGE_GLOWCAP_MUSHROOM = FEATURES.register("huge_glowcap_mushroom",
            () -> new com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature> HUGE_GLOWCAP_MUSHROOM_WORLDGEN = FEATURES.register("huge_glowcap_mushroom_worldgen",
            () -> new com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisAntiwaterPoolFeature> THORAXIS_ANTIWATER_POOL = FEATURES.register("thoraxis_antiwater_pool",
            () -> new ThoraxisAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LucidAntiwaterPoolFeature> LUCID_ANTIWATER_POOL = FEATURES.register("lucid_antiwater_pool",
            () -> new LucidAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisDuneFeature> THORAXIS_DUNE = FEATURES.register("thoraxis_dune",
            () -> new ThoraxisDuneFeature(ThoraxisDuneConfiguration.CODEC));

    // Biome sources
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ElythiaBiomeSource>> ELYTHIA_BIOME_SOURCE = BIOME_SOURCES.register("elythia_biome_source",
            () -> ElythiaBiomeSource.CODEC);
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ThoraxisBiomeSource>> THORAXIS_BIOME_SOURCE = BIOME_SOURCES.register("thoraxis_biome_source",
            () -> ThoraxisBiomeSource.CODEC);

    // Density function
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverCarveFunction>> ELYTHIA_RIVER_CARVE = DENSITY_FUNCTION_TYPES.register("elythia_river_carve",
            () -> ElythiaRiverCarveFunction.CODEC);

    private AntarchyNeoforgeMisc() {}

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        FEATURES.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        DENSITY_FUNCTION_TYPES.register(modEventBus);
        ENTITY_SUB_PREDICATES.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }


    static BaseFlowingFluid.Properties ichorProperties() {
        return new BaseFlowingFluid.Properties(com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes.ICHOR_TYPE, ICHOR, FLOWING_ICHOR)
                .bucket(() -> AntarchyNeoforgeItems.ICHOR_BUCKET.get())
                .block(() -> AntarchyNeoforgeBlocks.ICHOR_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static BaseFlowingFluid.Properties bileProperties() {
        return new BaseFlowingFluid.Properties(com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes.BILE_TYPE, BILE, FLOWING_BILE)
                .bucket(() -> AntarchyNeoforgeItems.BILE_BUCKET.get())
                .block(() -> AntarchyNeoforgeBlocks.BILE_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static BaseFlowingFluid.Properties antiwaterProperties() {
        return new BaseFlowingFluid.Properties(com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes.ANTIWATER_TYPE, ANTIWATER, FLOWING_ANTIWATER)
                .bucket(() -> AntarchyNeoforgeItems.ANTIWATER_BUCKET.get())
                .block(() -> AntarchyNeoforgeBlocks.ANTIWATER_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static BaseFlowingFluid.Properties lumenProperties() {
        return new BaseFlowingFluid.Properties(com.craisinlord.antarchy.neoforge.AntarchyNeoForgeFluidTypes.LUMEN_TYPE, LUMEN, FLOWING_LUMEN)
                .bucket(() -> AntarchyNeoforgeItems.LUMEN_BUCKET.get())
                .block(() -> AntarchyNeoforgeBlocks.LUMEN_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    private static <T extends net.minecraft.core.particles.ParticleOptions> ParticleType<T> particleType(
            Function<ParticleType<T>, com.mojang.serialization.MapCodec<T>> codecFactory,
            Function<ParticleType<T>, StreamCodec<? super io.netty.buffer.ByteBuf, T>> streamCodecFactory
    ) {
        return new ParticleType<T>(false) {
            @Override
            public com.mojang.serialization.MapCodec<T> codec() {
                return codecFactory.apply(this);
            }

            @Override
            public StreamCodec<? super io.netty.buffer.ByteBuf, T> streamCodec() {
                return streamCodecFactory.apply(this);
            }
        };
    }
}
