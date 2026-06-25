package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.content.item.BloodCrystalAppleItem;
import com.craisinlord.antarchy.content.item.BloodCrystalKatanaItem;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.worldgen.ants.BrownAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RainbowAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RedAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.TermiteNestFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.AntiwaterSpringsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.NyxiteSpikeFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.PotentNyxiteFeature;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterFluid;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterLiquidBlock;
import com.craisinlord.antarchy.fabric.item.DeferredSpawnEggItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import com.craisinlord.antarchy.fabric.registry.DeferredBlock;
import com.craisinlord.antarchy.fabric.registry.DeferredHolder;
import com.craisinlord.antarchy.fabric.registry.DeferredItem;
import com.craisinlord.antarchy.fabric.registry.DeferredRegister;
import com.craisinlord.antarchy.content.effect.DreadMobEffect;
import com.craisinlord.antarchy.content.effect.GrowthMobEffect;
import com.craisinlord.antarchy.content.effect.InvertedMobEffect;
import com.craisinlord.antarchy.content.effect.ParalyzedMobEffect;
import com.craisinlord.antarchy.content.effect.ShrinkMobEffect;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.AppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.GoldenAppleCow;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.entity.DrTrayaurusEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.craisinlord.antarchy.content.entity.lucid.LucidEyeProjectileEntity;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodBoatEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodChestBoatEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RainbowAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import com.craisinlord.antarchy.content.item.BattleAxeItem;
import com.craisinlord.antarchy.content.item.BasiliskDaggerItem;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.AntimetalBlockItem;
import com.craisinlord.antarchy.fabric.item.AntimetalScaffoldingItem;
import com.craisinlord.antarchy.content.item.CloudSharkFinSoupItem;
import com.craisinlord.antarchy.content.item.CorneaEarItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.DuctTapeBlockItem;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.MobComingSoonTooltipItem;
import com.craisinlord.antarchy.content.item.MogglesItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateAxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateHoeItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimatePickaxeItem;
import com.craisinlord.antarchy.content.item.ultimate.UtlimateShovelItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateSwordItem;
import com.craisinlord.antarchy.content.item.DiamondMinecartItem;
import com.craisinlord.antarchy.content.item.ReverieBottleItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipItem;
import com.craisinlord.antarchy.content.item.ScorpionWhipTetherSync;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import com.craisinlord.antarchy.content.item.SquidzookaItem;
import com.craisinlord.antarchy.content.item.SimpleToolTier;
import com.craisinlord.antarchy.content.item.ultimate.UltimateArmorItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateBowItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateCrossbowItem;
import com.craisinlord.antarchy.content.item.NightmareArmorItem;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import com.craisinlord.antarchy.content.item.OuranwoodBoatOnlyItem;
import com.craisinlord.antarchy.content.item.OuranwoodChestBoatItem;
import com.craisinlord.antarchy.content.item.RainbowSugarItem;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaBiomeSource;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaRiverCarveFunction;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaFloraFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaLargeTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaPondFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaSurfaceCoverFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaTuffBoulderFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaUndergroundFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormCaveEntranceFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormSurfaceMoundsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.BrutalflyCocoonFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormTunnelsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormWarrensFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodCocoonTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.TriffidPatchFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugNestFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.BedBugSurfaceClusterFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.CloudSeaCalciteFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.LucidAntiwaterPoolFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBiomeSource;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisDuneFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisFissureFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisBloodCrystalFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisAntiwaterPoolConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisAntiwaterPoolFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisRibColumnsFeature;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisSpikeFeature;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserBaseParticleOptions;
import com.craisinlord.antarchy.content.client.particle.InvertedGeyserParticleOptions;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public final class AntarchyFabricContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Antarchy.MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Antarchy.MODID);
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Antarchy.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Antarchy.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Antarchy.MODID);
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Antarchy.MODID);
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Antarchy.MODID);
    private static final TagKey<DamageType> BYPASSES_BLOODGLASS =
            TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("antarchy", "bypasses_bloodglass"));
    private static final DeferredHolder<Attribute, Attribute> DOUBLE_DAMAGE_CHANCE = ATTRIBUTES.register(
            "double_damage_chance",
            () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.antarchy.double_damage_chance", 0.0, 0.0, 1.0).setSyncable(true)
    );
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Antarchy.MODID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Antarchy.MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Antarchy.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Antarchy.MODID);
    private static final Tier ULTIMATE_TIER = new SimpleToolTier(
            3072,
            10.5F,
            0.0F,
            Tiers.NETHERITE.getIncorrectBlocksForDrops(),
            25,
            AntarchyFabricContent::ultimateRepairIngredient
    );
    private static final Tier BLOOD_CRYSTAL_KATANA_TIER = new SimpleToolTier(
            1200,
            8.0F,
            0.0F,
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            18,
            AntarchyFabricContent::bloodCrystalRepairIngredient
    );
    public static final DeferredItem<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale", new Item.Properties().rarity(Rarity.RARE).fireResistant());
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUIDZOOKA_FIRE = registerSoundEvent("squidzooka_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHRINK_RAY_SOUND = registerSoundEvent("shrink_ray");
    public static final DeferredHolder<SoundEvent, SoundEvent> GROWTH_RAY_SOUND = registerSoundEvent("growth_ray");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIZE_RAY_CHARGE = registerSoundEvent("size_ray_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_AMBIENT = registerSoundEvent("ant_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_IDLE = registerSoundEvent("ant_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_HURT = registerSoundEvent("ant_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_BITE = registerSoundEvent("ant_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_GATHER = registerSoundEvent("ant_gather");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANT_NEST = registerSoundEvent("ant_nest");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_BITE = registerSoundEvent("cloud_shark_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_IDLE = registerSoundEvent("cloud_shark_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_HURT = registerSoundEvent("cloud_shark_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_DEATH = registerSoundEvent("cloud_shark_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOUD_SHARK_FLY = registerSoundEvent("cloud_shark_fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_IDLE = registerSoundEvent("caterpillar_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_HURT = registerSoundEvent("caterpillar_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CATERPILLAR_CRAWL = registerSoundEvent("caterpillar_crawl");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTERFLY_HURT = registerSoundEvent("butterfly_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRUTALFLY_IDLE = registerSoundEvent("brutalfly_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRUTALFLY_DEATH = registerSoundEvent("brutalfly_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELYTHIA_FIREFLY_AMBIENT = registerSoundEvent("ambient.elythia.firefly");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_AMBIENT = registerSoundEvent("missile_squid_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_HURT = registerSoundEvent("missile_squid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_DEATH = registerSoundEvent("missile_squid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_SQUID_ATTACK = registerSoundEvent("missile_squid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_FLYING_LOOP = registerSoundEvent("kraken_flying_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_FLYING_SIDEWAYS_LOOP = registerSoundEvent("kraken_flying_sideways_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_ATTACK = registerSoundEvent("kraken_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SPIN = registerSoundEvent("kraken_spin");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_ROAR = registerSoundEvent("kraken_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SUMMON = registerSoundEvent("kraken_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_HURT = registerSoundEvent("kraken_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_DEATH = registerSoundEvent("kraken_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_IDLE_LOOP = registerSoundEvent("basilisk_idle_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_SLITHER_LOOP = registerSoundEvent("basilisk_slither_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_BITE = registerSoundEvent("basilisk_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HISS = registerSoundEvent("basilisk_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_HURT = registerSoundEvent("basilisk_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BASILISK_DEATH = registerSoundEvent("basilisk_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_AMBIENT = registerSoundEvent("thoraxis_nightmare_wastes_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_ADDITIONS = registerSoundEvent("thoraxis_nightmare_wastes_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_NIGHTMARE_WASTES_MOOD = registerSoundEvent("thoraxis_nightmare_wastes_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_AMBIENT = registerSoundEvent("thoraxis_dream_dunes_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_ADDITIONS = registerSoundEvent("thoraxis_dream_dunes_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_DREAM_DUNES_MOOD = registerSoundEvent("thoraxis_dream_dunes_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_AMBIENT = registerSoundEvent("thoraxis_lucid_pools_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_ADDITIONS = registerSoundEvent("thoraxis_lucid_pools_additions");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORAXIS_LUCID_POOLS_MOOD = registerSoundEvent("thoraxis_lucid_pools_mood");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_AMBIENT = registerSoundEvent("lucid_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_FLYING = registerSoundEvent("lucid_flying");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_ATTACK = registerSoundEvent("lucid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_BOLT_SOUND = registerSoundEvent("lucid_bolt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_HURT = registerSoundEvent("lucid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUCID_DEATH = registerSoundEvent("lucid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_IDLE = registerSoundEvent("reverie_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_HURT = registerSoundEvent("reverie_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_WORRY = registerSoundEvent("reverie_worry");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_SAVE = registerSoundEvent("reverie_save");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_JOIN_PLAYER = registerSoundEvent("reverie_join_player");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVERIE_ALERT = registerSoundEvent("reverie_alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_IDLE = registerSoundEvent("flying_squirrel_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_BEG = registerSoundEvent("flying_squirrel_beg");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_NUT = registerSoundEvent("flying_squirrel_nut");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_GLIDE_LOOP = registerSoundEvent("flying_squirrel_glide_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_HURT = registerSoundEvent("flying_squirrel_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLYING_SQUIRREL_DEATH = registerSoundEvent("flying_squirrel_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_IDLE = registerSoundEvent("nightmare_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_HURT = registerSoundEvent("nightmare_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_ROAR = registerSoundEvent("nightmare_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_DEATH = registerSoundEvent("nightmare_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_BITE = registerSoundEvent("nightmare_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_FLAP = registerSoundEvent("nightmare_flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_ATTACK = registerSoundEvent("triffid_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_GRAB = registerSoundEvent("triffid_grab");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_HURT = registerSoundEvent("triffid_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_DEATH = registerSoundEvent("triffid_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_HISS = registerSoundEvent("triffid_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRIFFID_GROWL = registerSoundEvent("triffid_growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_AMBIENT = registerSoundEvent("mantis_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_HURT = registerSoundEvent("mantis_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_ATTACK = registerSoundEvent("mantis_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTIS_FLY_LOOP = registerSoundEvent("mantis_fly_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_PICKUP = registerSoundEvent("gravity_gun_pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_DROP = registerSoundEvent("gravity_gun_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_HOLD_LOOP = registerSoundEvent("gravity_gun_hold_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_LAUNCH = registerSoundEvent("gravity_gun_launch");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVITY_GUN_DRYFIRE = registerSoundEvent("gravity_gun_dryfire");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_AMBIENT = registerSoundEvent("bed_bug_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_HURT = registerSoundEvent("bed_bug_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BED_BUG_ATTACK = registerSoundEvent("bed_bug_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_IDLE = registerSoundEvent("wasp_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_HURT = registerSoundEvent("wasp_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_ATTACK = registerSoundEvent("wasp_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WASP_DEATH = registerSoundEvent("wasp_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_AMBIENT = registerSoundEvent("scorpion_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_HURT = registerSoundEvent("scorpion_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCORPION_ATTACK = registerSoundEvent("scorpion_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_AMBIENT = registerSoundEvent("emperor_scorpion_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_HURT = registerSoundEvent("emperor_scorpion_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_ATTACK = registerSoundEvent("emperor_scorpion_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMPEROR_SCORPION_ROAR = registerSoundEvent("emperor_scorpion_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_AMBIENT = registerSoundEvent("moleworm_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_HURT = registerSoundEvent("moleworm_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_ATTACK = registerSoundEvent("moleworm_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEWORM_DIG = registerSoundEvent("moleworm_dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_AMBIENT = registerSoundEvent("molevore_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_HURT = registerSoundEvent("molevore_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_ATTACK = registerSoundEvent("molevore_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLEVORE_DIG = registerSoundEvent("molevore_dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUCT_TAPE_USE = registerSoundEvent("duct_tape_use");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_HYPNOTIC_GAS = registerSoundEvent("potent_nyxite_hypnotic_gas");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_START = registerSoundEvent("potent_nyxite_geyser_eruption_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_ACTIVE = registerSoundEvent("potent_nyxite_geyser_eruption_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_START = registerSoundEvent("potent_nyxite_geyser_continuous_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_ACTIVE = registerSoundEvent("potent_nyxite_geyser_continuous_active");
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ULTIMATE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("ultimate",
            () -> new ArmorMaterial(
                    createUltimateArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    AntarchyFabricContent::ultimateRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ultimate_armor"))),
                    2.0F,
                    (float) AntarchySettings.ultimateArmorKnockbackResistance()
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> NIGHTMARE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("nightmare",
            () -> new ArmorMaterial(
                    createNightmareArmorDefense(),
                    10,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(NIGHTMARE_SCALE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_armor"))),
                    3.0F,
                    (float) AntarchySettings.nightmareArmorKnockbackResistance()
            ));
    public static final DeferredItem<Item> PRIMORDIAL_SCUTE = ITEMS.registerSimpleItem("primordial_scute",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRIMORDIAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("primordial",
            () -> new ArmorMaterial(
                    createPrimordialArmorDefense(),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(PRIMORDIAL_SCUTE.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "primordial"))),
                    3.0F,
                    0.1F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> BLOOD_CRYSTAL_ARMOR_MATERIAL = ARMOR_MATERIALS.register("blood_crystal",
            () -> new ArmorMaterial(
                    createBloodCrystalArmorDefense(),
                    30,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    AntarchyFabricContent::bloodCrystalRepairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal"))),
                    0.0F,
                    0.0F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> MOGGLES_ARMOR_MATERIAL = ARMOR_MATERIALS.register("moggles",
            () -> new ArmorMaterial(
                    createMogglesArmorDefense(),
                    12,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moggles"))),
                    0.0F,
                    0.0F
            ));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> FALLEN_KING_CROWN_ARMOR_MATERIAL = ARMOR_MATERIALS.register("fallen_king_crown",
            () -> new ArmorMaterial(
                    createFallenKingCrownDefense(),
                    25,
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    () -> Ingredient.of(Items.GOLD_INGOT),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "fallen_king_crown"))),
                    0.0F,
                    0.0F
            ));
    public static final DeferredBlock<DuplicatorLogBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new DuplicatorLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).randomTicks()));
    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_LOG = BLOCKS.register("ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_WOOD = BLOCKS.register("ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_LOG = BLOCKS.register("mossy_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_WOOD = BLOCKS.register("mossy_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_LOG = BLOCKS.register("stripped_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_WOOD = BLOCKS.register("stripped_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> OURANWOOD_PLANKS = BLOCKS.register("ouranwood_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> OURANWOOD_STAIRS = BLOCKS.register("ouranwood_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(OURANWOOD_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> OURANWOOD_SLAB = BLOCKS.register("ouranwood_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> OURANWOOD_FENCE = BLOCKS.register("ouranwood_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> OURANWOOD_FENCE_GATE = BLOCKS.register("ouranwood_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> OURANWOOD_DOOR = BLOCKS.register("ouranwood_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> OURANWOOD_TRAPDOOR = BLOCKS.register("ouranwood_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> OURANWOOD_PRESSURE_PLATE = BLOCKS.register("ouranwood_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> OURANWOOD_BUTTON = BLOCKS.register("ouranwood_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<OuranwoodLeavesBlock> OURANWOOD_LEAVES = BLOCKS.register("ouranwood_leaves",
            () -> new OuranwoodLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final DeferredBlock<OuranwoodAcornBlock> OURANWOOD_ACORN_BLOCK = BLOCKS.register("ouranwood_acorn",
            () -> new OuranwoodAcornBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final DeferredBlock<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)));
    public static final DeferredBlock<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)));
    public static final DeferredBlock<TorchflowerBushBlock> TORCHFLOWER_BUSH = BLOCKS.register("torchflower_bush",
            () -> new TorchflowerBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)
                    .lightLevel(state -> AntarchySettings.glowingTorchflowers() ? 15 : 0)));
    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            () -> new DuplicatorSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final DeferredBlock<DuctTapeBlock> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new DuctTapeBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()));
    public static final DeferredBlock<InfestedRootedDirtBlock> INFESTED_ROOTED_DIRT = BLOCKS.register("infested_rooted_dirt",
            () -> new InfestedRootedDirtBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ROOTED_DIRT).randomTicks()));
    public static final DeferredBlock<InfestedCoarseDirtBlock> INFESTED_COARSE_DIRT = BLOCKS.register("infested_coarse_dirt",
            () -> new InfestedCoarseDirtBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT).randomTicks()));
    public static final DeferredBlock<Block> NYXITE = BLOCKS.register("nyxite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<Block> POLISHED_NYXITE = BLOCKS.register("polished_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<Block> CHISELED_NYXITE = BLOCKS.register("chiseled_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<Block> NYXITE_BRICKS = BLOCKS.register("nyxite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> NYXITE_STAIRS = BLOCKS.register("nyxite_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(NYXITE.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> NYXITE_SLAB = BLOCKS.register("nyxite_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> NYXITE_WALL = BLOCKS.register("nyxite_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> POLISHED_NYXITE_STAIRS = BLOCKS.register("polished_nyxite_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(POLISHED_NYXITE.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> POLISHED_NYXITE_SLAB = BLOCKS.register("polished_nyxite_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> POLISHED_NYXITE_WALL = BLOCKS.register("polished_nyxite_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));
    public static final DeferredBlock<Block> SHELLSTONE = BLOCKS.register("shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<Block> POLISHED_SHELLSTONE = BLOCKS.register("polished_shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<Block> SHELLSTONE_BRICKS = BLOCKS.register("shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<Block> CHISELED_SHELLSTONE = BLOCKS.register("chiseled_shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<Block> MOSSY_SHELLSTONE_BRICKS = BLOCKS.register("mossy_shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<Block> CRACKED_SHELLSTONE_BRICKS = BLOCKS.register("cracked_shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> MOSSY_SHELLSTONE_BRICK_STAIRS = BLOCKS.register("mossy_shellstone_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(MOSSY_SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> MOSSY_SHELLSTONE_BRICK_SLAB = BLOCKS.register("mossy_shellstone_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> MOSSY_SHELLSTONE_BRICK_WALL = BLOCKS.register("mossy_shellstone_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SHELLSTONE_STAIRS = BLOCKS.register("shellstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SHELLSTONE_SLAB = BLOCKS.register("shellstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> SHELLSTONE_WALL = BLOCKS.register("shellstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> POLISHED_SHELLSTONE_STAIRS = BLOCKS.register("polished_shellstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(POLISHED_SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> POLISHED_SHELLSTONE_SLAB = BLOCKS.register("polished_shellstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> POLISHED_SHELLSTONE_WALL = BLOCKS.register("polished_shellstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SHELLSTONE_BRICK_STAIRS = BLOCKS.register("shellstone_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SHELLSTONE_BRICK_SLAB = BLOCKS.register("shellstone_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> SHELLSTONE_BRICK_WALL = BLOCKS.register("shellstone_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TriffidGooBlock> TRIFFID_GOO_BLOCK = BLOCKS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.block.TriffidGooBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            () -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyFabricContent::cloudBucketItem,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POWDER_SNOW).noLootTable().noOcclusion()
            ));
    public static final DeferredBlock<Block> PALE_NYXITE = BLOCKS.register("pale_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<NyxiteSpikeBlock> NYXITE_SPIKE = BLOCKS.register("nyxite_spike",
            () -> new NyxiteSpikeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE)));
    public static final DeferredBlock<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            () -> new PotentNyxiteBlock(
                    AntarchyFabricContent::potentNyxiteBlockEntityType,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERRACK).lightLevel(state -> 3)
            ));
    public static final DeferredBlock<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            () -> new UmbralMossBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)));
    public static final DeferredBlock<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET).noOcclusion()));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DREAM_FIRE_FLAME = PARTICLE_TYPES.register("dream_fire_flame",
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
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_BASE = PARTICLE_TYPES.register("inverted_geyser_base",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_PLUME = PARTICLE_TYPES.register("inverted_geyser_plume",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserBaseParticleOptions>> INVERTED_GEYSER_POOF = PARTICLE_TYPES.register("inverted_geyser_poof",
            () -> particleType(InvertedGeyserBaseParticleOptions::codec, InvertedGeyserBaseParticleOptions::streamCodec));
    public static final DeferredHolder<ParticleType<?>, ParticleType<InvertedGeyserParticleOptions>> INVERTED_GEYSER_ERUPTION = PARTICLE_TYPES.register("inverted_geyser_eruption",
            () -> particleType(InvertedGeyserParticleOptions::codec, InvertedGeyserParticleOptions::streamCodec));
    public static final DeferredBlock<DreamTorchBlock> DREAM_TORCH = BLOCKS.register("dream_torch",
            () -> new DreamTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_TORCH)));
    public static final DeferredBlock<DreamWallTorchBlock> DREAM_WALL_TORCH = BLOCKS.register("dream_wall_torch",
            () -> new DreamWallTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_WALL_TORCH)));
    public static final DeferredBlock<net.minecraft.world.level.block.LanternBlock> DREAM_LANTERN = BLOCKS.register("dream_lantern",
            () -> new net.minecraft.world.level.block.LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN)));
    public static final DeferredBlock<DreamCampfireBlock> DREAM_CAMPFIRE = BLOCKS.register("dream_campfire",
            () -> new DreamCampfireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_CAMPFIRE)));
    public static final DeferredBlock<DreamFireBlock> DREAM_FIRE = BLOCKS.register("dream_fire",
            () -> new DreamFireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE)));
    public static final DeferredBlock<DreamCeilingFireBlock> DREAM_CEILING_FIRE = BLOCKS.register("dream_fire_ceiling",
            () -> new DreamCeilingFireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE)));
    public static final DeferredBlock<CreepingHorrorEggBlock> CREEPING_HORROR_EGGS = BLOCKS.register("creeping_horror_eggs",
            () -> new CreepingHorrorEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));
    public static final DeferredBlock<LurkingTerrorEggBlock> LURKING_TERROR_EGGS = BLOCKS.register("lurking_terror_eggs",
            () -> new LurkingTerrorEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));
    public static final DeferredBlock<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            () -> new BedBugEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));
    public static final DeferredBlock<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            () -> new WaspNestBlock(AntarchyFabricContent::waspNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.BEE_NEST)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            () -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT).noLootTable()));
    public static final DeferredBlock<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            () -> new HushweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA).noCollission().noOcclusion()));
    public static final DeferredHolder<MobEffect, DreadMobEffect> DREAD = MOB_EFFECTS.register("dread", DreadMobEffect::new);
    public static final DeferredHolder<MobEffect, ParalyzedMobEffect> PARALYZED = MOB_EFFECTS.register("paralyzed", ParalyzedMobEffect::new);
    public static final DeferredHolder<MobEffect, InvertedMobEffect> INVERTED = MOB_EFFECTS.register("inverted", InvertedMobEffect::new);
    public static final DeferredHolder<MobEffect, com.craisinlord.antarchy.content.effect.BloodglassWardEffect> BLOODGLASS_WARD = MOB_EFFECTS.register("bloodglass_ward", com.craisinlord.antarchy.content.effect.BloodglassWardEffect::new);
    private static final DeferredHolder<Attribute, Attribute> BLOODGLASS_MAX_HEARTS = ATTRIBUTES.register(
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
    public static final DeferredHolder<Fluid, Fluid> ICHOR = FLUIDS.register("ichor",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Source(
                    () -> lookupFlowingFluid("ichor"),
                    () -> lookupFlowingFluid("flowing_ichor"),
                    () -> lookupItem("ichor_bucket"),
                    "ichor",
                    4,
                    1,
                    5
            ));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_ICHOR = FLUIDS.register("flowing_ichor",
            () -> new com.craisinlord.antarchy.fabric.content.fluid.SimpleFluid.Flowing(
                    () -> lookupFlowingFluid("ichor"),
                    () -> lookupFlowingFluid("flowing_ichor"),
                    () -> lookupItem("ichor_bucket"),
                    "ichor",
                    4,
                    1,
                    5
            ));
    public static final DeferredBlock<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            () -> new LiquidBlock((net.minecraft.world.level.material.FlowingFluid) ICHOR.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredHolder<Fluid, Fluid> ANTIWATER = FLUIDS.register("antiwater",
            AntiwaterFluid.Source::new);
    public static final DeferredHolder<Fluid, Fluid> FLOWING_ANTIWATER = FLUIDS.register("flowing_antiwater",
            AntiwaterFluid.Flowing::new);
    public static final DeferredBlock<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            () -> new AntiwaterLiquidBlock((net.minecraft.world.level.material.FlowingFluid) ANTIWATER.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredHolder<EntityType<?>, EntityType<RedAntEntity>> RED_ANT = ENTITY_TYPES.register("red_ant",
            () -> buildAntType(RedAntEntity::new, MobCategory.CREATURE, "red_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrownAntEntity>> BROWN_ANT = ENTITY_TYPES.register("brown_ant",
            () -> buildAntType(BrownAntEntity::new, MobCategory.CREATURE, "brown_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<RainbowAntEntity>> RAINBOW_ANT = ENTITY_TYPES.register("rainbow_ant",
            () -> buildAntType(RainbowAntEntity::new, MobCategory.CREATURE, "rainbow_ant"));
    public static final DeferredHolder<EntityType<?>, EntityType<MolewormEntity>> MOLEWORM = ENTITY_TYPES.register("moleworm",
            () -> EntityType.Builder.of(MolewormEntity::new, MobCategory.MONSTER)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(8)
                    .build("moleworm"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantisEntity>> MANTIS = ENTITY_TYPES.register("mantis",
            () -> EntityType.Builder.of(MantisEntity::new, MobCategory.MONSTER)
                    .sized(3.125F, 2.5F)
                    .clientTrackingRange(8)
                    .build("mantis"));
    public static final DeferredHolder<EntityType<?>, EntityType<OuranwoodBoatEntity>> OURANWOOD_BOAT_ENTITY = ENTITY_TYPES.register("ouranwood_boat",
            () -> EntityType.Builder.<OuranwoodBoatEntity>of(OuranwoodBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("ouranwood_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<OuranwoodChestBoatEntity>> OURANWOOD_CHEST_BOAT_ENTITY = ENTITY_TYPES.register("ouranwood_chest_boat",
            () -> EntityType.Builder.<OuranwoodChestBoatEntity>of(OuranwoodChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("ouranwood_chest_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<MolevoreEntity>> MOLEVORE = ENTITY_TYPES.register("molevore",
            () -> EntityType.Builder.of(MolevoreEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 1.1F)
                    .clientTrackingRange(10)
                    .build("molevore"));
    public static final DeferredHolder<EntityType<?>, EntityType<TriffidEntity>> TRIFFID = ENTITY_TYPES.register("triffid",
            () -> EntityType.Builder.of(TriffidEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 5.0F)
                    .clientTrackingRange(10)
                    .build("triffid"));
    public static final DeferredHolder<EntityType<?>, EntityType<BedBugEntity>> BED_BUG = ENTITY_TYPES.register("bed_bug",
            () -> EntityType.Builder.of(BedBugEntity::new, MobCategory.CREATURE)
                    .sized(1.15F, 0.8F)
                    .clientTrackingRange(8)
                    .build("bed_bug"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaspEntity>> WASP = ENTITY_TYPES.register("wasp",
            () -> EntityType.Builder.of(WaspEntity::new, MobCategory.MONSTER)
                    .sized(0.8625F, 1.365F)
                    .clientTrackingRange(8)
                    .build("wasp"));
    public static final DeferredHolder<EntityType<?>, EntityType<BomberEntity>> BOMBER = ENTITY_TYPES.register("bomber",
            () -> EntityType.Builder.of(BomberEntity::new, MobCategory.MONSTER)
                    .sized(0.55F, 0.75F)
                    .clientTrackingRange(8)
                    .build("bomber"));
    public static final DeferredHolder<EntityType<?>, EntityType<JumpyBugEntity>> JUMPY_BUG = ENTITY_TYPES.register("jumpy_bug",
            () -> EntityType.Builder.of(JumpyBugEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 3.0F)
                    .clientTrackingRange(8)
                    .build("jumpy_bug"));
    public static final DeferredHolder<EntityType<?>, EntityType<SizeRayProjectileEntity>> SHRINK_RAY_PROJECTILE = ENTITY_TYPES.register("shrink_ray_projectile",
            () -> EntityType.Builder.<SizeRayProjectileEntity>of(SizeRayProjectileEntity::createShrink, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("shrink_ray_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SizeRayProjectileEntity>> GROWTH_RAY_PROJECTILE = ENTITY_TYPES.register("growth_ray_projectile",
            () -> EntityType.Builder.<SizeRayProjectileEntity>of(SizeRayProjectileEntity::createGrowth, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("growth_ray_projectile"));
    public static final DeferredBlock<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            () -> new AntNestBlock(
                    RED_ANT,
                    AntarchyFabricContent::antNestBlockEntityType,
                    true,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));
    public static final DeferredBlock<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            () -> new AntNestBlock(BROWN_ANT, AntarchyFabricContent::antNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noLootTable()));
    public static final DeferredBlock<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            () -> new AntNestBlock(RAINBOW_ANT, AntarchyFabricContent::antNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noLootTable()));
    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> createOre(Blocks.EMERALD_ORE, 4, 8));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8));
    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.register("titanium_ore",
            () -> createOre(Blocks.DIAMOND_ORE, 4, 8));
    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.register("deepslate_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8));
    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.register("uranium_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK));
    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> createStorageBlock(Blocks.DIAMOND_BLOCK));
    public static final DeferredBlock<Block> RAW_URANIUM_BLOCK = BLOCKS.register("raw_uranium_block",
            () -> createRawStorageBlock(Blocks.RAW_GOLD_BLOCK));
    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = BLOCKS.register("raw_titanium_block",
            () -> createRawStorageBlock(Blocks.RAW_IRON_BLOCK));
    public static final DeferredBlock<Block> CUT_URANIUM = BLOCKS.register("cut_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER));
    public static final DeferredBlock<Block> CUT_TITANIUM = BLOCKS.register("cut_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_URANIUM_SLAB = BLOCKS.register("cut_uranium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_COPPER_SLAB)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_TITANIUM_SLAB = BLOCKS.register("cut_titanium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_COPPER_SLAB)));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_URANIUM_STAIRS = BLOCKS.register("cut_uranium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_URANIUM.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_COPPER_STAIRS)));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_TITANIUM_STAIRS = BLOCKS.register("cut_titanium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_TITANIUM.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_COPPER_STAIRS)));
    public static final DeferredBlock<Block> CHISELED_URANIUM = BLOCKS.register("chiseled_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_COPPER));
    public static final DeferredBlock<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_COPPER));
    public static final DeferredBlock<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BULB)));
    public static final DeferredBlock<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BULB)));
    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> URANIUM_DOOR = BLOCKS.register("uranium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> TITANIUM_DOOR = BLOCKS.register("titanium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> URANIUM_TRAPDOOR = BLOCKS.register("uranium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> TITANIUM_TRAPDOOR = BLOCKS.register("titanium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_TRAPDOOR)));
    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> URANIUM_BARS = BLOCKS.register("uranium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)));
    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> TITANIUM_BARS = BLOCKS.register("titanium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)));
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> ANTIMETAL = BLOCKS.register("antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT)));
    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_BASALT)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            () -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING)));
    public static final DeferredBlock<CorneaStalkBlock> CORNEA_STALK = BLOCKS.register("cornea_stalk",
            () -> new CorneaStalkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).randomTicks()));
    public static final DeferredBlock<Block> FALLEN_KING_CROWN_BLOCK = BLOCKS.register("fallen_king_crown",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<AmethystClusterBlock> SMALL_BLOOD_CRYSTAL_BUD = BLOCKS.register("small_blood_crystal_bud",
            () -> new AmethystClusterBlock(3.0F, 4.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD)));
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_BLOOD_CRYSTAL_BUD = BLOCKS.register("medium_blood_crystal_bud",
            () -> new AmethystClusterBlock(4.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.MEDIUM_AMETHYST_BUD)));
    public static final DeferredBlock<AmethystClusterBlock> LARGE_BLOOD_CRYSTAL_BUD = BLOCKS.register("large_blood_crystal_bud",
            () -> new AmethystClusterBlock(5.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.LARGE_AMETHYST_BUD)));
    public static final DeferredBlock<Block> BUDDING_BLOOD_CRYSTAL = BLOCKS.register("budding_blood_crystal",
            () -> new BuddingBloodCrystalBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST),
                    AntarchyFabricContent::smallBloodCrystalBudBlock,
                    AntarchyFabricContent::mediumBloodCrystalBudBlock,
                    AntarchyFabricContent::largeBloodCrystalBudBlock,
                    AntarchyFabricContent::bloodCrystalCrystalBlock
            ));
    public static final DeferredBlock<Block> BLOOD_CRYSTAL = BLOCKS.register("blood_crystal_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)));
    public static final DeferredBlock<AmethystClusterBlock> BLOOD_CRYSTAL_CRYSTAL = BLOCKS.register("blood_crystal_cluster",
            () -> new AmethystClusterBlock(7.0F, 3.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)));
    public static final DeferredBlock<Block> DREAM_SAND = BLOCKS.register("dream_sand",
            () -> new DreamSandBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)));
    public static final DeferredBlock<Block> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredBlock<Block> CHISELED_DREAM_SANDSTONE = BLOCKS.register("chiseled_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_SANDSTONE)));
    public static final DeferredBlock<Block> CUT_DREAM_SANDSTONE = BLOCKS.register("cut_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_SANDSTONE)));
    public static final DeferredBlock<Block> SMOOTH_DREAM_SANDSTONE = BLOCKS.register("smooth_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE)));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> DREAM_SANDSTONE_STAIRS = BLOCKS.register("dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE_STAIRS)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> DREAM_SANDSTONE_SLAB = BLOCKS.register("dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE_SLAB)));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> DREAM_SANDSTONE_WALL = BLOCKS.register("dream_sandstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE_WALL)));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SMOOTH_DREAM_SANDSTONE_STAIRS = BLOCKS.register("smooth_dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SMOOTH_DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE_STAIRS)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SMOOTH_DREAM_SANDSTONE_SLAB = BLOCKS.register("smooth_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_SANDSTONE_SLAB)));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_DREAM_SANDSTONE_SLAB = BLOCKS.register("cut_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CUT_SANDSTONE_SLAB)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> BlockEntityType.Builder.of(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dream_campfire",
            () -> BlockEntityType.Builder.of(
                    DreamCampfireBlockEntity::new,
                    DREAM_CAMPFIRE.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("wasp_nest",
            () -> BlockEntityType.Builder.of(
                    WaspNestBlockEntity::new,
                    WASP_NEST.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("hushweed",
            () -> BlockEntityType.Builder.of(
                    HushweedBlockEntity::new,
                    HUSHWEED.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotentNyxiteBlockEntity>> POTENT_NYXITE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("potent_nyxite",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new PotentNyxiteBlockEntity(pos, state, AntarchyFabricContent::potentNyxiteBlockEntityType),
                    POTENT_NYXITE.get()
            ).build(null));
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
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_FOREST_FLORA = FEATURES.register("elythia_forest_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FOREST));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_MEADOW_FLORA = FEATURES.register("elythia_meadow_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.MEADOW));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> FLOWER_FOREST_MILKWEED = FEATURES.register("flower_forest_milkweed",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.FLOWER_FOREST_MILKWEED));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_BUTTERFLY_FIELDS_FLORA = FEATURES.register("butterfly_fields_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.BUTTERFLY_FIELDS));
    public static final DeferredHolder<Feature<?>, ElythiaFloraFeature> ELYTHIA_TORCHFLOWER_FIELDS_FLORA = FEATURES.register("elythia_torchflower_fields_flora",
            () -> new ElythiaFloraFeature(NoneFeatureConfiguration.CODEC, ElythiaFloraFeature.Variant.TORCHFLOWER_FIELDS));
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
    public static final DeferredHolder<Feature<?>, ElythiaTuffBoulderFeature> ELYTHIA_TUFF_BOULDER = FEATURES.register("elythia_tuff_boulder",
            () -> new ElythiaTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ElythiaLargeTuffBoulderFeature> ELYTHIA_LARGE_TUFF_BOULDER = FEATURES.register("elythia_large_tuff_boulder",
            () -> new ElythiaLargeTuffBoulderFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisFissureFeature> THORAXIS_FISSURE = FEATURES.register("thoraxis_fissure",
            () -> new ThoraxisFissureFeature(ThoraxisFissureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisRibColumnsFeature> THORAXIS_RIB_COLUMNS = FEATURES.register("thoraxis_rib_columns",
            () -> new ThoraxisRibColumnsFeature(ThoraxisRibColumnsConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisSpikeFeature> THORAXIS_SPIKE = FEATURES.register("thoraxis_spike",
            () -> new ThoraxisSpikeFeature(ThoraxisSpikeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, NyxiteSpikeFeature> NYXITE_SPIKES = FEATURES.register("nyxite_spikes",
            () -> new NyxiteSpikeFeature(NyxiteSpikeConfiguration.CODEC));
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
    public static final DeferredHolder<Feature<?>, ThoraxisAntiwaterPoolFeature> THORAXIS_ANTIWATER_POOL = FEATURES.register("thoraxis_antiwater_pool",
            () -> new ThoraxisAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, LucidAntiwaterPoolFeature> LUCID_ANTIWATER_POOL = FEATURES.register("lucid_antiwater_pool",
            () -> new LucidAntiwaterPoolFeature(ThoraxisAntiwaterPoolConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, ThoraxisDuneFeature> THORAXIS_DUNE = FEATURES.register("thoraxis_dune",
            () -> new ThoraxisDuneFeature(ThoraxisDuneConfiguration.CODEC));
    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<ThoraxisBiomeSource>> THORAXIS_BIOME_SOURCE = BIOME_SOURCES.register("thoraxis_biome_source",
            () -> ThoraxisBiomeSource.CODEC);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_LOG_ITEM = ITEMS.registerSimpleBlockItem(DUPLICATOR_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(STRIPPED_OURANWOOD_LOG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> STRIPPED_OURANWOOD_WOOD_ITEM = ITEMS.registerSimpleBlockItem(STRIPPED_OURANWOOD_WOOD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_PLANKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_SLAB_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_FENCE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_FENCE_GATE_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_DOOR_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_PRESSURE_PLATE_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_PRESSURE_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_BUTTON_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_BUTTON);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(OURANWOOD_LEAVES);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> OURANWOOD_ACORN = ITEMS.registerSimpleBlockItem(OURANWOOD_ACORN_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ORANGE_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(ORANGE_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PINK_MILKWEED_ITEM = ITEMS.registerSimpleBlockItem(PINK_MILKWEED);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TORCHFLOWER_BUSH_ITEM = ITEMS.registerSimpleBlockItem(TORCHFLOWER_BUSH);
    public static final DeferredItem<net.minecraft.world.item.SignItem> OURANWOOD_SIGN_ITEM = ITEMS.register("ouranwood_sign",
            () -> new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), OURANWOOD_SIGN.get(), OURANWOOD_WALL_SIGN.get()));
    public static final DeferredItem<net.minecraft.world.item.HangingSignItem> OURANWOOD_HANGING_SIGN_ITEM = ITEMS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.item.HangingSignItem(OURANWOOD_HANGING_SIGN.get(), OURANWOOD_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> OURANWOOD_BOAT = ITEMS.register("ouranwood_boat",
            () -> new OuranwoodBoatOnlyItem(OURANWOOD_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> OURANWOOD_CHEST_BOAT = ITEMS.register("ouranwood_chest_boat",
            () -> new OuranwoodChestBoatItem(OURANWOOD_CHEST_BOAT_ENTITY.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUPLICATOR_SAPLING_ITEM = ITEMS.registerSimpleBlockItem(DUPLICATOR_SAPLING);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RED_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(RED_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BROWN_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(BROWN_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAINBOW_ANT_NEST_ITEM = ITEMS.registerSimpleBlockItem(RAINBOW_ANT_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(URANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_URANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_URANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(TITANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DEEPSLATE_TITANIUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_TITANIUM_ORE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(URANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TITANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_URANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(RAW_URANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAW_TITANIUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(RAW_TITANIUM_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(CUT_URANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(CUT_TITANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(CUT_URANIUM_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_SLAB_ITEM = ITEMS.registerSimpleBlockItem(CUT_TITANIUM_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_URANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(CUT_URANIUM_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_TITANIUM_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(CUT_TITANIUM_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_URANIUM_ITEM = ITEMS.registerSimpleBlockItem(CHISELED_URANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem(CHISELED_TITANIUM);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BULB_ITEM = ITEMS.register("uranium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(URANIUM_BULB.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BULB_ITEM = ITEMS.register("titanium_bulb",
            () -> new com.craisinlord.antarchy.content.item.SignalSavingBulbItem(TITANIUM_BULB.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(URANIUM_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem(TITANIUM_DOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(URANIUM_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(TITANIUM_TRAPDOOR);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> URANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(URANIUM_BARS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TITANIUM_BARS_ITEM = ITEMS.registerSimpleBlockItem(TITANIUM_BARS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_ITEM = ITEMS.register("antimetal",
            () -> new AntimetalBlockItem(ANTIMETAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_ANTIMETAL_ITEM = ITEMS.register("polished_antimetal",
            () -> new AntimetalBlockItem(POLISHED_ANTIMETAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANTIMETAL_SCAFFOLDING_ITEM = ITEMS.register("antimetal_scaffolding",
            () -> new AntimetalScaffoldingItem(ANTIMETAL_SCAFFOLDING.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMALL_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(SMALL_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MEDIUM_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(MEDIUM_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LARGE_BLOOD_CRYSTAL_BUD_ITEM = ITEMS.registerSimpleBlockItem(LARGE_BLOOD_CRYSTAL_BUD);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BUDDING_BLOOD_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(BUDDING_BLOOD_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_ITEM = ITEMS.register("blood_crystal_block",
            () -> new net.minecraft.world.item.BlockItem(BLOOD_CRYSTAL.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BLOOD_CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem(BLOOD_CRYSTAL_CRYSTAL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SAND_ITEM = ITEMS.registerSimpleBlockItem(DREAM_SAND);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(CHISELED_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(CUT_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_ITEM = ITEMS.registerSimpleBlockItem(SMOOTH_DREAM_SANDSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(DREAM_SANDSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_SANDSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(DREAM_SANDSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(SMOOTH_DREAM_SANDSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SMOOTH_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(SMOOTH_DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CUT_DREAM_SANDSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(CUT_DREAM_SANDSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DUCT_TAPE_ITEM = ITEMS.register("duct_tape",
            () -> new DuctTapeBlockItem(DUCT_TAPE.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_ROOTED_DIRT_ITEM = ITEMS.registerSimpleBlockItem(INFESTED_ROOTED_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> INFESTED_COARSE_DIRT_ITEM = ITEMS.registerSimpleBlockItem(INFESTED_COARSE_DIRT);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_ITEM = ITEMS.registerSimpleBlockItem(NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(CHISELED_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_NYXITE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_NYXITE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_NYXITE_WALL_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_NYXITE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHISELED_SHELLSTONE_ITEM = ITEMS.registerSimpleBlockItem(CHISELED_SHELLSTONE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CRACKED_SHELLSTONE_BRICKS_ITEM = ITEMS.registerSimpleBlockItem(CRACKED_SHELLSTONE_BRICKS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_SHELLSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_SHELLSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MOSSY_SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(MOSSY_SHELLSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_SHELLSTONE_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_SLAB_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_SHELLSTONE_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POLISHED_SHELLSTONE_WALL_ITEM = ITEMS.registerSimpleBlockItem(POLISHED_SHELLSTONE_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_STAIRS_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_BRICK_STAIRS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_SLAB_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_BRICK_SLAB);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> SHELLSTONE_BRICK_WALL_ITEM = ITEMS.registerSimpleBlockItem(SHELLSTONE_BRICK_WALL);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TRIFFID_GOO_BLOCK_ITEM = ITEMS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.item.TriffidGooBlockItem(TRIFFID_GOO_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PALE_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(PALE_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> NYXITE_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(NYXITE_SPIKE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> POTENT_NYXITE_ITEM = ITEMS.registerSimpleBlockItem(POTENT_NYXITE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(UMBRAL_MOSS_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> UMBRAL_MOSS_CARPET_ITEM = ITEMS.registerSimpleBlockItem(UMBRAL_MOSS_CARPET);
    public static final DeferredItem<StandingAndWallBlockItem> DREAM_TORCH_ITEM = ITEMS.register("dream_torch",
            () -> new StandingAndWallBlockItem(DREAM_TORCH.get(), DREAM_WALL_TORCH.get(), new Item.Properties(), Direction.UP));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_LANTERN_ITEM = ITEMS.registerSimpleBlockItem(DREAM_LANTERN);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> DREAM_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem(DREAM_CAMPFIRE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CREEPING_HORROR_EGGS_ITEM = ITEMS.registerSimpleBlockItem(CREEPING_HORROR_EGGS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LURKING_TERROR_EGGS_ITEM = ITEMS.registerSimpleBlockItem(LURKING_TERROR_EGGS);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BED_BUG_EGG_ITEM = ITEMS.registerSimpleBlockItem(BED_BUG_EGG);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WASP_NEST_ITEM = ITEMS.registerSimpleBlockItem(WASP_NEST);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> HUSHWEED_ITEM = ITEMS.registerSimpleBlockItem(HUSHWEED);
    public static final DeferredItem<BucketItem> ICHOR_BUCKET = ITEMS.register("ichor_bucket",
            () -> new BucketItem(ICHOR.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> ANTIWATER_BUCKET = ITEMS.register("antiwater_bucket",
            () -> new BucketItem(ANTIWATER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.CloudBucketItem> CLOUD_BUCKET = ITEMS.register("cloud_bucket",
            () -> new com.craisinlord.antarchy.content.item.CloudBucketItem(CLOUD_BLOCK.get(), new Item.Properties().craftRemainder(Items.BUCKET)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.BloodCrystalShardItem> BLOOD_CRYSTAL_SHARD = ITEMS.register("blood_crystal_shard",
            () -> new com.craisinlord.antarchy.content.item.BloodCrystalShardItem(new Item.Properties()));
    public static final DeferredItem<Item> BLOOD_CRYSTAL_APPLE = ITEMS.register("blood_crystal_apple",
            () -> new BloodCrystalAppleItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(1.2f)
                            .effect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1), 1.0f)
                            .alwaysEdible()
                            .build())));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_HELMET = ITEMS.register("blood_crystal_helmet",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_CHESTPLATE = ITEMS.register("blood_crystal_chestplate",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_LEGGINGS = ITEMS.register("blood_crystal_leggings",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalArmorItem> BLOOD_CRYSTAL_BOOTS = ITEMS.register("blood_crystal_boots",
            () -> new BloodCrystalArmorItem(net.minecraft.core.Holder.direct(BLOOD_CRYSTAL_ARMOR_MATERIAL.get()), ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<BloodCrystalKatanaItem> BLOOD_CRYSTAL_KATANA = ITEMS.register("blood_crystal_katana",
            () -> new BloodCrystalKatanaItem(
                    BLOOD_CRYSTAL_KATANA_TIER,
                    new Item.Properties().stacksTo(1).durability(1200).rarity(Rarity.RARE),
                    AntarchySettings.bloodCrystalKatanaAttackDamage(),
                    -2.2F
            ));
    public static final DeferredItem<Item> MANTIS_CLAW = ITEMS.registerSimpleItem("mantis_claw", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> KING_SCALE = ITEMS.register("king_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> QUEEN_SCALE = ITEMS.register("queen_scale",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> JUMPY_BUG_LEG = ITEMS.registerSimpleItem("jumpy_bug_leg", new Item.Properties().rarity(Rarity.UNCOMMON));
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> JUMPY_BOOTS_ARMOR_MATERIAL = ARMOR_MATERIALS.register("jumpy_boots",
            () -> new ArmorMaterial(
                    createJumpyBootsDefense(),
                    15,
                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(net.minecraft.world.item.Items.NETHERITE_BOOTS),
                    java.util.List.of(new ArmorMaterial.Layer(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_boots"))),
                    3.0F,
                    0.1F
            ));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.JumpyBootsItem> JUMPY_BOOTS = ITEMS.register("jumpy_boots",
            () -> new com.craisinlord.antarchy.content.item.JumpyBootsItem(
                    net.minecraft.core.Holder.direct(JUMPY_BOOTS_ARMOR_MATERIAL.get()),
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))
            ));
    public static final DeferredItem<Item> BRUTALFLY_WING = ITEMS.registerSimpleItem("brutalfly_wing", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BrutalflyElytraItem> BRUTALFLY_ELYTRA = ITEMS.register("brutalfly_elytra",
            () -> new BrutalflyElytraItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(480)));
    public static final DeferredItem<Item> CORNEA_EAR = ITEMS.register("cornea_ear",
            () -> new CorneaEarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.4F)
                            .build())));
    public static final DeferredItem<Item> TRIFFID_GOO = ITEMS.registerSimpleItem("triffid_goo",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> VORTEX_EYE = ITEMS.register("vortex_eye",
            () -> new MobComingSoonTooltipItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> RAW_URANIUM_SCRAP = ITEMS.registerSimpleItem("raw_uranium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM_SCRAP = ITEMS.registerSimpleItem("raw_titanium_scrap", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MUD_PIE = ITEMS.register("mud_pie",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.9F)
                            .effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1.0F)
                            .build())));
    public static final DeferredItem<RainbowSugarItem> RAINBOW_SUGAR = ITEMS.register("rainbow_sugar",
            () -> new RainbowSugarItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.0F).alwaysEdible().build())));
    public static final DeferredItem<Item> URANIUM_NUGGET = ITEMS.registerSimpleItem("uranium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot", new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
    public static final DeferredItem<Item> MOLEVORE_NOSE = ITEMS.registerSimpleItem("molevore_nose", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> MOLEWORM_ITEM = ITEMS.register("moleworm",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.0F)
                            .effect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)
                            .build())));
    public static final DeferredItem<Item> CLOUD_SHARK_FIN = ITEMS.registerSimpleItem("cloud_shark_fin", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CloudSharkFinSoupItem> CLOUD_SHARK_FIN_SOUP = ITEMS.register("cloud_shark_fin_soup",
            () -> new CloudSharkFinSoupItem(new Item.Properties()
                    .stacksTo(1)
                    .craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder()
                            .nutrition(10)
                            .saturationModifier(0.8F)
                            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
                            .effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0), 1.0F)
                            .build())));
    public static final DeferredItem<Item> KRAKEN_TOOTH = ITEMS.register("kraken_tooth",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredItem<Item> BASILISK_FANG = ITEMS.registerSimpleItem("basilisk_fang", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BasiliskDaggerItem> BASILISK_DAGGER = ITEMS.register("basilisk_dagger",
            () -> new BasiliskDaggerItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE), 4, -1.8F));
    public static final DeferredItem<Item> EMPEROR_SCORPION_STINGER = ITEMS.registerSimpleItem("emperor_scorpion_stinger", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<ScorpionWhipItem> SCORPION_WHIP = ITEMS.register("scorpion_whip",
            () -> new ScorpionWhipItem(Tiers.IRON, new Item.Properties().rarity(Rarity.RARE).durability(384)));
    public static final DeferredItem<MogglesItem> MOGGLES = ITEMS.register("moggles",
            () -> new MogglesItem(net.minecraft.core.Holder.direct(MOGGLES_ARMOR_MATERIAL.get()), new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final DeferredItem<com.craisinlord.antarchy.content.item.FallenKingCrownItem> FALLEN_KING_CROWN = ITEMS.register("fallen_king_crown",
            () -> new com.craisinlord.antarchy.content.item.FallenKingCrownItem(
                    net.minecraft.core.Holder.direct(FALLEN_KING_CROWN_ARMOR_MATERIAL.get()),
                    new Item.Properties().rarity(Rarity.RARE)
            ));
    public static final DeferredItem<ArmorItem> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.HELMET.getDurability(41))
            ));
    public static final DeferredItem<ArmorItem> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(41))
            ));
    public static final DeferredItem<ArmorItem> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(41))
            ));
    public static final DeferredItem<ArmorItem> ULTIMATE_BOOTS = ITEMS.register("ultimate_boots",
            () -> new UltimateArmorItem(
                    net.minecraft.core.Holder.direct(ULTIMATE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.BOOTS.getDurability(41))
            ));
    public static final DeferredItem<UltimateSwordItem> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            () -> new UltimateSwordItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateSwordAttackDamage,
                    -2.4F
            ));
    public static final DeferredItem<UltimatePickaxeItem> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            () -> new UltimatePickaxeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimatePickaxeAttackDamage,
                    -2.8F
            ));
    public static final DeferredItem<UltimateAxeItem> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            () -> new UltimateAxeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateAxeAttackDamage,
                    -3.0F
            ));
    public static final DeferredItem<UtlimateShovelItem> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            () -> new UtlimateShovelItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateShovelAttackDamage,
                    -3.0F
            ));
    public static final DeferredItem<UltimateHoeItem> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            () -> new UltimateHoeItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    AntarchySettings::ultimateHoeAttackDamage,
                    0.0F
            ));
    public static final DeferredItem<UltimateBowItem> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            () -> new UltimateBowItem(new Item.Properties().stacksTo(1).durability(768).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<UltimateCrossbowItem> ULTIMATE_CROSSBOW = ITEMS.register("ultimate_crossbow",
            () -> new UltimateCrossbowItem(new Item.Properties().stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));
//    public static final DeferredItem<UltimateMaceItem> ULTIMATE_MACE = ITEMS.register("ultimate_mace",
//            () -> new UltimateMaceItem(new Item.Properties().stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_HELMET = ITEMS.register("nightmare_helmet",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.HELMET.getDurability(41))
            ));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_CHESTPLATE = ITEMS.register("nightmare_chestplate",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(41))
            ));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_LEGGINGS = ITEMS.register("nightmare_leggings",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(41))
            ));
    public static final DeferredItem<NightmareArmorItem> NIGHTMARE_BOOTS = ITEMS.register("nightmare_boots",
            () -> new NightmareArmorItem(
                    net.minecraft.core.Holder.direct(NIGHTMARE_ARMOR_MATERIAL.get()),
                    ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .durability(ArmorItem.Type.BOOTS.getDurability(41))
            ));
    public static final DeferredItem<NightmareSwordItem> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            () -> new NightmareSwordItem(
                    ULTIMATE_TIER,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(),
                    -2.4F
            ));
    public static final DeferredItem<SizeRayItem> SHRINK_RAY = ITEMS.register("shrink_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    SHRINK_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.SHRINK,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/shrink_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/shrink_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/shrink_ray.animation.json"),
                    "shrink_ray_active"
            ));
    public static final DeferredItem<SizeRayItem> GROWTH_RAY = ITEMS.register("growth_ray",
            () -> new SizeRayItem(
                    new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant(),
                    GROWTH_RAY_PROJECTILE,
                    SizeRayProjectileEntity.SizeRayType.GROWTH,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/growth_ray.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/growth_ray.png"),
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/growth_ray.animation.json"),
                    "growth_ray_active"
            ));
    public static final DeferredItem<GravityGunItem> GRAVITY_GUN = ITEMS.register("gravity_gun",
            () -> new GravityGunItem(new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE).fireResistant()));
    public static final DeferredItem<SquidzookaItem> SQUIDZOOKA = ITEMS.register("squidzooka",
            () -> new SquidzookaItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.RARE)));
    public static final DeferredItem<BattleAxeItem> BATTLE_AXE = ITEMS.register("battle_axe",
            () -> new BattleAxeItem(
                    Tiers.NETHERITE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(),
                    AntarchySettings::battleAxeAttackDamage,
                    -3.1F
            ));
    public static final DeferredHolder<EntityType<?>, EntityType<DiamondMinecartEntity>> DIAMOND_MINECART = ENTITY_TYPES.register("diamond_minecart",
            () -> EntityType.Builder.<DiamondMinecartEntity>of((entityType, level) -> new DiamondMinecartEntity(entityType, level, diamondMinecartItem()), MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(8)
                    .build("diamond_minecart"));
    public static final DeferredItem<DiamondMinecartItem> DIAMOND_MINECART_ITEM = ITEMS.register("diamond_minecart",
            () -> new DiamondMinecartItem(DIAMOND_MINECART, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<ReverieBottleItem> REVERIE_BOTTLE = ITEMS.register("reverie_bottle",
            () -> new ReverieBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BIG_BERTHA_BLADE = ITEMS.registerSimpleItem(
            "big_bertha_blade",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );
    public static final DeferredItem<Item> BIG_BERTHA_HANDLE = ITEMS.registerSimpleItem(
            "big_bertha_handle",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );
    public static final DeferredItem<Item> BIG_BERTHA_HILT = ITEMS.registerSimpleItem(
            "big_bertha_hilt",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant()
    );
    public static final DeferredItem<BigBerthaItem> BIG_BERTHA = ITEMS.register("big_bertha",
            () -> new BigBerthaItem(
                    Tiers.NETHERITE,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
                            .fireResistant()
            ));

    public static final DeferredHolder<EntityType<?>, EntityType<EasterBunnyEntity>> EASTER_BUNNY = ENTITY_TYPES.register("easter_bunny",
            () -> EntityType.Builder.of(EasterBunnyEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.0F)
                    .clientTrackingRange(8)
                    .build("easter_bunny"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingSquirrelEntity>> FLYING_SQUIRREL = ENTITY_TYPES.register("flying_squirrel",
            () -> EntityType.Builder.of(FlyingSquirrelEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(8)
                    .build("flying_squirrel"));
    public static final DeferredHolder<EntityType<?>, EntityType<CaterpillarEntity>> CATERPILLAR = ENTITY_TYPES.register("caterpillar",
            () -> EntityType.Builder.of(CaterpillarEntity::new, MobCategory.CREATURE)
                    .sized(0.8775F, 0.585F)
                    .clientTrackingRange(8)
                    .build("caterpillar"));
    public static final DeferredHolder<EntityType<?>, EntityType<ButterflyEntity>> BUTTERFLY = ENTITY_TYPES.register("butterfly",
            () -> EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                    .sized(1.125F, 1.375F)
                    .clientTrackingRange(8)
                    .build("butterfly"));
    public static final DeferredHolder<EntityType<?>, EntityType<ReverieEntity>> REVERIE = ENTITY_TYPES.register("reverie",
            () -> EntityType.Builder.of(ReverieEntity::new, MobCategory.AMBIENT)
                    .sized(0.7F, 0.7F)
                    .clientTrackingRange(8)
                    .build("reverie"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrutalflyEntity>> BRUTALFLY = ENTITY_TYPES.register("brutalfly",
            () -> EntityType.Builder.of(BrutalflyEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 2.1F)
                    .clientTrackingRange(12)
                    .build("brutalfly"));
    public static final DeferredHolder<EntityType<?>, EntityType<AppleCow>> APPLE_COW = ENTITY_TYPES.register("apple_cow",
            () -> buildCowType(AppleCow::new, "apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<GoldenAppleCow>> GOLDEN_APPLE_COW = ENTITY_TYPES.register("golden_apple_cow",
            () -> buildCowType(GoldenAppleCow::new, "golden_apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedGoldenAppleCow>> ENCHANTED_GOLDEN_APPLE_COW = ENTITY_TYPES.register("enchanted_golden_apple_cow",
            () -> buildCowType(EnchantedGoldenAppleCow::new, "enchanted_golden_apple_cow"));
    public static final DeferredHolder<EntityType<?>, EntityType<DrTrayaurusEntity>> DR_TRAYAURUS = ENTITY_TYPES.register("dr_trayaurus",
            () -> EntityType.Builder.of(DrTrayaurusEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("dr_trayaurus"));
    public static final DeferredHolder<EntityType<?>, EntityType<CloudSharkEntity>> CLOUD_SHARK = ENTITY_TYPES.register("cloud_shark",
            () -> EntityType.Builder.of(CloudSharkEntity::new, MobCategory.MONSTER)
                    .sized(2.4F, 0.9F)
                    .clientTrackingRange(10)
                    .build("cloud_shark"));
    public static final DeferredHolder<EntityType<?>, EntityType<KrakenEntity>> KRAKEN = ENTITY_TYPES.register("kraken",
            () -> EntityType.Builder.of(KrakenEntity::new, MobCategory.MONSTER)
                    .sized(5.2F, 9.8F)
                    .clientTrackingRange(12)
                    .build("kraken"));
    public static final DeferredHolder<EntityType<?>, EntityType<MissileSquidEntity>> MISSILE_SQUID = ENTITY_TYPES.register("missile_squid",
            () -> EntityType.Builder.of(MissileSquidEntity::new, MobCategory.MONSTER)
                    .sized(1.62F, 3.18F)
                    .clientTrackingRange(10)
                    .build("missile_squid"));
    public static final DeferredHolder<EntityType<?>, EntityType<NightmareEntity>> NIGHTMARE = ENTITY_TYPES.register("nightmare",
            () -> EntityType.Builder.of(NightmareEntity::new, MobCategory.MONSTER)
                    .sized(3.2F, 3.8F)
                    .clientTrackingRange(12)
                    .build("nightmare"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidEntity>> LUCID = ENTITY_TYPES.register("lucid",
            () -> EntityType.Builder.of(LucidEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(10)
                    .build("lucid"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidBoltEntity>> LUCID_BOLT = ENTITY_TYPES.register("lucid_bolt",
            () -> EntityType.Builder.<LucidBoltEntity>of(LucidBoltEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("lucid_bolt"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScorpionEntity>> SCORPION = ENTITY_TYPES.register("scorpion",
            () -> EntityType.Builder.of(ScorpionEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, 1.0F)
                    .clientTrackingRange(8)
                    .build("scorpion"));
    public static final DeferredHolder<EntityType<?>, EntityType<BasiliskEntity>> BASILISK = ENTITY_TYPES.register("basilisk",
            () -> EntityType.Builder.of(BasiliskEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 3.5F)
                    .clientTrackingRange(14)
                    .build("basilisk"));
    public static final DeferredHolder<EntityType<?>, EntityType<EmperorScorpionEntity>> EMPEROR_SCORPION = ENTITY_TYPES.register("emperor_scorpion",
            () -> EntityType.Builder.of(EmperorScorpionEntity::new, MobCategory.MONSTER)
                    .sized(4.05F, 1.35F)
                    .clientTrackingRange(10)
                    .build("emperor_scorpion"));
    public static final DeferredHolder<EntityType<?>, EntityType<LucidEyeProjectileEntity>> LUCID_PEARL_PROJECTILE = ENTITY_TYPES.register("lucid_pearl_projectile",
            () -> EntityType.Builder.<LucidEyeProjectileEntity>of(LucidEyeProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("lucid_pearl_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<HushProjectileEntity>> HUSH_PROJECTILE = ENTITY_TYPES.register("hush_projectile",
            () -> EntityType.Builder.<HushProjectileEntity>of(HushProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("hush_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<BrutalflyOrbEntity>> BRUTALFLY_ORB = ENTITY_TYPES.register("brutalfly_orb",
            () -> EntityType.Builder.<BrutalflyOrbEntity>of(BrutalflyOrbEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("brutalfly_orb"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity>> UPWARD_FALLING_BLOCK = ENTITY_TYPES.register("upward_falling_block",
            () -> EntityType.Builder.<com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity>of(
                            com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("upward_falling_block"));

    public static final DeferredItem<DeferredSpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            () -> new DeferredSpawnEggItem(EASTER_BUNNY, 0xFFF2B2, 0xFF85B5, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FLYING_SQUIRREL_SPAWN_EGG = ITEMS.register("flying_squirrel_spawn_egg",
            () -> new DeferredSpawnEggItem(FLYING_SQUIRREL, 0x7D6649, 0xDCC59C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg",
            () -> new DeferredSpawnEggItem(CATERPILLAR, 0xA8D96A, 0xF4E04D, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new DeferredSpawnEggItem(BUTTERFLY, 0x111111, 0xFF7A00, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> REVERIE_SPAWN_EGG = ITEMS.register("reverie_spawn_egg",
            () -> new DeferredSpawnEggItem(REVERIE, 0xF2F2F2, 0xBFC3C7, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            () -> new DeferredSpawnEggItem(BRUTALFLY, 0x4A2214, 0xFF8A1D, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(RED_ANT, 0xA31818, 0x2B0909, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BROWN_ANT_SPAWN_EGG = ITEMS.register("brown_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(BROWN_ANT, 0x6A4320, 0x26160A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            () -> new DeferredSpawnEggItem(RAINBOW_ANT, 0x56D4F0, 0xF66DBB, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOLEWORM_SPAWN_EGG = ITEMS.register("moleworm_spawn_egg",
            () -> new DeferredSpawnEggItem(MOLEWORM, 0x7A6150, 0xD2B8A3, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            () -> new DeferredSpawnEggItem(MANTIS, 0xF8F8F2, 0x63B44A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOLEVORE_SPAWN_EGG = ITEMS.register("molevore_spawn_egg",
            () -> new DeferredSpawnEggItem(MOLEVORE, 0x3E2E24, 0xB67B4F, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> TRIFFID_SPAWN_EGG = ITEMS.register("triffid_spawn_egg",
            () -> new DeferredSpawnEggItem(TRIFFID, 0x4C8F3A, 0xFF2FB3, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(APPLE_COW, 0xFF1A1A, 0x32FF32, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(GOLDEN_APPLE_COW, 0xFFE14A, 0x32FF32, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ENCHANTED_GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_golden_apple_cow_spawn_egg",
            () -> new DeferredSpawnEggItem(ENCHANTED_GOLDEN_APPLE_COW, 0x7040B6, 0xFFE14A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DR_TRAYAURUS_SPAWN_EGG = ITEMS.register("dr_trayaurus_spawn_egg",
            () -> new DeferredSpawnEggItem(DR_TRAYAURUS, 0xB7A27B, 0x4A3D29, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> WASP_SPAWN_EGG = ITEMS.register("wasp_spawn_egg",
            () -> new DeferredSpawnEggItem(WASP, 0x111111, 0xF1D800, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BOMBER_SPAWN_EGG = ITEMS.register("bomber_spawn_egg",
            () -> new DeferredSpawnEggItem(BOMBER, 0x7A7A7A, 0xB32020, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> JUMPY_BUG_SPAWN_EGG = ITEMS.register("jumpy_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(JUMPY_BUG, 0x111111, 0xFF7A00, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CLOUD_SHARK_SPAWN_EGG = ITEMS.register("cloud_shark_spawn_egg",
            () -> new DeferredSpawnEggItem(CLOUD_SHARK, 0xDDEAF4, 0x7F96A8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new DeferredSpawnEggItem(KRAKEN, 0x163C53, 0x4F8E99, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> MISSILE_SQUID_SPAWN_EGG = ITEMS.register("missile_squid_spawn_egg",
            () -> new DeferredSpawnEggItem(MISSILE_SQUID, 0xD88FA7, 0x8D5269, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<DeferredSpawnEggItem> NIGHTMARE_SPAWN_EGG = ITEMS.register("nightmare_spawn_egg",
            () -> new DeferredSpawnEggItem(NIGHTMARE, 0x22121C, 0xB51B2D, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<DeferredSpawnEggItem> BED_BUG_SPAWN_EGG = ITEMS.register("bed_bug_spawn_egg",
            () -> new DeferredSpawnEggItem(BED_BUG, 0x3B2218, 0x611111, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LUCID_SPAWN_EGG = ITEMS.register("lucid_spawn_egg",
            () -> new DeferredSpawnEggItem(LUCID, 0xE53935, 0xF4D03F, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<DeferredSpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(SCORPION, 0xA8D8FF, 0xE04B5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            () -> new DeferredSpawnEggItem(BASILISK, 0x4A7C40, 0xD4A040, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            () -> new DeferredSpawnEggItem(EMPEROR_SCORPION, 0x1A1A0A, 0x8B2200, new Item.Properties()));
    public static final DeferredItem<LucidEyeItem> LUCID_EYE = ITEMS.register("lucid_eye",
            () -> new LucidEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LucidPearlItem> LUCID_PEARL = ITEMS.register("lucid_pearl",
            () -> new LucidPearlItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON),
                    LUCID_PEARL_PROJECTILE
            ));
    public static final DeferredItem<WaterCannonItem> WATER_CANNON = ITEMS.register("water_cannon",
            () -> new WaterCannonItem(new Item.Properties().stacksTo(1).durability(192).rarity(Rarity.RARE)));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_HELMET = ITEMS.register("primordial_helmet",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = ITEMS.register("primordial_chestplate",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = ITEMS.register("primordial_leggings",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_BOOTS = ITEMS.register("primordial_boots",
            () -> new PrimordialArmorItem(net.minecraft.core.Holder.direct(PRIMORDIAL_ARMOR_MATERIAL.get()), ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));
    public static final DeferredHolder<EntityType<?>, EntityType<ToreterrorEntity>> TORETERROR = ENTITY_TYPES.register("toreterror",
            () -> EntityType.Builder.of(ToreterrorEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.0F)
                    .clientTrackingRange(14)
                    .build("toreterror"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaterBombEntity>> WATER_BOMB = ENTITY_TYPES.register("water_bomb",
            () -> EntityType.Builder.<WaterBombEntity>of(WaterBombEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("water_bomb"));
    public static final DeferredHolder<EntityType<?>, EntityType<CreepingHorrorEntity>> CREEPING_HORROR = ENTITY_TYPES.register("creeping_horror",
            () -> EntityType.Builder.of(CreepingHorrorEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.5F)
                    .clientTrackingRange(10)
                    .build("creeping_horror"));
    public static final DeferredHolder<EntityType<?>, EntityType<LurkingTerrorEntity>> LURKING_TERROR = ENTITY_TYPES.register("lurking_terror",
            () -> EntityType.Builder.of(LurkingTerrorEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.6F)
                    .clientTrackingRange(10)
                    .build("lurking_terror"));
    public static final DeferredItem<DeferredSpawnEggItem> TORETERROR_SPAWN_EGG = ITEMS.register("toreterror_spawn_egg",
            () -> new DeferredSpawnEggItem(TORETERROR, 0x90EE90, 0x5C4033, new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<DeferredSpawnEggItem> CREEPING_HORROR_SPAWN_EGG = ITEMS.register("creeping_horror_spawn_egg",
            () -> new DeferredSpawnEggItem(CREEPING_HORROR, 0x6B3A1F, 0x6B0000, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LURKING_TERROR_SPAWN_EGG = ITEMS.register("lurking_terror_spawn_egg",
            () -> new DeferredSpawnEggItem(LURKING_TERROR, 0x2D5A1B, 0x8B0000, new Item.Properties()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANTARCHY_TAB = CREATIVE_MODE_TABS.register("antarchy",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.antarchy.antarchy"))
                    .icon(() -> new ItemStack(GRAVITY_GUN.get()))
                    .displayItems((parameters, output) -> populateCreativeTab(output))
                    .build());

    private static <T extends Cow> EntityType<T> buildCowType(EntityType.EntityFactory<T> factory, String name) {
        return EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(0.9F, 1.4F)
                .clientTrackingRange(10)
                .build(name);
    }

    private static <T extends BaseAntEntity> EntityType<T> buildAntType(EntityType.EntityFactory<T> factory, MobCategory category, String name) {
        return EntityType.Builder.of(factory, category)
                .sized(0.6375F, 0.2125F)
                .clientTrackingRange(8)
                .build(name);
    }

    private static AttributeSupplier.Builder buildAntAttributes(double health, double speed, double attackDamage) {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage);
    }

    private static Holder<MobEffect> mobEffectHolder(DeferredHolder<MobEffect, ? extends MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }

    private static Holder<Attribute> attributeHolder(DeferredHolder<Attribute, ? extends Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.get());
    }

    private static Holder<Potion> potionHolder(DeferredHolder<Potion, ? extends Potion> potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }

    private static Holder<ArmorMaterial> armorMaterialHolder(DeferredHolder<ArmorMaterial, ? extends ArmorMaterial> material) {
        return BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(material.get());
    }

    private static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(
                net.minecraft.world.entity.EntityType.PLAYER,
                net.minecraft.world.entity.player.Player.createAttributes()
                        .add(attributeHolder(DOUBLE_DAMAGE_CHANCE))
                        .add(attributeHolder(BLOODGLASS_MAX_HEARTS))
                        .build()
        );

        AttributeSupplier rabbitAttributes = Rabbit.createAttributes().build();
        FabricDefaultAttributeRegistry.register(EASTER_BUNNY.get(), rabbitAttributes);
        FabricDefaultAttributeRegistry.register(FLYING_SQUIRREL.get(), FlyingSquirrelEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CATERPILLAR.get(), CaterpillarEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(REVERIE.get(), ReverieEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BRUTALFLY.get(), BrutalflyEntity.createAttributes().build());

        AttributeSupplier cowAttributes = Cow.createAttributes().build();
        FabricDefaultAttributeRegistry.register(APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(ENCHANTED_GOLDEN_APPLE_COW.get(), cowAttributes);
        FabricDefaultAttributeRegistry.register(DR_TRAYAURUS.get(), Villager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CLOUD_SHARK.get(), CloudSharkEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(KRAKEN.get(), KrakenEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MISSILE_SQUID.get(), MissileSquidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(NIGHTMARE.get(), NightmareEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BED_BUG.get(), BedBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(WASP.get(), WaspEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BOMBER.get(), BomberEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(JUMPY_BUG.get(), JumpyBugEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MANTIS.get(), MantisEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(TRIFFID.get(), TriffidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(LUCID.get(), LucidEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SCORPION.get(), ScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BASILISK.get(), BasiliskEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(EMPEROR_SCORPION.get(), EmperorScorpionEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(TORETERROR.get(), ToreterrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CREEPING_HORROR.get(), CreepingHorrorEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(LURKING_TERROR.get(), LurkingTerrorEntity.createAttributes().build());

        FabricDefaultAttributeRegistry.register(RED_ANT.get(), buildAntAttributes(
                AntarchySettings.redAntHealth(),
                AntarchySettings.redAntMovementSpeed(),
                AntarchySettings.redAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(BROWN_ANT.get(), buildAntAttributes(
                AntarchySettings.brownAntHealth(),
                AntarchySettings.brownAntMovementSpeed(),
                AntarchySettings.brownAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(RAINBOW_ANT.get(), buildAntAttributes(
                AntarchySettings.rainbowAntHealth(),
                AntarchySettings.rainbowAntMovementSpeed(),
                AntarchySettings.rainbowAntAttackDamage()
        ).build());
        FabricDefaultAttributeRegistry.register(MOLEWORM.get(), MolewormEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MOLEVORE.get(), MolevoreEntity.createAttributes().build());
    }

    private static void registerSpawnPlacements() {
        SpawnPlacements.register(FLYING_SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FlyingSquirrelEntity::canSpawn);
        SpawnPlacements.register(CATERPILLAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaterpillarEntity::canSpawn);
        SpawnPlacements.register(BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn);
        SpawnPlacements.register(REVERIE.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ReverieEntity::canSpawn);
        SpawnPlacements.register(BRUTALFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(ENCHANTED_GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Cow::checkAnimalSpawnRules);
        SpawnPlacements.register(CLOUD_SHARK.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CloudSharkEntity::canSpawn);
        SpawnPlacements.register(WASP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaspEntity::canSpawn);
        SpawnPlacements.register(BOMBER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BomberEntity::canSpawn);
        SpawnPlacements.register(JUMPY_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JumpyBugEntity::canSpawn);
        SpawnPlacements.register(KRAKEN.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, KrakenEntity::canSpawn);
        SpawnPlacements.register(MISSILE_SQUID.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MissileSquidEntity::canSpawn);
        SpawnPlacements.register(NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightmareEntity::canSpawn);
        SpawnPlacements.register(MOLEWORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolewormEntity::canSpawn);
        SpawnPlacements.register(MANTIS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MantisEntity::canSpawn);
        SpawnPlacements.register(MOLEVORE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MolevoreEntity::canSpawn);
        SpawnPlacements.register(BED_BUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BedBugEntity::canSpawn);
        SpawnPlacements.register(LUCID.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LucidEntity::canSpawn);
        SpawnPlacements.register(SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::canSpawn);
        SpawnPlacements.register(BASILISK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BasiliskEntity::canSpawn);
        SpawnPlacements.register(EMPEROR_SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EmperorScorpionEntity::canSpawn);
        SpawnPlacements.register(TORETERROR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ToreterrorEntity::canSpawn);
    }

    private static void populateCreativeTab(CreativeModeTab.Output output) {
        ArrayList<Item> sortedItems = new ArrayList<>();
        for (var holder : ITEMS.getEntries()) {
            Item item = holder.get();
            if (item != Items.AIR) {
                sortedItems.add(item);
            }
        }
        sortedItems.sort(Comparator.comparingInt(AntarchyFabricContent::creativeTabGroup));
        sortedItems.forEach(output::accept);
        addPotionVariants(output);
    }

    private static void addPotionVariants(CreativeModeTab.Output output) {
        acceptPotionFamily(output, DREAD_POTION);
        acceptPotionFamily(output, LONG_DREAD);
        acceptPotionFamily(output, INVERSION);
        acceptPotionFamily(output, LONG_INVERSION);
        acceptPotionFamily(output, PARALYSIS);
        acceptPotionFamily(output, LONG_PARALYSIS);
        acceptPotionFamily(output, HASTE);
        acceptPotionFamily(output, STRONG_HASTE);
        acceptPotionFamily(output, SHRINKING);
        acceptPotionFamily(output, STRONG_SHRINKING);
        acceptPotionFamily(output, EXTREME_SHRINKING);
        acceptPotionFamily(output, GROWING);
        acceptPotionFamily(output, STRONG_GROWING);
        acceptPotionFamily(output, EXTREME_GROWING);
    }

    private static void acceptPotionFamily(CreativeModeTab.Output output, DeferredHolder<Potion, ? extends Potion> potion) {
        Holder<Potion> holder = potionHolder(potion);
        output.accept(PotionContents.createItemStack(Items.POTION, holder));
        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, holder));
        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, holder));
        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, holder));
    }

    private static int creativeTabGroup(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            case "ouranwood_log", "ouranwood_wood", "mossy_ouranwood_log", "mossy_ouranwood_wood",
                 "stripped_ouranwood_log", "stripped_ouranwood_wood", "duplicator_log",
                 "ouranwood_planks", "ouranwood_stairs", "ouranwood_slab",
                 "ouranwood_fence", "ouranwood_fence_gate",
                 "ouranwood_door", "ouranwood_trapdoor",
                 "ouranwood_pressure_plate", "ouranwood_button",
                 "ouranwood_sign", "ouranwood_hanging_sign",
                 "ouranwood_boat", "ouranwood_chest_boat" -> 0;
            case "nyxite", "polished_nyxite", "chiseled_nyxite", "nyxite_bricks",
                 "nyxite_stairs", "nyxite_slab", "nyxite_wall",
                 "polished_nyxite_stairs", "polished_nyxite_slab", "polished_nyxite_wall",
                 "nyxite_brick_stairs", "nyxite_brick_slab", "nyxite_brick_wall",
                 "pale_nyxite", "nyxite_spike", "potent_nyxite" -> 1;
            case "shellstone", "polished_shellstone", "shellstone_bricks", "chiseled_shellstone",
                 "mossy_shellstone_bricks", "cracked_shellstone_bricks",
                 "shellstone_stairs", "shellstone_slab", "shellstone_wall",
                 "polished_shellstone_stairs", "polished_shellstone_slab", "polished_shellstone_wall",
                 "shellstone_brick_stairs", "shellstone_brick_slab", "shellstone_brick_wall",
                 "mossy_shellstone_brick_stairs", "mossy_shellstone_brick_slab", "mossy_shellstone_brick_wall" -> 2;
            case "antimetal", "polished_antimetal" -> 3;
            case "dream_sand", "dream_sandstone", "chiseled_dream_sandstone",
                 "cut_dream_sandstone", "smooth_dream_sandstone",
                 "dream_sandstone_stairs", "dream_sandstone_slab", "dream_sandstone_wall",
                 "smooth_dream_sandstone_stairs", "smooth_dream_sandstone_slab",
                 "cut_dream_sandstone_slab" -> 4;
            case "umbral_moss_block", "umbral_moss_carpet" -> 5;
            case "torchflower_bush", "hushweed", "orange_milkweed", "pink_milkweed" -> 6;
            case "blood_crystal_block", "budding_blood_crystal",
                 "small_blood_crystal_bud", "medium_blood_crystal_bud",
                 "large_blood_crystal_bud", "blood_crystal_cluster" -> 7;
            case "uranium_ore", "deepslate_uranium_ore", "titanium_ore", "deepslate_titanium_ore",
                 "uranium_block", "titanium_block", "raw_uranium_block", "raw_titanium_block",
                 "cut_uranium", "cut_titanium", "cut_uranium_slab", "cut_titanium_slab",
                 "cut_uranium_stairs", "cut_titanium_stairs", "chiseled_uranium",
                 "chiseled_titanium", "uranium_bulb", "titanium_bulb",
                 "uranium_door", "titanium_door", "uranium_trapdoor", "titanium_trapdoor",
                 "uranium_bars", "titanium_bars" -> 8;
            case "infested_rooted_dirt", "infested_coarse_dirt", "triffid_goo_block",
                 "cloud_block" -> 9;
            case "dream_torch", "dream_lantern", "dream_campfire", "dream_fire", "dream_fire_ceiling" -> 10;
            case "easter_bunny_spawn_egg", "flying_squirrel_spawn_egg", "caterpillar_spawn_egg",
                 "butterfly_spawn_egg", "reverie_spawn_egg", "brutalfly_spawn_egg",
                 "red_ant_spawn_egg", "brown_ant_spawn_egg", "rainbow_ant_spawn_egg",
                 "moleworm_spawn_egg", "mantis_spawn_egg", "molevore_spawn_egg", "triffid_spawn_egg",
                 "apple_cow_spawn_egg", "golden_apple_cow_spawn_egg", "enchanted_golden_apple_cow_spawn_egg",
                 "honeyed_apple_cow_spawn_egg", "dr_trayaurus_spawn_egg", "wasp_spawn_egg",
                 "bomber_spawn_egg", "jumpy_bug_spawn_egg", "cloud_shark_spawn_egg", "kraken_spawn_egg", "missile_squid_spawn_egg",
                 "nightmare_spawn_egg", "bed_bug_spawn_egg", "lucid_spawn_egg", "scorpion_spawn_egg",
                 "basilisk_spawn_egg", "emperor_scorpion_spawn_egg", "toreterror_spawn_egg",
                 "creeping_horror_spawn_egg", "lurking_terror_spawn_egg" -> 90;
            case "water_cannon" -> 52;
            case "primordial_helmet", "primordial_chestplate", "primordial_leggings", "primordial_boots" -> 53;
            case "jumpy_boots" -> 54;
            case "king_scale", "queen_scale" -> 22;
            default -> 50;
        };
    }

    private static BlockEntityType<AntNestBlockEntity> antNestBlockEntityType() {
        return ANT_NEST_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<WaspNestBlockEntity> waspNestBlockEntityType() {
        return WASP_NEST_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<PotentNyxiteBlockEntity> potentNyxiteBlockEntityType() {
        return POTENT_NYXITE_BLOCK_ENTITY.get();
    }

    private static Block smallBloodCrystalBudBlock() {
        return SMALL_BLOOD_CRYSTAL_BUD.get();
    }

    private static Item cloudBucketItem() {
        return CLOUD_BUCKET.get();
    }

    private static Block mediumBloodCrystalBudBlock() {
        return MEDIUM_BLOOD_CRYSTAL_BUD.get();
    }

    private static Block largeBloodCrystalBudBlock() {
        return LARGE_BLOOD_CRYSTAL_BUD.get();
    }

    private static Block bloodCrystalCrystalBlock() {
        return BLOOD_CRYSTAL_CRYSTAL.get();
    }

    private static Block createDirtOre(Block copyFrom) {
        return new Block(dirtOreProperties());
    }

    private static Block createDirtOre(Block copyFrom, int minExperience, int maxExperience) {
        return new DropExperienceBlock(UniformInt.of(minExperience, maxExperience), dirtOreProperties());
    }

    private static BlockBehaviour.Properties dirtOreProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                .sound(SoundType.ROOTED_DIRT)
                .strength(0.65F, 0.8F)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties nyxiteProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERRACK);
    }

    private static Block createOre(Block copyFrom) {
        return new Block(BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
    }

    private static Block createOre(Block copyFrom, int minExperience, int maxExperience) {
        return new DropExperienceBlock(UniformInt.of(minExperience, maxExperience), BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
    }

    private static Block createStorageBlock(Block copyFrom) {
        return new Block(BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
    }

    private static Block createHorizontalFacingStorageBlock(Block copyFrom) {
        return new SimpleHorizontalFacingBlock(BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
    }

    private static final class SimpleHorizontalFacingBlock extends HorizontalDirectionalBlock {
        private static final MapCodec<SimpleHorizontalFacingBlock> CODEC = simpleCodec(SimpleHorizontalFacingBlock::new);

        private SimpleHorizontalFacingBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        protected BlockState mirror(BlockState state, Mirror mirror) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }
    }

    private static Block createRawStorageBlock(Block copyFrom) {
        return new Block(BlockBehaviour.Properties.ofFullCopy(copyFrom)
                .strength(3.5F, 6.0F)
                .requiresCorrectToolForDrops());
    }

    private static Item diamondMinecartItem() {
        return DIAMOND_MINECART_ITEM.get();
    }


    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String path) {
        return SOUND_EVENTS.register(path,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)));
    }

    private static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    private static Ingredient bloodCrystalRepairIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal_shard"))
                .orElse(Items.AIR));
    }

    private static Ingredient ultimateRepairIngredient() {
        return Ingredient.of(TITANIUM_INGOT.get());
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

    private static net.minecraft.world.level.material.FlowingFluid lookupFlowingFluid(String path) {
        return (net.minecraft.world.level.material.FlowingFluid) BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    private static Item lookupItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    private static LiquidBlock lookupLiquidBlock(String path) {
        return (LiquidBlock) BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }

    public static void register() {
        com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity.TYPE = UPWARD_FALLING_BLOCK;
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(LUCID_EYE.get()), potionHolder(INVERSION));
            builder.registerPotionRecipe(potionHolder(INVERSION), Ingredient.of(Items.REDSTONE), potionHolder(LONG_INVERSION));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(BASILISK_FANG.get()), potionHolder(PARALYSIS));
            builder.registerPotionRecipe(potionHolder(PARALYSIS), Ingredient.of(Items.REDSTONE), potionHolder(LONG_PARALYSIS));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(MOLEWORM_ITEM.get()), potionHolder(HASTE));
            builder.registerPotionRecipe(potionHolder(HASTE), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(STRONG_HASTE));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(URANIUM_NUGGET.get()), potionHolder(SHRINKING));
            builder.registerPotionRecipe(potionHolder(SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(STRONG_SHRINKING));
            builder.registerPotionRecipe(potionHolder(STRONG_SHRINKING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(EXTREME_SHRINKING));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(TITANIUM_NUGGET.get()), potionHolder(GROWING));
            builder.registerPotionRecipe(potionHolder(GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(STRONG_GROWING));
            builder.registerPotionRecipe(potionHolder(STRONG_GROWING), Ingredient.of(Items.GLOWSTONE_DUST), potionHolder(EXTREME_GROWING));
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(CLOUD_SHARK_FIN.get()), net.minecraft.world.item.alchemy.Potions.SLOW_FALLING);
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(JUMPY_BUG_LEG.get()), Potions.LEAPING);
            builder.registerPotionRecipe(net.minecraft.world.item.alchemy.Potions.AWKWARD, Ingredient.of(CORNEA_EAR.get()), net.minecraft.world.item.alchemy.Potions.NIGHT_VISION);
        });

        SOUND_EVENTS.register();
        ARMOR_MATERIALS.register();
        FLUIDS.register();
        BLOCKS.register();
        ENTITY_TYPES.register();
        ATTRIBUTES.register();
        registerEntityAttributes();
        registerSpawnPlacements();
        BLOCK_ENTITY_TYPES.register();
        PARTICLE_TYPES.register();
        FEATURES.register();
        BIOME_SOURCES.register();
        DENSITY_FUNCTION_TYPES.register();
        ENTITY_SUB_PREDICATES.register();
        MOB_EFFECTS.register();
        POTIONS.register();
        ITEMS.register();
        CREATIVE_MODE_TABS.register();

        AntarchySoundEvents.bind(
                SQUIDZOOKA_FIRE,
                SHRINK_RAY_SOUND,
                GROWTH_RAY_SOUND,
                SIZE_RAY_CHARGE,
                ANT_AMBIENT,
                ANT_IDLE,
                ANT_HURT,
                ANT_BITE,
                ANT_GATHER,
                ANT_NEST,
                CLOUD_SHARK_BITE,
                CLOUD_SHARK_IDLE,
                CLOUD_SHARK_HURT,
                CLOUD_SHARK_DEATH,
                CLOUD_SHARK_FLY,
                CATERPILLAR_IDLE,
                CATERPILLAR_HURT,
                CATERPILLAR_CRAWL,
                BUTTERFLY_HURT,
                BRUTALFLY_IDLE,
                BRUTALFLY_DEATH,
                ELYTHIA_FIREFLY_AMBIENT,
                MISSILE_SQUID_AMBIENT,
                MISSILE_SQUID_HURT,
                MISSILE_SQUID_DEATH,
                MISSILE_SQUID_ATTACK,
                KRAKEN_FLYING_LOOP,
                KRAKEN_FLYING_SIDEWAYS_LOOP,
                KRAKEN_ATTACK,
                KRAKEN_SPIN,
                KRAKEN_ROAR,
                KRAKEN_SUMMON,
                KRAKEN_HURT,
                KRAKEN_DEATH,
                BASILISK_IDLE_LOOP,
                BASILISK_SLITHER_LOOP,
                BASILISK_BITE,
                BASILISK_HISS,
                BASILISK_HURT,
                BASILISK_DEATH,
                THORAXIS_NIGHTMARE_WASTES_AMBIENT,
                THORAXIS_NIGHTMARE_WASTES_ADDITIONS,
                THORAXIS_NIGHTMARE_WASTES_MOOD,
                THORAXIS_DREAM_DUNES_AMBIENT,
                THORAXIS_DREAM_DUNES_ADDITIONS,
                THORAXIS_DREAM_DUNES_MOOD,
                THORAXIS_LUCID_POOLS_AMBIENT,
                THORAXIS_LUCID_POOLS_ADDITIONS,
                THORAXIS_LUCID_POOLS_MOOD,
                LUCID_AMBIENT,
                LUCID_FLYING,
                LUCID_ATTACK,
                LUCID_BOLT_SOUND,
                LUCID_HURT,
                LUCID_DEATH,
                REVERIE_IDLE,
                REVERIE_HURT,
                REVERIE_WORRY,
                REVERIE_SAVE,
                REVERIE_JOIN_PLAYER,
                REVERIE_ALERT,
                FLYING_SQUIRREL_IDLE,
                FLYING_SQUIRREL_BEG,
                FLYING_SQUIRREL_NUT,
                FLYING_SQUIRREL_GLIDE_LOOP,
                FLYING_SQUIRREL_HURT,
                FLYING_SQUIRREL_DEATH,
                NIGHTMARE_IDLE,
                NIGHTMARE_HURT,
                NIGHTMARE_ROAR,
                NIGHTMARE_DEATH,
                NIGHTMARE_BITE,
                NIGHTMARE_FLAP,
                TRIFFID_ATTACK,
                TRIFFID_GRAB,
                TRIFFID_HURT,
                TRIFFID_DEATH,
                TRIFFID_HISS,
                TRIFFID_GROWL,
                MANTIS_AMBIENT,
                MANTIS_HURT,
                MANTIS_ATTACK,
                MANTIS_FLY_LOOP,
                GRAVITY_GUN_PICKUP,
                GRAVITY_GUN_DROP,
                GRAVITY_GUN_HOLD_LOOP,
                GRAVITY_GUN_LAUNCH,
                GRAVITY_GUN_DRYFIRE,
                BED_BUG_AMBIENT,
                BED_BUG_HURT,
                BED_BUG_ATTACK,
                WASP_IDLE,
                WASP_HURT,
                WASP_ATTACK,
                WASP_DEATH,
                SCORPION_AMBIENT,
                SCORPION_HURT,
                SCORPION_ATTACK,
                EMPEROR_SCORPION_AMBIENT,
                EMPEROR_SCORPION_HURT,
                EMPEROR_SCORPION_ATTACK,
                EMPEROR_SCORPION_ROAR,
                MOLEWORM_AMBIENT,
                MOLEWORM_HURT,
                MOLEWORM_ATTACK,
                MOLEWORM_DIG,
                MOLEVORE_AMBIENT,
                MOLEVORE_HURT,
                MOLEVORE_ATTACK,
                MOLEVORE_DIG,
                DUCT_TAPE_USE
        );

        AntarchyObjects.bind(
                EASTER_BUNNY,
                FLYING_SQUIRREL,
                KRAKEN,
                MISSILE_SQUID,
                MOLEWORM,
                MANTIS,
                BED_BUG,
                WASP,
                BOMBER,
                SCORPION,
                CATERPILLAR,
                BUTTERFLY,
                REVERIE,
                TRIFFID,
                BRUTALFLY,
                BRUTALFLY_ORB,
                HUSH_PROJECTILE,
                TORETERROR,
                WATER_BOMB,
                CREEPING_HORROR,
                LURKING_TERROR,
                () -> DUPLICATOR_LOG.get(),
                () -> DUPLICATOR_SAPLING.get(),
                () -> DUCT_TAPE.get(),
                () -> INFESTED_ROOTED_DIRT.get(),
                () -> INFESTED_COARSE_DIRT.get(),
                () -> NYXITE.get(),
                () -> SHELLSTONE.get(),
                () -> POLISHED_SHELLSTONE.get(),
                () -> SHELLSTONE_BRICKS.get(),
                () -> CHISELED_SHELLSTONE.get(),
                () -> MOSSY_SHELLSTONE_BRICKS.get(),
                () -> CRACKED_SHELLSTONE_BRICKS.get(),
                () -> MOSSY_SHELLSTONE_BRICK_STAIRS.get(),
                () -> MOSSY_SHELLSTONE_BRICK_SLAB.get(),
                () -> MOSSY_SHELLSTONE_BRICK_WALL.get(),
                () -> SHELLSTONE_STAIRS.get(),
                () -> SHELLSTONE_SLAB.get(),
                () -> SHELLSTONE_WALL.get(),
                () -> POLISHED_SHELLSTONE_STAIRS.get(),
                () -> POLISHED_SHELLSTONE_SLAB.get(),
                () -> POLISHED_SHELLSTONE_WALL.get(),
                () -> SHELLSTONE_BRICK_STAIRS.get(),
                () -> SHELLSTONE_BRICK_SLAB.get(),
                () -> SHELLSTONE_BRICK_WALL.get(),
                () -> CLOUD_BLOCK.get(),
                () -> TRIFFID_GOO_BLOCK.get(),
                () -> PALE_NYXITE.get(),
                () -> NYXITE_SPIKE.get(),
                () -> POTENT_NYXITE.get(),
                () -> ANTIMETAL.get(),
                () -> POLISHED_ANTIMETAL.get(),
                () -> BUDDING_BLOOD_CRYSTAL.get(),
                () -> SMALL_BLOOD_CRYSTAL_BUD.get(),
                () -> MEDIUM_BLOOD_CRYSTAL_BUD.get(),
                () -> LARGE_BLOOD_CRYSTAL_BUD.get(),
                () -> BLOOD_CRYSTAL_CRYSTAL.get(),
                () -> OURANWOOD_ACORN.get(),
                KRAKEN_TOOTH,
                () -> MOGGLES.get(),
                () -> REVERIE_BOTTLE.get(),
                () -> mobEffectHolder(DREAD),
                () -> mobEffectHolder(PARALYZED),
                () -> mobEffectHolder(INVERTED),
                () -> OURANWOOD_ACORN_BLOCK.get(),
                () -> MOSSY_OURANWOOD_LOG.get(),
                () -> MOSSY_OURANWOOD_WOOD.get(),
                () -> ORANGE_MILKWEED.get(),
                () -> PINK_MILKWEED.get(),
                () -> TORCHFLOWER_BUSH.get(),
                () -> BED_BUG_EGG.get(),
                () -> CREEPING_HORROR_EGGS.get(),
                () -> LURKING_TERROR_EGGS.get(),
                () -> WASP_NEST.get(),
                () -> HUSHWEED.get(),
                () -> OURANWOOD_SQUIRREL_NEST.get(),
                () -> ANT_NEST_BLOCK_ENTITY.get(),
                () -> DREAM_CAMPFIRE_BLOCK_ENTITY.get(),
                () -> WASP_NEST_BLOCK_ENTITY.get(),
                () -> HUSHWEED_BLOCK_ENTITY.get(),
                () -> attributeHolder(DOUBLE_DAMAGE_CHANCE),
                () -> attributeHolder(BLOODGLASS_MAX_HEARTS),
                () -> mobEffectHolder(BLOODGLASS_WARD)
        );

        LucidEyeProjectileEntity.defaultItemSupplier = () -> LUCID_PEARL.get();
        LucidEntity.invertedEffectSupplier = () -> mobEffectHolder(INVERTED);
        LucidEntity.boltEntityTypeSupplier = () -> LUCID_BOLT.get();
        LucidBoltEntity.invertedEffectSupplier = () -> mobEffectHolder(INVERTED);
        LucidEyeProjectileEntity.invertedEffectSupplier = () -> mobEffectHolder(INVERTED);

        ScorpionWhipTetherSync.setSink(AntarchyFabricNetworking::syncScorpionWhipTether);
        BloodCrystalKatanaItem.setTrailCallback(AntarchyFabricNetworking::syncKatanaTrail);
        com.craisinlord.antarchy.content.gravity.AntarchyGravityApi.setSyncDispatcher(AntarchyFabricNetworking::syncGravityEntity);
        AntarchyFabricEvents.register();

        if (FabricInfinityCompat.isAvailableOnClasspath()) {
            InfinityCompat.bind(new FabricInfinityCompat());
        }
    }
}




