package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DimensionalTearMarkerBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity;
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
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.neoforge.content.block.WildCornBlock;
import com.craisinlord.antarchy.neoforge.AntarchyWoodTypes;
import com.craisinlord.antarchy.neoforge.content.fluid.AntiwaterFluidType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AntarchyNeoforgeBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);
    public static final DeferredBlock<UndertrialSpawnerBlock> UNDERTRIAL_SPAWNER = BLOCKS.register("undertrial_spawner",
            id -> new UndertrialSpawnerBlock(AntarchyNeoforgeBlocks::undertrialSpawnerBlockEntityType,
                    copyOf(id, Blocks.TRIAL_SPAWNER).noOcclusion().isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<UnderVaultBlock> UNDERVAULT = BLOCKS.register("undervault",
            id -> new UnderVaultBlock(AntarchyNeoforgeBlocks::underVaultBlockEntityType,
                    copyOf(id, Blocks.VAULT).noOcclusion().isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<QueenTrailSpawnMarkerBlock> QUEEN_TRAIL_SPAWN_MARKER = BLOCKS.register("queen_trail_spawn_marker",
            id -> new QueenTrailSpawnMarkerBlock(AntarchyNeoforgeBlocks::queenTrailSpawnMarkerBlockEntityType,
                    copyOf(id, Blocks.BEDROCK).noLootTable()));
    public static final DeferredBlock<DuplicatorLogBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            id -> new DuplicatorLogBlock(copyOf(id, Blocks.OAK_LOG).randomTicks()));
    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_LOG = BLOCKS.register("ouranwood_log",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_WOOD = BLOCKS.register("ouranwood_wood",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_LOG = BLOCKS.register("mossy_ouranwood_log",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_WOOD = BLOCKS.register("mossy_ouranwood_wood",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_LOG = BLOCKS.register("stripped_ouranwood_log",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_WOOD = BLOCKS.register("stripped_ouranwood_wood",
            id -> new OuranwoodLogBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> OURANWOOD_PLANKS = BLOCKS.register("ouranwood_planks",
            id -> new Block(copyOf(id, Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<StairBlock> OURANWOOD_STAIRS = BLOCKS.register("ouranwood_stairs",
            id -> new StairBlock(OURANWOOD_PLANKS.get().defaultBlockState(), copyOf(id, Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<SlabBlock> OURANWOOD_SLAB = BLOCKS.register("ouranwood_slab",
            id -> new SlabBlock(copyOf(id, Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<FenceBlock> OURANWOOD_FENCE = BLOCKS.register("ouranwood_fence",
            id -> new FenceBlock(copyOf(id, Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<FenceGateBlock> OURANWOOD_FENCE_GATE = BLOCKS.register("ouranwood_fence_gate",
            id -> new FenceGateBlock(WoodType.JUNGLE, copyOf(id, Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> OURANWOOD_DOOR = BLOCKS.register("ouranwood_door",
            id -> new DoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> OURANWOOD_TRAPDOOR = BLOCKS.register("ouranwood_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> OURANWOOD_PRESSURE_PLATE = BLOCKS.register("ouranwood_pressure_plate",
            id -> new PressurePlateBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> OURANWOOD_BUTTON = BLOCKS.register("ouranwood_button",
            id -> new ButtonBlock(BlockSetType.JUNGLE, 30, copyOf(id, Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<OuranwoodLeavesBlock> OURANWOOD_LEAVES = BLOCKS.register("ouranwood_leaves",
            id -> new OuranwoodLeavesBlock(copyOf(id, Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final DeferredBlock<OuranwoodAcornBlock> OURANWOOD_ACORN_BLOCK = BLOCKS.register("ouranwood_acorn",
            id -> new OuranwoodAcornBlock(copyOf(id, Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_OURANWOOD_ACORN = BLOCKS.register("potted_ouranwood_acorn",
            id -> new FlowerPotBlock(OURANWOOD_ACORN_BLOCK.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            id -> new MilkweedBlock(copyOf(id, Blocks.PEONY)));
    public static final DeferredBlock<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            id -> new MilkweedBlock(copyOf(id, Blocks.PEONY)));
    public static final DeferredBlock<GlimmeringReedBlock> GLIMMERING_REED = BLOCKS.register("glimmering_reed",
            id -> new GlimmeringReedBlock(copyOf(id, Blocks.SUNFLOWER).lightLevel(state -> 8)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CamelliaBlock> CAMELLIA = BLOCKS.register("camellia",
            id -> new com.craisinlord.antarchy.content.block.CamelliaBlock(copyOf(id, Blocks.PEONY)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.SpiderLilyBlock> SPIDER_LILY = BLOCKS.register("spider_lily",
            id -> new com.craisinlord.antarchy.content.block.SpiderLilyBlock(copyOf(id, Blocks.POPPY)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_SPIDER_LILY = BLOCKS.register("potted_spider_lily",
            id -> new FlowerPotBlock(SPIDER_LILY.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<GiantLilyPadBlock> GIANT_LILY_PAD = BLOCKS.register("giant_lily_pad",
            id -> new GiantLilyPadBlock(copyOf(id, Blocks.LILY_PAD)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.LotusBlock> LOTUS = BLOCKS.register("lotus",
            id -> new com.craisinlord.antarchy.content.block.LotusBlock(copyOf(id, Blocks.SPORE_BLOSSOM)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_LOTUS = BLOCKS.register("potted_lotus",
            id -> new FlowerPotBlock(LOTUS.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<SeashellBlock> SEASHELL = BLOCKS.register("seashell",
            id -> new SeashellBlock(copyOf(id, Blocks.TURTLE_EGG).noOcclusion()));
    public static final DeferredBlock<LucidAnchorBlock> LUCID_ANCHOR = BLOCKS.register("lucid_anchor",
            id -> new LucidAnchorBlock(copyOf(id, Blocks.BEACON).strength(3.0F)));
    public static final DeferredBlock<CritterCageBlock> CRITTER_CAGE_BLOCK = BLOCKS.register("critter_cage_block",
            id -> new CritterCageBlock(propertiesOf(id).strength(5.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<PortalGunPortalMasterBlock> PORTAL_GUN_PORTAL_MASTER = BLOCKS.register("portal_gun_portal_master",
            id -> new PortalGunPortalMasterBlock(AntarchyNeoforgeBlocks::portalGunPortalMasterBlockEntityType, portalProperties(id)));
    public static final DeferredBlock<PortalGunPortalBaseBlock> PORTAL_GUN_PORTAL_BASE = BLOCKS.register("portal_gun_portal_base",
            id -> new PortalGunPortalBaseBlock(AntarchyNeoforgeBlocks::portalGunPortalBaseBlockEntityType, portalProperties(id)));
    public static final DeferredBlock<DimensionalTearMarkerBlock> DIMENSIONAL_TEAR_MARKER = BLOCKS.register("dimensional_tear_marker",
            id -> new DimensionalTearMarkerBlock(AntarchyNeoforgeBlocks::dimensionalTearMarkerBlockEntityType,
                    propertiesOf(id).noCollision().noOcclusion().noLootTable().instabreak()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final DeferredBlock<StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            id -> new StandingSignBlock(AntarchyWoodTypes.OURANWOOD, copyOf(id, Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            id -> new WallSignBlock(AntarchyWoodTypes.OURANWOOD, copyOf(id, Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            id -> new CeilingHangingSignBlock(AntarchyWoodTypes.OURANWOOD, copyOf(id, Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            id -> new WallHangingSignBlock(AntarchyWoodTypes.OURANWOOD, copyOf(id, Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<RotatedPillarBlock> PEACH_LOG = BLOCKS.register("peach_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> PEACH_WOOD = BLOCKS.register("peach_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_LOG = BLOCKS.register("stripped_peach_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_WOOD = BLOCKS.register("stripped_peach_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> PEACH_PLANKS = BLOCKS.register("peach_planks",
            id -> new Block(copyOf(id, Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<StairBlock> PEACH_STAIRS = BLOCKS.register("peach_stairs",
            id -> new StairBlock(PEACH_PLANKS.get().defaultBlockState(), copyOf(id, Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<SlabBlock> PEACH_SLAB = BLOCKS.register("peach_slab",
            id -> new SlabBlock(copyOf(id, Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<FenceBlock> PEACH_FENCE = BLOCKS.register("peach_fence",
            id -> new FenceBlock(copyOf(id, Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<FenceGateBlock> PEACH_FENCE_GATE = BLOCKS.register("peach_fence_gate",
            id -> new FenceGateBlock(WoodType.JUNGLE, copyOf(id, Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> PEACH_DOOR = BLOCKS.register("peach_door",
            id -> new DoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> PEACH_TRAPDOOR = BLOCKS.register("peach_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> PEACH_PRESSURE_PLATE = BLOCKS.register("peach_pressure_plate",
            id -> new PressurePlateBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> PEACH_BUTTON = BLOCKS.register("peach_button",
            id -> new ButtonBlock(BlockSetType.JUNGLE, 30, copyOf(id, Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<PeachLeavesBlock> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            id -> new PeachLeavesBlock(copyOf(id, Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final DeferredBlock<HangingPeachBlock> PEACH_HANGING_PEACH = BLOCKS.register("hanging_peach",
            id -> new HangingPeachBlock(propertiesOf(id).noOcclusion().instabreak()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachSaplingBlock> PEACH_SAPLING = BLOCKS.register("peach_sapling",
            id -> new com.craisinlord.antarchy.content.block.PeachSaplingBlock(copyOf(id, Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_PEACH_SAPLING = BLOCKS.register("potted_peach_sapling",
            id -> new FlowerPotBlock(PEACH_SAPLING.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<StandingSignBlock> PEACH_SIGN = BLOCKS.register("peach_sign",
            id -> new StandingSignBlock(AntarchyWoodTypes.PEACH, copyOf(id, Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<WallSignBlock> PEACH_WALL_SIGN = BLOCKS.register("peach_wall_sign",
            id -> new WallSignBlock(AntarchyWoodTypes.PEACH, copyOf(id, Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> PEACH_HANGING_SIGN = BLOCKS.register("peach_hanging_sign",
            id -> new CeilingHangingSignBlock(AntarchyWoodTypes.PEACH, copyOf(id, Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> PEACH_WALL_HANGING_SIGN = BLOCKS.register("peach_wall_hanging_sign",
            id -> new WallHangingSignBlock(AntarchyWoodTypes.PEACH, copyOf(id, Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<RotatedPillarBlock> NADIR_LOG = BLOCKS.register("nadir_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> NADIR_WOOD = BLOCKS.register("nadir_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NADIR_LOG = BLOCKS.register("stripped_nadir_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NADIR_WOOD = BLOCKS.register("stripped_nadir_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> NADIR_PLANKS = BLOCKS.register("nadir_planks",
            id -> new Block(copyOf(id, Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<StairBlock> NADIR_STAIRS = BLOCKS.register("nadir_stairs",
            id -> new StairBlock(NADIR_PLANKS.get().defaultBlockState(), copyOf(id, Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<SlabBlock> NADIR_SLAB = BLOCKS.register("nadir_slab",
            id -> new SlabBlock(copyOf(id, Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<FenceBlock> NADIR_FENCE = BLOCKS.register("nadir_fence",
            id -> new FenceBlock(copyOf(id, Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<FenceGateBlock> NADIR_FENCE_GATE = BLOCKS.register("nadir_fence_gate",
            id -> new FenceGateBlock(WoodType.JUNGLE, copyOf(id, Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> NADIR_DOOR = BLOCKS.register("nadir_door",
            id -> new DoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> NADIR_TRAPDOOR = BLOCKS.register("nadir_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> NADIR_PRESSURE_PLATE = BLOCKS.register("nadir_pressure_plate",
            id -> new PressurePlateBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> NADIR_BUTTON = BLOCKS.register("nadir_button",
            id -> new ButtonBlock(BlockSetType.JUNGLE, 30, copyOf(id, Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.NadirVeilBlock> NADIR_VEIL = BLOCKS.register("nadir_veil",
            id -> new com.craisinlord.antarchy.content.block.NadirVeilBlock(copyOf(id, Blocks.JUNGLE_LEAVES).sound(SoundType.SLIME_BLOCK).noOcclusion()));
    public static final DeferredBlock<NadirSaplingBlock> NADIR_SAPLING = BLOCKS.register("nadir_sapling",
            id -> new NadirSaplingBlock(copyOf(id, Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_NADIR_SAPLING = BLOCKS.register("potted_nadir_sapling",
            id -> new FlowerPotBlock(NADIR_SAPLING.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<StandingSignBlock> NADIR_SIGN = BLOCKS.register("nadir_sign",
            id -> new StandingSignBlock(AntarchyWoodTypes.NADIR, copyOf(id, Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<WallSignBlock> NADIR_WALL_SIGN = BLOCKS.register("nadir_wall_sign",
            id -> new WallSignBlock(AntarchyWoodTypes.NADIR, copyOf(id, Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> NADIR_HANGING_SIGN = BLOCKS.register("nadir_hanging_sign",
            id -> new CeilingHangingSignBlock(AntarchyWoodTypes.NADIR, copyOf(id, Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> NADIR_WALL_HANGING_SIGN = BLOCKS.register("nadir_wall_hanging_sign",
            id -> new WallHangingSignBlock(AntarchyWoodTypes.NADIR, copyOf(id, Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            id -> new DuplicatorSaplingBlock(copyOf(id, Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<RotatedPillarBlock> ROYAL_LOG = BLOCKS.register("royal_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> ROYAL_WOOD = BLOCKS.register("royal_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_WOOD)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ROYAL_LOG = BLOCKS.register("stripped_royal_log",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ROYAL_WOOD = BLOCKS.register("stripped_royal_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> ROYAL_PLANKS = BLOCKS.register("royal_planks",
            id -> new Block(copyOf(id, Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<StairBlock> ROYAL_STAIRS = BLOCKS.register("royal_stairs",
            id -> new StairBlock(ROYAL_PLANKS.get().defaultBlockState(), copyOf(id, Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<SlabBlock> ROYAL_SLAB = BLOCKS.register("royal_slab",
            id -> new SlabBlock(copyOf(id, Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<FenceBlock> ROYAL_FENCE = BLOCKS.register("royal_fence",
            id -> new FenceBlock(copyOf(id, Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<FenceGateBlock> ROYAL_FENCE_GATE = BLOCKS.register("royal_fence_gate",
            id -> new FenceGateBlock(WoodType.JUNGLE, copyOf(id, Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> ROYAL_DOOR = BLOCKS.register("royal_door",
            id -> new DoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> ROYAL_TRAPDOOR = BLOCKS.register("royal_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> ROYAL_PRESSURE_PLATE = BLOCKS.register("royal_pressure_plate",
            id -> new PressurePlateBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> ROYAL_BUTTON = BLOCKS.register("royal_button",
            id -> new ButtonBlock(BlockSetType.JUNGLE, 30, copyOf(id, Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<RoyalLeavesBlock> ROYAL_LEAVES = BLOCKS.register("royal_leaves",
            id -> new RoyalLeavesBlock(copyOf(id, Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final DeferredBlock<RoyalLeavesBlock> ROYAL_FLOWERING_LEAVES = BLOCKS.register("royal_flowering_leaves",
            id -> new RoyalLeavesBlock(copyOf(id, Blocks.JUNGLE_LEAVES).randomTicks().lightLevel(state -> 12)));
    public static final DeferredBlock<RoyalSaplingBlock> ROYAL_SAPLING = BLOCKS.register("royal_sapling",
            id -> new RoyalSaplingBlock(copyOf(id, Blocks.OAK_SAPLING).noCollision()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_ROYAL_SAPLING = BLOCKS.register("potted_royal_sapling",
            id -> new FlowerPotBlock(ROYAL_SAPLING.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<StandingSignBlock> ROYAL_SIGN = BLOCKS.register("royal_sign",
            id -> new StandingSignBlock(AntarchyWoodTypes.ROYAL, copyOf(id, Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<WallSignBlock> ROYAL_WALL_SIGN = BLOCKS.register("royal_wall_sign",
            id -> new WallSignBlock(AntarchyWoodTypes.ROYAL, copyOf(id, Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> ROYAL_HANGING_SIGN = BLOCKS.register("royal_hanging_sign",
            id -> new CeilingHangingSignBlock(AntarchyWoodTypes.ROYAL, copyOf(id, Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> ROYAL_WALL_HANGING_SIGN = BLOCKS.register("royal_wall_hanging_sign",
            id -> new WallHangingSignBlock(AntarchyWoodTypes.ROYAL, copyOf(id, Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<TruffaloLogBlock> TRUFFALO_LOG = BLOCKS.register("truffalo_log",
            id -> new TruffaloLogBlock(copyOf(id, Blocks.JUNGLE_LOG)) {
                @Override
                public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
                    if (itemAbility == ItemAbilities.AXE_STRIP) {
                        return STRIPPED_TRUFFALO_LOG.get().withPropertiesOf(state);
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });
    public static final DeferredBlock<RotatedPillarBlock> TRUFFALO_WOOD = BLOCKS.register("truffalo_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.JUNGLE_WOOD)) {
                @Override
                public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
                    if (itemAbility == ItemAbilities.AXE_STRIP) {
                        return STRIPPED_TRUFFALO_WOOD.get().withPropertiesOf(state);
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });
    public static final DeferredBlock<TruffaloLogBlock> STRIPPED_TRUFFALO_LOG = BLOCKS.register("stripped_truffalo_log",
            id -> new TruffaloLogBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_TRUFFALO_WOOD = BLOCKS.register("stripped_truffalo_wood",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final DeferredBlock<Block> TRUFFALO_PLANKS = BLOCKS.register("truffalo_planks",
            id -> new Block(copyOf(id, Blocks.JUNGLE_PLANKS)));
    public static final DeferredBlock<StairBlock> TRUFFALO_STAIRS = BLOCKS.register("truffalo_stairs",
            id -> new StairBlock(TRUFFALO_PLANKS.get().defaultBlockState(), copyOf(id, Blocks.JUNGLE_STAIRS)));
    public static final DeferredBlock<SlabBlock> TRUFFALO_SLAB = BLOCKS.register("truffalo_slab",
            id -> new SlabBlock(copyOf(id, Blocks.JUNGLE_SLAB)));
    public static final DeferredBlock<FenceBlock> TRUFFALO_FENCE = BLOCKS.register("truffalo_fence",
            id -> new FenceBlock(copyOf(id, Blocks.JUNGLE_FENCE)));
    public static final DeferredBlock<FenceGateBlock> TRUFFALO_FENCE_GATE = BLOCKS.register("truffalo_fence_gate",
            id -> new FenceGateBlock(WoodType.JUNGLE, copyOf(id, Blocks.JUNGLE_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> TRUFFALO_DOOR = BLOCKS.register("truffalo_door",
            id -> new DoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> TRUFFALO_TRAPDOOR = BLOCKS.register("truffalo_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_TRAPDOOR)));
    public static final DeferredBlock<PressurePlateBlock> TRUFFALO_PRESSURE_PLATE = BLOCKS.register("truffalo_pressure_plate",
            id -> new PressurePlateBlock(BlockSetType.JUNGLE, copyOf(id, Blocks.JUNGLE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> TRUFFALO_BUTTON = BLOCKS.register("truffalo_button",
            id -> new ButtonBlock(BlockSetType.JUNGLE, 30, copyOf(id, Blocks.JUNGLE_BUTTON)));
    public static final DeferredBlock<TruffaloSaplingBlock> TRUFFALO_SAPLING = BLOCKS.register("truffalo_sapling",
            id -> new TruffaloSaplingBlock(copyOf(id, Blocks.OAK_SAPLING).randomTicks().noCollision()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_TRUFFALO_SAPLING = BLOCKS.register("potted_truffalo_sapling",
            id -> new FlowerPotBlock(TRUFFALO_SAPLING.get(), copyOf(id, Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<StandingSignBlock> TRUFFALO_SIGN = BLOCKS.register("truffalo_sign",
            id -> new StandingSignBlock(AntarchyWoodTypes.TRUFFALO, copyOf(id, Blocks.JUNGLE_SIGN)));
    public static final DeferredBlock<WallSignBlock> TRUFFALO_WALL_SIGN = BLOCKS.register("truffalo_wall_sign",
            id -> new WallSignBlock(AntarchyWoodTypes.TRUFFALO, copyOf(id, Blocks.JUNGLE_WALL_SIGN)));
    public static final DeferredBlock<CeilingHangingSignBlock> TRUFFALO_HANGING_SIGN = BLOCKS.register("truffalo_hanging_sign",
            id -> new CeilingHangingSignBlock(AntarchyWoodTypes.TRUFFALO, copyOf(id, Blocks.JUNGLE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> TRUFFALO_WALL_HANGING_SIGN = BLOCKS.register("truffalo_wall_hanging_sign",
            id -> new WallHangingSignBlock(AntarchyWoodTypes.TRUFFALO, copyOf(id, Blocks.JUNGLE_WALL_HANGING_SIGN)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_WHITE = BLOCKS.register("white_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.WHITE_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_ORANGE = BLOCKS.register("orange_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.ORANGE_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_MAGENTA = BLOCKS.register("magenta_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.MAGENTA_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_BLUE = BLOCKS.register("light_blue_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.LIGHT_BLUE_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_YELLOW = BLOCKS.register("yellow_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.YELLOW_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_LIME = BLOCKS.register("lime_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.LIME_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_PINK = BLOCKS.register("pink_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.PINK_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_GRAY = BLOCKS.register("gray_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.GRAY_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_GRAY = BLOCKS.register("light_gray_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.LIGHT_GRAY_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_CYAN = BLOCKS.register("cyan_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.CYAN_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_PURPLE = BLOCKS.register("purple_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.PURPLE_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_BLUE = BLOCKS.register("blue_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.BLUE_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_BROWN = BLOCKS.register("brown_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.BROWN_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_GREEN = BLOCKS.register("green_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.GREEN_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_RED = BLOCKS.register("red_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.RED_WOOL)));
    public static final DeferredBlock<TruffaloTuftBlock> TRUFFALO_TUFT_BLACK = BLOCKS.register("black_truffalo_tuft",
            id -> new TruffaloTuftBlock(copyOf(id, Blocks.BLACK_WOOL)));
    public static final DeferredBlock<DuctTapeBlock> DUCT_TAPE = BLOCKS.register("duct_tape",
            id -> new DuctTapeBlock(propertiesOf(id)
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()));
    public static final DeferredBlock<InfestedRootedDirtBlock> INFESTED_ROOTED_DIRT = BLOCKS.register("infested_rooted_dirt",
            id -> new InfestedRootedDirtBlock(copyOf(id, Blocks.ROOTED_DIRT).randomTicks()));
    public static final DeferredBlock<InfestedCoarseDirtBlock> INFESTED_COARSE_DIRT = BLOCKS.register("infested_coarse_dirt",
            id -> new InfestedCoarseDirtBlock(copyOf(id, Blocks.COARSE_DIRT).randomTicks()));
    public static final DeferredBlock<Block> NYXITE = BLOCKS.register("nyxite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<Block> POLISHED_NYXITE = BLOCKS.register("polished_nyxite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<Block> CHISELED_NYXITE = BLOCKS.register("chiseled_nyxite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<Block> NYXITE_BRICKS = BLOCKS.register("nyxite_bricks",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<Block> MOSSY_NYXITE_BRICKS = BLOCKS.register("mossy_nyxite_bricks",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> NYXITE_STAIRS = BLOCKS.register("nyxite_stairs",
            id -> new StairBlock(NYXITE.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> NYXITE_SLAB = BLOCKS.register("nyxite_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> NYXITE_WALL = BLOCKS.register("nyxite_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> POLISHED_NYXITE_STAIRS = BLOCKS.register("polished_nyxite_stairs",
            id -> new StairBlock(POLISHED_NYXITE.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> POLISHED_NYXITE_SLAB = BLOCKS.register("polished_nyxite_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> POLISHED_NYXITE_WALL = BLOCKS.register("polished_nyxite_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock> POLISHED_NYXITE_PRESSURE_PLATE = BLOCKS.register("polished_nyxite_pressure_plate",
            id -> new com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock(BlockSetType.STONE, copyOf(id, Blocks.STONE_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> POLISHED_NYXITE_BUTTON = BLOCKS.register("polished_nyxite_button",
            id -> new ButtonBlock(BlockSetType.STONE, 20, copyOf(id, Blocks.STONE_BUTTON)));
    public static final DeferredBlock<StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            id -> new StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> MOSSY_NYXITE_BRICK_STAIRS = BLOCKS.register("mossy_nyxite_brick_stairs",
            id -> new StairBlock(MOSSY_NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> MOSSY_NYXITE_BRICK_SLAB = BLOCKS.register("mossy_nyxite_brick_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> MOSSY_NYXITE_BRICK_WALL = BLOCKS.register("mossy_nyxite_brick_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<RotatedPillarBlock> NYXITE_PILLAR = BLOCKS.register("nyxite_pillar",
            id -> new RotatedPillarBlock(nyxiteProperties(id)));
    public static final DeferredBlock<Block> SHELLSTONE = BLOCKS.register("shellstone",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<Block> POLISHED_SHELLSTONE = BLOCKS.register("polished_shellstone",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<Block> SHELLSTONE_BRICKS = BLOCKS.register("shellstone_bricks",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<Block> CHISELED_SHELLSTONE = BLOCKS.register("chiseled_shellstone",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<Block> MOSSY_SHELLSTONE_BRICKS = BLOCKS.register("mossy_shellstone_bricks",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<Block> CRACKED_SHELLSTONE_BRICKS = BLOCKS.register("cracked_shellstone_bricks",
            id -> new Block(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> MOSSY_SHELLSTONE_BRICK_STAIRS = BLOCKS.register("mossy_shellstone_brick_stairs",
            id -> new StairBlock(MOSSY_SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> MOSSY_SHELLSTONE_BRICK_SLAB = BLOCKS.register("mossy_shellstone_brick_slab",
            id -> new SlabBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> MOSSY_SHELLSTONE_BRICK_WALL = BLOCKS.register("mossy_shellstone_brick_wall",
            id -> new WallBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SHELLSTONE_STAIRS = BLOCKS.register("shellstone_stairs",
            id -> new StairBlock(SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SHELLSTONE_SLAB = BLOCKS.register("shellstone_slab",
            id -> new SlabBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SHELLSTONE_WALL = BLOCKS.register("shellstone_wall",
            id -> new WallBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> POLISHED_SHELLSTONE_STAIRS = BLOCKS.register("polished_shellstone_stairs",
            id -> new StairBlock(POLISHED_SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> POLISHED_SHELLSTONE_SLAB = BLOCKS.register("polished_shellstone_slab",
            id -> new SlabBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> POLISHED_SHELLSTONE_WALL = BLOCKS.register("polished_shellstone_wall",
            id -> new WallBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SHELLSTONE_BRICK_STAIRS = BLOCKS.register("shellstone_brick_stairs",
            id -> new StairBlock(SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SHELLSTONE_BRICK_SLAB = BLOCKS.register("shellstone_brick_slab",
            id -> new SlabBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SHELLSTONE_BRICK_WALL = BLOCKS.register("shellstone_brick_wall",
            id -> new WallBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<RotatedPillarBlock> SHELLSTONE_PILLAR = BLOCKS.register("shellstone_pillar",
            id -> new RotatedPillarBlock(AntarchyObjects.shellstoneProperties().setId(blockKey(id))));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.FlytrapGooBlock> FLYTRAP_GOO_BLOCK = BLOCKS.register("flytrap_goo_block",
            id -> new com.craisinlord.antarchy.content.block.FlytrapGooBlock(copyOf(id, Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            id -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyNeoforgeItems::cloudBucketItem,
                    copyOf(id, Blocks.POWDER_SNOW).noLootTable().noOcclusion().emissiveRendering((state, level, pos) -> true)
            ));
    public static final DeferredBlock<AntDimensionPortalBlock> ELYTHIA_PORTAL = BLOCKS.register("elythia_portal",
            id -> new AntDimensionPortalBlock(PermanentPortalType.ELYTHIA, portalProperties(id)));
    public static final DeferredBlock<AntDimensionPortalBlock> THORAXIS_PORTAL = BLOCKS.register("thoraxis_portal",
            id -> new AntDimensionPortalBlock(PermanentPortalType.THORAXIS, portalProperties(id)));
    public static final DeferredBlock<AntDimensionPortalBlock> CAVARYN_PORTAL = BLOCKS.register("cavaryn_portal",
            id -> new AntDimensionPortalBlock(PermanentPortalType.CAVARYN, portalProperties(id)));
    public static final DeferredBlock<Block> PALE_NYXITE = BLOCKS.register("pale_nyxite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<NyxiteSpikeBlock> NYXITE_SPIKE = BLOCKS.register("nyxite_spike",
            id -> new NyxiteSpikeBlock(copyOf(id, Blocks.POINTED_DRIPSTONE)));
    public static final DeferredBlock<ChitinSpikeBlock> CHITIN_SPIKE = BLOCKS.register("chitin_spike",
            id -> new ChitinSpikeBlock(copyOf(id, Blocks.POINTED_DRIPSTONE)));
    public static final DeferredBlock<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            id -> new PotentNyxiteBlock(
                    AntarchyNeoforgeItems::potentNyxiteBlockEntityType,
                    copyOf(id, Blocks.NETHERRACK).lightLevel(state -> 3)
            ));
    public static final DeferredBlock<Block> MYRMITE = BLOCKS.register("myrmite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> MYRMITE_STAIRS = BLOCKS.register("myrmite_stairs",
            id -> new StairBlock(MYRMITE.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> MYRMITE_SLAB = BLOCKS.register("myrmite_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> MYRMITE_WALL = BLOCKS.register("myrmite_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<Block> POLISHED_MYRMITE = BLOCKS.register("polished_myrmite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> POLISHED_MYRMITE_STAIRS = BLOCKS.register("polished_myrmite_stairs",
            id -> new StairBlock(POLISHED_MYRMITE.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> POLISHED_MYRMITE_SLAB = BLOCKS.register("polished_myrmite_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> POLISHED_MYRMITE_WALL = BLOCKS.register("polished_myrmite_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<Block> CHISELED_MYRMITE = BLOCKS.register("chiseled_myrmite",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<Block> MYRMITE_BRICKS = BLOCKS.register("myrmite_bricks",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> MYRMITE_BRICK_STAIRS = BLOCKS.register("myrmite_brick_stairs",
            id -> new StairBlock(MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> MYRMITE_BRICK_SLAB = BLOCKS.register("myrmite_brick_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> MYRMITE_BRICK_WALL = BLOCKS.register("myrmite_brick_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<Block> MOSSY_MYRMITE_BRICKS = BLOCKS.register("mossy_myrmite_bricks",
            id -> new Block(nyxiteProperties(id)));
    public static final DeferredBlock<StairBlock> MOSSY_MYRMITE_BRICK_STAIRS = BLOCKS.register("mossy_myrmite_brick_stairs",
            id -> new StairBlock(MOSSY_MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties(id)));
    public static final DeferredBlock<SlabBlock> MOSSY_MYRMITE_BRICK_SLAB = BLOCKS.register("mossy_myrmite_brick_slab",
            id -> new SlabBlock(nyxiteProperties(id)));
    public static final DeferredBlock<WallBlock> MOSSY_MYRMITE_BRICK_WALL = BLOCKS.register("mossy_myrmite_brick_wall",
            id -> new WallBlock(nyxiteProperties(id)));
    public static final DeferredBlock<RotatedPillarBlock> MYRMITE_PILLAR = BLOCKS.register("myrmite_pillar",
            id -> new RotatedPillarBlock(nyxiteProperties(id)));
    public static final DeferredBlock<Block> BIOMITE = BLOCKS.register("biomite",
            id -> new Block(copyOf(id, Blocks.NETHERRACK)));
    public static final DeferredBlock<Block> BIOMITE_TURF = BLOCKS.register("biomite_turf",
            id -> new Block(copyOf(id, Blocks.NETHERRACK).lightLevel(state -> 2)));
    public static final DeferredBlock<BiowartBlock> BIOWART = BLOCKS.register("biowart",
            id -> new BiowartBlock(copyOf(id, Blocks.MOSS_BLOCK).lightLevel(state -> 2)));
    public static final DeferredBlock<BiowartTendrilsBlock> BIOWART_TENDRILS = BLOCKS.register("biowart_tendrils",
            id -> new BiowartTendrilsBlock(copyOf(id, Blocks.MOSS_CARPET).lightLevel(state -> 2).noCollision().noOcclusion()));
    public static final DeferredBlock<Block> MYRMITE_COAL_ORE = BLOCKS.register("myrmite_coal_ore",
            id -> createOre(id, Blocks.COAL_ORE, 0, 2, MapColor.COLOR_GRAY));
    public static final DeferredBlock<Block> BROODSTONE = BLOCKS.register("broodstone",
            id -> new Block(broodstoneProperties(id)));
    public static final DeferredBlock<Block> POLISHED_BROODSTONE = BLOCKS.register("polished_broodstone",
            id -> new Block(broodstoneProperties(id)));
    public static final DeferredBlock<Block> CHISELED_BROODSTONE = BLOCKS.register("chiseled_broodstone",
            id -> new Block(broodstoneProperties(id)));
    public static final DeferredBlock<Block> BROODSTONE_BRICKS = BLOCKS.register("broodstone_bricks",
            id -> new Block(broodstoneProperties(id)));
    public static final DeferredBlock<Block> MOSSY_BROODSTONE_BRICKS = BLOCKS.register("mossy_broodstone_bricks",
            id -> new Block(broodstoneProperties(id)));
    public static final DeferredBlock<StairBlock> BROODSTONE_STAIRS = BLOCKS.register("broodstone_stairs",
            id -> new StairBlock(BROODSTONE.get().defaultBlockState(), broodstoneProperties(id)));
    public static final DeferredBlock<SlabBlock> BROODSTONE_SLAB = BLOCKS.register("broodstone_slab",
            id -> new SlabBlock(broodstoneProperties(id)));
    public static final DeferredBlock<WallBlock> BROODSTONE_WALL = BLOCKS.register("broodstone_wall",
            id -> new WallBlock(broodstoneProperties(id)));
    public static final DeferredBlock<StairBlock> POLISHED_BROODSTONE_STAIRS = BLOCKS.register("polished_broodstone_stairs",
            id -> new StairBlock(POLISHED_BROODSTONE.get().defaultBlockState(), broodstoneProperties(id)));
    public static final DeferredBlock<SlabBlock> POLISHED_BROODSTONE_SLAB = BLOCKS.register("polished_broodstone_slab",
            id -> new SlabBlock(broodstoneProperties(id)));
    public static final DeferredBlock<WallBlock> POLISHED_BROODSTONE_WALL = BLOCKS.register("polished_broodstone_wall",
            id -> new WallBlock(broodstoneProperties(id)));
    public static final DeferredBlock<StairBlock> BROODSTONE_BRICK_STAIRS = BLOCKS.register("broodstone_brick_stairs",
            id -> new StairBlock(BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties(id)));
    public static final DeferredBlock<SlabBlock> BROODSTONE_BRICK_SLAB = BLOCKS.register("broodstone_brick_slab",
            id -> new SlabBlock(broodstoneProperties(id)));
    public static final DeferredBlock<WallBlock> BROODSTONE_BRICK_WALL = BLOCKS.register("broodstone_brick_wall",
            id -> new WallBlock(broodstoneProperties(id)));
    public static final DeferredBlock<StairBlock> MOSSY_BROODSTONE_BRICK_STAIRS = BLOCKS.register("mossy_broodstone_brick_stairs",
            id -> new StairBlock(MOSSY_BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties(id)));
    public static final DeferredBlock<SlabBlock> MOSSY_BROODSTONE_BRICK_SLAB = BLOCKS.register("mossy_broodstone_brick_slab",
            id -> new SlabBlock(broodstoneProperties(id)));
    public static final DeferredBlock<WallBlock> MOSSY_BROODSTONE_BRICK_WALL = BLOCKS.register("mossy_broodstone_brick_wall",
            id -> new WallBlock(broodstoneProperties(id)));
    public static final DeferredBlock<RotatedPillarBlock> BROODSTONE_PILLAR = BLOCKS.register("broodstone_pillar",
            id -> new RotatedPillarBlock(broodstoneProperties(id)));
    public static final DeferredBlock<Block> BROODSTONE_URANIUM_ORE = BLOCKS.register("broodstone_uranium_ore",
            id -> createOre(id, Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> BROODSTONE_TITANIUM_ORE = BLOCKS.register("broodstone_titanium_ore",
            id -> createOre(id, Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<RotatedPillarBlock> CHITIN_BLOCK = BLOCKS.register("chitin_block",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.BONE_BLOCK).requiresCorrectToolForDrops()));
    public static final DeferredBlock<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            id -> new UmbralMossBlock(copyOf(id, Blocks.MOSS_BLOCK)));
    public static final DeferredBlock<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            id -> new UmbralMossCarpetBlock(copyOf(id, Blocks.MOSS_CARPET).noOcclusion()));
    public static final DeferredBlock<AmberMossBlock> AMBER_MOSS_BLOCK = BLOCKS.register("amber_moss_block",
            id -> new AmberMossBlock(copyOf(id, Blocks.MOSS_BLOCK)));
    public static final DeferredBlock<UmbralMossCarpetBlock> AMBER_MOSS_CARPET = BLOCKS.register("amber_moss_carpet",
            id -> new UmbralMossCarpetBlock(copyOf(id, Blocks.MOSS_CARPET).noOcclusion()));
    public static final DeferredBlock<AmberLichenBlock> AMBER_LICHEN = BLOCKS.register("amber_lichen",
            id -> new AmberLichenBlock(copyOf(id, Blocks.GLOW_LICHEN).lightLevel(state -> 4)));
    public static final DeferredBlock<Block> BILE_VEIN = BLOCKS.register("bile_vein",
            id -> new Block(copyOf(id, Blocks.NETHERRACK).lightLevel(state -> 2)));
    public static final DeferredBlock<CreepvineBlock> CREEPVINE = BLOCKS.register("creepvine",
            id -> new CreepvineBlock(copyOf(id, Blocks.VINE)));
    public static final DeferredBlock<BlushMossBlock> BLUSH_MOSS_BLOCK = BLOCKS.register("blush_moss_block",
            id -> new BlushMossBlock(copyOf(id, Blocks.MOSS_BLOCK)));
    public static final DeferredBlock<BlushMossCarpetBlock> BLUSH_MOSS_CARPET = BLOCKS.register("blush_moss_carpet",
            id -> new BlushMossCarpetBlock(copyOf(id, Blocks.MOSS_CARPET).noOcclusion()));
    public static final DeferredBlock<DreamTorchBlock> DREAM_TORCH = BLOCKS.register("dream_torch",
            id -> new DreamTorchBlock(copyOf(id, Blocks.SOUL_TORCH)));
    public static final DeferredBlock<DreamWallTorchBlock> DREAM_WALL_TORCH = BLOCKS.register("dream_wall_torch",
            id -> new DreamWallTorchBlock(copyOf(id, Blocks.SOUL_WALL_TORCH)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock> DREAM_CEILING_TORCH = BLOCKS.register("dream_ceiling_torch",
            id -> new com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock(copyOf(id, Blocks.SOUL_TORCH)));
    public static final DeferredBlock<LanternBlock> DREAM_LANTERN = BLOCKS.register("dream_lantern",
            id -> new LanternBlock(copyOf(id, Blocks.SOUL_LANTERN)));
    public static final DeferredBlock<DreamCampfireBlock> DREAM_CAMPFIRE = BLOCKS.register("dream_campfire",
            id -> new DreamCampfireBlock(copyOf(id, Blocks.SOUL_CAMPFIRE)));
    public static final DeferredBlock<DreamFireBlock> DREAM_FIRE = BLOCKS.register("dream_fire",
            id -> new DreamFireBlock(copyOf(id, Blocks.SOUL_FIRE)));
    public static final DeferredBlock<DreamCeilingFireBlock> DREAM_CEILING_FIRE = BLOCKS.register("dream_fire_ceiling",
            id -> new DreamCeilingFireBlock(copyOf(id, Blocks.SOUL_FIRE)));
    public static final DeferredBlock<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            id -> new BedBugEggBlock(propertiesOf(id)
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollision()
                    .replaceable()));
    public static final DeferredBlock<CrawlingBlightEggBlock> CRAWLING_BLIGHT_EGG = BLOCKS.register("crawling_blight_egg",
            id -> new CrawlingBlightEggBlock(copyOf(id, Blocks.TURTLE_EGG)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PrinceEggBlock> PRINCE_EGG = BLOCKS.register("prince_egg",
            id -> new com.craisinlord.antarchy.content.block.PrinceEggBlock(copyOf(id, Blocks.DRAGON_EGG).randomTicks().noOcclusion()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PrincessEggBlock> PRINCESS_EGG = BLOCKS.register("princess_egg",
            id -> new com.craisinlord.antarchy.content.block.PrincessEggBlock(copyOf(id, Blocks.DRAGON_EGG).randomTicks().noOcclusion()));
    public static final DeferredBlock<SpringbugEggBlock> SPRINGBUG_EGG = BLOCKS.register("springbug_egg",
            id -> new SpringbugEggBlock(copyOf(id, Blocks.TURTLE_EGG)));
    public static final DeferredBlock<SpitBugEggBlock> SPIT_BUG_EGG = BLOCKS.register("spit_bug_egg",
            id -> new SpitBugEggBlock(copyOf(id, Blocks.TURTLE_EGG)));
    public static final DeferredBlock<JerryEggBlock> JERRY_EGG = BLOCKS.register("jerry_egg",
            id -> new JerryEggBlock(copyOf(id, Blocks.TURTLE_EGG).randomTicks()));
    public static final DeferredBlock<SkulkingFrightEggBlock> SKULKING_FRIGHT_EGG = BLOCKS.register("skulking_fright_egg",
            id -> new SkulkingFrightEggBlock(copyOf(id, Blocks.TURTLE_EGG)));

    static {
        BiowartBlock.bindTendrils(BIOWART_TENDRILS);
    }
    public static final DeferredBlock<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            id -> new WaspNestBlock(AntarchyNeoforgeItems::waspNestBlockEntityType, copyOf(id, Blocks.BEE_NEST)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            id -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(copyOf(id, Blocks.COARSE_DIRT).noLootTable()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock> BRUTALFLY_COCOON_SPAWNER = BLOCKS.register("brutalfly_cocoon_spawner",
            id -> new com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock(propertiesOf(id).noCollision().instabreak().noLootTable()));
    public static final DeferredBlock<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            id -> new HushweedBlock(copyOf(id, Blocks.AZALEA).noCollision().noOcclusion()));
    public static final DeferredBlock<HangingCreeprootsBlock> HANGING_CREEPROOTS = BLOCKS.register("hanging_creeproots",
            id -> new HangingCreeprootsBlock(copyOf(id, Blocks.HANGING_ROOTS)));
    public static final DeferredBlock<MoltingVinesBlock> MOLTING_VINES = BLOCKS.register("molting_vines",
            id -> new MoltingVinesBlock(copyOf(id, Blocks.WEEPING_VINES)));
    public static final DeferredBlock<LiquidBlock> BILE_BLOCK = BLOCKS.register("bile",
            id -> new BileLiquidBlock((FlowingFluid) AntarchyNeoforgeMisc.BILE.get(), copyOf(id, Blocks.WATER).lightLevel(state -> 5).noLootTable()));
    public static final DeferredBlock<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            id -> new LiquidBlock((FlowingFluid) AntarchyNeoforgeMisc.ICHOR.get(), copyOf(id, Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            id -> new com.craisinlord.antarchy.content.fluid.AntiwaterLiquidBlock((FlowingFluid) AntarchyNeoforgeMisc.ANTIWATER.get(),
                    copyOf(id, Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> LUMEN_BLOCK = BLOCKS.register("lumen",
            id -> new LumenLiquidBlock((FlowingFluid) AntarchyNeoforgeMisc.LUMEN.get(),
                    copyOf(id, Blocks.WATER).lightLevel(state -> 9).noLootTable()));
    public static final DeferredBlock<Block> LUMEN_FROGLIGHT = BLOCKS.register("lumen_froglight",
            id -> new Block(copyOf(id, Blocks.OCHRE_FROGLIGHT)));
    public static final DeferredBlock<Block> ROSEATE_FROGLIGHT = BLOCKS.register("roseate_froglight",
            id -> new Block(copyOf(id, Blocks.OCHRE_FROGLIGHT)));
    public static final DeferredBlock<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            id -> new AntNestBlock(
                    AntarchyNeoforgeEntites.RED_ANT,
                    AntarchyNeoforgeItems::antNestBlockEntityType,
                    true,
                    copyOf(id, Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));
    public static final DeferredBlock<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            id -> new AntNestBlock(AntarchyNeoforgeEntites.BROWN_ANT, AntarchyNeoforgeItems::antNestBlockEntityType, copyOf(id, Blocks.DIRT).noLootTable()));
    public static final DeferredBlock<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            id -> new AntNestBlock(AntarchyNeoforgeEntites.RAINBOW_ANT, AntarchyNeoforgeItems::antNestBlockEntityType, copyOf(id, Blocks.DIRT).noLootTable()));
    public static final DeferredBlock<AntNestBlock> TERMITE_NEST = BLOCKS.register("termite_nest",
            id -> new AntNestBlock(AntarchyNeoforgeEntites.TERMITE, AntarchyNeoforgeItems::antNestBlockEntityType, copyOf(id, Blocks.DIRT).noLootTable()));

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntTrapBlock> ANT_TRAP = BLOCKS.register("ant_trap",
            id -> new com.craisinlord.antarchy.content.block.AntTrapBlock(
                    () -> java.util.List.of(AntarchyNeoforgeEntites.RED_ANT.get(), AntarchyNeoforgeEntites.BROWN_ANT.get(), AntarchyNeoforgeEntites.RAINBOW_ANT.get(), AntarchyNeoforgeEntites.TERMITE.get()),
                    AntarchyNeoforgeItems::antNestBlockEntityType, copyOf(id, Blocks.OAK_PRESSURE_PLATE).noOcclusion()));
    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            id -> createOre(id, Blocks.EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            id -> createOre(id, Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.register("titanium_ore",
            id -> createOre(id, Blocks.DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.register("deepslate_titanium_ore",
            id -> createOre(id, Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<BluestoneOreBlock> BLUESTONE_ORE = BLOCKS.register("bluestone_ore",
            id -> new BluestoneOreBlock(copyOf(id, Blocks.REDSTONE_ORE).mapColor(MapColor.COLOR_BLUE)));
    public static final DeferredBlock<BluestoneWireBlock> BLUESTONE_WIRE = BLOCKS.register("bluestone_wire",
            id -> new BluestoneWireBlock(copyOf(id, Blocks.REDSTONE_WIRE).mapColor(MapColor.COLOR_BLUE).noCollision().instabreak()));
    public static final DeferredBlock<BluestoneBlock> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block",
            id -> new BluestoneBlock(copyOf(id, Blocks.REDSTONE_BLOCK).mapColor(MapColor.COLOR_BLUE)));
    public static final DeferredBlock<VortexLensBlock> VORTEX_LENS = BLOCKS.register("vortex_lens",
            id -> new VortexLensBlock(AntarchyNeoforgeBlocks::vortexLensBlockEntityType,
                    copyOf(id, Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GREEN).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<BluestoneRepeaterBlock> BLUESTONE_REPEATER = BLOCKS.register("bluestone_repeater",
            id -> new BluestoneRepeaterBlock(copyOf(id, Blocks.REPEATER).mapColor(MapColor.COLOR_BLUE).noCollision()));
    public static final DeferredBlock<BluestoneComparatorBlock> BLUESTONE_COMPARATOR = BLOCKS.register("bluestone_comparator",
            id -> new BluestoneComparatorBlock(copyOf(id, Blocks.COMPARATOR).mapColor(MapColor.COLOR_BLUE).noCollision(), AntarchyNeoforgeBlocks::bluestoneComparatorBlockEntityType));
    public static final DeferredBlock<BluestoneTorchBlock> BLUESTONE_TORCH = BLOCKS.register("bluestone_torch",
            id -> new BluestoneTorchBlock(copyOf(id, Blocks.REDSTONE_TORCH).mapColor(MapColor.COLOR_BLUE).lightLevel(state -> state.getValue(BluestoneTorchBlock.LIT) ? 7 : 0).noCollision().instabreak()));
    public static final DeferredBlock<BluestoneLampBlock> BLUESTONE_LAMP = BLOCKS.register("bluestone_lamp",
            id -> new BluestoneLampBlock(copyOf(id, Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_BLUE)));
    public static final DeferredBlock<Block> TIGERS_EYE_BLOCK = BLOCKS.register("tigers_eye_block",
            id -> createStorageBlock(id, Blocks.EMERALD_BLOCK, MapColor.COLOR_ORANGE));
    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.register("uranium_block",
            id -> createStorageBlock(id, Blocks.EMERALD_BLOCK, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            id -> createStorageBlock(id, Blocks.DIAMOND_BLOCK, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<Block> RAW_URANIUM_BLOCK = BLOCKS.register("raw_uranium_block",
            id -> createRawStorageBlock(id, Blocks.EMERALD_ORE, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = BLOCKS.register("raw_titanium_block",
            id -> createRawStorageBlock(id, Blocks.DIAMOND_ORE, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<Block> CUT_URANIUM = BLOCKS.register("cut_uranium",
            id -> createHorizontalFacingStorageBlock(id, Blocks.CUT_COPPER, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> CUT_TITANIUM = BLOCKS.register("cut_titanium",
            id -> createHorizontalFacingStorageBlock(id, Blocks.CUT_COPPER, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<SlabBlock> CUT_URANIUM_SLAB = BLOCKS.register("cut_uranium_slab",
            id -> new SlabBlock(copyOf(id, Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<SlabBlock> CUT_TITANIUM_SLAB = BLOCKS.register("cut_titanium_slab",
            id -> new SlabBlock(copyOf(id, Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<StairBlock> CUT_URANIUM_STAIRS = BLOCKS.register("cut_uranium_stairs",
            id -> new StairBlock(CUT_URANIUM.get().defaultBlockState(), copyOf(id, Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<StairBlock> CUT_TITANIUM_STAIRS = BLOCKS.register("cut_titanium_stairs",
            id -> new StairBlock(CUT_TITANIUM.get().defaultBlockState(), copyOf(id, Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> CHISELED_URANIUM = BLOCKS.register("chiseled_uranium",
            id -> createHorizontalFacingStorageBlock(id, Blocks.CHISELED_COPPER, MapColor.COLOR_YELLOW));
    public static final DeferredBlock<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            id -> createHorizontalFacingStorageBlock(id, Blocks.CHISELED_COPPER, MapColor.COLOR_LIGHT_BLUE));
    public static final DeferredBlock<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            id -> new SignalSavingBulbBlock(copyOf(id, Blocks.COPPER_BULB).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            id -> new SignalSavingBulbBlock(copyOf(id, Blocks.COPPER_BULB).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<DoorBlock> URANIUM_DOOR = BLOCKS.register("uranium_door",
            id -> new DoorBlock(BlockSetType.IRON, copyOf(id, Blocks.IRON_DOOR).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<DoorBlock> TITANIUM_DOOR = BLOCKS.register("titanium_door",
            id -> new DoorBlock(BlockSetType.IRON, copyOf(id, Blocks.IRON_DOOR).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<TrapDoorBlock> URANIUM_TRAPDOOR = BLOCKS.register("uranium_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.IRON, copyOf(id, Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<TrapDoorBlock> TITANIUM_TRAPDOOR = BLOCKS.register("titanium_trapdoor",
            id -> new TrapDoorBlock(BlockSetType.IRON, copyOf(id, Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<IronBarsBlock> URANIUM_BARS = BLOCKS.register("uranium_bars",
            id -> new IronBarsBlock(copyOf(id, Blocks.IRON_BARS).mapColor(MapColor.COLOR_YELLOW)));
    public static final DeferredBlock<IronBarsBlock> TITANIUM_BARS = BLOCKS.register("titanium_bars",
            id -> new IronBarsBlock(copyOf(id, Blocks.IRON_BARS).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<RotatedPillarBlock> ANTIMETAL = BLOCKS.register("antimetal",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<StairBlock> ANTIMETAL_STAIRS = BLOCKS.register("antimetal_stairs",
            id -> new StairBlock(ANTIMETAL.get().defaultBlockState(), copyOf(id, Blocks.BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<SlabBlock> ANTIMETAL_SLAB = BLOCKS.register("antimetal_slab",
            id -> new SlabBlock(copyOf(id, Blocks.BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<StairBlock> POLISHED_ANTIMETAL_STAIRS = BLOCKS.register("polished_antimetal_stairs",
            id -> new StairBlock(POLISHED_ANTIMETAL.get().defaultBlockState(), copyOf(id, Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<SlabBlock> POLISHED_ANTIMETAL_SLAB = BLOCKS.register("polished_antimetal_slab",
            id -> new SlabBlock(copyOf(id, Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            id -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(copyOf(id, Blocks.SCAFFOLDING).sound(antimetalScaffoldingSoundType())));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalRailBlock> ANTIMETAL_RAIL = BLOCKS.register("antimetal_rail",
            id -> new com.craisinlord.antarchy.content.block.AntimetalRailBlock(copyOf(id, Blocks.RAIL)));
    public static final DeferredBlock<UpperBlock> UPPER = BLOCKS.register("upper",
            id -> new UpperBlock(AntarchyNeoforgeBlocks::upperBlockEntityType, copyOf(id, Blocks.HOPPER)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock> ANTIMETAL_POWERED_RAIL = BLOCKS.register("antimetal_powered_rail",
            id -> new com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock(copyOf(id, Blocks.POWERED_RAIL)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock> ANTIMETAL_DETECTOR_RAIL = BLOCKS.register("antimetal_detector_rail",
            id -> new com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock(copyOf(id, Blocks.DETECTOR_RAIL)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock> ANTIMETAL_ACTIVATOR_RAIL = BLOCKS.register("antimetal_activator_rail",
            id -> new com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock(copyOf(id, Blocks.ACTIVATOR_RAIL)));
    public static final DeferredBlock<CorneaStalkBlock> CORNEA_STALK = BLOCKS.register("cornea_stalk",
            id -> new CorneaStalkBlock(copyOf(id, Blocks.SWEET_BERRY_BUSH).randomTicks()));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.GlowcapMushroomBlock> GLOWCAP_MUSHROOM = BLOCKS.register("glowcap_mushroom",
            id -> new com.craisinlord.antarchy.content.block.GlowcapMushroomBlock(copyOf(id, Blocks.RED_MUSHROOM).lightLevel(state -> 12).randomTicks()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_GLOWCAP_MUSHROOM = BLOCKS.register("potted_glowcap_mushroom",
            id -> new FlowerPotBlock(GLOWCAP_MUSHROOM.get(), copyOf(id, Blocks.POTTED_DANDELION).lightLevel(state -> 12)));
    public static final DeferredBlock<net.minecraft.world.level.block.HugeMushroomBlock> GLOWCAP_MUSHROOM_BLOCK = BLOCKS.register("glowcap_mushroom_block",
            id -> new net.minecraft.world.level.block.HugeMushroomBlock(copyOf(id, Blocks.RED_MUSHROOM_BLOCK).lightLevel(state -> 13)));
    public static final DeferredBlock<CornCropBlock> CORN_CROP = BLOCKS.register("corn_crop",
            id -> new CornCropBlock(copyOf(id, Blocks.WHEAT).mapColor(net.minecraft.world.level.material.MapColor.PLANT).randomTicks().noCollision().noOcclusion()));
    public static final DeferredBlock<WildCornBlock> WILD_CORN = BLOCKS.register("wild_corn",
            id -> new WildCornBlock(copyOf(id, Blocks.WHEAT).mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollision().noOcclusion()));
    public static final DeferredBlock<Block> FALLEN_KING_CROWN = BLOCKS.register("fallen_king_crown",
            id -> new Block(propertiesOf(id)
                    .strength(0.2F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<AmethystClusterBlock> SMALL_BLOOD_CRYSTAL_BUD = BLOCKS.register("small_blood_crystal_bud",
            id -> new AmethystClusterBlock(3.0F, 4.0F, copyOf(id, Blocks.SMALL_AMETHYST_BUD)));
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_BLOOD_CRYSTAL_BUD = BLOCKS.register("medium_blood_crystal_bud",
            id -> new AmethystClusterBlock(4.0F, 3.0F, copyOf(id, Blocks.MEDIUM_AMETHYST_BUD)));
    public static final DeferredBlock<AmethystClusterBlock> LARGE_BLOOD_CRYSTAL_BUD = BLOCKS.register("large_blood_crystal_bud",
            id -> new AmethystClusterBlock(5.0F, 3.0F, copyOf(id, Blocks.LARGE_AMETHYST_BUD)));
    public static final DeferredBlock<Block> BUDDING_BLOOD_CRYSTAL = BLOCKS.register("budding_blood_crystal",
            id -> new BuddingBloodCrystalBlock(
                    copyOf(id, Blocks.BUDDING_AMETHYST),
                    AntarchyNeoforgeBlocks::smallBloodCrystalBudBlock,
                    AntarchyNeoforgeBlocks::mediumBloodCrystalBudBlock,
                    AntarchyNeoforgeBlocks::largeBloodCrystalBudBlock,
                    AntarchyNeoforgeBlocks::bloodCrystalCrystalBlock
            ));
    public static final DeferredBlock<Block> BLOOD_CRYSTAL = BLOCKS.register("blood_crystal_block",
            id -> new Block(copyOf(id, Blocks.AMETHYST_BLOCK)));
    public static final DeferredBlock<AmethystClusterBlock> BLOOD_CRYSTAL_CRYSTAL = BLOCKS.register("blood_crystal_cluster",
            id -> new AmethystClusterBlock(7.0F, 3.0F, copyOf(id, Blocks.AMETHYST_CLUSTER)));
    public static final DeferredBlock<Block> DREAM_SAND = BLOCKS.register("dream_sand",
            id -> new DreamSandBlock(copyOf(id, Blocks.SAND)));
    public static final DeferredBlock<AntigravelBlock> ANTIGRAVEL = BLOCKS.register("antigravel",
            id -> new AntigravelBlock(copyOf(id, Blocks.GRAVEL)));
    public static final DeferredBlock<Block> LOAM = BLOCKS.register("loam",
            id -> new Block(copyOf(id, Blocks.MUD)));
    public static final DeferredBlock<Block> PRIMORDIAL_LOAM = BLOCKS.register("primordial_loam",
            id -> new Block(copyOf(id, Blocks.MUD).sound(primordialLoamSoundType())));
    public static final DeferredBlock<Block> PACKED_LOAM = BLOCKS.register("packed_loam",
            id -> new Block(copyOf(id, Blocks.PACKED_MUD)));
    public static final DeferredBlock<Block> LOAM_BRICKS = BLOCKS.register("loam_bricks",
            id -> new Block(copyOf(id, Blocks.MUD_BRICKS)));
    public static final DeferredBlock<StairBlock> LOAM_BRICK_STAIRS = BLOCKS.register("loam_brick_stairs",
            id -> new StairBlock(LOAM_BRICKS.get().defaultBlockState(), copyOf(id, Blocks.MUD_BRICK_STAIRS)));
    public static final DeferredBlock<SlabBlock> LOAM_BRICK_SLAB = BLOCKS.register("loam_brick_slab",
            id -> new SlabBlock(copyOf(id, Blocks.MUD_BRICK_SLAB)));
    public static final DeferredBlock<WallBlock> LOAM_BRICK_WALL = BLOCKS.register("loam_brick_wall",
            id -> new WallBlock(copyOf(id, Blocks.MUD_BRICK_WALL)));
    public static final DeferredBlock<Block> CHISELED_LOAM_BRICKS = BLOCKS.register("chiseled_loam_bricks",
            id -> new Block(copyOf(id, Blocks.MUD_BRICKS)));
    public static final DeferredBlock<MucusBlock> MUCUS = BLOCKS.register("mucus",
            id -> new MucusBlock(copyOf(id, Blocks.GLOW_LICHEN).friction(0.995F).lightLevel(state -> 0).sound(SoundType.SLIME_BLOCK)));
    public static final DeferredBlock<DreamSandstoneBlock> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            id -> new DreamSandstoneBlock(copyOf(id, Blocks.SANDSTONE)));
    public static final DeferredBlock<Block> CHISELED_DREAM_SANDSTONE = BLOCKS.register("chiseled_dream_sandstone",
            id -> new Block(copyOf(id, Blocks.CHISELED_SANDSTONE)));
    public static final DeferredBlock<Block> CUT_DREAM_SANDSTONE = BLOCKS.register("cut_dream_sandstone",
            id -> new Block(copyOf(id, Blocks.CUT_SANDSTONE)));
    public static final DeferredBlock<Block> SMOOTH_DREAM_SANDSTONE = BLOCKS.register("smooth_dream_sandstone",
            id -> new Block(copyOf(id, Blocks.SMOOTH_SANDSTONE)));
    public static final DeferredBlock<StairBlock> DREAM_SANDSTONE_STAIRS = BLOCKS.register("dream_sandstone_stairs",
            id -> new StairBlock(DREAM_SANDSTONE.get().defaultBlockState(), copyOf(id, Blocks.SANDSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> DREAM_SANDSTONE_SLAB = BLOCKS.register("dream_sandstone_slab",
            id -> new SlabBlock(copyOf(id, Blocks.SANDSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> DREAM_SANDSTONE_WALL = BLOCKS.register("dream_sandstone_wall",
            id -> new WallBlock(copyOf(id, Blocks.SANDSTONE_WALL)));
    public static final DeferredBlock<StairBlock> SMOOTH_DREAM_SANDSTONE_STAIRS = BLOCKS.register("smooth_dream_sandstone_stairs",
            id -> new StairBlock(SMOOTH_DREAM_SANDSTONE.get().defaultBlockState(), copyOf(id, Blocks.SMOOTH_SANDSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> SMOOTH_DREAM_SANDSTONE_SLAB = BLOCKS.register("smooth_dream_sandstone_slab",
            id -> new SlabBlock(copyOf(id, Blocks.SMOOTH_SANDSTONE_SLAB)));
    public static final DeferredBlock<SlabBlock> CUT_DREAM_SANDSTONE_SLAB = BLOCKS.register("cut_dream_sandstone_slab",
            id -> new SlabBlock(copyOf(id, Blocks.CUT_SANDSTONE_SLAB)));
    public static final DeferredBlock<Block> TYPHONITE = BLOCKS.register("typhonite",
            id -> new Block(copyOf(id, Blocks.BLACKSTONE)));
    public static final DeferredBlock<DirectionalNyxiumBlock> NADIR_NYXIUM = BLOCKS.register("nadir_nyxium",
            id -> new DirectionalNyxiumBlock(nyxiteProperties(id), DirectionalNyxiumBlock.Type.NADIR));
    public static final DeferredBlock<DirectionalNyxiumBlock> VERDANT_NYXIUM = BLOCKS.register("verdant_nyxium",
            id -> new DirectionalNyxiumBlock(nyxiteProperties(id), DirectionalNyxiumBlock.Type.VERDANT));
    public static final DeferredBlock<RotatedPillarBlock> TYPHONITE_PILLAR = BLOCKS.register("typhonite_pillar",
            id -> new RotatedPillarBlock(copyOf(id, Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> TYPHONITE_BRICKS = BLOCKS.register("typhonite_bricks",
            id -> new Block(copyOf(id, Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> CHISELED_TYPHONITE = BLOCKS.register("chiseled_typhonite",
            id -> new Block(copyOf(id, Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> POLISHED_TYPHONITE = BLOCKS.register("polished_typhonite",
            id -> new Block(copyOf(id, Blocks.BLACKSTONE)));
    public static final DeferredBlock<Block> VEINED_TYPHONITE = BLOCKS.register("veined_typhonite",
            id -> new Block(copyOf(id, Blocks.BLACKSTONE).lightLevel(state -> 8)));
    public static final DeferredBlock<TyphoniteSpikeBlock> TYPHONITE_SPIKE = BLOCKS.register("typhonite_spike",
            id -> new TyphoniteSpikeBlock(copyOf(id, Blocks.POINTED_DRIPSTONE)));
    public static final DeferredBlock<StairBlock> TYPHONITE_STAIRS = BLOCKS.register("typhonite_stairs",
            id -> new StairBlock(TYPHONITE.get().defaultBlockState(), copyOf(id, Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> TYPHONITE_SLAB = BLOCKS.register("typhonite_slab",
            id -> new SlabBlock(copyOf(id, Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> TYPHONITE_WALL = BLOCKS.register("typhonite_wall",
            id -> new WallBlock(copyOf(id, Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<StairBlock> TYPHONITE_BRICK_STAIRS = BLOCKS.register("typhonite_brick_stairs",
            id -> new StairBlock(TYPHONITE_BRICKS.get().defaultBlockState(), copyOf(id, Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> TYPHONITE_BRICK_SLAB = BLOCKS.register("typhonite_brick_slab",
            id -> new SlabBlock(copyOf(id, Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> TYPHONITE_BRICK_WALL = BLOCKS.register("typhonite_brick_wall",
            id -> new WallBlock(copyOf(id, Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<StairBlock> POLISHED_TYPHONITE_STAIRS = BLOCKS.register("polished_typhonite_stairs",
            id -> new StairBlock(POLISHED_TYPHONITE.get().defaultBlockState(), copyOf(id, Blocks.BLACKSTONE_STAIRS)));
    public static final DeferredBlock<SlabBlock> POLISHED_TYPHONITE_SLAB = BLOCKS.register("polished_typhonite_slab",
            id -> new SlabBlock(copyOf(id, Blocks.BLACKSTONE_SLAB)));
    public static final DeferredBlock<WallBlock> POLISHED_TYPHONITE_WALL = BLOCKS.register("polished_typhonite_wall",
            id -> new WallBlock(copyOf(id, Blocks.BLACKSTONE_WALL)));
    public static final DeferredBlock<SpiralingVinesBlock> SPIRALING_VINES = BLOCKS.register("spiraling_vines",
            id -> new SpiralingVinesBlock(copyOf(id, Blocks.TWISTING_VINES)));
    public static final DeferredBlock<GorevineBlock> GOREVINE = BLOCKS.register("gorevine",
            id -> new GorevineBlock(copyOf(id, Blocks.WEEPING_VINES).lightLevel(state -> 4)));
    public static final DeferredBlock<WhirlflowerBlock> WHIRLFLOWER = BLOCKS.register("whirlflower",
            id -> new WhirlflowerBlock(copyOf(id, Blocks.ALLIUM)));
    public static final DeferredBlock<NadirFernBlock> NADIR_FERN = BLOCKS.register("nadir_fern",
            id -> new NadirFernBlock(copyOf(id, Blocks.FERN)));
    public static final DeferredBlock<LargeNadirFernBlock> LARGE_NADIR_FERN = BLOCKS.register("large_nadir_fern",
            id -> new LargeNadirFernBlock(copyOf(id, Blocks.LARGE_FERN).noCollision()));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> DUSKBELL = BLOCKS.register("duskbell",
            id -> new DirectionalThoraxisFlowerBlock(copyOf(id, Blocks.ALLIUM).lightLevel(state -> 4)));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> NADIR_ROOTS = BLOCKS.register("nadir_roots",
            id -> new DirectionalThoraxisFlowerBlock(copyOf(id, Blocks.CRIMSON_ROOTS)));
    public static final DeferredBlock<DirectionalThoraxisFlowerBlock> VERDANT_ROOTS = BLOCKS.register("verdant_roots",
            id -> new DirectionalThoraxisFlowerBlock(copyOf(id, Blocks.WARPED_ROOTS)));
    public static final DeferredBlock<Block> DEAD_STAR_CORAL_BLOCK = BLOCKS.register("dead_star_coral_block",
            id -> new Block(copyOf(id, Blocks.TUBE_CORAL_BLOCK)));
    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralPlantBlock> DEAD_STAR_CORAL = BLOCKS.register("dead_star_coral",
            id -> new net.minecraft.world.level.block.BaseCoralPlantBlock(copyOf(id, Blocks.TUBE_CORAL)));
    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralFanBlock> DEAD_STAR_CORAL_FAN = BLOCKS.register("dead_star_coral_fan",
            id -> new net.minecraft.world.level.block.BaseCoralFanBlock(copyOf(id, Blocks.TUBE_CORAL_FAN)));
    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralWallFanBlock> DEAD_STAR_CORAL_WALL_FAN = BLOCKS.register("dead_star_coral_wall_fan",
            id -> new net.minecraft.world.level.block.BaseCoralWallFanBlock(copyOf(id, Blocks.TUBE_CORAL_WALL_FAN)));
    public static final DeferredBlock<Block> STAR_CORAL_BLOCK = BLOCKS.register("star_coral_block",
            id -> new com.craisinlord.antarchy.content.block.StarCoralBlock(DEAD_STAR_CORAL_BLOCK.get(), copyOf(id, Blocks.TUBE_CORAL_BLOCK).lightLevel(state -> 8)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.StarCoralPlantBlock> STAR_CORAL = BLOCKS.register("star_coral",
            id -> new com.craisinlord.antarchy.content.block.StarCoralPlantBlock(DEAD_STAR_CORAL.get(), copyOf(id, Blocks.TUBE_CORAL).lightLevel(state -> 8)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.StarCoralFanBlock> STAR_CORAL_FAN = BLOCKS.register("star_coral_fan",
            id -> new com.craisinlord.antarchy.content.block.StarCoralFanBlock(DEAD_STAR_CORAL_FAN.get(), copyOf(id, Blocks.TUBE_CORAL_FAN).lightLevel(state -> 8)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.StarCoralWallFanBlock> STAR_CORAL_WALL_FAN = BLOCKS.register("star_coral_wall_fan",
            id -> new com.craisinlord.antarchy.content.block.StarCoralWallFanBlock(DEAD_STAR_CORAL_WALL_FAN.get(), copyOf(id, Blocks.TUBE_CORAL_WALL_FAN).lightLevel(state -> 8)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity>> ROYAL_EGG_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("royal_egg",
            () -> new BlockEntityType<>(
                    com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity::new,
                    PRINCE_EGG.get(),
                    PRINCESS_EGG.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> new BlockEntityType<>(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get(),
                    TERMITE_NEST.get(),
                    ANT_TRAP.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dream_campfire",
            () -> new BlockEntityType<>(
                    DreamCampfireBlockEntity::new,
                    DREAM_CAMPFIRE.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("wasp_nest",
            () -> new BlockEntityType<>(
                    WaspNestBlockEntity::new,
                    WASP_NEST.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("hushweed",
            () -> new BlockEntityType<>(
                    HushweedBlockEntity::new,
                    HUSHWEED.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotentNyxiteBlockEntity>> POTENT_NYXITE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("potent_nyxite",
            () -> new BlockEntityType<>(
                    (pos, state) -> new PotentNyxiteBlockEntity(pos, state, AntarchyNeoforgeItems::potentNyxiteBlockEntityType),
                    POTENT_NYXITE.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeashellBlockEntity>> SEASHELL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("seashell",
            () -> new BlockEntityType<>(
                    SeashellBlockEntity::new,
                    SEASHELL.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lucid_anchor",
            () -> new BlockEntityType<>(
                    LucidAnchorBlockEntity::new,
                    LUCID_ANCHOR.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("critter_cage_block",
            () -> new BlockEntityType<>(
                    CritterCageBlockEntity::new,
                    CRITTER_CAGE_BLOCK.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity>> BLUESTONE_COMPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("bluestone_comparator",
            () -> new BlockEntityType<>(
                    (pos, state) -> new com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity(pos, state, AntarchyNeoforgeBlocks::bluestoneComparatorBlockEntityType),
                    BLUESTONE_COMPARATOR.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpperBlockEntity>> UPPER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("upper",
            () -> new BlockEntityType<>(
                    (pos, state) -> new UpperBlockEntity(pos, state, AntarchyNeoforgeBlocks::upperBlockEntityType),
                    UPPER.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VortexLensBlockEntity>> VORTEX_LENS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vortex_lens",
            () -> new BlockEntityType<>(
                    (pos, state) -> new VortexLensBlockEntity(pos, state, AntarchyNeoforgeBlocks::vortexLensBlockEntityType),
                    VORTEX_LENS.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalGunPortalMasterBlockEntity>> PORTAL_GUN_PORTAL_MASTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_master",
            () -> new BlockEntityType<>(
                    (pos, state) -> new PortalGunPortalMasterBlockEntity(pos, state, AntarchyNeoforgeBlocks::portalGunPortalMasterBlockEntityType),
                    PORTAL_GUN_PORTAL_MASTER.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalGunPortalBaseBlockEntity>> PORTAL_GUN_PORTAL_BASE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_base",
            () -> new BlockEntityType<>(
                    (pos, state) -> new PortalGunPortalBaseBlockEntity(pos, state, AntarchyNeoforgeBlocks::portalGunPortalBaseBlockEntityType),
                    PORTAL_GUN_PORTAL_BASE.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DimensionalTearMarkerBlockEntity>> DIMENSIONAL_TEAR_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dimensional_tear_marker",
            () -> new BlockEntityType<>(
                    (pos, state) -> new DimensionalTearMarkerBlockEntity(pos, state, AntarchyNeoforgeBlocks::dimensionalTearMarkerBlockEntityType),
                    DIMENSIONAL_TEAR_MARKER.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> QUEEN_TRAIL_SPAWN_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("queen_trail_spawn_marker",
            () -> new BlockEntityType<>(
                    (pos, state) -> new QueenTrailSpawnMarkerBlockEntity(pos, state, AntarchyNeoforgeBlocks::queenTrailSpawnMarkerBlockEntityType),
                    QUEEN_TRAIL_SPAWN_MARKER.get()
            ));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UndertrialSpawnerBlockEntity>> UNDERTRIAL_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undertrial_spawner",
            () -> new BlockEntityType<>((pos, state) -> new UndertrialSpawnerBlockEntity(pos, state, AntarchyNeoforgeBlocks::undertrialSpawnerBlockEntityType), UNDERTRIAL_SPAWNER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnderVaultBlockEntity>> UNDERVAULT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undervault",
            () -> new BlockEntityType<>((pos, state) -> new UnderVaultBlockEntity(pos, state, AntarchyNeoforgeBlocks::underVaultBlockEntityType), UNDERVAULT.get()));

    private AntarchyNeoforgeBlocks() {}

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    static BlockBehaviour.Properties propertiesOf(Identifier id) {
        return BlockBehaviour.Properties.of().setId(blockKey(id));
    }

    static BlockBehaviour.Properties copyOf(Identifier id, BlockBehaviour block) {
        return BlockBehaviour.Properties.ofFullCopy(block).setId(blockKey(id));
    }


    private static BlockEntityType<UndertrialSpawnerBlockEntity> undertrialSpawnerBlockEntityType() {
        return UNDERTRIAL_SPAWNER_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<UnderVaultBlockEntity> underVaultBlockEntityType() {
        return UNDERVAULT_BLOCK_ENTITY.get();
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    static BlockBehaviour.Properties nyxiteProperties(Identifier id) {
        return copyOf(id, Blocks.NETHERRACK);
    }

    static BlockBehaviour.Properties broodstoneProperties(Identifier id) {
        return copyOf(id, Blocks.DEEPSLATE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties portalProperties(Identifier id) {
        return propertiesOf(id)
                .strength(-1.0F)
                .noCollision()
                .noOcclusion()
                .lightLevel(state -> 11)
                .noLootTable()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    private static Block createOre(Identifier id, Block copyFrom, int minExperience, int maxExperience, MapColor mapColor) {
        return new DropExperienceBlock(UniformInt.of(minExperience, maxExperience), copyOf(id, copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    static Block createStorageBlock(Identifier id, Block copyFrom, MapColor mapColor) {
        return new Block(copyOf(id, copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    static Block createHorizontalFacingStorageBlock(Identifier id, Block copyFrom, MapColor mapColor) {
        return new SimpleHorizontalFacingBlock(copyOf(id, copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    static Block createRawStorageBlock(Identifier id, Block copyFrom, MapColor mapColor) {
        return new Block(copyOf(id, copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
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
                AntarchyNeoforgeSounds.ANTIMETAL_PLACE.get(),
                AntarchyNeoforgeSounds.ANTIMETAL_STEP.get(),
                AntarchyNeoforgeSounds.ANTIMETAL_PLACE.get(),
                AntarchyNeoforgeSounds.ANTIMETAL_STEP.get(),
                AntarchyNeoforgeSounds.ANTIMETAL_PLACE.get());
    }

    private static SoundType antimetalScaffoldingSoundType() {
        SoundEvent scaffold = AntarchyNeoforgeSounds.ANTIMETAL_SCAFFOLD.get();
        return new SoundType(1.0F, 1.0F, scaffold, scaffold, scaffold, scaffold, scaffold);
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
}
