package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatVersion;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.CreativeTabOrder;
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
import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.LumenLiquidBlock;
import com.craisinlord.antarchy.content.worldgen.ants.BrownAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RainbowAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.RedAntNestFeature;
import com.craisinlord.antarchy.content.worldgen.ants.TermiteNestFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileCystFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynBileVeinFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynCreepvineFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWallAmberMossFeature;
import com.craisinlord.antarchy.content.worldgen.overworld.CornPatchFeature;
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
import com.craisinlord.antarchy.content.effect.StinkyMobEffect;
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
import com.craisinlord.antarchy.content.entity.StinkBugEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodBoatEntity;
import com.craisinlord.antarchy.content.entity.OuranwoodChestBoatEntity;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import com.craisinlord.antarchy.content.item.LucidEyeItem;
import com.craisinlord.antarchy.content.item.LucidPearlItem;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
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
import com.craisinlord.antarchy.content.item.MinersDreamItem;
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
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
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
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormTunnelsFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.MolewormWarrensFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodCocoonTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeConfiguration;
import com.craisinlord.antarchy.content.worldgen.elythia.PeachTreeFeature;
import com.craisinlord.antarchy.content.worldgen.elythia.TriffidPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynEggPatchFeature;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitenSpikeConfiguration;
import com.craisinlord.antarchy.content.worldgen.cavaryn.ChitenSpikeFeature;
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
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
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
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
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
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;

public final class AntarchyFabricSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Antarchy.MODID);


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


    public static final DeferredHolder<SoundEvent, SoundEvent> BOMBER_WALK = registerSoundEvent("bomber_walk");


    public static final DeferredHolder<SoundEvent, SoundEvent> BOMBER_KNOCK = registerSoundEvent("bomber_knock");


    public static final DeferredHolder<SoundEvent, SoundEvent> BOMBER_EXPLODE = registerSoundEvent("bomber_explode");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_IDLE = registerSoundEvent("toreterror_idle");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_HURT = registerSoundEvent("toreterror_hurt");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_DEATH = registerSoundEvent("toreterror_death");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_BOMBER_FIRE = registerSoundEvent("toreterror_bomber_fire");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_SPIN = registerSoundEvent("toreterror_spin");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_RICOCHET = registerSoundEvent("toreterror_ricochet");


    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_CANNON_FIRE = registerSoundEvent("water_cannon_fire");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_JUMP_UP = registerSoundEvent("toreterror_jump_up");


    public static final DeferredHolder<SoundEvent, SoundEvent> TORETERROR_JUMP_LAND = registerSoundEvent("toreterror_jump_land");


    public static final DeferredHolder<SoundEvent, SoundEvent> STINKY_FLY_SOUND = registerSoundEvent("stinky_fly");


    public static final DeferredHolder<SoundEvent, SoundEvent> STINK_BUG_FART = registerSoundEvent("stink_bug_fart");


    public static final DeferredHolder<SoundEvent, SoundEvent> STINK_BUG_IDLE = registerSoundEvent("stink_bug_idle");


    public static final DeferredHolder<SoundEvent, SoundEvent> CAVARYN_HEARTBEAT = registerSoundEvent("cavaryn_heartbeat");


    public static final DeferredHolder<SoundEvent, SoundEvent> ROLLY_POLLY_IDLE = registerSoundEvent("rolly_polly_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROLLY_POLLY_WHEEL_MODE = registerSoundEvent("rolly_polly_wheel_mode");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROLLY_POLLY_NORMAL_MODE = registerSoundEvent("rolly_polly_normal_mode");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROLLY_POLLY_ROLL = registerSoundEvent("rolly_polly_roll");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_YOUNG_IDLE = registerSoundEvent("jerry_young_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_YOUNG_HURT = registerSoundEvent("jerry_young_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_YOUNG_DEATH = registerSoundEvent("jerry_young_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_YOUNG_ATTACK = registerSoundEvent("jerry_young_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_ADULT_IDLE = registerSoundEvent("jerry_adult_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_ADULT_HURT = registerSoundEvent("jerry_adult_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_ADULT_DEATH = registerSoundEvent("jerry_adult_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERRY_ADULT_ATTACK = registerSoundEvent("jerry_adult_attack");


    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPING_HORROR_GROWL = registerSoundEvent("creeping_horror_growl");


    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPING_HORROR_HURT = registerSoundEvent("creeping_horror_hurt");


    public static final DeferredHolder<SoundEvent, SoundEvent> CREEPING_HORROR_BITE = registerSoundEvent("creeping_horror_bite");


    public static final DeferredHolder<SoundEvent, SoundEvent> LURKING_TERROR_SNARL = registerSoundEvent("lurking_terror_snarl");


    public static final DeferredHolder<SoundEvent, SoundEvent> LURKING_TERROR_HURT = registerSoundEvent("lurking_terror_hurt");


    public static final DeferredHolder<SoundEvent, SoundEvent> LURKING_TERROR_BITE = registerSoundEvent("lurking_terror_bite");


    public static final DeferredHolder<SoundEvent, SoundEvent> LURKING_TERROR_FLY_LOOP = registerSoundEvent("lurking_terror_fly_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_IDLE = registerSoundEvent("hercules_beetle_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_HURT = registerSoundEvent("hercules_beetle_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_ATTACK = registerSoundEvent("hercules_beetle_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_CRY = registerSoundEvent("hercules_beetle_cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_CHARGE_START = registerSoundEvent("hercules_beetle_charge_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> HERCULES_BEETLE_KNOCKED_DOWN = registerSoundEvent("hercules_beetle_knocked_down");
    public static final DeferredHolder<SoundEvent, SoundEvent> JUMPY_BUG_IDLE = registerSoundEvent("jumpy_bug_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIT_BUG_IDLE = registerSoundEvent("spit_bug_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> JUMPY_BUG_HURT = registerSoundEvent("jumpy_bug_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> JUMPY_BUG_JUMP = registerSoundEvent("jumpy_bug_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIT_BUG_HURT = registerSoundEvent("spit_bug_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIT_BUG_SPIT = registerSoundEvent("spit_bug_spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRUTALFLY_SPIT = registerSoundEvent("brutalfly_spit");


    public static final DeferredHolder<SoundEvent, SoundEvent> DUCT_TAPE_USE = registerSoundEvent("duct_tape_use");


    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_HYPNOTIC_GAS = registerSoundEvent("potent_nyxite_hypnotic_gas");


    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_START = registerSoundEvent("potent_nyxite_geyser_eruption_start");


    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_ERUPTION_ACTIVE = registerSoundEvent("potent_nyxite_geyser_eruption_active");


    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_START = registerSoundEvent("potent_nyxite_geyser_continuous_start");


    public static final DeferredHolder<SoundEvent, SoundEvent> POTENT_NYXITE_GEYSER_CONTINUOUS_ACTIVE = registerSoundEvent("potent_nyxite_geyser_continuous_active");




    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String path) {
        return SOUND_EVENTS.register(path,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)));
    }


    public static void register() {
        SOUND_EVENTS.register();
    }

}
