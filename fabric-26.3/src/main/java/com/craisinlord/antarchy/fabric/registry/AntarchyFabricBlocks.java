package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.fabric.AntarchyWoodTypes;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity;
import com.craisinlord.antarchy.content.block.DimensionalTearMarkerBlock;
import com.craisinlord.antarchy.content.block.entity.DimensionalTearMarkerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.craisinlord.antarchy.content.block.entity.UpperBlockEntity;
import com.craisinlord.antarchy.content.block.entity.UndertrialSpawnerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.craisinlord.antarchy.content.block.entity.VortexLensBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.LumenLiquidBlock;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterLiquidBlock;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.VineBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;

public final class AntarchyFabricBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);

    public static final DeferredBlock<UndertrialSpawnerBlock> UNDERTRIAL_SPAWNER = BLOCKS.register("undertrial_spawner",
            () -> new UndertrialSpawnerBlock(AntarchyFabricBlocks::undertrialSpawnerBlockEntityType,
                    DeferredRegister.copyBlockProperties(Blocks.TRIAL_SPAWNER).noOcclusion().isViewBlocking((state, level, pos, bounds) -> false)));
    public static final DeferredBlock<UnderVaultBlock> UNDERVAULT = BLOCKS.register("undervault",
            () -> new UnderVaultBlock(AntarchyFabricBlocks::underVaultBlockEntityType,
                    DeferredRegister.copyBlockProperties(Blocks.VAULT).noOcclusion().isViewBlocking((state, level, pos, bounds) -> false)));
    public static final DeferredBlock<QueenTrailSpawnMarkerBlock> QUEEN_TRAIL_SPAWN_MARKER = BLOCKS.register("queen_trail_spawn_marker",
            () -> new QueenTrailSpawnMarkerBlock(AntarchyFabricBlocks::queenTrailSpawnMarkerBlockEntityType,
                    DeferredRegister.copyBlockProperties(Blocks.BEDROCK).noLootTable()));

    public static final DeferredBlock<DuplicatorLogBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new DuplicatorLogBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_LOG).randomTicks()));

    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_LOG = BLOCKS.register("ouranwood_log",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_WOOD = BLOCKS.register("ouranwood_wood",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_LOG = BLOCKS.register("mossy_ouranwood_log",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_WOOD = BLOCKS.register("mossy_ouranwood_wood",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_LOG = BLOCKS.register("stripped_ouranwood_log",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_LOG)));

    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_WOOD = BLOCKS.register("stripped_ouranwood_wood",
            () -> new OuranwoodLogBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_WOOD)));

    public static final DeferredBlock<Block> OURANWOOD_PLANKS = BLOCKS.register("ouranwood_planks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PLANKS)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> OURANWOOD_STAIRS = BLOCKS.register("ouranwood_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(OURANWOOD_PLANKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.JUNGLE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> OURANWOOD_SLAB = BLOCKS.register("ouranwood_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> OURANWOOD_FENCE = BLOCKS.register("ouranwood_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> OURANWOOD_FENCE_GATE = BLOCKS.register("ouranwood_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE_GATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> OURANWOOD_DOOR = BLOCKS.register("ouranwood_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_DOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> OURANWOOD_TRAPDOOR = BLOCKS.register("ouranwood_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_TRAPDOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> OURANWOOD_PRESSURE_PLATE = BLOCKS.register("ouranwood_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PRESSURE_PLATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> OURANWOOD_BUTTON = BLOCKS.register("ouranwood_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_BUTTON)));

    public static final DeferredBlock<OuranwoodLeavesBlock> OURANWOOD_LEAVES = BLOCKS.register("ouranwood_leaves",
            () -> new OuranwoodLeavesBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LEAVES).randomTicks()));

    public static final DeferredBlock<OuranwoodAcornBlock> OURANWOOD_ACORN_BLOCK = BLOCKS.register("ouranwood_acorn",
            () -> new OuranwoodAcornBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_OURANWOOD_ACORN = BLOCKS.register("potted_ouranwood_acorn",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(OURANWOOD_ACORN_BLOCK.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            () -> new MilkweedBlock(DeferredRegister.copyBlockProperties(Blocks.PEONY)));

    public static final DeferredBlock<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            () -> new MilkweedBlock(DeferredRegister.copyBlockProperties(Blocks.PEONY)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.GlimmeringReedBlock> GLIMMERING_REED = BLOCKS.register("glimmering_reed",
            () -> new com.craisinlord.antarchy.content.block.GlimmeringReedBlock(DeferredRegister.copyBlockProperties(Blocks.SUNFLOWER).lightLevel(state -> 8)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CamelliaBlock> CAMELLIA = BLOCKS.register("camellia",
            () -> new com.craisinlord.antarchy.content.block.CamelliaBlock(DeferredRegister.copyBlockProperties(Blocks.PEONY)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.SpiderLilyBlock> SPIDER_LILY = BLOCKS.register("spider_lily",
            () -> new com.craisinlord.antarchy.content.block.SpiderLilyBlock(DeferredRegister.copyBlockProperties(Blocks.POPPY)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_SPIDER_LILY = BLOCKS.register("potted_spider_lily",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(SPIDER_LILY.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<GiantLilyPadBlock> GIANT_LILY_PAD = BLOCKS.register("giant_lily_pad",
            () -> new GiantLilyPadBlock(DeferredRegister.copyBlockProperties(Blocks.LILY_PAD)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.LotusBlock> LOTUS = BLOCKS.register("lotus",
            () -> new com.craisinlord.antarchy.content.block.LotusBlock(DeferredRegister.copyBlockProperties(Blocks.SPORE_BLOSSOM)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_LOTUS = BLOCKS.register("potted_lotus",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(LOTUS.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<SeashellBlock> SEASHELL = BLOCKS.register("seashell",
            () -> new SeashellBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG).noOcclusion()));
    public static final DeferredBlock<LucidAnchorBlock> LUCID_ANCHOR = BLOCKS.register("lucid_anchor",
            () -> new LucidAnchorBlock(DeferredRegister.copyBlockProperties(Blocks.BEACON).strength(3.0F)));
    public static final DeferredBlock<CritterCageBlock> CRITTER_CAGE_BLOCK = BLOCKS.register("critter_cage_block",
            () -> new CritterCageBlock(DeferredRegister.blockProperties().strength(5.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<PortalGunPortalMasterBlock> PORTAL_GUN_PORTAL_MASTER = BLOCKS.register("portal_gun_portal_master",
            () -> new PortalGunPortalMasterBlock(AntarchyFabricBlocks::portalGunPortalMasterBlockEntityType, portalProperties()));
    public static final DeferredBlock<PortalGunPortalBaseBlock> PORTAL_GUN_PORTAL_BASE = BLOCKS.register("portal_gun_portal_base",
            () -> new PortalGunPortalBaseBlock(AntarchyFabricBlocks::portalGunPortalBaseBlockEntityType, portalProperties()));
    public static final DeferredBlock<DimensionalTearMarkerBlock> DIMENSIONAL_TEAR_MARKER = BLOCKS.register("dimensional_tear_marker",
            () -> new DimensionalTearMarkerBlock(AntarchyFabricBlocks::dimensionalTearMarkerBlockEntityType,
                    DeferredRegister.blockProperties().noCollision().noOcclusion().noLootTable().instabreak()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)));

    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.OURANWOOD, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.OURANWOOD, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_SIGN).overrideDescription(OURANWOOD_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.OURANWOOD, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_HANGING_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.OURANWOOD, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_HANGING_SIGN).overrideDescription(OURANWOOD_HANGING_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<RotatedPillarBlock> PEACH_LOG = BLOCKS.register("peach_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> PEACH_WOOD = BLOCKS.register("peach_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_LOG = BLOCKS.register("stripped_peach_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_WOOD = BLOCKS.register("stripped_peach_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_WOOD)));

    public static final DeferredBlock<Block> PEACH_PLANKS = BLOCKS.register("peach_planks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PLANKS)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> PEACH_STAIRS = BLOCKS.register("peach_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(PEACH_PLANKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.JUNGLE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> PEACH_SLAB = BLOCKS.register("peach_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> PEACH_FENCE = BLOCKS.register("peach_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> PEACH_FENCE_GATE = BLOCKS.register("peach_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE_GATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> PEACH_DOOR = BLOCKS.register("peach_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_DOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> PEACH_TRAPDOOR = BLOCKS.register("peach_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_TRAPDOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> PEACH_PRESSURE_PLATE = BLOCKS.register("peach_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PRESSURE_PLATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> PEACH_BUTTON = BLOCKS.register("peach_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_BUTTON)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachLeavesBlock> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            () -> new com.craisinlord.antarchy.content.block.PeachLeavesBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LEAVES).randomTicks()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.HangingPeachBlock> PEACH_HANGING_PEACH = BLOCKS.register("hanging_peach",
            () -> new com.craisinlord.antarchy.content.block.HangingPeachBlock(DeferredRegister.blockProperties().noOcclusion().instabreak()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachSaplingBlock> PEACH_SAPLING = BLOCKS.register("peach_sapling",
            () -> new com.craisinlord.antarchy.content.block.PeachSaplingBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_PEACH_SAPLING = BLOCKS.register("potted_peach_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(PEACH_SAPLING.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> PEACH_SIGN = BLOCKS.register("peach_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.PEACH, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> PEACH_WALL_SIGN = BLOCKS.register("peach_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.PEACH, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_SIGN).overrideDescription(PEACH_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> PEACH_HANGING_SIGN = BLOCKS.register("peach_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.PEACH, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_HANGING_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> PEACH_WALL_HANGING_SIGN = BLOCKS.register("peach_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.PEACH, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_HANGING_SIGN).overrideDescription(PEACH_HANGING_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<RotatedPillarBlock> NADIR_LOG = BLOCKS.register("nadir_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> NADIR_WOOD = BLOCKS.register("nadir_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NADIR_LOG = BLOCKS.register("stripped_nadir_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NADIR_WOOD = BLOCKS.register("stripped_nadir_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_WOOD)));

    public static final DeferredBlock<Block> NADIR_PLANKS = BLOCKS.register("nadir_planks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PLANKS)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> NADIR_STAIRS = BLOCKS.register("nadir_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(NADIR_PLANKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.JUNGLE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> NADIR_SLAB = BLOCKS.register("nadir_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> NADIR_FENCE = BLOCKS.register("nadir_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> NADIR_FENCE_GATE = BLOCKS.register("nadir_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE_GATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> NADIR_DOOR = BLOCKS.register("nadir_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_DOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> NADIR_TRAPDOOR = BLOCKS.register("nadir_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_TRAPDOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> NADIR_PRESSURE_PLATE = BLOCKS.register("nadir_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PRESSURE_PLATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> NADIR_BUTTON = BLOCKS.register("nadir_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_BUTTON)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.NadirVeilBlock> NADIR_VEIL = BLOCKS.register("nadir_veil",
            () -> new com.craisinlord.antarchy.content.block.NadirVeilBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LEAVES).sound(net.minecraft.world.level.block.SoundType.SLIME_BLOCK).noOcclusion()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.NadirSaplingBlock> NADIR_SAPLING = BLOCKS.register("nadir_sapling",
            () -> new com.craisinlord.antarchy.content.block.NadirSaplingBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_NADIR_SAPLING = BLOCKS.register("potted_nadir_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(NADIR_SAPLING.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> NADIR_SIGN = BLOCKS.register("nadir_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.NADIR, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> NADIR_WALL_SIGN = BLOCKS.register("nadir_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.NADIR, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_SIGN).overrideDescription(NADIR_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> NADIR_HANGING_SIGN = BLOCKS.register("nadir_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.NADIR, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_HANGING_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> NADIR_WALL_HANGING_SIGN = BLOCKS.register("nadir_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.NADIR, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_HANGING_SIGN).overrideDescription(NADIR_HANGING_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            () -> new DuplicatorSaplingBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).randomTicks().noCollision()));

    public static final DeferredBlock<RotatedPillarBlock> ROYAL_LOG = BLOCKS.register("royal_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> ROYAL_WOOD = BLOCKS.register("royal_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ROYAL_LOG = BLOCKS.register("stripped_royal_log",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ROYAL_WOOD = BLOCKS.register("stripped_royal_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_WOOD)));

    public static final DeferredBlock<Block> ROYAL_PLANKS = BLOCKS.register("royal_planks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PLANKS)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> ROYAL_STAIRS = BLOCKS.register("royal_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(ROYAL_PLANKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.JUNGLE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> ROYAL_SLAB = BLOCKS.register("royal_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> ROYAL_FENCE = BLOCKS.register("royal_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> ROYAL_FENCE_GATE = BLOCKS.register("royal_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE_GATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> ROYAL_DOOR = BLOCKS.register("royal_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_DOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> ROYAL_TRAPDOOR = BLOCKS.register("royal_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_TRAPDOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> ROYAL_PRESSURE_PLATE = BLOCKS.register("royal_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PRESSURE_PLATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> ROYAL_BUTTON = BLOCKS.register("royal_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_BUTTON)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.RoyalLeavesBlock> ROYAL_LEAVES = BLOCKS.register("royal_leaves",
            () -> new com.craisinlord.antarchy.content.block.RoyalLeavesBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LEAVES).randomTicks()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.RoyalLeavesBlock> ROYAL_FLOWERING_LEAVES = BLOCKS.register("royal_flowering_leaves",
            () -> new com.craisinlord.antarchy.content.block.RoyalLeavesBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LEAVES).randomTicks().lightLevel(state -> 12)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.RoyalSaplingBlock> ROYAL_SAPLING = BLOCKS.register("royal_sapling",
            () -> new com.craisinlord.antarchy.content.block.RoyalSaplingBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).noCollision()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_ROYAL_SAPLING = BLOCKS.register("potted_royal_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(ROYAL_SAPLING.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> ROYAL_SIGN = BLOCKS.register("royal_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.ROYAL, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> ROYAL_WALL_SIGN = BLOCKS.register("royal_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.ROYAL, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_SIGN).overrideDescription(ROYAL_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> ROYAL_HANGING_SIGN = BLOCKS.register("royal_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.ROYAL, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_HANGING_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> ROYAL_WALL_HANGING_SIGN = BLOCKS.register("royal_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.ROYAL, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_HANGING_SIGN).overrideDescription(ROYAL_HANGING_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloLogBlock> TRUFFALO_LOG = BLOCKS.register("truffalo_log",
            () -> new com.craisinlord.antarchy.content.block.TruffaloLogBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> TRUFFALO_WOOD = BLOCKS.register("truffalo_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WOOD)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloLogBlock> STRIPPED_TRUFFALO_LOG = BLOCKS.register("stripped_truffalo_log",
            () -> new com.craisinlord.antarchy.content.block.TruffaloLogBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_TRUFFALO_WOOD = BLOCKS.register("stripped_truffalo_wood",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.STRIPPED_JUNGLE_WOOD)));

    public static final DeferredBlock<Block> TRUFFALO_PLANKS = BLOCKS.register("truffalo_planks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PLANKS)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> TRUFFALO_STAIRS = BLOCKS.register("truffalo_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(TRUFFALO_PLANKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.JUNGLE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> TRUFFALO_SLAB = BLOCKS.register("truffalo_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> TRUFFALO_FENCE = BLOCKS.register("truffalo_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE)));

    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> TRUFFALO_FENCE_GATE = BLOCKS.register("truffalo_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_FENCE_GATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> TRUFFALO_DOOR = BLOCKS.register("truffalo_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_DOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> TRUFFALO_TRAPDOOR = BLOCKS.register("truffalo_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_TRAPDOOR)));

    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> TRUFFALO_PRESSURE_PLATE = BLOCKS.register("truffalo_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_PRESSURE_PLATE)));

    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> TRUFFALO_BUTTON = BLOCKS.register("truffalo_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_BUTTON)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloSaplingBlock> TRUFFALO_SAPLING = BLOCKS.register("truffalo_sapling",
            () -> new com.craisinlord.antarchy.content.block.TruffaloSaplingBlock(DeferredRegister.copyBlockProperties(Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_TRUFFALO_SAPLING = BLOCKS.register("potted_truffalo_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(TRUFFALO_SAPLING.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION)));

    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> TRUFFALO_SIGN = BLOCKS.register("truffalo_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(AntarchyWoodTypes.TRUFFALO, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> TRUFFALO_WALL_SIGN = BLOCKS.register("truffalo_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(AntarchyWoodTypes.TRUFFALO, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_SIGN).overrideDescription(TRUFFALO_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> TRUFFALO_HANGING_SIGN = BLOCKS.register("truffalo_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(AntarchyWoodTypes.TRUFFALO, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_HANGING_SIGN)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> TRUFFALO_WALL_HANGING_SIGN = BLOCKS.register("truffalo_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(AntarchyWoodTypes.TRUFFALO, DeferredRegister.copyBlockProperties(Blocks.JUNGLE_WALL_HANGING_SIGN).overrideDescription(TRUFFALO_HANGING_SIGN.get().getDescriptionId())));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_WHITE = BLOCKS.register("white_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.white())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_ORANGE = BLOCKS.register("orange_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.orange())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_MAGENTA = BLOCKS.register("magenta_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.magenta())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_BLUE = BLOCKS.register("light_blue_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.lightBlue())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_YELLOW = BLOCKS.register("yellow_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.yellow())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_LIME = BLOCKS.register("lime_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.lime())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_PINK = BLOCKS.register("pink_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.pink())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_GRAY = BLOCKS.register("gray_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.gray())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_GRAY = BLOCKS.register("light_gray_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.lightGray())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_CYAN = BLOCKS.register("cyan_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.cyan())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_PURPLE = BLOCKS.register("purple_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.purple())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_BLUE = BLOCKS.register("blue_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.blue())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_BROWN = BLOCKS.register("brown_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.brown())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_GREEN = BLOCKS.register("green_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.green())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_RED = BLOCKS.register("red_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.red())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TruffaloTuftBlock> TRUFFALO_TUFT_BLACK = BLOCKS.register("black_truffalo_tuft",
            () -> new com.craisinlord.antarchy.content.block.TruffaloTuftBlock(DeferredRegister.copyBlockProperties(Blocks.WOOL.black())));

    public static final DeferredBlock<DuctTapeBlock> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new DuctTapeBlock(DeferredRegister.blockProperties()
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()));

    public static final DeferredBlock<InfestedRootedDirtBlock> INFESTED_ROOTED_DIRT = BLOCKS.register("infested_rooted_dirt",
            () -> new InfestedRootedDirtBlock(DeferredRegister.copyBlockProperties(Blocks.ROOTED_DIRT).randomTicks()));

    public static final DeferredBlock<InfestedCoarseDirtBlock> INFESTED_COARSE_DIRT = BLOCKS.register("infested_coarse_dirt",
            () -> new InfestedCoarseDirtBlock(DeferredRegister.copyBlockProperties(Blocks.COARSE_DIRT).randomTicks()));

    public static final DeferredBlock<Block> NYXITE = BLOCKS.register("nyxite",
            () -> new Block(nyxiteProperties()));

    public static final DeferredBlock<Block> POLISHED_NYXITE = BLOCKS.register("polished_nyxite",
            () -> new Block(nyxiteProperties()));

    public static final DeferredBlock<Block> CHISELED_NYXITE = BLOCKS.register("chiseled_nyxite",
            () -> new Block(nyxiteProperties()));

    public static final DeferredBlock<Block> NYXITE_BRICKS = BLOCKS.register("nyxite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<Block> MOSSY_NYXITE_BRICKS = BLOCKS.register("mossy_nyxite_bricks",
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

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock> POLISHED_NYXITE_PRESSURE_PLATE = BLOCKS.register("polished_nyxite_pressure_plate",
            () -> new com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.STONE, DeferredRegister.copyBlockProperties(Blocks.STONE_PRESSURE_PLATE)));
    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> POLISHED_NYXITE_BUTTON = BLOCKS.register("polished_nyxite_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.STONE, 20, DeferredRegister.copyBlockProperties(Blocks.STONE_BUTTON)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> MOSSY_NYXITE_BRICK_STAIRS = BLOCKS.register("mossy_nyxite_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(MOSSY_NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> MOSSY_NYXITE_BRICK_SLAB = BLOCKS.register("mossy_nyxite_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> MOSSY_NYXITE_BRICK_WALL = BLOCKS.register("mossy_nyxite_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));

    public static final DeferredBlock<RotatedPillarBlock> NYXITE_PILLAR = BLOCKS.register("nyxite_pillar",
            () -> new RotatedPillarBlock(nyxiteProperties()));

    public static final DeferredBlock<Block> SHELLSTONE = BLOCKS.register("shellstone",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<Block> POLISHED_SHELLSTONE = BLOCKS.register("polished_shellstone",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<Block> SHELLSTONE_BRICKS = BLOCKS.register("shellstone_bricks",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<Block> CHISELED_SHELLSTONE = BLOCKS.register("chiseled_shellstone",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<Block> MOSSY_SHELLSTONE_BRICKS = BLOCKS.register("mossy_shellstone_bricks",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<Block> CRACKED_SHELLSTONE_BRICKS = BLOCKS.register("cracked_shellstone_bricks",
            () -> new Block(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> MOSSY_SHELLSTONE_BRICK_STAIRS = BLOCKS.register("mossy_shellstone_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(MOSSY_SHELLSTONE_BRICKS.get().defaultBlockState(), DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> MOSSY_SHELLSTONE_BRICK_SLAB = BLOCKS.register("mossy_shellstone_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> MOSSY_SHELLSTONE_BRICK_WALL = BLOCKS.register("mossy_shellstone_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SHELLSTONE_STAIRS = BLOCKS.register("shellstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SHELLSTONE.get().defaultBlockState(), DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SHELLSTONE_SLAB = BLOCKS.register("shellstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> SHELLSTONE_WALL = BLOCKS.register("shellstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> POLISHED_SHELLSTONE_STAIRS = BLOCKS.register("polished_shellstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(POLISHED_SHELLSTONE.get().defaultBlockState(), DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> POLISHED_SHELLSTONE_SLAB = BLOCKS.register("polished_shellstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> POLISHED_SHELLSTONE_WALL = BLOCKS.register("polished_shellstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SHELLSTONE_BRICK_STAIRS = BLOCKS.register("shellstone_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SHELLSTONE_BRICKS.get().defaultBlockState(), DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SHELLSTONE_BRICK_SLAB = BLOCKS.register("shellstone_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> SHELLSTONE_BRICK_WALL = BLOCKS.register("shellstone_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<RotatedPillarBlock> SHELLSTONE_PILLAR = BLOCKS.register("shellstone_pillar",
            () -> new RotatedPillarBlock(DeferredRegister.withBlockId(AntarchyObjects.shellstoneProperties())));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.FlytrapGooBlock> FLYTRAP_GOO_BLOCK = BLOCKS.register("flytrap_goo_block",
            () -> new com.craisinlord.antarchy.content.block.FlytrapGooBlock(DeferredRegister.copyBlockProperties(Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p, b) -> false).isSuffocating((s, l, p) -> false)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            () -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyFabricItems::cloudBucketItem,
                    DeferredRegister.copyBlockProperties(Blocks.POWDER_SNOW).noLootTable().noOcclusion().emissiveRendering(state -> true)
            ));

    public static final DeferredBlock<AntDimensionPortalBlock> ELYTHIA_PORTAL = BLOCKS.register("elythia_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.ELYTHIA, portalProperties()));

    public static final DeferredBlock<AntDimensionPortalBlock> THORAXIS_PORTAL = BLOCKS.register("thoraxis_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.THORAXIS, portalProperties()));

    public static final DeferredBlock<AntDimensionPortalBlock> CAVARYN_PORTAL = BLOCKS.register("cavaryn_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.CAVARYN, portalProperties()));

    public static final DeferredBlock<Block> PALE_NYXITE = BLOCKS.register("pale_nyxite",
            () -> new Block(nyxiteProperties()));

    public static final DeferredBlock<NyxiteSpikeBlock> NYXITE_SPIKE = BLOCKS.register("nyxite_spike",
            () -> new NyxiteSpikeBlock(DeferredRegister.copyBlockProperties(Blocks.POINTED_DRIPSTONE)));

    public static final DeferredBlock<ChitinSpikeBlock> CHITIN_SPIKE = BLOCKS.register("chitin_spike",
            () -> new ChitinSpikeBlock(DeferredRegister.copyBlockProperties(Blocks.POINTED_DRIPSTONE)));

    public static final DeferredBlock<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            () -> new PotentNyxiteBlock(
                    AntarchyFabricBlocks::potentNyxiteBlockEntityType,
                    DeferredRegister.copyBlockProperties(Blocks.NETHERRACK).lightLevel(state -> 3)
            ));

    public static final DeferredBlock<Block> MYRMITE = BLOCKS.register("myrmite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<StairBlock> MYRMITE_STAIRS = BLOCKS.register("myrmite_stairs",
            () -> new StairBlock(MYRMITE.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<SlabBlock> MYRMITE_SLAB = BLOCKS.register("myrmite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<WallBlock> MYRMITE_WALL = BLOCKS.register("myrmite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final DeferredBlock<Block> POLISHED_MYRMITE = BLOCKS.register("polished_myrmite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<StairBlock> POLISHED_MYRMITE_STAIRS = BLOCKS.register("polished_myrmite_stairs",
            () -> new StairBlock(POLISHED_MYRMITE.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<SlabBlock> POLISHED_MYRMITE_SLAB = BLOCKS.register("polished_myrmite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<WallBlock> POLISHED_MYRMITE_WALL = BLOCKS.register("polished_myrmite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final DeferredBlock<Block> CHISELED_MYRMITE = BLOCKS.register("chiseled_myrmite",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<Block> MYRMITE_BRICKS = BLOCKS.register("myrmite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<StairBlock> MYRMITE_BRICK_STAIRS = BLOCKS.register("myrmite_brick_stairs",
            () -> new StairBlock(MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<SlabBlock> MYRMITE_BRICK_SLAB = BLOCKS.register("myrmite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<WallBlock> MYRMITE_BRICK_WALL = BLOCKS.register("myrmite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final DeferredBlock<Block> MOSSY_MYRMITE_BRICKS = BLOCKS.register("mossy_myrmite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final DeferredBlock<StairBlock> MOSSY_MYRMITE_BRICK_STAIRS = BLOCKS.register("mossy_myrmite_brick_stairs",
            () -> new StairBlock(MOSSY_MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final DeferredBlock<SlabBlock> MOSSY_MYRMITE_BRICK_SLAB = BLOCKS.register("mossy_myrmite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final DeferredBlock<WallBlock> MOSSY_MYRMITE_BRICK_WALL = BLOCKS.register("mossy_myrmite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final DeferredBlock<RotatedPillarBlock> MYRMITE_PILLAR = BLOCKS.register("myrmite_pillar",
            () -> new RotatedPillarBlock(nyxiteProperties()));

    public static final DeferredBlock<Block> BIOMITE = BLOCKS.register("biomite",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.NETHERRACK)));

    public static final DeferredBlock<Block> BIOMITE_TURF = BLOCKS.register("biomite_turf",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.NETHERRACK).lightLevel(state -> 2)));

    public static final DeferredBlock<BiowartBlock> BIOWART = BLOCKS.register("biowart",
            () -> new BiowartBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_BLOCK).lightLevel(state -> 2)));

    public static final DeferredBlock<BiowartTendrilsBlock> BIOWART_TENDRILS = BLOCKS.register("biowart_tendrils",
            () -> new BiowartTendrilsBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_CARPET).lightLevel(state -> 2).noCollision().noOcclusion()));

    public static final DeferredBlock<Block> MYRMITE_COAL_ORE = BLOCKS.register("myrmite_coal_ore",
            () -> createOre(Blocks.COAL_ORE, 0, 2, MapColor.COLOR_GRAY));

    public static final DeferredBlock<Block> BROODSTONE = BLOCKS.register("broodstone",
            () -> new Block(broodstoneProperties()));

    public static final DeferredBlock<Block> POLISHED_BROODSTONE = BLOCKS.register("polished_broodstone",
            () -> new Block(broodstoneProperties()));

    public static final DeferredBlock<Block> CHISELED_BROODSTONE = BLOCKS.register("chiseled_broodstone",
            () -> new Block(broodstoneProperties()));

    public static final DeferredBlock<Block> BROODSTONE_BRICKS = BLOCKS.register("broodstone_bricks",
            () -> new Block(broodstoneProperties()));
    public static final DeferredBlock<Block> MOSSY_BROODSTONE_BRICKS = BLOCKS.register("mossy_broodstone_bricks",
            () -> new Block(broodstoneProperties()));

    public static final DeferredBlock<StairBlock> BROODSTONE_STAIRS = BLOCKS.register("broodstone_stairs",
            () -> new StairBlock(BROODSTONE.get().defaultBlockState(), broodstoneProperties()));

    public static final DeferredBlock<SlabBlock> BROODSTONE_SLAB = BLOCKS.register("broodstone_slab",
            () -> new SlabBlock(broodstoneProperties()));

    public static final DeferredBlock<WallBlock> BROODSTONE_WALL = BLOCKS.register("broodstone_wall",
            () -> new WallBlock(broodstoneProperties()));

    public static final DeferredBlock<StairBlock> POLISHED_BROODSTONE_STAIRS = BLOCKS.register("polished_broodstone_stairs",
            () -> new StairBlock(POLISHED_BROODSTONE.get().defaultBlockState(), broodstoneProperties()));

    public static final DeferredBlock<SlabBlock> POLISHED_BROODSTONE_SLAB = BLOCKS.register("polished_broodstone_slab",
            () -> new SlabBlock(broodstoneProperties()));

    public static final DeferredBlock<WallBlock> POLISHED_BROODSTONE_WALL = BLOCKS.register("polished_broodstone_wall",
            () -> new WallBlock(broodstoneProperties()));

    public static final DeferredBlock<StairBlock> BROODSTONE_BRICK_STAIRS = BLOCKS.register("broodstone_brick_stairs",
            () -> new StairBlock(BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties()));

    public static final DeferredBlock<SlabBlock> BROODSTONE_BRICK_SLAB = BLOCKS.register("broodstone_brick_slab",
            () -> new SlabBlock(broodstoneProperties()));

    public static final DeferredBlock<WallBlock> BROODSTONE_BRICK_WALL = BLOCKS.register("broodstone_brick_wall",
            () -> new WallBlock(broodstoneProperties()));
    public static final DeferredBlock<StairBlock> MOSSY_BROODSTONE_BRICK_STAIRS = BLOCKS.register("mossy_broodstone_brick_stairs",
            () -> new StairBlock(MOSSY_BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties()));
    public static final DeferredBlock<SlabBlock> MOSSY_BROODSTONE_BRICK_SLAB = BLOCKS.register("mossy_broodstone_brick_slab",
            () -> new SlabBlock(broodstoneProperties()));
    public static final DeferredBlock<WallBlock> MOSSY_BROODSTONE_BRICK_WALL = BLOCKS.register("mossy_broodstone_brick_wall",
            () -> new WallBlock(broodstoneProperties()));

    public static final DeferredBlock<RotatedPillarBlock> BROODSTONE_PILLAR = BLOCKS.register("broodstone_pillar",
            () -> new RotatedPillarBlock(broodstoneProperties()));

    public static final DeferredBlock<Block> BROODSTONE_URANIUM_ORE = BLOCKS.register("broodstone_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> BROODSTONE_TITANIUM_ORE = BLOCKS.register("broodstone_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<RotatedPillarBlock> CHITIN_BLOCK = BLOCKS.register("chitin_block",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.BONE_BLOCK).requiresCorrectToolForDrops()));

    public static final DeferredBlock<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            () -> new UmbralMossBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_BLOCK)));

    public static final DeferredBlock<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            () -> new UmbralMossCarpetBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_CARPET).noOcclusion()));

    public static final DeferredBlock<AmberMossBlock> AMBER_MOSS_BLOCK = BLOCKS.register("amber_moss_block",
            () -> new AmberMossBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_BLOCK)));

    public static final DeferredBlock<UmbralMossCarpetBlock> AMBER_MOSS_CARPET = BLOCKS.register("amber_moss_carpet",
            () -> new UmbralMossCarpetBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_CARPET).noOcclusion()));

    public static final DeferredBlock<AmberLichenBlock> AMBER_LICHEN = BLOCKS.register("amber_lichen",
            () -> new AmberLichenBlock(DeferredRegister.copyBlockProperties(Blocks.GLOW_LICHEN).lightLevel(state -> 4)));

    public static final DeferredBlock<Block> BILE_VEIN = BLOCKS.register("bile_vein",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.NETHERRACK).lightLevel(state -> 2)));

    public static final DeferredBlock<CreepvineBlock> CREEPVINE = BLOCKS.register("creepvine",
            () -> new CreepvineBlock(DeferredRegister.copyBlockProperties(Blocks.VINE)));

    public static final DeferredBlock<BlushMossBlock> BLUSH_MOSS_BLOCK = BLOCKS.register("blush_moss_block",
            () -> new BlushMossBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_BLOCK)));

    public static final DeferredBlock<BlushMossCarpetBlock> BLUSH_MOSS_CARPET = BLOCKS.register("blush_moss_carpet",
            () -> new BlushMossCarpetBlock(DeferredRegister.copyBlockProperties(Blocks.MOSS_CARPET).noOcclusion()));

    public static final DeferredBlock<DreamTorchBlock> DREAM_TORCH = BLOCKS.register("dream_torch",
            () -> new DreamTorchBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_TORCH)));

    public static final DeferredBlock<DreamWallTorchBlock> DREAM_WALL_TORCH = BLOCKS.register("dream_wall_torch",
            () -> new DreamWallTorchBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_WALL_TORCH).overrideDescription(DREAM_TORCH.get().getDescriptionId())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock> DREAM_CEILING_TORCH = BLOCKS.register("dream_ceiling_torch",
            () -> new com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_TORCH)));

    public static final DeferredBlock<net.minecraft.world.level.block.LanternBlock> DREAM_LANTERN = BLOCKS.register("dream_lantern",
            () -> new net.minecraft.world.level.block.LanternBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_LANTERN)));

    public static final DeferredBlock<DreamCampfireBlock> DREAM_CAMPFIRE = BLOCKS.register("dream_campfire",
            () -> new DreamCampfireBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_CAMPFIRE)));

    public static final DeferredBlock<DreamFireBlock> DREAM_FIRE = BLOCKS.register("dream_fire",
            () -> new DreamFireBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_FIRE)));

    public static final DeferredBlock<DreamCeilingFireBlock> DREAM_CEILING_FIRE = BLOCKS.register("dream_fire_ceiling",
            () -> new DreamCeilingFireBlock(DeferredRegister.copyBlockProperties(Blocks.SOUL_FIRE)));

    public static final DeferredBlock<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            () -> new BedBugEggBlock(DeferredRegister.blockProperties()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollision()
                    .replaceable()));

    public static final DeferredBlock<CrawlingBlightEggBlock> CRAWLING_BLIGHT_EGG = BLOCKS.register("crawling_blight_egg",
            () -> new CrawlingBlightEggBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PrinceEggBlock> PRINCE_EGG = BLOCKS.register("prince_egg",
            () -> new com.craisinlord.antarchy.content.block.PrinceEggBlock(DeferredRegister.copyBlockProperties(Blocks.DRAGON_EGG).randomTicks().noOcclusion()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PrincessEggBlock> PRINCESS_EGG = BLOCKS.register("princess_egg",
            () -> new com.craisinlord.antarchy.content.block.PrincessEggBlock(DeferredRegister.copyBlockProperties(Blocks.DRAGON_EGG).randomTicks().noOcclusion()));

    public static final DeferredBlock<SpringbugEggBlock> SPRINGBUG_EGG = BLOCKS.register("springbug_egg",
            () -> new SpringbugEggBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG)));

    public static final DeferredBlock<SpitBugEggBlock> SPIT_BUG_EGG = BLOCKS.register("spit_bug_egg",
            () -> new SpitBugEggBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG)));

    public static final DeferredBlock<JerryEggBlock> JERRY_EGG = BLOCKS.register("jerry_egg",
            () -> new JerryEggBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG).randomTicks()));

    public static final DeferredBlock<SkulkingFrightEggBlock> SKULKING_FRIGHT_EGG = BLOCKS.register("skulking_fright_egg",
            () -> new SkulkingFrightEggBlock(DeferredRegister.copyBlockProperties(Blocks.TURTLE_EGG)));

    static {
        BiowartBlock.bindTendrils(BIOWART_TENDRILS);
    }

    public static final DeferredBlock<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            () -> new WaspNestBlock(AntarchyFabricBlocks::waspNestBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.BEE_NEST)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            () -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(DeferredRegister.copyBlockProperties(Blocks.COARSE_DIRT).noLootTable()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock> BRUTALFLY_COCOON_SPAWNER = BLOCKS.register("brutalfly_cocoon_spawner",
            () -> new com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock(DeferredRegister.blockProperties().noCollision().instabreak().noLootTable()));

    public static final DeferredBlock<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            () -> new HushweedBlock(DeferredRegister.copyBlockProperties(Blocks.AZALEA).noCollision().noOcclusion()));

    public static final DeferredBlock<HangingCreeprootsBlock> HANGING_CREEPROOTS = BLOCKS.register("hanging_creeproots",
            () -> new HangingCreeprootsBlock(DeferredRegister.copyBlockProperties(Blocks.HANGING_ROOTS)));

    public static final DeferredBlock<MoltingVinesBlock> MOLTING_VINES = BLOCKS.register("molting_vines",
            () -> new MoltingVinesBlock(DeferredRegister.copyBlockProperties(Blocks.WEEPING_VINES)));

    public static final DeferredBlock<LiquidBlock> BILE_BLOCK = BLOCKS.register("bile",
            () -> new BileLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.BILE.get(), DeferredRegister.copyBlockProperties(Blocks.WATER).lightLevel(state -> 5).noLootTable()));

    public static final DeferredBlock<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            () -> new LiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ICHOR.get(), DeferredRegister.copyBlockProperties(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            () -> new AntiwaterLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ANTIWATER.get(),
                    DeferredRegister.copyBlockProperties(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> LUMEN_BLOCK = BLOCKS.register("lumen",
            () -> new LumenLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.LUMEN.get(),
                    DeferredRegister.copyBlockProperties(Blocks.WATER).lightLevel(state -> 9).noLootTable()));

    public static final DeferredBlock<Block> LUMEN_FROGLIGHT = BLOCKS.register("lumen_froglight",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.OCHRE_FROGLIGHT)));

    public static final DeferredBlock<Block> ROSEATE_FROGLIGHT = BLOCKS.register("roseate_froglight",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.OCHRE_FROGLIGHT)));

    public static final DeferredBlock<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            () -> new AntNestBlock(
                    AntarchyFabricEntities.RED_ANT,
                    AntarchyFabricBlocks::antNestBlockEntityType,
                    true,
                    DeferredRegister.copyBlockProperties(Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));

    public static final DeferredBlock<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.BROWN_ANT, AntarchyFabricBlocks::antNestBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.DIRT).noLootTable()));

    public static final DeferredBlock<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.RAINBOW_ANT, AntarchyFabricBlocks::antNestBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.DIRT).noLootTable()));

    public static final DeferredBlock<AntNestBlock> TERMITE_NEST = BLOCKS.register("termite_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.TERMITE, AntarchyFabricBlocks::antNestBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.DIRT).noLootTable()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntTrapBlock> ANT_TRAP = BLOCKS.register("ant_trap",
            () -> new com.craisinlord.antarchy.content.block.AntTrapBlock(
                    () -> java.util.List.of(AntarchyFabricEntities.RED_ANT.get(), AntarchyFabricEntities.BROWN_ANT.get(), AntarchyFabricEntities.RAINBOW_ANT.get(), AntarchyFabricEntities.TERMITE.get()),
                    AntarchyFabricBlocks::antNestBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.OAK_PRESSURE_PLATE).noOcclusion()));

    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> createOre(Blocks.EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.register("titanium_ore",
            () -> createOre(Blocks.DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.register("deepslate_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<BluestoneOreBlock> BLUESTONE_ORE = BLOCKS.register("bluestone_ore",
            () -> new BluestoneOreBlock(DeferredRegister.copyBlockProperties(Blocks.REDSTONE_ORE).mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<BluestoneWireBlock> BLUESTONE_WIRE = BLOCKS.register("bluestone_wire",
            () -> new BluestoneWireBlock(DeferredRegister.copyBlockProperties(Blocks.REDSTONE_WIRE).mapColor(MapColor.COLOR_BLUE).noCollision().instabreak()));

    public static final DeferredBlock<BluestoneBlock> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block",
            () -> new BluestoneBlock(DeferredRegister.copyBlockProperties(Blocks.REDSTONE_BLOCK).mapColor(MapColor.COLOR_BLUE)));
    public static final DeferredBlock<VortexLensBlock> VORTEX_LENS = BLOCKS.register("vortex_lens",
            () -> new VortexLensBlock(AntarchyFabricBlocks::vortexLensBlockEntityType,
                    DeferredRegister.copyBlockProperties(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.COLOR_GREEN).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<BluestoneRepeaterBlock> BLUESTONE_REPEATER = BLOCKS.register("bluestone_repeater",
            () -> new BluestoneRepeaterBlock(DeferredRegister.copyBlockProperties(Blocks.REPEATER).mapColor(MapColor.COLOR_BLUE).noCollision()));
    public static final DeferredBlock<BluestoneComparatorBlock> BLUESTONE_COMPARATOR = BLOCKS.register("bluestone_comparator",
            () -> new BluestoneComparatorBlock(DeferredRegister.copyBlockProperties(Blocks.COMPARATOR).mapColor(MapColor.COLOR_BLUE).noCollision(), AntarchyFabricBlocks::bluestoneComparatorBlockEntityType));
    public static final DeferredBlock<BluestoneTorchBlock> BLUESTONE_TORCH = BLOCKS.register("bluestone_torch",
            () -> new BluestoneTorchBlock(DeferredRegister.copyBlockProperties(Blocks.REDSTONE_TORCH).mapColor(MapColor.COLOR_BLUE).lightLevel(state -> state.getValue(BluestoneTorchBlock.LIT) ? 7 : 0).noCollision().instabreak()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.BluestoneLampBlock> BLUESTONE_LAMP = BLOCKS.register("bluestone_lamp",
            () -> new com.craisinlord.antarchy.content.block.BluestoneLampBlock(DeferredRegister.copyBlockProperties(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<Block> TIGERS_EYE_BLOCK = BLOCKS.register("tigers_eye_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK, MapColor.COLOR_ORANGE));

    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.register("uranium_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK, MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> createStorageBlock(Blocks.DIAMOND_BLOCK, MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<Block> RAW_URANIUM_BLOCK = BLOCKS.register("raw_uranium_block",
            () -> createRawStorageBlock(Blocks.EMERALD_ORE, MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = BLOCKS.register("raw_titanium_block",
            () -> createRawStorageBlock(Blocks.DIAMOND_ORE, MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<Block> CUT_URANIUM = BLOCKS.register("cut_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER.weathering().unaffected(), MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> CUT_TITANIUM = BLOCKS.register("cut_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER.weathering().unaffected(), MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_URANIUM_SLAB = BLOCKS.register("cut_uranium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.CUT_COPPER_SLAB.weathering().unaffected()).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_TITANIUM_SLAB = BLOCKS.register("cut_titanium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.CUT_COPPER_SLAB.weathering().unaffected()).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_URANIUM_STAIRS = BLOCKS.register("cut_uranium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_URANIUM.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.CUT_COPPER_STAIRS.weathering().unaffected()).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_TITANIUM_STAIRS = BLOCKS.register("cut_titanium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_TITANIUM.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.CUT_COPPER_STAIRS.weathering().unaffected()).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<Block> CHISELED_URANIUM = BLOCKS.register("chiseled_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_COPPER.weathering().unaffected(), MapColor.COLOR_YELLOW));

    public static final DeferredBlock<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_COPPER.weathering().unaffected(), MapColor.COLOR_LIGHT_BLUE));

    public static final DeferredBlock<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            () -> new SignalSavingBulbBlock(DeferredRegister.copyBlockProperties(Blocks.COPPER_BULB.weathering().unaffected()).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            () -> new SignalSavingBulbBlock(DeferredRegister.copyBlockProperties(Blocks.COPPER_BULB.weathering().unaffected()).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> URANIUM_DOOR = BLOCKS.register("uranium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, DeferredRegister.copyBlockProperties(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> TITANIUM_DOOR = BLOCKS.register("titanium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, DeferredRegister.copyBlockProperties(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> URANIUM_TRAPDOOR = BLOCKS.register("uranium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, DeferredRegister.copyBlockProperties(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> TITANIUM_TRAPDOOR = BLOCKS.register("titanium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.IRON, DeferredRegister.copyBlockProperties(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> URANIUM_BARS = BLOCKS.register("uranium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(DeferredRegister.copyBlockProperties(Blocks.IRON_BARS).mapColor(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> TITANIUM_BARS = BLOCKS.register("titanium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(DeferredRegister.copyBlockProperties(Blocks.IRON_BARS).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> ANTIMETAL = BLOCKS.register("antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.BASALT).sound(antimetalSoundType())));

    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<StairBlock> ANTIMETAL_STAIRS = BLOCKS.register("antimetal_stairs",
            () -> new StairBlock(ANTIMETAL.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<SlabBlock> ANTIMETAL_SLAB = BLOCKS.register("antimetal_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<StairBlock> POLISHED_ANTIMETAL_STAIRS = BLOCKS.register("polished_antimetal_stairs",
            () -> new StairBlock(POLISHED_ANTIMETAL.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<SlabBlock> POLISHED_ANTIMETAL_SLAB = BLOCKS.register("polished_antimetal_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            () -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(DeferredRegister.copyBlockProperties(Blocks.SCAFFOLDING).sound(antimetalScaffoldingSoundType())));

    public static final DeferredBlock<UpperBlock> UPPER = BLOCKS.register("upper",
            () -> new UpperBlock(AntarchyFabricBlocks::upperBlockEntityType, DeferredRegister.copyBlockProperties(Blocks.HOPPER)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalRailBlock> ANTIMETAL_RAIL = BLOCKS.register("antimetal_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalRailBlock(DeferredRegister.copyBlockProperties(Blocks.RAIL)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock> ANTIMETAL_POWERED_RAIL = BLOCKS.register("antimetal_powered_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock(DeferredRegister.copyBlockProperties(Blocks.POWERED_RAIL)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock> ANTIMETAL_DETECTOR_RAIL = BLOCKS.register("antimetal_detector_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock(DeferredRegister.copyBlockProperties(Blocks.DETECTOR_RAIL)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock> ANTIMETAL_ACTIVATOR_RAIL = BLOCKS.register("antimetal_activator_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock(DeferredRegister.copyBlockProperties(Blocks.ACTIVATOR_RAIL)));

    public static final DeferredBlock<CorneaStalkBlock> CORNEA_STALK = BLOCKS.register("cornea_stalk",
            () -> new CorneaStalkBlock(DeferredRegister.copyBlockProperties(Blocks.SWEET_BERRY_BUSH).randomTicks()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.GlowcapMushroomBlock> GLOWCAP_MUSHROOM = BLOCKS.register("glowcap_mushroom",
            () -> new com.craisinlord.antarchy.content.block.GlowcapMushroomBlock(DeferredRegister.copyBlockProperties(Blocks.RED_MUSHROOM).lightLevel(state -> 12).randomTicks()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_GLOWCAP_MUSHROOM = BLOCKS.register("potted_glowcap_mushroom",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(GLOWCAP_MUSHROOM.get(), DeferredRegister.copyBlockProperties(Blocks.POTTED_DANDELION).lightLevel(state -> 12)));

    public static final DeferredBlock<net.minecraft.world.level.block.HugeMushroomBlock> GLOWCAP_MUSHROOM_BLOCK = BLOCKS.register("glowcap_mushroom_block",
            () -> new net.minecraft.world.level.block.HugeMushroomBlock(DeferredRegister.copyBlockProperties(Blocks.RED_MUSHROOM_BLOCK).lightLevel(state -> 13)));

    public static final DeferredBlock<CornCropBlock> CORN_CROP = BLOCKS.register("corn_crop",
            () -> new CornCropBlock(DeferredRegister.copyBlockProperties(Blocks.WHEAT).mapColor(net.minecraft.world.level.material.MapColor.PLANT).randomTicks().noCollision().noOcclusion()));

    public static final DeferredBlock<WildCornBlock> WILD_CORN = BLOCKS.register("wild_corn",
            () -> new WildCornBlock(DeferredRegister.copyBlockProperties(Blocks.WHEAT).mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollision().noOcclusion()));

    public static final DeferredBlock<Block> FALLEN_KING_CROWN_BLOCK = BLOCKS.register("fallen_king_crown",
            () -> new Block(DeferredRegister.blockProperties()
                    .strength(0.2F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos, bounds) -> false)));

    public static final DeferredBlock<AmethystClusterBlock> SMALL_BLOOD_CRYSTAL_BUD = BLOCKS.register("small_blood_crystal_bud",
            () -> new AmethystClusterBlock(3.0F, 4.0F, DeferredRegister.copyBlockProperties(Blocks.SMALL_AMETHYST_BUD)));

    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_BLOOD_CRYSTAL_BUD = BLOCKS.register("medium_blood_crystal_bud",
            () -> new AmethystClusterBlock(4.0F, 3.0F, DeferredRegister.copyBlockProperties(Blocks.MEDIUM_AMETHYST_BUD)));

    public static final DeferredBlock<AmethystClusterBlock> LARGE_BLOOD_CRYSTAL_BUD = BLOCKS.register("large_blood_crystal_bud",
            () -> new AmethystClusterBlock(5.0F, 3.0F, DeferredRegister.copyBlockProperties(Blocks.LARGE_AMETHYST_BUD)));

    public static final DeferredBlock<Block> BUDDING_BLOOD_CRYSTAL = BLOCKS.register("budding_blood_crystal",
            () -> new BuddingBloodCrystalBlock(
                    DeferredRegister.copyBlockProperties(Blocks.BUDDING_AMETHYST),
                    AntarchyFabricBlocks::smallBloodCrystalBudBlock,
                    AntarchyFabricBlocks::mediumBloodCrystalBudBlock,
                    AntarchyFabricBlocks::largeBloodCrystalBudBlock,
                    AntarchyFabricBlocks::bloodCrystalCrystalBlock
            ));

    public static final DeferredBlock<Block> BLOOD_CRYSTAL = BLOCKS.register("blood_crystal_block",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.AMETHYST_BLOCK)));

    public static final DeferredBlock<AmethystClusterBlock> BLOOD_CRYSTAL_CRYSTAL = BLOCKS.register("blood_crystal_cluster",
            () -> new AmethystClusterBlock(7.0F, 3.0F, DeferredRegister.copyBlockProperties(Blocks.AMETHYST_CLUSTER)));

    public static final DeferredBlock<Block> DREAM_SAND = BLOCKS.register("dream_sand",
            () -> new DreamSandBlock(DeferredRegister.copyBlockProperties(Blocks.SAND)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntigravelBlock> ANTIGRAVEL = BLOCKS.register("antigravel",
            () -> new com.craisinlord.antarchy.content.block.AntigravelBlock(DeferredRegister.copyBlockProperties(Blocks.GRAVEL)));

    public static final DeferredBlock<Block> LOAM = BLOCKS.register("loam",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.MUD)));
    public static final DeferredBlock<Block> PRIMORDIAL_LOAM = BLOCKS.register("primordial_loam",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.MUD).sound(primordialLoamSoundType())));
    public static final DeferredBlock<Block> PACKED_LOAM = BLOCKS.register("packed_loam",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.PACKED_MUD)));
    public static final DeferredBlock<Block> LOAM_BRICKS = BLOCKS.register("loam_bricks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.MUD_BRICKS)));
    public static final DeferredBlock<StairBlock> LOAM_BRICK_STAIRS = BLOCKS.register("loam_brick_stairs",
            () -> new StairBlock(LOAM_BRICKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.MUD_BRICK_STAIRS)));
    public static final DeferredBlock<SlabBlock> LOAM_BRICK_SLAB = BLOCKS.register("loam_brick_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.MUD_BRICK_SLAB)));
    public static final DeferredBlock<WallBlock> LOAM_BRICK_WALL = BLOCKS.register("loam_brick_wall",
            () -> new WallBlock(DeferredRegister.copyBlockProperties(Blocks.MUD_BRICK_WALL)));
    public static final DeferredBlock<Block> CHISELED_LOAM_BRICKS = BLOCKS.register("chiseled_loam_bricks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.MUD_BRICKS)));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.MucusBlock> MUCUS = BLOCKS.register("mucus",
            () -> new com.craisinlord.antarchy.content.block.MucusBlock(DeferredRegister.copyBlockProperties(Blocks.GLOW_LICHEN).friction(0.995F).lightLevel(state -> 0).sound(SoundType.SLIME_BLOCK)));

    public static final DeferredBlock<DreamSandstoneBlock> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            () -> new DreamSandstoneBlock(DeferredRegister.copyBlockProperties(Blocks.SANDSTONE)));

    public static final DeferredBlock<Block> CHISELED_DREAM_SANDSTONE = BLOCKS.register("chiseled_dream_sandstone",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.CHISELED_SANDSTONE)));

    public static final DeferredBlock<Block> CUT_DREAM_SANDSTONE = BLOCKS.register("cut_dream_sandstone",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.CUT_SANDSTONE)));

    public static final DeferredBlock<Block> SMOOTH_DREAM_SANDSTONE = BLOCKS.register("smooth_dream_sandstone",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.SMOOTH_SANDSTONE)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> DREAM_SANDSTONE_STAIRS = BLOCKS.register("dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(DREAM_SANDSTONE.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.SANDSTONE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> DREAM_SANDSTONE_SLAB = BLOCKS.register("dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.SANDSTONE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> DREAM_SANDSTONE_WALL = BLOCKS.register("dream_sandstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(DeferredRegister.copyBlockProperties(Blocks.SANDSTONE_WALL)));

    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SMOOTH_DREAM_SANDSTONE_STAIRS = BLOCKS.register("smooth_dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SMOOTH_DREAM_SANDSTONE.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.SMOOTH_SANDSTONE_STAIRS)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SMOOTH_DREAM_SANDSTONE_SLAB = BLOCKS.register("smooth_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.SMOOTH_SANDSTONE_SLAB)));

    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_DREAM_SANDSTONE_SLAB = BLOCKS.register("cut_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(DeferredRegister.copyBlockProperties(Blocks.CUT_SANDSTONE_SLAB)));

    public static final DeferredBlock<Block> TYPHONITE = BLOCKS.register("typhonite",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE)));
    public static final DeferredBlock<DirectionalNyxiumBlock> NADIR_NYXIUM = BLOCKS.register("nadir_nyxium",
            () -> new DirectionalNyxiumBlock(nyxiteProperties(), DirectionalNyxiumBlock.Type.NADIR));
    public static final DeferredBlock<DirectionalNyxiumBlock> VERDANT_NYXIUM = BLOCKS.register("verdant_nyxium",
            () -> new DirectionalNyxiumBlock(nyxiteProperties(), DirectionalNyxiumBlock.Type.VERDANT));
    public static final DeferredBlock<RotatedPillarBlock> TYPHONITE_PILLAR = BLOCKS.register("typhonite_pillar",
            () -> new RotatedPillarBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> TYPHONITE_BRICKS = BLOCKS.register("typhonite_bricks",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> CHISELED_TYPHONITE = BLOCKS.register("chiseled_typhonite",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> POLISHED_TYPHONITE = BLOCKS.register("polished_typhonite",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> VEINED_TYPHONITE = BLOCKS.register("veined_typhonite",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE).lightLevel(state -> 8)));
    public static final DeferredBlock<TyphoniteSpikeBlock> TYPHONITE_SPIKE = BLOCKS.register("typhonite_spike",
            () -> new TyphoniteSpikeBlock(DeferredRegister.copyBlockProperties(Blocks.POINTED_DRIPSTONE)));
    public static final DeferredBlock<StairBlock> TYPHONITE_STAIRS = BLOCKS.register("typhonite_stairs",
            () -> new StairBlock(TYPHONITE.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> TYPHONITE_SLAB = BLOCKS.register("typhonite_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> TYPHONITE_WALL = BLOCKS.register("typhonite_wall",
            () -> new WallBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<StairBlock> TYPHONITE_BRICK_STAIRS = BLOCKS.register("typhonite_brick_stairs",
            () -> new StairBlock(TYPHONITE_BRICKS.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> TYPHONITE_BRICK_SLAB = BLOCKS.register("typhonite_brick_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> TYPHONITE_BRICK_WALL = BLOCKS.register("typhonite_brick_wall",
            () -> new WallBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<StairBlock> POLISHED_TYPHONITE_STAIRS = BLOCKS.register("polished_typhonite_stairs",
            () -> new StairBlock(POLISHED_TYPHONITE.get().defaultBlockState(), DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> POLISHED_TYPHONITE_SLAB = BLOCKS.register("polished_typhonite_slab",
            () -> new SlabBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> POLISHED_TYPHONITE_WALL = BLOCKS.register("polished_typhonite_wall",
            () -> new WallBlock(DeferredRegister.copyBlockProperties(Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<SpiralingVinesBlock> SPIRALING_VINES = BLOCKS.register("spiraling_vines",
            () -> new SpiralingVinesBlock(DeferredRegister.copyBlockProperties(Blocks.TWISTING_VINES)));
    public static final DeferredBlock<GorevineBlock> GOREVINE = BLOCKS.register("gorevine",
            () -> new GorevineBlock(DeferredRegister.copyBlockProperties(Blocks.WEEPING_VINES).lightLevel(state -> 4)));
    public static final DeferredBlock<WhirlflowerBlock> WHIRLFLOWER = BLOCKS.register("whirlflower",
            () -> new WhirlflowerBlock(DeferredRegister.copyBlockProperties(Blocks.ALLIUM)));
    public static final DeferredBlock<NadirFernBlock> NADIR_FERN = BLOCKS.register("nadir_fern",
            () -> new NadirFernBlock(DeferredRegister.copyBlockProperties(Blocks.FERN)));
    public static final DeferredBlock<LargeNadirFernBlock> LARGE_NADIR_FERN = BLOCKS.register("large_nadir_fern",
            () -> new LargeNadirFernBlock(DeferredRegister.copyBlockProperties(Blocks.LARGE_FERN).noCollision()));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> DUSKBELL = BLOCKS.register("duskbell",
            () -> new DirectionalThoraxisFlowerBlock(DeferredRegister.copyBlockProperties(Blocks.ALLIUM).lightLevel(state -> 4)));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> NADIR_ROOTS = BLOCKS.register("nadir_roots",
            () -> new DirectionalThoraxisFlowerBlock(DeferredRegister.copyBlockProperties(Blocks.CRIMSON_ROOTS)));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> VERDANT_ROOTS = BLOCKS.register("verdant_roots",
            () -> new DirectionalThoraxisFlowerBlock(DeferredRegister.copyBlockProperties(Blocks.WARPED_ROOTS)));
    public static final DeferredBlock<Block> DEAD_STAR_CORAL_BLOCK = BLOCKS.register("dead_star_coral_block",
            () -> new Block(DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_BLOCK)));

    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralPlantBlock> DEAD_STAR_CORAL = BLOCKS.register("dead_star_coral",
            () -> new net.minecraft.world.level.block.BaseCoralPlantBlock(DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL)));

    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralFanBlock> DEAD_STAR_CORAL_FAN = BLOCKS.register("dead_star_coral_fan",
            () -> new net.minecraft.world.level.block.BaseCoralFanBlock(DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_FAN)));

    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralWallFanBlock> DEAD_STAR_CORAL_WALL_FAN = BLOCKS.register("dead_star_coral_wall_fan",
            () -> new net.minecraft.world.level.block.BaseCoralWallFanBlock(DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_WALL_FAN)));

    public static final DeferredBlock<Block> STAR_CORAL_BLOCK = BLOCKS.register("star_coral_block",
            () -> new StarCoralBlock(DEAD_STAR_CORAL_BLOCK.get(), DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_BLOCK).lightLevel(state -> 8)));

    public static final DeferredBlock<StarCoralPlantBlock> STAR_CORAL = BLOCKS.register("star_coral",
            () -> new StarCoralPlantBlock(DEAD_STAR_CORAL.get(), DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL).lightLevel(state -> 8)));

    public static final DeferredBlock<StarCoralFanBlock> STAR_CORAL_FAN = BLOCKS.register("star_coral_fan",
            () -> new StarCoralFanBlock(DEAD_STAR_CORAL_FAN.get(), DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_FAN).lightLevel(state -> 8)));

    public static final DeferredBlock<StarCoralWallFanBlock> STAR_CORAL_WALL_FAN = BLOCKS.register("star_coral_wall_fan",
            () -> new StarCoralWallFanBlock(DEAD_STAR_CORAL_WALL_FAN.get(), DeferredRegister.copyBlockProperties(Blocks.TUBE_CORAL_WALL_FAN).lightLevel(state -> 8)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity>> ROYAL_EGG_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("royal_egg",
            () -> FabricBlockEntityTypeBuilder.create(
                    com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity::new,
                    PRINCE_EGG.get(),
                    PRINCESS_EGG.get()
            ).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> FabricBlockEntityTypeBuilder.create(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get(),
                    TERMITE_NEST.get(),
                    ANT_TRAP.get()
            ).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dream_campfire",
            () -> FabricBlockEntityTypeBuilder.create(
                    DreamCampfireBlockEntity::new,
                    DREAM_CAMPFIRE.get()
            ).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("wasp_nest",
            () -> FabricBlockEntityTypeBuilder.create(
                    WaspNestBlockEntity::new,
                    WASP_NEST.get()
            ).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("hushweed",
            () -> FabricBlockEntityTypeBuilder.create(
                    HushweedBlockEntity::new,
                    HUSHWEED.get()
            ).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotentNyxiteBlockEntity>> POTENT_NYXITE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("potent_nyxite",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new PotentNyxiteBlockEntity(pos, state, AntarchyFabricBlocks::potentNyxiteBlockEntityType),
                    POTENT_NYXITE.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeashellBlockEntity>> SEASHELL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("seashell",
            () -> FabricBlockEntityTypeBuilder.create(
                    SeashellBlockEntity::new,
                    SEASHELL.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lucid_anchor",
            () -> FabricBlockEntityTypeBuilder.create(
                    LucidAnchorBlockEntity::new,
                    LUCID_ANCHOR.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("critter_cage_block",
            () -> FabricBlockEntityTypeBuilder.create(
                    CritterCageBlockEntity::new,
                    CRITTER_CAGE_BLOCK.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity>> BLUESTONE_COMPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("bluestone_comparator",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity(pos, state, AntarchyFabricBlocks::bluestoneComparatorBlockEntityType),
                    BLUESTONE_COMPARATOR.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpperBlockEntity>> UPPER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("upper",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new UpperBlockEntity(pos, state, AntarchyFabricBlocks::upperBlockEntityType),
                    UPPER.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VortexLensBlockEntity>> VORTEX_LENS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vortex_lens",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new VortexLensBlockEntity(pos, state, AntarchyFabricBlocks::vortexLensBlockEntityType),
                    VORTEX_LENS.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalGunPortalMasterBlockEntity>> PORTAL_GUN_PORTAL_MASTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_master",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new PortalGunPortalMasterBlockEntity(pos, state, AntarchyFabricBlocks::portalGunPortalMasterBlockEntityType),
                    PORTAL_GUN_PORTAL_MASTER.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalGunPortalBaseBlockEntity>> PORTAL_GUN_PORTAL_BASE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_base",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new PortalGunPortalBaseBlockEntity(pos, state, AntarchyFabricBlocks::portalGunPortalBaseBlockEntityType),
                    PORTAL_GUN_PORTAL_BASE.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DimensionalTearMarkerBlockEntity>> DIMENSIONAL_TEAR_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dimensional_tear_marker",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new DimensionalTearMarkerBlockEntity(pos, state, AntarchyFabricBlocks::dimensionalTearMarkerBlockEntityType),
                    DIMENSIONAL_TEAR_MARKER.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> QUEEN_TRAIL_SPAWN_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("queen_trail_spawn_marker",
            () -> FabricBlockEntityTypeBuilder.create(
                    (pos, state) -> new QueenTrailSpawnMarkerBlockEntity(pos, state, AntarchyFabricBlocks::queenTrailSpawnMarkerBlockEntityType),
                    QUEEN_TRAIL_SPAWN_MARKER.get()
            ).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UndertrialSpawnerBlockEntity>> UNDERTRIAL_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undertrial_spawner",
            () -> FabricBlockEntityTypeBuilder.create((pos, state) -> new UndertrialSpawnerBlockEntity(pos, state, AntarchyFabricBlocks::undertrialSpawnerBlockEntityType), UNDERTRIAL_SPAWNER.get()).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnderVaultBlockEntity>> UNDERVAULT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undervault",
            () -> FabricBlockEntityTypeBuilder.create((pos, state) -> new UnderVaultBlockEntity(pos, state, AntarchyFabricBlocks::underVaultBlockEntityType), UNDERVAULT.get()).build());

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

    private static BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity> bluestoneComparatorBlockEntityType() {
        return BLUESTONE_COMPARATOR_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<UpperBlockEntity> upperBlockEntityType() {
        return UPPER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<VortexLensBlockEntity> vortexLensBlockEntityType() {
        return VORTEX_LENS_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<PortalGunPortalMasterBlockEntity> portalGunPortalMasterBlockEntityType() {
        return PORTAL_GUN_PORTAL_MASTER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<PortalGunPortalBaseBlockEntity> portalGunPortalBaseBlockEntityType() {
        return PORTAL_GUN_PORTAL_BASE_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<DimensionalTearMarkerBlockEntity> dimensionalTearMarkerBlockEntityType() {
        return DIMENSIONAL_TEAR_MARKER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<QueenTrailSpawnMarkerBlockEntity> queenTrailSpawnMarkerBlockEntityType() {
        return QUEEN_TRAIL_SPAWN_MARKER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<UndertrialSpawnerBlockEntity> undertrialSpawnerBlockEntityType() {
        return UNDERTRIAL_SPAWNER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<UnderVaultBlockEntity> underVaultBlockEntityType() {
        return UNDERVAULT_BLOCK_ENTITY.get();
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
        return DeferredRegister.copyBlockProperties(Blocks.DIRT)
                .sound(SoundType.ROOTED_DIRT)
                .strength(0.65F, 0.8F)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties nyxiteProperties() {
        return DeferredRegister.copyBlockProperties(Blocks.NETHERRACK);
    }

    private static BlockBehaviour.Properties broodstoneProperties() {
        return DeferredRegister.copyBlockProperties(Blocks.DEEPSLATE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties portalProperties() {
        return DeferredRegister.blockProperties()
                .strength(-1.0F)
                .noCollision()
                .noOcclusion()
                .lightLevel(state -> 11)
                .noLootTable()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos, bounds) -> false);
    }

    private static Block createOre(Block copyFrom, MapColor mapColor) {
        return new Block(DeferredRegister.copyBlockProperties(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static Block createOre(Block copyFrom, int minExperience, int maxExperience, MapColor mapColor) {
        return new DropExperienceBlock(UniformInt.of(minExperience, maxExperience), DeferredRegister.copyBlockProperties(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static Block createStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(DeferredRegister.copyBlockProperties(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static Block createHorizontalFacingStorageBlock(Block copyFrom, MapColor mapColor) {
        return new SimpleHorizontalFacingBlock(DeferredRegister.copyBlockProperties(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static final class SimpleHorizontalFacingBlock extends HorizontalDirectionalBlock {

        private SimpleHorizontalFacingBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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

    private static Block createRawStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(DeferredRegister.copyBlockProperties(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static SoundType primordialLoamSoundType() {
        return new SoundType(1.0F, 1.0F,
                SoundType.MUD.getBreakSound(),
                SoundType.MOSS.getStepSound(),
                SoundType.MUD.getPlaceSound(),
                SoundType.MUD.getHitSound(),
                SoundType.MUD.getFallSound());
    }


    private static SoundType antimetalSoundType() {
        return new SoundType(1.0F, 1.0F,
                AntarchyFabricSounds.ANTIMETAL_PLACE.get(),
                AntarchyFabricSounds.ANTIMETAL_STEP.get(),
                AntarchyFabricSounds.ANTIMETAL_PLACE.get(),
                AntarchyFabricSounds.ANTIMETAL_STEP.get(),
                AntarchyFabricSounds.ANTIMETAL_PLACE.get());
    }

    private static SoundType antimetalScaffoldingSoundType() {
        SoundEvent scaffold = AntarchyFabricSounds.ANTIMETAL_SCAFFOLD.get();
        return new SoundType(1.0F, 1.0F, scaffold, scaffold, scaffold, scaffold, scaffold);
    }

    public static void register() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }

    public static void registerStripping() {
        BlockTransformerHelper.registerStripping(OURANWOOD_LOG.get(), new CopyPropertiesProvider(STRIPPED_OURANWOOD_LOG.get()));
        BlockTransformerHelper.registerStripping(OURANWOOD_WOOD.get(), new CopyPropertiesProvider(STRIPPED_OURANWOOD_WOOD.get()));
        BlockTransformerHelper.registerStripping(PEACH_LOG.get(), new CopyPropertiesProvider(STRIPPED_PEACH_LOG.get()));
        BlockTransformerHelper.registerStripping(PEACH_WOOD.get(), new CopyPropertiesProvider(STRIPPED_PEACH_WOOD.get()));
        BlockTransformerHelper.registerStripping(NADIR_LOG.get(), new CopyPropertiesProvider(STRIPPED_NADIR_LOG.get()));
        BlockTransformerHelper.registerStripping(NADIR_WOOD.get(), new CopyPropertiesProvider(STRIPPED_NADIR_WOOD.get()));
        BlockTransformerHelper.registerStripping(ROYAL_LOG.get(), new CopyPropertiesProvider(STRIPPED_ROYAL_LOG.get()));
        BlockTransformerHelper.registerStripping(ROYAL_WOOD.get(), new CopyPropertiesProvider(STRIPPED_ROYAL_WOOD.get()));
        BlockTransformerHelper.registerStripping(TRUFFALO_LOG.get(), new CopyPropertiesProvider(STRIPPED_TRUFFALO_LOG.get()));
        BlockTransformerHelper.registerStripping(TRUFFALO_WOOD.get(), new CopyPropertiesProvider(STRIPPED_TRUFFALO_WOOD.get()));
    }

}
