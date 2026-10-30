package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.worldgen.elythia.RiverLogBridgeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.RiverSteppingStonesFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.TruffaloSpireFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.GlimmeringFallFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ThrowbackNerdPoleFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaButteFunction;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaShelfLip;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaShelfTerrace;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverFlatten;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaColumnCache;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaBasinFill;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverCoastFade;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverSpan;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverProject;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaShared;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideMountainsFunction;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisTerrainFunction;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBiowartGutDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBroodNestDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCarapaceRiftDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynGlowcapGrottoDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynMolewormWarrenDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWormBoreDressingFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWormBoreFunction;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCavityFunction;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCavityUnionFunction;
import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import com.craisinlord.antarchy.content.effect.DreadMobEffect;
import com.craisinlord.antarchy.content.effect.BleedingMobEffect;
import com.craisinlord.antarchy.content.effect.BloodMoonMobEffect;
import com.craisinlord.antarchy.content.effect.GoopedMobEffect;
import com.craisinlord.antarchy.content.effect.GrowthMobEffect;
import com.craisinlord.antarchy.content.effect.CommandedMobEffect;
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
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitinSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.MoltingVinesFeature;
import com.craisinlord.antarchy.content.worldgen.ocean.SeashellFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.*;
import com.craisinlord.antarchy.neoforge.worldgen.CornPatchFeature;
import com.craisinlord.antarchy.neoforge.worldgen.LumenLilyPadFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.*;
import com.craisinlord.antarchy.content.worldgen.truffalo.*;
import com.craisinlord.antarchy.content.worldgen.royal.*;
import com.craisinlord.antarchy.neoforge.content.fluid.AntiwaterFluid;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.function.Function;

public final class AntarchyNeoforgeMisc {
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Antarchy.MODID);
    private static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Antarchy.MODID);

    public static final DeferredHolder<PoiType, PoiType> COMPUTER_POI = POI_TYPES.register("computer",
            () -> new PoiType(
                    java.util.Set.copyOf(com.craisinlord.antos.neoforge.AntOSNeoForgeContent.COMPUTER.get().getStateDefinition().getPossibleStates()),
                    1,
                    1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> COMPUTER_SCIENTIST = VILLAGER_PROFESSIONS.register("computer_scientist",
            () -> new VillagerProfession(
                    Component.translatable("entity.minecraft.villager.computer_scientist"),
                    holder -> holder.value() == COMPUTER_POI.get(),
                    holder -> holder.value() == COMPUTER_POI.get(),
                    com.google.common.collect.ImmutableSet.of(),
                    com.google.common.collect.ImmutableSet.of(),
                    net.minecraft.sounds.SoundEvents.VILLAGER_WORK_LIBRARIAN,
                    new Int2ObjectOpenHashMap<>()));

    public static final DeferredHolder<PoiType, PoiType> PEST_CONTROL_POI = POI_TYPES.register("pest_control",
            () -> new PoiType(java.util.Set.copyOf(AntarchyNeoforgeBlocks.ANT_TRAP.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<PoiType, PoiType> ELYTHIA_PORTAL_POI = POI_TYPES.register("elythia_portal",
            () -> new PoiType(java.util.Set.copyOf(AntarchyNeoforgeBlocks.ELYTHIA_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    public static final DeferredHolder<PoiType, PoiType> THORAXIS_PORTAL_POI = POI_TYPES.register("thoraxis_portal",
            () -> new PoiType(java.util.Set.copyOf(AntarchyNeoforgeBlocks.THORAXIS_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    public static final DeferredHolder<PoiType, PoiType> CAVARYN_PORTAL_POI = POI_TYPES.register("cavaryn_portal",
            () -> new PoiType(java.util.Set.copyOf(AntarchyNeoforgeBlocks.CAVARYN_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PEST_CONTROL = VILLAGER_PROFESSIONS.register("pest_control",
            () -> new VillagerProfession(Component.translatable("entity.minecraft.villager.pest_control"), holder -> holder.value() == PEST_CONTROL_POI.get(),
                    holder -> holder.value() == PEST_CONTROL_POI.get(), com.google.common.collect.ImmutableSet.of(), com.google.common.collect.ImmutableSet.of(), net.minecraft.sounds.SoundEvents.VILLAGER_WORK_FLETCHER,
                    new Int2ObjectOpenHashMap<>()));
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Antarchy.MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Antarchy.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Antarchy.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Antarchy.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Antarchy.MODID);
    private static final DeferredRegister<com.mojang.serialization.Codec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Antarchy.MODID);
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
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<net.minecraft.resources.Identifier>> CRITTER_CAGE_ENTITY_TYPE_COMPONENT =
            DATA_COMPONENT_TYPES.register("critter_cage_entity_type",
                    () -> net.minecraft.core.component.DataComponentType.<net.minecraft.resources.Identifier>builder()
                            .persistent(net.minecraft.resources.Identifier.CODEC)
                            .networkSynchronized(net.minecraft.resources.Identifier.STREAM_CODEC)
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
    public static final DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>, net.minecraft.world.item.crafting.RecipeSerializer<com.craisinlord.antarchy.content.recipe.PortalGunVariantRecipe>> PORTAL_GUN_VARIANT_SERIALIZER =
            RECIPE_SERIALIZERS.register("portal_gun_variant", () -> com.craisinlord.antarchy.content.recipe.PortalGunVariantRecipe.SERIALIZER);
    public static final DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>, net.minecraft.world.item.crafting.RecipeSerializer<com.craisinlord.antarchy.content.recipe.BigBerthaVariantRecipe>> BIG_BERTHA_VARIANT_SERIALIZER =
            RECIPE_SERIALIZERS.register("big_bertha_variant", () -> com.craisinlord.antarchy.content.recipe.BigBerthaVariantRecipe.SERIALIZER);

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
    public static final DeferredHolder<MenuType<?>, MenuType<com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu>> ROYAL_JUDGMENT_MENU = MENU_TYPES.register(
            "royal_judgment",
            () -> new MenuType<>(com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
    public static final DeferredHolder<MenuType<?>, MenuType<com.craisinlord.antarchy.content.menu.GiantFryingPanMenu>> GIANT_FRYING_PAN_MENU = MENU_TYPES.register(
            "giant_frying_pan",
            () -> new MenuType<>(com.craisinlord.antarchy.content.menu.GiantFryingPanMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredHolder<MobEffect, DreadMobEffect> DREAD = MOB_EFFECTS.register("dread", DreadMobEffect::new);
    public static final DeferredHolder<MobEffect, BleedingMobEffect> BLEEDING = MOB_EFFECTS.register("bleeding", BleedingMobEffect::new);
    public static final DeferredHolder<MobEffect, BloodMoonMobEffect> BLOOD_MOON = MOB_EFFECTS.register("blood_moon", BloodMoonMobEffect::new);
    public static final DeferredHolder<MobEffect, ParalyzedMobEffect> PARALYZED = MOB_EFFECTS.register("paralyzed", ParalyzedMobEffect::new);
    public static final DeferredHolder<MobEffect, CommandedMobEffect> COMMANDED = MOB_EFFECTS.register("commanded", CommandedMobEffect::new);
    public static final DeferredHolder<MobEffect, StinkyMobEffect> STINKY = MOB_EFFECTS.register("stinky", StinkyMobEffect::new);
    public static final DeferredHolder<MobEffect, GoopedMobEffect> GOOPED = MOB_EFFECTS.register("gooped", GoopedMobEffect::new);
    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.BloodglassWardEffect> BLOODGLASS_WARD = MOB_EFFECTS.register("bloodglass_ward", com.craisinlord.antarchy.content.effect.BloodglassWardEffect::new);

    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.GlimmeringMobEffect> GLIMMERING = MOB_EFFECTS.register("glimmering", com.craisinlord.antarchy.content.effect.GlimmeringMobEffect::new);
    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.RoyalVitalityPenaltyEffect> ROYAL_VITALITY_PENALTY = MOB_EFFECTS.register("royal_vitality_penalty", com.craisinlord.antarchy.content.effect.RoyalVitalityPenaltyEffect::new);
    public static final DeferredHolder<MobEffect, ShrinkMobEffect> SHRINKING_EFFECT = MOB_EFFECTS.register("shrinking", ShrinkMobEffect::new);
    public static final DeferredHolder<MobEffect, GrowthMobEffect> GROWTH_EFFECT = MOB_EFFECTS.register("growth", GrowthMobEffect::new);

    public static final DeferredHolder<Potion, Potion> INVERSION = POTIONS.register("inversion",
            () -> new Potion("inversion", new MobEffectInstance(AnthologyEffects.inverted(), 600)));
    public static final DeferredHolder<Potion, Potion> LONG_INVERSION = POTIONS.register("long_inversion",
            () -> new Potion("inversion", new MobEffectInstance(AnthologyEffects.inverted(), 2400)));
    public static final DeferredHolder<Potion, Potion> COMMAND = POTIONS.register("command",
            () -> new Potion("command", new MobEffectInstance(COMMANDED, 600)));
    public static final DeferredHolder<Potion, Potion> LONG_COMMAND = POTIONS.register("long_command",
            () -> new Potion("command", new MobEffectInstance(COMMANDED, 1200)));
    public static final DeferredHolder<Potion, Potion> TIME_DILATION = POTIONS.register("time_dilation",
            () -> new Potion("time_dilation", new MobEffectInstance(AnthologyEffects.dilated(), 1200)));
    public static final DeferredHolder<Potion, Potion> LONG_TIME_DILATION = POTIONS.register("long_time_dilation",
            () -> new Potion("time_dilation", new MobEffectInstance(AnthologyEffects.dilated(), 1200, 1)));
    public static final DeferredHolder<Potion, Potion> TIME_CONTRACTION = POTIONS.register("time_contraction",
            () -> new Potion("time_contraction", new MobEffectInstance(AnthologyEffects.contracted(), 1200)));
    public static final DeferredHolder<Potion, Potion> LONG_TIME_CONTRACTION = POTIONS.register("long_time_contraction",
            () -> new Potion("time_contraction", new MobEffectInstance(AnthologyEffects.contracted(), 1200, 1)));
    public static final DeferredHolder<Potion, Potion> STINKY_POTION = POTIONS.register("stinky",
            () -> new Potion("stinky", new MobEffectInstance(STINKY, 1200)));
    public static final DeferredHolder<Potion, Potion> LONG_STINKY = POTIONS.register("long_stinky",
            () -> new Potion("stinky", new MobEffectInstance(STINKY, 2400)));
    public static final DeferredHolder<Potion, Potion> PARALYSIS = POTIONS.register("paralysis",
            () -> new Potion("paralysis", new MobEffectInstance(PARALYZED, 200)));
    public static final DeferredHolder<Potion, Potion> LONG_PARALYSIS = POTIONS.register("long_paralysis",
            () -> new Potion("paralysis", new MobEffectInstance(PARALYZED, 400)));
    public static final DeferredHolder<Potion, Potion> HASTE = POTIONS.register("haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 600, 1)));
    public static final DeferredHolder<Potion, Potion> SHRINKING = POTIONS.register("shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(SHRINKING_EFFECT, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_SHRINKING = POTIONS.register("strong_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(SHRINKING_EFFECT, 900, 1)));
    public static final DeferredHolder<Potion, Potion> EXTREME_SHRINKING = POTIONS.register("extreme_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(SHRINKING_EFFECT, 600, 2)));
    public static final DeferredHolder<Potion, Potion> GROWING = POTIONS.register("growing",
            () -> new Potion("growing", new MobEffectInstance(GROWTH_EFFECT, 1200, 0)));
    public static final DeferredHolder<Potion, Potion> STRONG_GROWING = POTIONS.register("strong_growing",
            () -> new Potion("growing", new MobEffectInstance(GROWTH_EFFECT, 900, 1)));
    public static final DeferredHolder<Potion, Potion> EXTREME_GROWING = POTIONS.register("extreme_growing",
            () -> new Potion("growing", new MobEffectInstance(GROWTH_EFFECT, 600, 2)));

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

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DREAM_FIRE_FLAME = PARTICLE_TYPES.register("dream_fire_flame",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BUG_SPRAY_PARTICLE = PARTICLE_TYPES.register("bug_spray",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NIGHTMARE_FIRE_FLAME = PARTICLE_TYPES.register("nightmare_fire_flame",
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
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERTRIAL_SPAWNER_DETECTION = PARTICLE_TYPES.register("undertrial_spawner_detection",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERTRIAL_OMEN = PARTICLE_TYPES.register("undertrial_omen",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> UNDERVAULT_CONNECTION = PARTICLE_TYPES.register("undervault_connection",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_BASE = PARTICLE_TYPES.register("inverted_geyser_base",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_PLUME = PARTICLE_TYPES.register("inverted_geyser_plume",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_POOF = PARTICLE_TYPES.register("inverted_geyser_poof",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_ERUPTION = PARTICLE_TYPES.register("inverted_geyser_eruption",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RedAntNestFeature>> RED_ANT_NEST_FEATURE = FEATURES.register("red_ant_nest",
            () -> RedAntNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BrownAntNestFeature>> BROWN_ANT_NEST_FEATURE = FEATURES.register("brown_ant_nest",
            () -> BrownAntNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RainbowAntNestFeature>> RAINBOW_ANT_NEST_FEATURE = FEATURES.register("rainbow_ant_nest",
            () -> RainbowAntNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TermiteNestFeature>> TERMITE_NEST_FEATURE = FEATURES.register("termite_nest",
            () -> TermiteNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<OuranwoodTreeFeature>> OURANWOOD_LARGE_TREE = FEATURES.register("ouranwood_large_tree",
            () -> OuranwoodTreeFeature.LARGE_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<OuranwoodTreeFeature>> OURANWOOD_YOUNG_TREE = FEATURES.register("ouranwood_young_tree",
            () -> OuranwoodTreeFeature.YOUNG_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<OuranwoodTreeFeature>> OURANWOOD_MEDIUM_TREE = FEATURES.register("ouranwood_medium_tree",
            () -> OuranwoodTreeFeature.MEDIUM_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<OuranwoodCocoonTreeFeature>> OURANWOOD_COCOON_TREE = FEATURES.register("ouranwood_cocoon_tree",
            () -> OuranwoodCocoonTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PeachTreeFeature>> PEACH_TREE_FEATURE = FEATURES.register("peach_tree",
            () -> PeachTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PeachTreeFeature>> PEACH_LARGE_TREE_FEATURE = FEATURES.register("peach_large_tree",
            () -> PeachTreeFeature.LARGE_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<NadirTreeFeature>> NADIR_TREE_FEATURE = FEATURES.register("nadir_tree",
            () -> NadirTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TruffaloTreeFeature>> TRUFFALO_TREE_FEATURE = FEATURES.register("truffalo_tree",
            () -> TruffaloTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RoyalTreeFeature>> ROYAL_TREE_FEATURE = FEATURES.register("royal_tree",
            () -> RoyalTreeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<KingsTreeGridFeature>> KINGS_TREE_GRID = FEATURES.register("kings_tree_grid",
            () -> KingsTreeGridFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> ELYTHIA_FOREST_FLORA = FEATURES.register("elythia_forest_flora",
            () -> ElythiaFloraFeature.FOREST_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> ELYTHIA_MEADOW_FLORA = FEATURES.register("elythia_meadow_flora",
            () -> ElythiaFloraFeature.MEADOW_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> PRIMORDIAL_PLAINS_FLORA = FEATURES.register("primordial_plains_flora",
            () -> ElythiaFloraFeature.PRIMORDIAL_PLAINS_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> PEACH_FOREST_FLORA = FEATURES.register("peach_forest_flora",
            () -> ElythiaFloraFeature.PEACH_FOREST_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> FLOWER_FOREST_MILKWEED = FEATURES.register("flower_forest_milkweed",
            () -> ElythiaFloraFeature.FLOWER_FOREST_MILKWEED_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CornPatchFeature>> CORN_PATCH = FEATURES.register("corn_patch",
            () -> CornPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature>> STAR_CORAL_PATCH = FEATURES.register("star_coral_patch",
            () -> com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<SeashellFeature>> SEASHELL_PATCH = FEATURES.register("seashell_patch",
            () -> SeashellFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaFloraFeature>> ELYTHIA_BUTTERFLY_FIELDS_FLORA = FEATURES.register("butterfly_fields_flora",
            () -> ElythiaFloraFeature.BUTTERFLY_FIELDS_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaSurfaceCoverFeature>> ELYTHIA_SURFACE_COVER = FEATURES.register("elythia_surface_cover",
            () -> ElythiaSurfaceCoverFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaUndergroundFeature>> ELYTHIA_UNDERGROUND = FEATURES.register("elythia_underground",
            () -> ElythiaUndergroundFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BrutalflyCocoonFeature>> BRUTALFLY_COCOON = FEATURES.register("brutalfly_cocoon",
            () -> BrutalflyCocoonFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MolewormTunnelsFeature>> ELYTHIA_MOLEWORM_TUNNELS = FEATURES.register("elythia_moleworm_tunnels",
            () -> MolewormTunnelsFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MolewormCaveEntranceFeature>> ELYTHIA_MOLEWORM_CAVE_ENTRANCES = FEATURES.register("elythia_moleworm_cave_entrances",
            () -> MolewormCaveEntranceFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MolewormWarrensFeature>> ELYTHIA_MOLEWORM_WARRENS = FEATURES.register("elythia_moleworm_warrens",
            () -> MolewormWarrensFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MolewormSurfaceMoundsFeature>> ELYTHIA_MOLEWORM_SURFACE_MOUNDS = FEATURES.register("elythia_moleworm_surface_mounds",
            () -> MolewormSurfaceMoundsFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<FlytrapPatchFeature>> FLYTRAP_PATCH = FEATURES.register("flytrap_patch",
            () -> FlytrapPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaPondFeature>> ELYTHIA_POND = FEATURES.register("elythia_pond",
            () -> ElythiaPondFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LumenPoolFeature>> LUMEN_POOL = FEATURES.register("lumen_pools",
            () -> LumenPoolFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LumenLilyPadFeature>> LUMEN_LILY_PADS = FEATURES.register("lumen_lily_pads",
            () -> LumenLilyPadFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.elythia.GlimmeringReedFeature>> GLIMMERING_REEDS = FEATURES.register("glimmering_reeds",
            () -> com.craisinlord.antarchy.content.worldgen.elythia.GlimmeringReedFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<NaturalDirectionalVineFeature>> SPIRALING_VINES = FEATURES.register("spiraling_vines",
            () -> NaturalDirectionalVineFeature.SPIRALING_VINES_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<NaturalDirectionalVineFeature>> GOREVINE = FEATURES.register("gorevine",
            () -> NaturalDirectionalVineFeature.GOREVINE_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature>> GIANT_LILY_PAD_PATCH = FEATURES.register("giant_lily_pad_patch",
            () -> com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature>> GIANT_LILY_PAD_PATCH_PEACH_FOREST = FEATURES.register("giant_lily_pad_patch_peach_forest",
            () -> com.craisinlord.antarchy.content.worldgen.elythia.GiantLilyPadPatchFeature.PEACH_FOREST_CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LumenStreamFeature>> LUMEN_STREAM = FEATURES.register("lumen_streams",
            () -> LumenStreamFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LumenSpireFeature>> LUMEN_SPIRE = FEATURES.register("lumen_spires",
            () -> LumenSpireFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaTuffBoulderFeature>> ELYTHIA_TUFF_BOULDER = FEATURES.register("elythia_tuff_boulder",
            () -> ElythiaTuffBoulderFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaLargeTuffBoulderFeature>> ELYTHIA_LARGE_TUFF_BOULDER = FEATURES.register("elythia_large_tuff_boulder",
            () -> ElythiaLargeTuffBoulderFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PeachForestMossyBoulderFeature>> PEACH_FOREST_MOSSY_BOULDER = FEATURES.register("peach_forest_mossy_boulder",
            () -> PeachForestMossyBoulderFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PeachForestPondFeature>> PEACH_FOREST_POND = FEATURES.register("peach_forest_pond",
            () -> PeachForestPondFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CoralSpikeFeature>> ELYTHIA_CORAL_SPIKE = FEATURES.register("elythia_coral_spike",
            () -> CoralSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<FallenOuranwoodFeature>> FALLEN_OURANWOOD_TREE = FEATURES.register("fallen_ouranwood_tree",
            () -> FallenOuranwoodFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<FungalMushroomFeature>> FUNGAL_MUSHROOM = FEATURES.register("fungal_mushroom",
            () -> FungalMushroomFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisFissureFeature>> THORAXIS_FISSURE = FEATURES.register("thoraxis_fissure",
            () -> ThoraxisFissureFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisTransitionThroatFeature>> THORAXIS_TRANSITION_THROAT = FEATURES.register("thoraxis_transition_throat",
            () -> ThoraxisTransitionThroatFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<QueenHavocTrailFeature>> QUEEN_HAVOC_TRAIL = FEATURES.register("queen_havoc_trail",
            () -> QueenHavocTrailFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisRibColumnsFeature>> THORAXIS_RIB_COLUMNS = FEATURES.register("thoraxis_rib_columns",
            () -> ThoraxisRibColumnsFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisSpikeFeature>> THORAXIS_SPIKE = FEATURES.register("thoraxis_spike",
            () -> ThoraxisSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<NyxiteSpikeFeature>> NYXITE_SPIKES = FEATURES.register("nyxite_spikes",
            () -> NyxiteSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TyphoniteSpikeFeature>> TYPHONITE_SPIKES = FEATURES.register("typhonite_spikes",
            () -> TyphoniteSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TyphoniteSwirlFeature>> TYPHONITE_SWIRLS = FEATURES.register("typhonite_swirls",
            () -> TyphoniteSwirlFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ChitinSpikeFeature>> CAVARYN_CHITIN_SPIKES = FEATURES.register("cavaryn_chitin_spikes",
            () -> ChitinSpikeFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynBileVeinFeature>> CAVARYN_BILE_VEINS = FEATURES.register("cavaryn_bile_veins",
            () -> CavarynBileVeinFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynBileCystFeature>> CAVARYN_BILE_CYSTS = FEATURES.register("cavaryn_bile_cysts",
            () -> CavarynBileCystFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynCreepvineFeature>> CAVARYN_CREEPVINE = FEATURES.register("cavaryn_creepvine",
            () -> CavarynCreepvineFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<HangingCreeprootsFeature>> HANGING_CREEPROOTS = FEATURES.register("hanging_creeproots",
            () -> HangingCreeprootsFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MoltingVinesFeature>> MOLTING_VINES = FEATURES.register("molting_vines",
            () -> MoltingVinesFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature>> GROUND_MOLTING_VINES = FEATURES.register("ground_molting_vines",
            () -> com.craisinlord.antarchy.content.worldgen.cavaryn.GroundMoltingVinesFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.cavaryn.SpringbugEggCeilingFeature>> SPRINGBUG_EGG_CEILING = FEATURES.register("springbug_egg_ceiling",
            () -> com.craisinlord.antarchy.content.worldgen.cavaryn.SpringbugEggCeilingFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature>> SPIT_BUG_EGG_PATCH = FEATURES.register("spit_bug_egg_patch",
            () -> com.craisinlord.antarchy.content.worldgen.cavaryn.SpitBugEggPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature>> ROLLY_CAVES_GIANT_CAVERN = FEATURES.register("rolly_caves_giant_cavern",
            () -> com.craisinlord.antarchy.content.worldgen.cavaryn.RollyCavesGiantCavernFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynWallAmberMossFeature>> CAVARYN_WALL_AMBER_MOSS = FEATURES.register("cavaryn_wall_amber_moss",
            () -> CavarynWallAmberMossFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<AntiwaterSpringsFeature>> ANTIWATER_SPRINGS = FEATURES.register("antiwater_springs",
            () -> AntiwaterSpringsFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<PotentNyxiteFeature>> POTENT_NYXITE_FEATURE = FEATURES.register("potent_nyxite",
            () -> PotentNyxiteFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisBloodCrystalFeature>> THORAXIS_BLOOD_CRYSTAL_SPIRES = FEATURES.register("thoraxis_blood_crystal_spires",
            () -> ThoraxisBloodCrystalFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ElythiaCloudFeature>> ELYTHIA_CLOUDS = FEATURES.register("elythia_clouds",
            () -> ElythiaCloudFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BedBugNestFeature>> BED_BUG_NEST = FEATURES.register("bed_bug_nest",
            () -> BedBugNestFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BedBugSurfaceClusterFeature>> BED_BUG_SURFACE_CLUSTER = FEATURES.register("bed_bug_surface_cluster",
            () -> BedBugSurfaceClusterFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynEggPatchFeature>> CAVARYN_TERROR_EGG_PATCH = FEATURES.register("cavaryn_terror_egg_patch",
            () -> CavarynEggPatchFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature>> HUGE_GLOWCAP_MUSHROOM = FEATURES.register("huge_glowcap_mushroom",
            () -> com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature>> HUGE_GLOWCAP_MUSHROOM_WORLDGEN = FEATURES.register("huge_glowcap_mushroom_worldgen",
            () -> com.craisinlord.antarchy.content.worldgen.mushroom.GlowcapHugeMushroomWorldgenFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<LucidAntiwaterPoolFeature>> LUCID_ANTIWATER_POOL = FEATURES.register("lucid_antiwater_pool",
            () -> LucidAntiwaterPoolFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisDuneFeature>> THORAXIS_DUNE = FEATURES.register("thoraxis_dune",
            () -> ThoraxisDuneFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<DimensionalTearFeature>> DIMENSIONAL_TEAR = FEATURES.register("dimensional_tear",
            () -> DimensionalTearFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThoraxisIchorLakeFeature>> THORAXIS_ICHOR_LAKE = FEATURES.register("thoraxis_ichor_lake",
            () -> ThoraxisIchorLakeFeature.CODEC);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ElythiaBiomeSource>> ELYTHIA_BIOME_SOURCE = BIOME_SOURCES.register("elythia_biome_source",
            () -> ElythiaBiomeSource.CODEC);
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ThoraxisBiomeSource>> THORAXIS_BIOME_SOURCE = BIOME_SOURCES.register("thoraxis_biome_source",
            () -> ThoraxisBiomeSource.CODEC);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverCarveFunction>> ELYTHIA_RIVER_CARVE = DENSITY_FUNCTION_TYPES.register("elythia_river_carve",
            () -> ElythiaRiverCarveFunction.CODEC);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaHydrology>> ELYTHIA_HYDROLOGY = DENSITY_FUNCTION_TYPES.register("elythia_hydrology",
            () -> ElythiaHydrology.CODEC);

    private AntarchyNeoforgeMisc() {}

    public static void register(IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
        VILLAGER_PROFESSIONS.register(modEventBus);
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

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<CavarynWormBoreFunction>> CAVARYN_WORM_BORE = DENSITY_FUNCTION_TYPES.register("cavaryn_worm_bore",
            () -> CavarynWormBoreFunction.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<CavarynCavityFunction>> CAVARYN_CAVITY = DENSITY_FUNCTION_TYPES.register("cavaryn_cavity",
            () -> CavarynCavityFunction.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<CavarynCavityUnionFunction>> CAVARYN_CAVITY_UNION = DENSITY_FUNCTION_TYPES.register("cavaryn_cavity_union",
            () -> CavarynCavityUnionFunction.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynWormBoreDressingFeature>> CAVARYN_WORM_BORE_DRESSING = FEATURES.register("cavaryn_worm_bore_dressing",
            () -> CavarynWormBoreDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynBroodNestDressingFeature>> CAVARYN_BROOD_NEST_DRESSING = FEATURES.register("cavaryn_brood_nest_dressing",
            () -> CavarynBroodNestDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynBroodNestDressingFeature>> CAVARYN_BROOD_GALLERY_DRESSING = FEATURES.register("cavaryn_brood_gallery_dressing",
            () -> CavarynBroodNestDressingFeature.GALLERY_CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynGlowcapGrottoDressingFeature>> CAVARYN_GLOWCAP_GROTTO_DRESSING = FEATURES.register("cavaryn_glowcap_grotto_dressing",
            () -> CavarynGlowcapGrottoDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynMolewormWarrenDressingFeature>> CAVARYN_MOLEWORM_WARREN_DRESSING = FEATURES.register("cavaryn_moleworm_warren_dressing",
            () -> CavarynMolewormWarrenDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynCarapaceRiftDressingFeature>> CAVARYN_CARAPACE_RIFT_DRESSING = FEATURES.register("cavaryn_carapace_rift_dressing",
            () -> CavarynCarapaceRiftDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CavarynBiowartGutDressingFeature>> CAVARYN_BIOWART_GUT_DRESSING = FEATURES.register("cavaryn_biowart_gut_dressing",
            () -> CavarynBiowartGutDressingFeature.CODEC);;
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<com.craisinlord.antarchy.content.worldgen.elythia.reef.ElythiaReefFeature>> ELYTHIA_REEF = FEATURES.register("elythia_reef",
            () -> com.craisinlord.antarchy.content.worldgen.elythia.reef.ElythiaReefFeature.CODEC);


    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ThoraxisTerrainFunction>> THORAXIS_TERRAIN = DENSITY_FUNCTION_TYPES.register("thoraxis_terrain",
            () -> ThoraxisTerrainFunction.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ThoraxisUndersideMountainsFunction>> THORAXIS_UNDERSIDE_MOUNTAINS = DENSITY_FUNCTION_TYPES.register("thoraxis_underside_mountains",
            () -> ThoraxisUndersideMountainsFunction.CODEC);



    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaShared>> ELYTHIA_SHARED = DENSITY_FUNCTION_TYPES.register("elythia_shared",
            () -> ElythiaShared.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverProject>> ELYTHIA_RIVER_PROJECT = DENSITY_FUNCTION_TYPES.register("elythia_river_project",
            () -> ElythiaRiverProject.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverSpan>> ELYTHIA_RIVER_SPAN = DENSITY_FUNCTION_TYPES.register("elythia_river_span",
            () -> ElythiaRiverSpan.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverCoastFade>> ELYTHIA_RIVER_COAST_FADE = DENSITY_FUNCTION_TYPES.register("elythia_river_coast_fade",
            () -> ElythiaRiverCoastFade.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaBasinFill>> ELYTHIA_BASIN_FILL = DENSITY_FUNCTION_TYPES.register("elythia_basin_fill",
            () -> ElythiaBasinFill.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaColumnCache>> ELYTHIA_COLUMN_CACHE = DENSITY_FUNCTION_TYPES.register("elythia_column_cache",
            () -> ElythiaColumnCache.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverFlatten>> ELYTHIA_RIVER_FLATTEN = DENSITY_FUNCTION_TYPES.register("elythia_river_flatten",
            () -> ElythiaRiverFlatten.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaShelfTerrace>> ELYTHIA_SHELF_TERRACE = DENSITY_FUNCTION_TYPES.register("elythia_shelf_terrace",
            () -> ElythiaShelfTerrace.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaShelfLip>> ELYTHIA_SHELF_LIP = DENSITY_FUNCTION_TYPES.register("elythia_shelf_lip",
            () -> ElythiaShelfLip.CODEC);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaButteFunction>> ELYTHIA_BUTTE = DENSITY_FUNCTION_TYPES.register("elythia_butte",
            () -> ElythiaButteFunction.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ThrowbackNerdPoleFeature>> THROWBACK_NERD_POLE = FEATURES.register("throwback_nerd_pole", () -> ThrowbackNerdPoleFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GlimmeringFallFeature>> GLIMMERING_FALL = FEATURES.register("glimmering_fall", () -> GlimmeringFallFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<TruffaloSpireFeature>> TRUFFALO_SPIRE = FEATURES.register("truffalo_spire", () -> TruffaloSpireFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RiverSteppingStonesFeature>> RIVER_STEPPING_STONES = FEATURES.register("river_stepping_stones", () -> RiverSteppingStonesFeature.CODEC);
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<RiverLogBridgeFeature>> RIVER_LOG_BRIDGE = FEATURES.register("river_log_bridge", () -> RiverLogBridgeFeature.CODEC);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GROUND_MIST = PARTICLE_TYPES.register("ground_mist", () -> new SimpleParticleType(true) {});

}
