package com.craisinlord.antarchy.forge.registry;

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
import com.craisinlord.antarchy.forge.worldgen.CornPatchFeature;
import com.craisinlord.antarchy.forge.worldgen.LumenLilyPadFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.*;
import com.craisinlord.antarchy.forge.content.fluid.AntiwaterFluid;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

public final class AntarchyForgeMisc {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Antarchy.MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Antarchy.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Antarchy.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Antarchy.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Antarchy.MODID);
    private static final DeferredRegister<com.mojang.serialization.Codec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, Antarchy.MODID);
    private static final DeferredRegister<com.mojang.serialization.Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Antarchy.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Antarchy.MODID);
    private static final DeferredRegister<net.minecraft.world.entity.animal.FrogVariant> FROG_VARIANTS =
            DeferredRegister.create(Registries.FROG_VARIANT, Antarchy.MODID);
    public static final RegistryObject<net.minecraft.world.entity.animal.FrogVariant> ELYTHIA_FROG_VARIANT = FROG_VARIANTS.register(
            "elythia",
            () -> new net.minecraft.world.entity.animal.FrogVariant(com.craisinlord.antarchy.content.entity.ElythiaFrog.TEXTURE)
    );
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Antarchy.MODID);

    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<AmericanizeRecipe>> AMERICANIZE_SERIALIZER =
            RECIPE_SERIALIZERS.register("americanize", () -> AmericanizeRecipe.SERIALIZER);

    public static final RegistryObject<Attribute> SCALE = ATTRIBUTES.register(
            "scale",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.scale", 1.0, 0.0625, 16.0).setSyncable(true)
    );
    public static final RegistryObject<Attribute> DOUBLE_DAMAGE_CHANCE = ATTRIBUTES.register(
            "double_damage_chance",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.double_damage_chance", 0.0, 0.0, 1.0).setSyncable(true)
    );
    public static final RegistryObject<Attribute> BLOODGLASS_MAX_HEARTS = ATTRIBUTES.register(
            "bloodglass_max_hearts",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.bloodglass_max_hearts", 0.0, 0.0, 8.0).setSyncable(true)
    );
    public static final RegistryObject<MenuType<com.craisinlord.antarchy.content.menu.DorrieInventoryMenu>> DORRIE_INVENTORY_MENU = MENU_TYPES.register(
            "dorrie_inventory",
            () -> new MenuType<>(com.craisinlord.antarchy.content.menu.DorrieInventoryMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    // Mob effects
    public static final RegistryObject<DreadMobEffect> DREAD = MOB_EFFECTS.register("dread", DreadMobEffect::new);
    public static final RegistryObject<ParalyzedMobEffect> PARALYZED = MOB_EFFECTS.register("paralyzed", ParalyzedMobEffect::new);
    public static final RegistryObject<InvertedMobEffect> INVERTED = MOB_EFFECTS.register("inverted", InvertedMobEffect::new);
    public static final RegistryObject<StinkyMobEffect> STINKY = MOB_EFFECTS.register("stinky", StinkyMobEffect::new);
    public static final RegistryObject<GoopedMobEffect> GOOPED = MOB_EFFECTS.register("gooped", GoopedMobEffect::new);
    public static final RegistryObject<com.craisinlord.antarchy.content.effect.BloodglassWardEffect> BLOODGLASS_WARD = MOB_EFFECTS.register("bloodglass_ward", com.craisinlord.antarchy.content.effect.BloodglassWardEffect::new);
    public static final RegistryObject<ShrinkMobEffect> SHRINKING_EFFECT = MOB_EFFECTS.register("shrinking", ShrinkMobEffect::new);
    public static final RegistryObject<GrowthMobEffect> GROWTH_EFFECT = MOB_EFFECTS.register("growth", GrowthMobEffect::new);
    public static final RegistryObject<com.craisinlord.antarchy.content.effect.GlimmeringMobEffect> GLIMMERING = MOB_EFFECTS.register("glimmering", com.craisinlord.antarchy.content.effect.GlimmeringMobEffect::new);

    // Potions
    public static final RegistryObject<Potion> INVERSION = POTIONS.register("inversion",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(INVERTED), 600)));
    public static final RegistryObject<Potion> LONG_INVERSION = POTIONS.register("long_inversion",
            () -> new Potion("inversion", new MobEffectInstance(mobEffectHolder(INVERTED), 2400)));
    public static final RegistryObject<Potion> STINKY_POTION = POTIONS.register("stinky",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(STINKY), 1200)));
    public static final RegistryObject<Potion> LONG_STINKY = POTIONS.register("long_stinky",
            () -> new Potion("stinky", new MobEffectInstance(mobEffectHolder(STINKY), 2400)));
    public static final RegistryObject<Potion> PARALYSIS = POTIONS.register("paralysis",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(PARALYZED), 200)));
    public static final RegistryObject<Potion> LONG_PARALYSIS = POTIONS.register("long_paralysis",
            () -> new Potion("paralysis", new MobEffectInstance(mobEffectHolder(PARALYZED), 400)));
    public static final RegistryObject<Potion> HASTE = POTIONS.register("haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 0)));
    public static final RegistryObject<Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1)));
    public static final RegistryObject<Potion> SHRINKING = POTIONS.register("shrinking",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 1200, 0)));
    public static final RegistryObject<Potion> STRONG_SHRINKING = POTIONS.register("strong_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 900, 1)));
    public static final RegistryObject<Potion> EXTREME_SHRINKING = POTIONS.register("extreme_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 600, 2)));
    public static final RegistryObject<Potion> GROWING = POTIONS.register("growing",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 1200, 0)));
    public static final RegistryObject<Potion> STRONG_GROWING = POTIONS.register("strong_growing",
            () -> new Potion("growing", new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 900, 1)));
    public static final RegistryObject<Potion> EXTREME_GROWING = POTIONS.register("extreme_growing",
            () -> new Potion("growing", new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 600, 2)));

    // Fluids
    public static final RegistryObject<Fluid> BILE = FLUIDS.register("bile",
            () -> new ForgeFlowingFluid.Source(bileProperties()));
    public static final RegistryObject<Fluid> FLOWING_BILE = FLUIDS.register("flowing_bile",
            () -> new ForgeFlowingFluid.Flowing(bileProperties()));
    public static final RegistryObject<Fluid> ICHOR = FLUIDS.register("ichor",
            () -> new ForgeFlowingFluid.Source(ichorProperties()));
    public static final RegistryObject<Fluid> FLOWING_ICHOR = FLUIDS.register("flowing_ichor",
            () -> new ForgeFlowingFluid.Flowing(ichorProperties()));
    public static final RegistryObject<Fluid> ANTIWATER = FLUIDS.register("antiwater",
            () -> new AntiwaterFluid.Source(antiwaterProperties()));
    public static final RegistryObject<Fluid> FLOWING_ANTIWATER = FLUIDS.register("flowing_antiwater",
            () -> new AntiwaterFluid.Flowing(antiwaterProperties()));
    public static final RegistryObject<Fluid> LUMEN = FLUIDS.register("lumen",
            () -> new ForgeFlowingFluid.Source(lumenProperties()));
    public static final RegistryObject<Fluid> FLOWING_LUMEN = FLUIDS.register("flowing_lumen",
            () -> new ForgeFlowingFluid.Flowing(lumenProperties()));

    // Particle types
    public static final RegistryObject<SimpleParticleType> DREAM_FIRE_FLAME = PARTICLE_TYPES.register("dream_fire_flame",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> NIGHTMARE_FIRE_FLAME = PARTICLE_TYPES.register("nightmare_fire_flame",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> STINKY_GAS = PARTICLE_TYPES.register("stinky_gas",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> STINKY_FLY = PARTICLE_TYPES.register("stinky_fly",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> PEACH_LEAVES_PARTICLE = PARTICLE_TYPES.register("peach_leaves_particle",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> LOTUS_POLLEN = PARTICLE_TYPES.register("lotus_pollen",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> HYPNOTIC_GAS = PARTICLE_TYPES.register("hypnotic_gas",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> HYPNOTIC_GAS_CLOUD = PARTICLE_TYPES.register("hypnotic_gas_cloud",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> HYPNOTIC_GAS_DOWN = PARTICLE_TYPES.register("hypnotic_gas_down",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> HYPNOTIC_GAS_CLOUD_DOWN = PARTICLE_TYPES.register("hypnotic_gas_cloud_down",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> FIREFLY = PARTICLE_TYPES.register("firefly",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> ORANGE_ASH = PARTICLE_TYPES.register("orange_ash",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> LUCID_BOLT_IMPACT_SMALL = PARTICLE_TYPES.register("lucid_bolt_impact_small",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<SimpleParticleType> LUCID_BOLT_IMPACT_LARGE = PARTICLE_TYPES.register("lucid_bolt_impact_large",
            AntarchyForgeMisc::simpleParticleType);
    public static final RegistryObject<ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_BASE = PARTICLE_TYPES.register("inverted_geyser_base",
            () -> particleType(InvertedGeyserBaseParticleOptions.deserializer()));
    public static final RegistryObject<ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_PLUME = PARTICLE_TYPES.register("inverted_geyser_plume",
            () -> particleType(InvertedGeyserParticleOptions.deserializer()));
    public static final RegistryObject<ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_POOF = PARTICLE_TYPES.register("inverted_geyser_poof",
            () -> particleType(InvertedGeyserBaseParticleOptions.deserializer()));
    public static final RegistryObject<ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_ERUPTION = PARTICLE_TYPES.register("inverted_geyser_eruption",
            () -> particleType(InvertedGeyserParticleOptions.deserializer()));

    // Features
    public static final RegistryObject<RedAntNestFeature> RED_ANT_NEST_FEATURE = FEATURES.register("red_ant_nest",
            () -> new RedAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryObject<BrownAntNestFeature> BROWN_ANT_NEST_FEATURE = FEATURES.register("brown_ant_nest",
            () -> new BrownAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryObject<RainbowAntNestFeature> RAINBOW_ANT_NEST_FEATURE = FEATURES.register("rainbow_ant_nest",
            () -> new RainbowAntNestFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryObject<TermiteNestFeature> TERMITE_NEST_FEATURE = FEATURES.register("termite_nest",
            () -> new TermiteNestFeature(SimpleBlockConfiguration.CODEC));
    public static final RegistryObject<OuranwoodTreeFeature> OURANWOOD_LARGE_TREE = FEATURES.register("ouranwood_large_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final RegistryObject<OuranwoodTreeFeature> OURANWOOD_YOUNG_TREE = FEATURES.register("ouranwood_young_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final RegistryObject<OuranwoodTreeFeature> OURANWOOD_MEDIUM_TREE = FEATURES.register("ouranwood_medium_tree",
            () -> new OuranwoodTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final RegistryObject<OuranwoodCocoonTreeFeature> OURANWOOD_COCOON_TREE = FEATURES.register("ouranwood_cocoon_tree",
            () -> new OuranwoodCocoonTreeFeature(OuranwoodTreeConfiguration.CODEC));
    public static final RegistryObject<PeachTreeFeature> PEACH_TREE_FEATURE = FEATURES.register("peach_tree",
            () -> new PeachTreeFeature(PeachTreeConfiguration.CODEC));
    public static final RegistryObject<PeachTreeFeature> PEACH_LARGE_TREE_FEATURE = FEATURES.register("peach_large_tree",
            () -> new PeachTreeFeature(PeachTreeConfiguration.CODEC));
    public static final RegistryObject<ElythiaFloraFeature> ELYTHIA_FOREST_FLORA = FEATURES.register("elythia_forest_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FOREST));
    public static final RegistryObject<ElythiaFloraFeature> ELYTHIA_MEADOW_FLORA = FEATURES.register("elythia_meadow_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.MEADOW));
    public static final RegistryObject<ElythiaFloraFeature> PEACH_FOREST_FLORA = FEATURES.register("peach_forest_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.PEACH_FOREST));
    public static final RegistryObject<ElythiaFloraFeature> FLOWER_FOREST_MILKWEED = FEATURES.register("flower_forest_milkweed",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FLOWER_FOREST_MILKWEED));
    public static final RegistryObject<CornPatchFeature> CORN_PATCH = FEATURES.register("corn_patch",
            () -> new CornPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature> STAR_CORAL_PATCH = FEATURES.register("star_coral_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<SeashellFeature> SEASHELL_PATCH = FEATURES.register("seashell_patch",
            () -> new SeashellFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ElythiaFloraFeature> ELYTHIA_BUTTERFLY_FIELDS_FLORA = FEATURES.register("butterfly_fields_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.BUTTERFLY_FIELDS));
    public static final RegistryObject<ElythiaSurfaceCoverFeature> ELYTHIA_SURFACE_COVER = FEATURES.register("elythia_surface_cover",
            () -> new ElythiaSurfaceCoverFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ElythiaUndergroundFeature> ELYTHIA_UNDERGROUND = FEATURES.register("elythia_underground",
            () -> new ElythiaUndergroundFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<BrutalflyCocoonFeature> BRUTALFLY_COCOON = FEATURES.register("brutalfly_cocoon",
            () -> new BrutalflyCocoonFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<MolewormTunnelsFeature> ELYTHIA_MOLEWORM_TUNNELS = FEATURES.register("elythia_moleworm_tunnels",
            () -> new MolewormTunnelsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<MolewormCaveEntranceFeature> ELYTHIA_MOLEWORM_CAVE_ENTRANCES = FEATURES.register("elythia_moleworm_cave_entrances",
            () -> new MolewormCaveEntranceFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<MolewormWarrensFeature> ELYTHIA_MOLEWORM_WARRENS = FEATURES.register("elythia_moleworm_warrens",
            () -> new MolewormWarrensFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<MolewormSurfaceMoundsFeature> ELYTHIA_MOLEWORM_SURFACE_MOUNDS = FEATURES.register("elythia_moleworm_surface_mounds",
            () -> new MolewormSurfaceMoundsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<TriffidPatchFeature> TRIFFID_PATCH = FEATURES.register("triffid_patch",
            () -> new TriffidPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ElythiaPondFeature> ELYTHIA_POND = FEATURES.register("elythia_pond",
            () -> new ElythiaPondFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LumenPoolFeature> LUMEN_POOL = FEATURES.register("lumen_pools",
            () -> new LumenPoolFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LumenLilyPadFeature> LUMEN_LILY_PADS = FEATURES.register("lumen_lily_pads",
            () -> new LumenLilyPadFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature> GIANT_LILY_PAD_PATCH = FEATURES.register("giant_lily_pad_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature> GIANT_LILY_PAD_PATCH_PEACH_FOREST = FEATURES.register("giant_lily_pad_patch_peach_forest",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature(NoneFeatureConfiguration.CODEC, 0.12F));
    public static final RegistryObject<LumenStreamFeature> LUMEN_STREAM = FEATURES.register("lumen_streams",
            () -> new LumenStreamFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LumenSpireFeature> LUMEN_SPIRE = FEATURES.register("lumen_spires",
            () -> new LumenSpireFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ElythiaTuffBoulderFeature> ELYTHIA_TUFF_BOULDER = FEATURES.register("elythia_tuff_boulder",
            () -> new ElythiaTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ElythiaLargeTuffBoulderFeature> ELYTHIA_LARGE_TUFF_BOULDER = FEATURES.register("elythia_large_tuff_boulder",
            () -> new ElythiaLargeTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<PeachForestMossyBoulderFeature> PEACH_FOREST_MOSSY_BOULDER = FEATURES.register("peach_forest_mossy_boulder",
            () -> new PeachForestMossyBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<PeachForestPondFeature> PEACH_FOREST_POND = FEATURES.register("peach_forest_pond",
            () -> new PeachForestPondFeature(PeachForestPondConfiguration.CODEC));
    public static final RegistryObject<CoralSpikeFeature> ELYTHIA_CORAL_SPIKE = FEATURES.register("elythia_coral_spike",
            () -> new CoralSpikeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<DimensionalTearFeature> DIMENSIONAL_TEAR = FEATURES.register("dimensional_tear",
            () -> new DimensionalTearFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.elythia.FallenOuranwoodFeature> FALLEN_OURANWOOD_TREE = FEATURES.register("fallen_ouranwood_tree",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.FallenOuranwoodFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.elythia.FungalMushroomFeature> FUNGAL_MUSHROOM = FEATURES.register("fungal_mushroom",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.FungalMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ThoraxisFissureFeature> THORAXIS_FISSURE = FEATURES.register("thoraxis_fissure",
            () -> new ThoraxisFissureFeature(ThoraxisFissureConfiguration.CODEC));
    public static final RegistryObject<ThoraxisRibColumnsFeature> THORAXIS_RIB_COLUMNS = FEATURES.register("thoraxis_rib_columns",
            () -> new ThoraxisRibColumnsFeature(ThoraxisRibColumnsConfiguration.CODEC));
    public static final RegistryObject<ThoraxisSpikeFeature> THORAXIS_SPIKE = FEATURES.register("thoraxis_spike",
            () -> new ThoraxisSpikeFeature(ThoraxisSpikeConfiguration.CODEC));
    public static final RegistryObject<NyxiteSpikeFeature> NYXITE_SPIKES = FEATURES.register("nyxite_spikes",
            () -> new NyxiteSpikeFeature(NyxiteSpikeConfiguration.CODEC));
    public static final RegistryObject<ChitinSpikeFeature> CAVARYN_CHITIN_SPIKES = FEATURES.register("cavaryn_chitin_spikes",
            () -> new ChitinSpikeFeature(ChitinSpikeConfiguration.CODEC));
    public static final RegistryObject<CavarynBileVeinFeature> CAVARYN_BILE_VEINS = FEATURES.register("cavaryn_bile_veins",
            () -> new CavarynBileVeinFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CavarynBileCystFeature> CAVARYN_BILE_CYSTS = FEATURES.register("cavaryn_bile_cysts",
            () -> new CavarynBileCystFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CavarynCreepvineFeature> CAVARYN_CREEPVINE = FEATURES.register("cavaryn_creepvine",
            () -> new CavarynCreepvineFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<HangingCreeprootsFeature> HANGING_CREEPROOTS = FEATURES.register("hanging_creeproots",
            () -> new HangingCreeprootsFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<MoltingVinesFeature> MOLTING_VINES = FEATURES.register("molting_vines",
            () -> new MoltingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature> GROUND_MOLTING_VINES = FEATURES.register("ground_molting_vines",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.cavaryn.JumpyBugEggCeilingFeature> JUMPY_BUG_EGG_CEILING = FEATURES.register("jumpy_bug_egg_ceiling",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.JumpyBugEggCeilingFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature> SPIT_BUG_EGG_PATCH = FEATURES.register("spit_bug_egg_patch",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature> ROLLY_CAVES_GIANT_CAVERN = FEATURES.register("rolly_caves_giant_cavern",
            () -> new com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CavarynWallAmberMossFeature> CAVARYN_WALL_AMBER_MOSS = FEATURES.register("cavaryn_wall_amber_moss",
            () -> new CavarynWallAmberMossFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<AntiwaterSpringsFeature> ANTIWATER_SPRINGS = FEATURES.register("antiwater_springs",
            () -> new AntiwaterSpringsFeature(AntiwaterSpringsConfiguration.CODEC));
    public static final RegistryObject<PotentNyxiteFeature> POTENT_NYXITE_FEATURE = FEATURES.register("potent_nyxite",
            () -> new PotentNyxiteFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ThoraxisBloodCrystalFeature> THORAXIS_BLOOD_CRYSTAL_SPIRES = FEATURES.register("thoraxis_blood_crystal_spires",
            () -> new ThoraxisBloodCrystalFeature(ThoraxisBloodCrystalConfiguration.CODEC));
    public static final RegistryObject<ElythiaCloudFeature> ELYTHIA_CLOUDS = FEATURES.register("elythia_clouds",
            () -> new ElythiaCloudFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<BedBugNestFeature> BED_BUG_NEST = FEATURES.register("bed_bug_nest",
            () -> new BedBugNestFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<BedBugSurfaceClusterFeature> BED_BUG_SURFACE_CLUSTER = FEATURES.register("bed_bug_surface_cluster",
            () -> new BedBugSurfaceClusterFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CavarynEggPatchFeature> CAVARYN_TERROR_EGG_PATCH = FEATURES.register("cavaryn_terror_egg_patch",
            () -> new CavarynEggPatchFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature> HUGE_GLOWCAP_MUSHROOM = FEATURES.register("huge_glowcap_mushroom",
            () -> new com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature> HUGE_GLOWCAP_MUSHROOM_WORLDGEN = FEATURES.register("huge_glowcap_mushroom_worldgen",
            () -> new com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ThoraxisAntiwaterPoolFeature> THORAXIS_ANTIWATER_POOL = FEATURES.register("thoraxis_antiwater_pool",
            () -> new ThoraxisAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final RegistryObject<LucidAntiwaterPoolFeature> LUCID_ANTIWATER_POOL = FEATURES.register("lucid_antiwater_pool",
            () -> new LucidAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final RegistryObject<ThoraxisDuneFeature> THORAXIS_DUNE = FEATURES.register("thoraxis_dune",
            () -> new ThoraxisDuneFeature(ThoraxisDuneConfiguration.CODEC));

    // Biome sources
    public static final RegistryObject<com.mojang.serialization.Codec<ElythiaBiomeSource>> ELYTHIA_BIOME_SOURCE = BIOME_SOURCES.register("elythia_biome_source",
            () -> ElythiaBiomeSource.CODEC.codec());
    public static final RegistryObject<com.mojang.serialization.Codec<ThoraxisBiomeSource>> THORAXIS_BIOME_SOURCE = BIOME_SOURCES.register("thoraxis_biome_source",
            () -> ThoraxisBiomeSource.CODEC.codec());

    // Density function
    public static final RegistryObject<com.mojang.serialization.Codec<ElythiaRiverCarveFunction>> ELYTHIA_RIVER_CARVE = DENSITY_FUNCTION_TYPES.register("elythia_river_carve",
            () -> ElythiaRiverCarveFunction.KEY_CODEC.codec());

    private AntarchyForgeMisc() {}

    public static MobEffect mobEffectHolder(RegistryObject<? extends MobEffect> effect) {
        return effect.get();
    }

    public static Holder<Attribute> attributeHolder(RegistryObject<? extends Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get());
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        FEATURES.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        DENSITY_FUNCTION_TYPES.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        FROG_VARIANTS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }


    static ForgeFlowingFluid.Properties ichorProperties() {
        return new ForgeFlowingFluid.Properties(com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes.ICHOR_TYPE, ICHOR, FLOWING_ICHOR)
                .bucket(() -> AntarchyForgeItems.ICHOR_BUCKET.get())
                .block(() -> AntarchyForgeBlocks.ICHOR_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static ForgeFlowingFluid.Properties bileProperties() {
        return new ForgeFlowingFluid.Properties(com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes.BILE_TYPE, BILE, FLOWING_BILE)
                .bucket(() -> AntarchyForgeItems.BILE_BUCKET.get())
                .block(() -> AntarchyForgeBlocks.BILE_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static ForgeFlowingFluid.Properties antiwaterProperties() {
        return new ForgeFlowingFluid.Properties(com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes.ANTIWATER_TYPE, ANTIWATER, FLOWING_ANTIWATER)
                .bucket(() -> AntarchyForgeItems.ANTIWATER_BUCKET.get())
                .block(() -> AntarchyForgeBlocks.ANTIWATER_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    static ForgeFlowingFluid.Properties lumenProperties() {
        return new ForgeFlowingFluid.Properties(com.craisinlord.antarchy.forge.AntarchyForgeFluidTypes.LUMEN_TYPE, LUMEN, FLOWING_LUMEN)
                .bucket(() -> AntarchyForgeItems.LUMEN_BUCKET.get())
                .block(() -> AntarchyForgeBlocks.LUMEN_BLOCK.get())
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    private static SimpleParticleType simpleParticleType() {
        return new SimpleParticleType(true) {
        };
    }

    private static <T extends net.minecraft.core.particles.ParticleOptions> ParticleType<T> particleType(
            net.minecraft.core.particles.ParticleOptions.Deserializer<T> deserializer
    ) {
        return new ParticleType<>(false, deserializer) {
            @Override
            public com.mojang.serialization.Codec<T> codec() {
                return null;
            }
        };
    }
}
