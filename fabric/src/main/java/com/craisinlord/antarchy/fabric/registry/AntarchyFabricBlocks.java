package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.fabric.AntarchyWoodTypes;
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
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
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

public final class AntarchyFabricBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);


    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);


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
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_OURANWOOD_ACORN = BLOCKS.register("potted_ouranwood_acorn",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(OURANWOOD_ACORN_BLOCK.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)));


    public static final DeferredBlock<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CamelliaBlock> CAMELLIA = BLOCKS.register("camellia",
            () -> new com.craisinlord.antarchy.content.block.CamelliaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.SpiderLilyBlock> SPIDER_LILY = BLOCKS.register("spider_lily",
            () -> new com.craisinlord.antarchy.content.block.SpiderLilyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_SPIDER_LILY = BLOCKS.register("potted_spider_lily",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(SPIDER_LILY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<GiantLilyPadBlock> GIANT_LILY_PAD = BLOCKS.register("giant_lily_pad",
            () -> new GiantLilyPadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.LotusBlock> LOTUS = BLOCKS.register("lotus",
            () -> new com.craisinlord.antarchy.content.block.LotusBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPORE_BLOSSOM)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_LOTUS = BLOCKS.register("potted_lotus",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(LOTUS.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<SeashellBlock> SEASHELL = BLOCKS.register("seashell",
            () -> new SeashellBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TURTLE_EGG).noOcclusion()));
    public static final DeferredBlock<CritterCageBlock> CRITTER_CAGE_BLOCK = BLOCKS.register("critter_cage_block",
            () -> new CritterCageBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion()));


    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.OURANWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.OURANWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.OURANWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_HANGING_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.OURANWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_HANGING_SIGN)));


    public static final DeferredBlock<RotatedPillarBlock> PEACH_LOG = BLOCKS.register("peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG)));


    public static final DeferredBlock<RotatedPillarBlock> PEACH_WOOD = BLOCKS.register("peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WOOD)));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_LOG = BLOCKS.register("stripped_peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_LOG)));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_WOOD = BLOCKS.register("stripped_peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_JUNGLE_WOOD)));


    public static final DeferredBlock<Block> PEACH_PLANKS = BLOCKS.register("peach_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PLANKS)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> PEACH_STAIRS = BLOCKS.register("peach_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(PEACH_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_STAIRS)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> PEACH_SLAB = BLOCKS.register("peach_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SLAB)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> PEACH_FENCE = BLOCKS.register("peach_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> PEACH_FENCE_GATE = BLOCKS.register("peach_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_FENCE_GATE)));


    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> PEACH_DOOR = BLOCKS.register("peach_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_DOOR)));


    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> PEACH_TRAPDOOR = BLOCKS.register("peach_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_TRAPDOOR)));


    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> PEACH_PRESSURE_PLATE = BLOCKS.register("peach_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_PRESSURE_PLATE)));


    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> PEACH_BUTTON = BLOCKS.register("peach_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_BUTTON)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachLeavesBlock> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            () -> new com.craisinlord.antarchy.content.block.PeachLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES).randomTicks()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.HangingPeachBlock> PEACH_HANGING_PEACH = BLOCKS.register("hanging_peach",
            () -> new com.craisinlord.antarchy.content.block.HangingPeachBlock(BlockBehaviour.Properties.of().noOcclusion().instabreak()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachSaplingBlock> PEACH_SAPLING = BLOCKS.register("peach_sapling",
            () -> new com.craisinlord.antarchy.content.block.PeachSaplingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_PEACH_SAPLING = BLOCKS.register("potted_peach_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(PEACH_SAPLING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> PEACH_SIGN = BLOCKS.register("peach_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.PEACH, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> PEACH_WALL_SIGN = BLOCKS.register("peach_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.PEACH, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> PEACH_HANGING_SIGN = BLOCKS.register("peach_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.PEACH, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_HANGING_SIGN)));


    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> PEACH_WALL_HANGING_SIGN = BLOCKS.register("peach_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.PEACH, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_WALL_HANGING_SIGN)));


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
                    AntarchyFabricItems::cloudBucketItem,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POWDER_SNOW).noLootTable().noOcclusion()
            ));


    public static final DeferredBlock<AntDimensionPortalBlock> ELYTHIA_PORTAL = BLOCKS.register("elythia_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.ELYTHIA, portalProperties()));


    public static final DeferredBlock<AntDimensionPortalBlock> THORAXIS_PORTAL = BLOCKS.register("thoraxis_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.THORAXIS, portalProperties()));


    public static final DeferredBlock<Block> PALE_NYXITE = BLOCKS.register("pale_nyxite",
            () -> new Block(nyxiteProperties()));


    public static final DeferredBlock<NyxiteSpikeBlock> NYXITE_SPIKE = BLOCKS.register("nyxite_spike",
            () -> new NyxiteSpikeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE)));


    public static final DeferredBlock<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            () -> new PotentNyxiteBlock(
                    AntarchyFabricBlocks::potentNyxiteBlockEntityType,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERRACK).lightLevel(state -> 3)
            ));


    public static final DeferredBlock<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            () -> new UmbralMossBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)));


    public static final DeferredBlock<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET).noOcclusion()));


    public static final DeferredBlock<BlushMossBlock> BLUSH_MOSS_BLOCK = BLOCKS.register("blush_moss_block",
            () -> new BlushMossBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)));


    public static final DeferredBlock<BlushMossCarpetBlock> BLUSH_MOSS_CARPET = BLOCKS.register("blush_moss_carpet",
            () -> new BlushMossCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET).noOcclusion()));


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


    public static final DeferredBlock<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            () -> new BedBugEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));


    public static final DeferredBlock<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            () -> new WaspNestBlock(AntarchyFabricBlocks::waspNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.BEE_NEST)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            () -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT).noLootTable()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock> BRUTALFLY_COCOON_SPAWNER = BLOCKS.register("brutalfly_cocoon_spawner",
            () -> new com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock(BlockBehaviour.Properties.of().noCollission().instabreak().noLootTable()));


    public static final DeferredBlock<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            () -> new HushweedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA).noCollission().noOcclusion()));


    public static final DeferredBlock<LiquidBlock> BILE_BLOCK = BLOCKS.register("bile",
            () -> new BileLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.BILE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).lightLevel(state -> 5).noLootTable()));


    public static final DeferredBlock<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            () -> new LiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ICHOR.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));


    public static final DeferredBlock<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            () -> new AntiwaterLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ANTIWATER.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));


    public static final DeferredBlock<LiquidBlock> LUMEN_BLOCK = BLOCKS.register("lumen",
            () -> new LumenLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.LUMEN.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).lightLevel(state -> 9).noLootTable()));


    public static final DeferredBlock<Block> LUMEN_FROGLIGHT = BLOCKS.register("lumen_froglight",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OCHRE_FROGLIGHT)));


    public static final DeferredBlock<Block> ROSEATE_FROGLIGHT = BLOCKS.register("roseate_froglight",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OCHRE_FROGLIGHT)));


    public static final DeferredBlock<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            () -> new AntNestBlock(
                    AntarchyFabricEntities.RED_ANT,
                    AntarchyFabricBlocks::antNestBlockEntityType,
                    true,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));


    public static final DeferredBlock<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.BROWN_ANT, AntarchyFabricBlocks::antNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noLootTable()));


    public static final DeferredBlock<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.RAINBOW_ANT, AntarchyFabricBlocks::antNestBlockEntityType, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).noLootTable()));


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


    public static final DeferredBlock<CornCropBlock> CORN_CROP = BLOCKS.register("corn_crop",
            () -> new CornCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).randomTicks().noCollission().noOcclusion()));


    public static final DeferredBlock<WildCornBlock> WILD_CORN = BLOCKS.register("wild_corn",
            () -> new WildCornBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().noOcclusion()));


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
                    AntarchyFabricBlocks::smallBloodCrystalBudBlock,
                    AntarchyFabricBlocks::mediumBloodCrystalBudBlock,
                    AntarchyFabricBlocks::largeBloodCrystalBudBlock,
                    AntarchyFabricBlocks::bloodCrystalCrystalBlock
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


    public static final DeferredBlock<Block> DEAD_STAR_CORAL_BLOCK = BLOCKS.register("dead_star_coral_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_BLOCK)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralPlantBlock> DEAD_STAR_CORAL = BLOCKS.register("dead_star_coral",
            () -> new net.minecraft.world.level.block.BaseCoralPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralFanBlock> DEAD_STAR_CORAL_FAN = BLOCKS.register("dead_star_coral_fan",
            () -> new net.minecraft.world.level.block.BaseCoralFanBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_FAN)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralWallFanBlock> DEAD_STAR_CORAL_WALL_FAN = BLOCKS.register("dead_star_coral_wall_fan",
            () -> new net.minecraft.world.level.block.BaseCoralWallFanBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_WALL_FAN)));


    public static final DeferredBlock<Block> STAR_CORAL_BLOCK = BLOCKS.register("star_coral_block",
            () -> new StarCoralBlock(DEAD_STAR_CORAL_BLOCK.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_BLOCK).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralPlantBlock> STAR_CORAL = BLOCKS.register("star_coral",
            () -> new StarCoralPlantBlock(DEAD_STAR_CORAL.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralFanBlock> STAR_CORAL_FAN = BLOCKS.register("star_coral_fan",
            () -> new StarCoralFanBlock(DEAD_STAR_CORAL_FAN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_FAN).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralWallFanBlock> STAR_CORAL_WALL_FAN = BLOCKS.register("star_coral_wall_fan",
            () -> new StarCoralWallFanBlock(DEAD_STAR_CORAL_WALL_FAN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.TUBE_CORAL_WALL_FAN).lightLevel(state -> 8)));


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
                    (pos, state) -> new PotentNyxiteBlockEntity(pos, state, AntarchyFabricBlocks::potentNyxiteBlockEntityType),
                    POTENT_NYXITE.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeashellBlockEntity>> SEASHELL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("seashell",
            () -> BlockEntityType.Builder.of(
                    SeashellBlockEntity::new,
                    SEASHELL.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("critter_cage_block",
            () -> BlockEntityType.Builder.of(
                    CritterCageBlockEntity::new,
                    CRITTER_CAGE_BLOCK.get()
            ).build(null));



    private static BlockEntityType<AntNestBlockEntity> antNestBlockEntityType() {
        return ANT_NEST_BLOCK_ENTITY.get();
    }



    private static BlockEntityType<WaspNestBlockEntity> waspNestBlockEntityType() {
        return WASP_NEST_BLOCK_ENTITY.get();
    }



    private static BlockEntityType<PotentNyxiteBlockEntity> potentNyxiteBlockEntityType() {
        return POTENT_NYXITE_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<SeashellBlockEntity> seashellBlockEntityType() {
        return SEASHELL_BLOCK_ENTITY.get();
    }



    private static Block smallBloodCrystalBudBlock() {
        return SMALL_BLOOD_CRYSTAL_BUD.get();
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



    private static BlockBehaviour.Properties portalProperties() {
        return BlockBehaviour.Properties.of()
                .strength(-1.0F)
                .noCollission()
                .noOcclusion()
                .lightLevel(state -> 11)
                .noLootTable()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
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


    public static void register() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }

}
