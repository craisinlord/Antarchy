package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.recipe.AmericanizeRecipe;
import com.craisinlord.antarchy.content.worldgen.ants.BrownAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RainbowAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RedAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.TermiteNestFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileCystFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileVeinFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCreepvineFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWallAmberMossFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.HangingCreeprootsFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.MoltingVinesFeature;
import com.craisinlord.antarchy.content.worldgen.ocean.SeashellFeature;
import com.craisinlord.antarchy.content.worldgen.overworld.CornPatchFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.TyphoniteSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.TyphoniteSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.TyphoniteSwirlFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.PotentNyxiteFeature;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterFluid;
import com.craisinlord.antarchy.content.effect.DreadMobEffect;
import com.craisinlord.antarchy.content.effect.GoopedMobEffect;
import com.craisinlord.antarchy.content.effect.GrowthMobEffect;
import com.craisinlord.antarchy.content.effect.InvertedMobEffect;
import com.craisinlord.antarchy.content.effect.ParalyzedMobEffect;
import com.craisinlord.antarchy.content.effect.ShrinkMobEffect;
import com.craisinlord.antarchy.content.effect.StinkyMobEffect;
import com.craisinlord.antarchy.content.worldgen.elythia.CoralSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaBiomeSource;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverCarveFunction;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaFloraFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaLargeTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestMossyBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestPondConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachForestPondFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaPondFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaSurfaceCoverFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaUndergroundFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormCaveEntranceFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormSurfaceMoundsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.BrutalflyCocoonFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenPoolFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenLilyPadFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenStreamFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.LumenSpireFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormTunnelsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormWarrensFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodCocoonTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.TriffidPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynEggPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitinSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitinSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugNestFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugSurfaceClusterFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.DimensionalTearFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaCloudFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.LucidAntiwaterPoolFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NadirTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NadirTreeFeature;
import com.craisinlord.antarchy.content.worldgen.truffalo.TruffaloTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.truffalo.TruffaloTreeFeature;
import com.craisinlord.antarchy.content.worldgen.royal.RoyalTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.royal.RoyalTreeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBiomeSource;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisAntiwaterPoolConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeFeature;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;

import java.util.List;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.material.Fluid;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;

public final class AntarchyFabricMisc {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Antarchy.MODID);


    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Antarchy.MODID);


    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Antarchy.MODID);


    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, Antarchy.MODID);


    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Antarchy.MODID);


    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Antarchy.MODID);


    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Antarchy.MODID);


    private static final TagKey<DamageType> BYPASSES_BLOODGLASS =
            TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "bypasses_bloodglass"));


    public static final DeferredHolder<Attribute, Attribute> DOUBLE_DAMAGE_CHANCE = ATTRIBUTES.register(
            "double_damage_chance",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.double_damage_chance", 0.0, 0.0, 1.0).setSyncable(true)
    );


    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Antarchy.MODID);


    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Antarchy.MODID);


    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Antarchy.MODID);


    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Antarchy.MODID);


    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.craisinlord.antarchy.content.menu.DorrieInventoryMenu>> DORRIE_INVENTORY_MENU =
            MENUS.register("dorrie_inventory", () -> new net.minecraft.world.inventory.MenuType<>(com.craisinlord.antarchy.content.menu.DorrieInventoryMenu::new, FeatureFlags.DEFAULT_FLAGS));


    public static final DeferredRegister<net.minecraft.core.component.DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Antarchy.MODID);


    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
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


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ULTIMATE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("ultimate",
            () -> new ArmorMaterial(
                    createUltimateArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    AntarchyFabricItems::ultimateRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ultimate_armor"))),
                    2.0F,
                    (float) AntarchySettings.ultimateArmorKnockbackResistance()
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIGHTMARE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("nightmare",
            () -> new ArmorMaterial(
                    createNightmareArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(AntarchyFabricItems.NIGHTMARE_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_armor"))),
                    3.0F,
                    (float) AntarchySettings.nightmareArmorKnockbackResistance()
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRIMORDIAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("primordial",
            () -> new ArmorMaterial(
                    createPrimordialArmorDefense(),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(AntarchyFabricItems.PRIMORDIAL_SCUTE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "primordial"))),
                    3.5F,
                    0.1F
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BLOOD_CRYSTAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("blood_crystal",
            () -> new ArmorMaterial(
                    createBloodCrystalArmorDefense(),
                    30,
                    net.minecraft.core.Holder.direct(AntarchyFabricSounds.BLOOD_CRYSTAL_ARMOR_EQUIP.get()),
                    AntarchyFabricItems::bloodCrystalRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal"))),
                    0.0F,
                    0.0F
            ));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TIGERS_EYE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("tigers_eye",
            () -> new ArmorMaterial(
                    createDiamondArmorDefense(),
                    18,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(AntarchyFabricItems.TIGERS_EYE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "tigers_eye"))),
                    2.0F,
                    0.0F
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOGGLES_ARMOR_MATERIAL = ARMOR_MATERIALS.register("moggles",
            () -> new ArmorMaterial(
                    createMogglesArmorDefense(),
                    12,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moggles"))),
                    0.0F,
                    0.0F
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FALLEN_KING_CROWN_ARMOR_MATERIAL = ARMOR_MATERIALS.register("fallen_king_crown",
            () -> new ArmorMaterial(
                    createFallenKingCrownDefense(),
                    25,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "fallen_king_crown"))),
                    0.0F,
                    0.0F
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ROYAL_GUARDIAN_ARMOR_MATERIAL = ARMOR_MATERIALS.register("royal_guardian",
            () -> new ArmorMaterial(
                    new java.util.EnumMap<>(ArmorItem.Type.class),
                    AntarchySettings.royalArmorEnchantability(),
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(AntarchyFabricItems.KING_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_guardian"))),
                    0.0F,
                    0.0F
            ));


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ROYAL_ASSAILANT_ARMOR_MATERIAL = ARMOR_MATERIALS.register("royal_assailant",
            () -> new ArmorMaterial(
                    new java.util.EnumMap<>(ArmorItem.Type.class),
                    AntarchySettings.royalArmorEnchantability(),
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(AntarchyFabricItems.QUEEN_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_assailant"))),
                    0.0F,
                    0.0F
            ));


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DREAM_FIRE_FLAME = PARTICLE_TYPES.register("dream_fire_flame",
            () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NIGHTMARE_FIRE_FLAME = PARTICLE_TYPES.register("nightmare_fire_flame",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STINKY_GAS = PARTICLE_TYPES.register("stinky_gas",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STINKY_FLY = PARTICLE_TYPES.register("stinky_fly",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PEACH_LEAVES_PARTICLE = PARTICLE_TYPES.register("peach_leaves_particle",
            () -> simpleParticleType());
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LOTUS_POLLEN = PARTICLE_TYPES.register("lotus_pollen",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS = PARTICLE_TYPES.register("hypnotic_gas",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_CLOUD = PARTICLE_TYPES.register("hypnotic_gas_cloud",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_DOWN = PARTICLE_TYPES.register("hypnotic_gas_down",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYPNOTIC_GAS_CLOUD_DOWN = PARTICLE_TYPES.register("hypnotic_gas_cloud_down",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREFLY = PARTICLE_TYPES.register("firefly",
            () -> simpleParticleType());


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ORANGE_ASH = PARTICLE_TYPES.register("orange_ash",
            () -> simpleParticleType());

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUCID_BOLT_IMPACT_SMALL = PARTICLE_TYPES.register("lucid_bolt_impact_small",
            () -> simpleParticleType());

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LUCID_BOLT_IMPACT_LARGE = PARTICLE_TYPES.register("lucid_bolt_impact_large",
            () -> simpleParticleType());

    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_BASE = PARTICLE_TYPES.register("inverted_geyser_base",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));


    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_PLUME = PARTICLE_TYPES.register("inverted_geyser_plume",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));


    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_POOF = PARTICLE_TYPES.register("inverted_geyser_poof",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));


    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_ERUPTION = PARTICLE_TYPES.register("inverted_geyser_eruption",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));


    public static final DeferredHolder<MobEffect, DreadMobEffect> DREAD = MOB_EFFECTS.register("dread", DreadMobEffect::new);


    public static final DeferredHolder<MobEffect, ParalyzedMobEffect> PARALYZED = MOB_EFFECTS.register("paralyzed", ParalyzedMobEffect::new);


    public static final DeferredHolder<MobEffect, InvertedMobEffect> INVERTED = MOB_EFFECTS.register("inverted", InvertedMobEffect::new);


    public static final DeferredHolder<MobEffect, StinkyMobEffect> STINKY = MOB_EFFECTS.register("stinky", StinkyMobEffect::new);


    public static final DeferredHolder<MobEffect, GoopedMobEffect> GOOPED = MOB_EFFECTS.register("gooped", GoopedMobEffect::new);


    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.BloodglassWardEffect> BLOODGLASS_WARD = MOB_EFFECTS.register("bloodglass_ward", com.craisinlord.antarchy.content.effect.BloodglassWardEffect::new);


    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.GlimmeringMobEffect> GLIMMERING = MOB_EFFECTS.register("glimmering", com.craisinlord.antarchy.content.effect.GlimmeringMobEffect::new);


    public static final DeferredHolder<Attribute, Attribute> BLOODGLASS_MAX_HEARTS = ATTRIBUTES.register(
            "bloodglass_max_hearts",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.bloodglass_max_hearts", 0.0, 0.0, 8.0).setSyncable(true)
    );


    public static final DeferredHolder<Potion, Potion> DREAD_POTION = POTIONS.register("dread",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(DREAD), 600)));


    public static final DeferredHolder<Potion, Potion> LONG_DREAD = POTIONS.register("long_dread",
            () -> new Potion("dread", new MobEffectInstance(mobEffectHolder(DREAD), 2400)));


    public static final DeferredHolder<Potion, Potion> INVERSION = POTIONS.register("inversion",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(INVERTED), 600)));


    public static final DeferredHolder<Potion, Potion> LONG_INVERSION = POTIONS.register("long_inversion",
            () -> new Potion("inversion", new MobEffectInstance(mobEffectHolder(INVERTED), 2400)));


    public static final DeferredHolder<Potion, Potion> STINKY_POTION = POTIONS.register("stinky",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(STINKY), 1200)));


    public static final DeferredHolder<Potion, Potion> LONG_STINKY = POTIONS.register("long_stinky",
            () -> new Potion("stinky", new MobEffectInstance(mobEffectHolder(STINKY), 2400)));


    public static final DeferredHolder<MobEffect, ShrinkMobEffect> SHRINKING_EFFECT = MOB_EFFECTS.register("shrinking", ShrinkMobEffect::new);


    public static final DeferredHolder<MobEffect, GrowthMobEffect> GROWTH_EFFECT = MOB_EFFECTS.register("growth", GrowthMobEffect::new);


    public static final DeferredHolder<Potion, Potion> PARALYSIS = POTIONS.register("paralysis",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(PARALYZED), 200)));


    public static final DeferredHolder<Potion, Potion> LONG_PARALYSIS = POTIONS.register("long_paralysis",
            () -> new Potion("paralysis", new MobEffectInstance(mobEffectHolder(PARALYZED), 400)));


    public static final DeferredHolder<Potion, Potion> HASTE = POTIONS.register("haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 0)));


    public static final DeferredHolder<Potion, Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1)));


    public static final DeferredHolder<Potion, Potion> SHRINKING = POTIONS.register("shrinking",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 1200, 0)));


    public static final DeferredHolder<Potion, Potion> STRONG_SHRINKING = POTIONS.register("strong_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 900, 1)));


    public static final DeferredHolder<Potion, Potion> EXTREME_SHRINKING = POTIONS.register("extreme_shrinking",
            () -> new Potion("shrinking", new MobEffectInstance(mobEffectHolder(SHRINKING_EFFECT), 600, 2)));


    public static final DeferredHolder<Potion, Potion> GROWING = POTIONS.register("growing",
            () -> new Potion(new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 1200, 0)));


    public static final DeferredHolder<Potion, Potion> STRONG_GROWING = POTIONS.register("strong_growing",
            () -> new Potion("growing", new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 900, 1)));


    public static final DeferredHolder<Potion, Potion> EXTREME_GROWING = POTIONS.register("extreme_growing",
            () -> new Potion("growing", new MobEffectInstance(mobEffectHolder(GROWTH_EFFECT), 600, 2)));


    public static final DeferredHolder<Fluid, Fluid> BILE = FLUIDS.register("bile",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Source(
                    () -> AntarchyFabricContent.lookupFlowingFluid("bile"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_bile"),
                    () -> AntarchyFabricContent.lookupItem("bile_bucket"),
                    "bile",
                    4,
                    1,
                    5
            ));


    public static final DeferredHolder<Fluid, Fluid> FLOWING_BILE = FLUIDS.register("flowing_bile",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Flowing(
                    () -> AntarchyFabricContent.lookupFlowingFluid("bile"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_bile"),
                    () -> AntarchyFabricContent.lookupItem("bile_bucket"),
                    "bile",
                    4,
                    1,
                    5
            ));



    public static final DeferredHolder<Fluid, Fluid> ICHOR = FLUIDS.register("ichor",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Source(
                    () -> AntarchyFabricContent.lookupFlowingFluid("ichor"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_ichor"),
                    () -> AntarchyFabricContent.lookupItem("ichor_bucket"),
                    "ichor",
                    4,
                    1,
                    5
            ));


    public static final DeferredHolder<Fluid, Fluid> FLOWING_ICHOR = FLUIDS.register("flowing_ichor",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Flowing(
                    () -> AntarchyFabricContent.lookupFlowingFluid("ichor"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_ichor"),
                    () -> AntarchyFabricContent.lookupItem("ichor_bucket"),
                    "ichor",
                    4,
                    1,
                    5
            ));



    public static final DeferredHolder<Fluid, Fluid> ANTIWATER = FLUIDS.register("antiwater",
            AntiwaterFluid.Source::new);


    public static final DeferredHolder<Fluid, Fluid> FLOWING_ANTIWATER = FLUIDS.register("flowing_antiwater",
            AntiwaterFluid.Flowing::new);


    public static final DeferredHolder<Fluid, Fluid> LUMEN = FLUIDS.register("lumen",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Source(
                    () -> AntarchyFabricContent.lookupFlowingFluid("lumen"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_lumen"),
                    () -> AntarchyFabricContent.lookupItem("lumen_bucket"),
                    "lumen",
                    4,
                    1,
                    5
            ));


    public static final DeferredHolder<Fluid, Fluid> FLOWING_LUMEN = FLUIDS.register("flowing_lumen",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Flowing(
                    () -> AntarchyFabricContent.lookupFlowingFluid("lumen"),
                    () -> AntarchyFabricContent.lookupFlowingFluid("flowing_lumen"),
                    () -> AntarchyFabricContent.lookupItem("lumen_bucket"),
                    "lumen",
                    4,
                    1,
                    5
            ));


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


    public static final DeferredHolder<Feature<?>, NadirTreeFeature> NADIR_TREE_FEATURE = FEATURES.register("nadir_tree",
            () -> new NadirTreeFeature(NadirTreeConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, TruffaloTreeFeature> TRUFFALO_TREE_FEATURE = FEATURES.register("truffalo_tree",
            () -> new TruffaloTreeFeature(TruffaloTreeConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, RoyalTreeFeature> ROYAL_TREE_FEATURE = FEATURES.register("royal_tree",
            () -> new RoyalTreeFeature(RoyalTreeConfiguration.CODEC));


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


    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ElythiaBiomeSource>> ELYTHIA_BIOME_SOURCE = BIOME_SOURCES.register("elythia_biome_source",
            () -> ElythiaBiomeSource.CODEC);


    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ElythiaRiverCarveFunction>> ELYTHIA_RIVER_CARVE = DENSITY_FUNCTION_TYPES.register("elythia_river_carve",
            () -> ElythiaRiverCarveFunction.CODEC);


    public static final DeferredHolder<Feature<?>, ElythiaPondFeature> ELYTHIA_POND = FEATURES.register("elythia_pond",
            () -> new ElythiaPondFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, LumenPoolFeature> LUMEN_POOL = FEATURES.register("lumen_pools",
            () -> new LumenPoolFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, LumenLilyPadFeature> LUMEN_LILY_PADS = FEATURES.register("lumen_lily_pads",
            () -> new LumenLilyPadFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.elythia.GlimmeringReedFeature> GLIMMERING_REEDS = FEATURES.register("glimmering_reeds",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.GlimmeringReedFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.thoraxis.NaturalDirectionalVineFeature> SPIRALING_VINES = FEATURES.register("spiraling_vines",
            () -> new com.craisinlord.antarchy.content.worldgen.thoraxis.NaturalDirectionalVineFeature(NoneFeatureConfiguration.CODEC, "spiraling_vines", net.minecraft.core.Direction.UP, 4, 12));
    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.thoraxis.NaturalDirectionalVineFeature> GOREVINE = FEATURES.register("gorevine",
            () -> new com.craisinlord.antarchy.content.worldgen.thoraxis.NaturalDirectionalVineFeature(NoneFeatureConfiguration.CODEC, "gorevine", net.minecraft.core.Direction.DOWN, 8, 16));

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


    public static final DeferredHolder<Feature<?>, ThoraxisFissureFeature> THORAXIS_FISSURE = FEATURES.register("thoraxis_fissure",
            () -> new ThoraxisFissureFeature(ThoraxisFissureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, ThoraxisRibColumnsFeature> THORAXIS_RIB_COLUMNS = FEATURES.register("thoraxis_rib_columns",
            () -> new ThoraxisRibColumnsFeature(ThoraxisRibColumnsConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, ThoraxisSpikeFeature> THORAXIS_SPIKE = FEATURES.register("thoraxis_spike",
            () -> new ThoraxisSpikeFeature(ThoraxisSpikeConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, NyxiteSpikeFeature> NYXITE_SPIKES = FEATURES.register("nyxite_spikes",
            () -> new NyxiteSpikeFeature(NyxiteSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, TyphoniteSpikeFeature> TYPHONITE_SPIKES = FEATURES.register("typhonite_spikes",
            () -> new TyphoniteSpikeFeature(TyphoniteSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, TyphoniteSwirlFeature> TYPHONITE_SWIRLS = FEATURES.register("typhonite_swirls",
            () -> new TyphoniteSwirlFeature(NoneFeatureConfiguration.CODEC));


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


    public static final DeferredHolder<Feature<?>, ElythiaCloudFeature> ELYTHIA_CLOUDS = FEATURES.register("elythia_clouds",
            () -> new ElythiaCloudFeature(NoneFeatureConfiguration.CODEC));


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



    public static final DeferredHolder<Feature<?>, LucidAntiwaterPoolFeature> LUCID_ANTIWATER_POOL = FEATURES.register("lucid_antiwater_pool",
            () -> new LucidAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, ThoraxisDuneFeature> THORAXIS_DUNE = FEATURES.register("thoraxis_dune",
            () -> new ThoraxisDuneFeature(ThoraxisDuneConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, DimensionalTearFeature> DIMENSIONAL_TEAR = FEATURES.register("dimensional_tear",
            () -> new DimensionalTearFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, CoralSpikeFeature> ELYTHIA_CORAL_SPIKE = FEATURES.register("elythia_coral_spike",
            () -> new CoralSpikeFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.elythia.FallenOuranwoodFeature> FALLEN_OURANWOOD_TREE = FEATURES.register("fallen_ouranwood_tree",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.FallenOuranwoodFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, com.craisinlord.antarchy.content.worldgen.elythia.FungalMushroomFeature> FUNGAL_MUSHROOM = FEATURES.register("fungal_mushroom",
            () -> new com.craisinlord.antarchy.content.worldgen.elythia.FungalMushroomFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ThoraxisBiomeSource>> THORAXIS_BIOME_SOURCE = BIOME_SOURCES.register("thoraxis_biome_source",
            () -> ThoraxisBiomeSource.CODEC);


    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JUMPY_BOOTS_ARMOR_MATERIAL = ARMOR_MATERIALS.register("jumpy_boots",
            () -> new ArmorMaterial(
                    createJumpyBootsDefense(),
                    15,
                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(net.minecraft.world.item.Items.NETHERITE_BOOTS),
                    java.util.List.of(new ArmorMaterial.Layer(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_boots"))),
                    3.0F,
                    0.1F
            ));



    public static Holder<MobEffect> mobEffectHolder(DeferredHolder<MobEffect, ? extends MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }



    public static Holder<Attribute> attributeHolder(DeferredHolder<Attribute, ? extends Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get());
    }



    private static Holder<ArmorMaterial> armorMaterialHolder(DeferredHolder<ArmorMaterial, ? extends ArmorMaterial> material) {
        return BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(material.get());
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createUltimateArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 6);
        defense.put(ArmorItem.Type.LEGGINGS, 12);
        defense.put(ArmorItem.Type.CHESTPLATE, 16);
        defense.put(ArmorItem.Type.HELMET, 6);
        defense.put(ArmorItem.Type.BODY, 16);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createNightmareArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 4);
        defense.put(ArmorItem.Type.LEGGINGS, 9);
        defense.put(ArmorItem.Type.CHESTPLATE, 11);
        defense.put(ArmorItem.Type.HELMET, 4);
        defense.put(ArmorItem.Type.BODY, 11);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createJumpyBootsDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 0);
        defense.put(ArmorItem.Type.BODY, 0);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createBloodCrystalArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 2);
        defense.put(ArmorItem.Type.LEGGINGS, 5);
        defense.put(ArmorItem.Type.CHESTPLATE, 6);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 6);
        return defense;
    }

    private static java.util.EnumMap<ArmorItem.Type, Integer> createDiamondArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 11);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createMogglesArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 2);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createPrimordialArmorDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 8);
        return defense;
    }



    private static java.util.EnumMap<ArmorItem.Type, Integer> createFallenKingCrownDefense() {
        java.util.EnumMap<ArmorItem.Type, Integer> defense = new java.util.EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.BODY, 2);
        return defense;
    }



    private static SimpleParticleType simpleParticleType() {
        return new SimpleParticleType(true) {
        };
    }



    private static <T extends net.minecraft.core.particles.ParticleOptions> ParticleType<T> particleType(
            java.util.function.Function<ParticleType<T>, com.mojang.serialization.MapCodec<T>> codecFactory,
            java.util.function.Function<ParticleType<T>, net.minecraft.network.codec.StreamCodec<? super io.netty.buffer.ByteBuf, T>> streamCodecFactory
    ) {
        return new ParticleType<>(false) {
            @Override
            public com.mojang.serialization.MapCodec<T> codec() {
                return codecFactory.apply(this);
            }

            @Override
            public net.minecraft.network.codec.StreamCodec<? super io.netty.buffer.ByteBuf, T> streamCodec() {
                return streamCodecFactory.apply(this);
            }
        };
    }


    public static void register() {
        ARMOR_MATERIALS.register();
        FLUIDS.register();
        ATTRIBUTES.register();
        PARTICLE_TYPES.register();
        FEATURES.register();
        BIOME_SOURCES.register();
        DENSITY_FUNCTION_TYPES.register();
        ENTITY_SUB_PREDICATES.register();
        MOB_EFFECTS.register();
        POTIONS.register();
        MENUS.register();
        DATA_COMPONENT_TYPES.register();
        RECIPE_SERIALIZERS.register();
    }

}
