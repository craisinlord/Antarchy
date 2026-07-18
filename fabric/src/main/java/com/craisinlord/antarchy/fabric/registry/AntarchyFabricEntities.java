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
import com.craisinlord.antarchy.content.entity.CritterCageProjectileEntity;
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
import com.craisinlord.antarchy.content.entity.kraken.KrakensGraspThrownTrident;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
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

public final class AntarchyFabricEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Antarchy.MODID);


    public static final DeferredHolder<EntityType<?>, EntityType<RedAntEntity>> RED_ANT = ENTITY_TYPES.register("red_ant",
            () -> buildAntType(RedAntEntity::new, MobCategory.CREATURE, "red_ant"));


    public static final DeferredHolder<EntityType<?>, EntityType<BrownAntEntity>> BROWN_ANT = ENTITY_TYPES.register("brown_ant",
            () -> buildAntType(BrownAntEntity::new, MobCategory.CREATURE, "brown_ant"));


    public static final DeferredHolder<EntityType<?>, EntityType<RainbowAntEntity>> RAINBOW_ANT = ENTITY_TYPES.register("rainbow_ant",
            () -> buildAntType(RainbowAntEntity::new, MobCategory.CREATURE, "rainbow_ant"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.ant.TermiteEntity>> TERMITE = ENTITY_TYPES.register("termite",
            () -> buildAntType(com.craisinlord.antarchy.content.entity.ant.TermiteEntity::new, MobCategory.CREATURE, "termite"));


//    public static final DeferredHolder<EntityType<?>, EntityType<RollyPollyEntity>> ROLLY_POLLY = ENTITY_TYPES.register("rolly_polly",
//            () -> EntityType.Builder.of(RollyPollyEntity::new, MobCategory.CREATURE)
//                    .sized(0.95F, 0.85F)
//                    .clientTrackingRange(10)
//                    .build("rolly_polly"));


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


    public static final DeferredHolder<EntityType<?>, EntityType<AlphaMantisEntity>> ALPHA_MANTIS = ENTITY_TYPES.register("alpha_mantis",
            () -> EntityType.Builder.of(AlphaMantisEntity::new, MobCategory.MONSTER)
                    .sized(4.25F, 3.35F)
                    .clientTrackingRange(10)
                    .build("alpha_mantis"));


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


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.PeachBoatEntity>> PEACH_BOAT_ENTITY = ENTITY_TYPES.register("peach_boat",
            () -> EntityType.Builder.<com.craisinlord.antarchy.content.entity.PeachBoatEntity>of(com.craisinlord.antarchy.content.entity.PeachBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("peach_boat"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.PeachChestBoatEntity>> PEACH_CHEST_BOAT_ENTITY = ENTITY_TYPES.register("peach_chest_boat",
            () -> EntityType.Builder.<com.craisinlord.antarchy.content.entity.PeachChestBoatEntity>of(com.craisinlord.antarchy.content.entity.PeachChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("peach_chest_boat"));


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


    public static final DeferredHolder<EntityType<?>, EntityType<StinkBugEntity>> STINK_BUG = ENTITY_TYPES.register("stink_bug",
            () -> EntityType.Builder.of(StinkBugEntity::new, MobCategory.AMBIENT)
                    .sized(0.35F, 0.2F)
                    .clientTrackingRange(8)
                    .build("stink_bug"));


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
                    .sized(3.0F, 6.0F)
                    .clientTrackingRange(8)
                    .build("jumpy_bug"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.SpitBugEntity>> SPIT_BUG = ENTITY_TYPES.register("spit_bug",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.SpitBugEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 3.0F)
                    .clientTrackingRange(10)
                    .build("spit_bug"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.SpitBugProjectileEntity>> SPIT_BUG_PROJECTILE = ENTITY_TYPES.register("spit_bug_projectile",
            () -> EntityType.Builder.<com.craisinlord.antarchy.content.entity.SpitBugProjectileEntity>of(com.craisinlord.antarchy.content.entity.SpitBugProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("spit_bug_projectile"));


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


    public static final DeferredHolder<EntityType<?>, EntityType<DiamondMinecartEntity>> DIAMOND_MINECART = ENTITY_TYPES.register("diamond_minecart",
            () -> EntityType.Builder.<DiamondMinecartEntity>of((entityType, level) -> new DiamondMinecartEntity(entityType, level, AntarchyFabricItems.diamondMinecartItem()), MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(8)
                    .build("diamond_minecart"));



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
                    .sized(1.125F, 0.6875F)
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
                    .sized(11.4F, 39.0F)
                    .clientTrackingRange(12)
                    .build("kraken"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.fabric.entity.multipart.MultipartPartEntity>> KRAKEN_PART = ENTITY_TYPES.register("kraken_part",
            () -> EntityType.Builder.<com.craisinlord.antarchy.fabric.entity.multipart.MultipartPartEntity>of(
                            com.craisinlord.antarchy.fabric.entity.multipart.MultipartPartEntity::new,
                            MobCategory.MISC
                    )
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("kraken_part"));


    public static final DeferredHolder<EntityType<?>, EntityType<MissileSquidEntity>> MISSILE_SQUID = ENTITY_TYPES.register("missile_squid",
            () -> EntityType.Builder.of(MissileSquidEntity::new, MobCategory.MONSTER)
                    .sized(1.62F, 3.18F)
                    .clientTrackingRange(10)
                    .build("missile_squid"));


    public static final DeferredHolder<EntityType<?>, EntityType<OctopusBombEntity>> OCTOPUS_BOMB = ENTITY_TYPES.register("octopus_bomb",
            () -> EntityType.Builder.of(OctopusBombEntity::new, MobCategory.WATER_CREATURE)
                    .sized(2.4F, 2.9F)
                    .clientTrackingRange(10)
                    .build("octopus_bomb"));


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

    public static final DeferredHolder<EntityType<?>, EntityType<KrakensGraspThrownTrident>> KRAKENS_GRASP_TRIDENT = ENTITY_TYPES.register("krakens_grasp_trident",
            () -> EntityType.Builder.<KrakensGraspThrownTrident>of(KrakensGraspThrownTrident::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("krakens_grasp_trident"));

    public static final DeferredHolder<EntityType<?>, EntityType<TentacleEntity>> TENTACLE = ENTITY_TYPES.register("tentacle",
            () -> EntityType.Builder.of(TentacleEntity::new, MobCategory.MISC)
                    .sized(1.2F, 4.5F)
                    .clientTrackingRange(10)
                    .build("tentacle"));


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
                    .sized(6.0F, 3.0F)
                    .clientTrackingRange(12)
                    .build("emperor_scorpion"));


    public static final DeferredHolder<EntityType<?>, EntityType<LucidEyeProjectileEntity>> LUCID_PEARL_PROJECTILE = ENTITY_TYPES.register("lucid_pearl_projectile",
            () -> EntityType.Builder.<LucidEyeProjectileEntity>of(LucidEyeProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("lucid_pearl_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<CritterCageProjectileEntity>> CRITTER_CAGE_PROJECTILE = ENTITY_TYPES.register("critter_cage_projectile",
            () -> EntityType.Builder.<CritterCageProjectileEntity>of(CritterCageProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("critter_cage_projectile"));


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



    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.CreepingHorrorEntity>> CREEPING_HORROR = ENTITY_TYPES.register("creeping_horror",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.CreepingHorrorEntity::new, MobCategory.MONSTER)
                    .sized(1.3F, 1.5F)
                    .clientTrackingRange(10)
                    .build("creeping_horror"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.LurkingTerrorEntity>> LURKING_TERROR = ENTITY_TYPES.register("lurking_terror",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.LurkingTerrorEntity::new, MobCategory.MONSTER)
                    .sized(1.3F, 1.5F)
                    .clientTrackingRange(10)
                    .build("lurking_terror"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.CheepEntity>> CHEEP = ENTITY_TYPES.register("cheep",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.CheepEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(1.0F, 1.2F)
                    .clientTrackingRange(8)
                    .build("cheep"));



    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.DorrieEntity>> DORRIE = ENTITY_TYPES.register("dorrie",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.DorrieEntity::new, MobCategory.CREATURE)
                    .sized(1.6F, 1.4F)
                    .clientTrackingRange(10)
                    .build("dorrie"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.HerculesBeetleEntity>> HERCULES_BEETLE = ENTITY_TYPES.register("hercules_beetle",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.HerculesBeetleEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 4.0F)
                    .clientTrackingRange(12)
                    .build("hercules_beetle"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity>> OURANWOOD_DEER = ENTITY_TYPES.register("ouranwood_deer",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity::new, MobCategory.CREATURE)
                    .sized(1.125F, 1.75F)
                    .clientTrackingRange(8)
                    .build("ouranwood_deer"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity>> GLIMMER = ENTITY_TYPES.register("glimmer",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F)
                    .clientTrackingRange(8)
                    .build("glimmer"));


    public static final DeferredHolder<EntityType<?>, EntityType<com.craisinlord.antarchy.content.entity.ElkaEntity>> ELKA = ENTITY_TYPES.register("elka",
            () -> EntityType.Builder.of(com.craisinlord.antarchy.content.entity.ElkaEntity::new, MobCategory.CREATURE)
                    .sized(2.2F, 3.5F)
                    .clientTrackingRange(10)
                    .build("elka"));



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


    public static void register() {
        ENTITY_TYPES.register();
    }

}
