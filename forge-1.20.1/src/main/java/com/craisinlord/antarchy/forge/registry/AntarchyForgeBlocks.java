package com.craisinlord.antarchy.forge.registry;

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
import com.craisinlord.antarchy.forge.content.block.WildCornBlock;
import com.craisinlord.antarchy.forge.AntarchyWoodTypes;
import com.craisinlord.antarchy.forge.content.fluid.AntiwaterFluidType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;

public final class AntarchyForgeBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);
    public static final RegistryObject<UndertrialSpawnerBlock> UNDERTRIAL_SPAWNER = BLOCKS.register("undertrial_spawner",
            () -> new UndertrialSpawnerBlock(AntarchyForgeBlocks::undertrialSpawnerBlockEntityType,
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(50.0F).noOcclusion().isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<UnderVaultBlock> UNDERVAULT = BLOCKS.register("undervault",
            () -> new UnderVaultBlock(AntarchyForgeBlocks::underVaultBlockEntityType,
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(50.0F).noOcclusion().isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<QueenTrailSpawnMarkerBlock> QUEEN_TRAIL_SPAWN_MARKER = BLOCKS.register("queen_trail_spawn_marker",
            () -> new QueenTrailSpawnMarkerBlock(AntarchyForgeBlocks::queenTrailSpawnMarkerBlockEntityType,
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK).noLootTable()));
    public static final RegistryObject<DuplicatorLogBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new DuplicatorLogBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).randomTicks()));
    public static final RegistryObject<OuranwoodLogBlock> OURANWOOD_LOG = BLOCKS.register("ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));
    public static final RegistryObject<OuranwoodLogBlock> OURANWOOD_WOOD = BLOCKS.register("ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));
    public static final RegistryObject<OuranwoodLogBlock> MOSSY_OURANWOOD_LOG = BLOCKS.register("mossy_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));
    public static final RegistryObject<OuranwoodLogBlock> MOSSY_OURANWOOD_WOOD = BLOCKS.register("mossy_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));
    public static final RegistryObject<OuranwoodLogBlock> STRIPPED_OURANWOOD_LOG = BLOCKS.register("stripped_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final RegistryObject<OuranwoodLogBlock> STRIPPED_OURANWOOD_WOOD = BLOCKS.register("stripped_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final RegistryObject<Block> OURANWOOD_PLANKS = BLOCKS.register("ouranwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));
    public static final RegistryObject<StairBlock> OURANWOOD_STAIRS = BLOCKS.register("ouranwood_stairs",
            () -> new StairBlock(OURANWOOD_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));
    public static final RegistryObject<SlabBlock> OURANWOOD_SLAB = BLOCKS.register("ouranwood_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));
    public static final RegistryObject<FenceBlock> OURANWOOD_FENCE = BLOCKS.register("ouranwood_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));
    public static final RegistryObject<FenceGateBlock> OURANWOOD_FENCE_GATE = BLOCKS.register("ouranwood_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), WoodType.JUNGLE));
    public static final RegistryObject<DoorBlock> OURANWOOD_DOOR = BLOCKS.register("ouranwood_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<TrapDoorBlock> OURANWOOD_TRAPDOOR = BLOCKS.register("ouranwood_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<PressurePlateBlock> OURANWOOD_PRESSURE_PLATE = BLOCKS.register("ouranwood_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), BlockSetType.JUNGLE));
    public static final RegistryObject<ButtonBlock> OURANWOOD_BUTTON = BLOCKS.register("ouranwood_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, true));
    public static final RegistryObject<OuranwoodLeavesBlock> OURANWOOD_LEAVES = BLOCKS.register("ouranwood_leaves",
            () -> new OuranwoodLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final RegistryObject<OuranwoodAcornBlock> OURANWOOD_ACORN_BLOCK = BLOCKS.register("ouranwood_acorn",
            () -> new OuranwoodAcornBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final RegistryObject<FlowerPotBlock> POTTED_OURANWOOD_ACORN = BLOCKS.register("potted_ouranwood_acorn",
            () -> new FlowerPotBlock(OURANWOOD_ACORN_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));
    public static final RegistryObject<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));
    public static final RegistryObject<GlimmeringReedBlock> GLIMMERING_REED = BLOCKS.register("glimmering_reed",
            () -> new GlimmeringReedBlock(BlockBehaviour.Properties.copy(Blocks.SUNFLOWER).lightLevel(state -> 8)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.CamelliaBlock> CAMELLIA = BLOCKS.register("camellia",
            () -> new com.craisinlord.antarchy.content.block.CamelliaBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.SpiderLilyBlock> SPIDER_LILY = BLOCKS.register("spider_lily",
            () -> new com.craisinlord.antarchy.content.block.SpiderLilyBlock(BlockBehaviour.Properties.copy(Blocks.POPPY)));
    public static final RegistryObject<FlowerPotBlock> POTTED_SPIDER_LILY = BLOCKS.register("potted_spider_lily",
            () -> new FlowerPotBlock(SPIDER_LILY.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<GiantLilyPadBlock> GIANT_LILY_PAD = BLOCKS.register("giant_lily_pad",
            () -> new GiantLilyPadBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.LotusBlock> LOTUS = BLOCKS.register("lotus",
            () -> new com.craisinlord.antarchy.content.block.LotusBlock(BlockBehaviour.Properties.copy(Blocks.SPORE_BLOSSOM)));
    public static final RegistryObject<FlowerPotBlock> POTTED_LOTUS = BLOCKS.register("potted_lotus",
            () -> new FlowerPotBlock(LOTUS.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<SeashellBlock> SEASHELL = BLOCKS.register("seashell",
            () -> new SeashellBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).noOcclusion()));
    public static final RegistryObject<LucidAnchorBlock> LUCID_ANCHOR = BLOCKS.register("lucid_anchor",
            () -> new LucidAnchorBlock(BlockBehaviour.Properties.copy(Blocks.BEACON).strength(3.0F)));
    public static final RegistryObject<CritterCageBlock> CRITTER_CAGE_BLOCK = BLOCKS.register("critter_cage_block",
            () -> new CritterCageBlock(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));
    public static final RegistryObject<PortalGunPortalMasterBlock> PORTAL_GUN_PORTAL_MASTER = BLOCKS.register("portal_gun_portal_master",
            () -> new PortalGunPortalMasterBlock(AntarchyForgeBlocks::portalGunPortalMasterBlockEntityType, portalProperties()));
    public static final RegistryObject<PortalGunPortalBaseBlock> PORTAL_GUN_PORTAL_BASE = BLOCKS.register("portal_gun_portal_base",
            () -> new PortalGunPortalBaseBlock(AntarchyForgeBlocks::portalGunPortalBaseBlockEntityType, portalProperties()));
    public static final RegistryObject<DimensionalTearMarkerBlock> DIMENSIONAL_TEAR_MARKER = BLOCKS.register("dimensional_tear_marker",
            () -> new DimensionalTearMarkerBlock(AntarchyForgeBlocks::dimensionalTearMarkerBlockEntityType,
                    BlockBehaviour.Properties.of().noCollission().noOcclusion().noLootTable().instabreak()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final RegistryObject<StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            () -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.OURANWOOD));
    public static final RegistryObject<WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            () -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.OURANWOOD));
    public static final RegistryObject<CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            () -> new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.OURANWOOD));
    public static final RegistryObject<WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            () -> new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.OURANWOOD));
    public static final RegistryObject<RotatedPillarBlock> PEACH_LOG = BLOCKS.register("peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> PEACH_WOOD = BLOCKS.register("peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_PEACH_LOG = BLOCKS.register("stripped_peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_PEACH_WOOD = BLOCKS.register("stripped_peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final RegistryObject<Block> PEACH_PLANKS = BLOCKS.register("peach_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));
    public static final RegistryObject<StairBlock> PEACH_STAIRS = BLOCKS.register("peach_stairs",
            () -> new StairBlock(PEACH_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));
    public static final RegistryObject<SlabBlock> PEACH_SLAB = BLOCKS.register("peach_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));
    public static final RegistryObject<FenceBlock> PEACH_FENCE = BLOCKS.register("peach_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));
    public static final RegistryObject<FenceGateBlock> PEACH_FENCE_GATE = BLOCKS.register("peach_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), WoodType.JUNGLE));
    public static final RegistryObject<DoorBlock> PEACH_DOOR = BLOCKS.register("peach_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<TrapDoorBlock> PEACH_TRAPDOOR = BLOCKS.register("peach_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<PressurePlateBlock> PEACH_PRESSURE_PLATE = BLOCKS.register("peach_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), BlockSetType.JUNGLE));
    public static final RegistryObject<ButtonBlock> PEACH_BUTTON = BLOCKS.register("peach_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, true));
    public static final RegistryObject<PeachLeavesBlock> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            () -> new PeachLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final RegistryObject<HangingPeachBlock> PEACH_HANGING_PEACH = BLOCKS.register("hanging_peach",
            () -> new HangingPeachBlock(BlockBehaviour.Properties.of().noOcclusion().instabreak()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.PeachSaplingBlock> PEACH_SAPLING = BLOCKS.register("peach_sapling",
            () -> new com.craisinlord.antarchy.content.block.PeachSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final RegistryObject<FlowerPotBlock> POTTED_PEACH_SAPLING = BLOCKS.register("potted_peach_sapling",
            () -> new FlowerPotBlock(PEACH_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<StandingSignBlock> PEACH_SIGN = BLOCKS.register("peach_sign",
            () -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.PEACH));
    public static final RegistryObject<WallSignBlock> PEACH_WALL_SIGN = BLOCKS.register("peach_wall_sign",
            () -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.PEACH));
    public static final RegistryObject<CeilingHangingSignBlock> PEACH_HANGING_SIGN = BLOCKS.register("peach_hanging_sign",
            () -> new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.PEACH));
    public static final RegistryObject<WallHangingSignBlock> PEACH_WALL_HANGING_SIGN = BLOCKS.register("peach_wall_hanging_sign",
            () -> new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.PEACH));
    public static final RegistryObject<RotatedPillarBlock> NADIR_LOG = BLOCKS.register("nadir_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> NADIR_WOOD = BLOCKS.register("nadir_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_NADIR_LOG = BLOCKS.register("stripped_nadir_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_NADIR_WOOD = BLOCKS.register("stripped_nadir_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final RegistryObject<Block> NADIR_PLANKS = BLOCKS.register("nadir_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));
    public static final RegistryObject<StairBlock> NADIR_STAIRS = BLOCKS.register("nadir_stairs",
            () -> new StairBlock(NADIR_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));
    public static final RegistryObject<SlabBlock> NADIR_SLAB = BLOCKS.register("nadir_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));
    public static final RegistryObject<FenceBlock> NADIR_FENCE = BLOCKS.register("nadir_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));
    public static final RegistryObject<FenceGateBlock> NADIR_FENCE_GATE = BLOCKS.register("nadir_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), WoodType.JUNGLE));
    public static final RegistryObject<DoorBlock> NADIR_DOOR = BLOCKS.register("nadir_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<TrapDoorBlock> NADIR_TRAPDOOR = BLOCKS.register("nadir_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<PressurePlateBlock> NADIR_PRESSURE_PLATE = BLOCKS.register("nadir_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), BlockSetType.JUNGLE));
    public static final RegistryObject<ButtonBlock> NADIR_BUTTON = BLOCKS.register("nadir_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, true));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.NadirVeilBlock> NADIR_VEIL = BLOCKS.register("nadir_veil",
            () -> new com.craisinlord.antarchy.content.block.NadirVeilBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).sound(SoundType.SLIME_BLOCK).noOcclusion()));
    public static final RegistryObject<NadirSaplingBlock> NADIR_SAPLING = BLOCKS.register("nadir_sapling",
            () -> new NadirSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final RegistryObject<FlowerPotBlock> POTTED_NADIR_SAPLING = BLOCKS.register("potted_nadir_sapling",
            () -> new FlowerPotBlock(NADIR_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<StandingSignBlock> NADIR_SIGN = BLOCKS.register("nadir_sign",
            () -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.NADIR));
    public static final RegistryObject<WallSignBlock> NADIR_WALL_SIGN = BLOCKS.register("nadir_wall_sign",
            () -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.NADIR));
    public static final RegistryObject<CeilingHangingSignBlock> NADIR_HANGING_SIGN = BLOCKS.register("nadir_hanging_sign",
            () -> new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.NADIR));
    public static final RegistryObject<WallHangingSignBlock> NADIR_WALL_HANGING_SIGN = BLOCKS.register("nadir_wall_hanging_sign",
            () -> new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.NADIR));
    public static final RegistryObject<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            () -> new DuplicatorSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final RegistryObject<RotatedPillarBlock> ROYAL_LOG = BLOCKS.register("royal_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> ROYAL_WOOD = BLOCKS.register("royal_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_ROYAL_LOG = BLOCKS.register("stripped_royal_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_ROYAL_WOOD = BLOCKS.register("stripped_royal_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final RegistryObject<Block> ROYAL_PLANKS = BLOCKS.register("royal_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));
    public static final RegistryObject<StairBlock> ROYAL_STAIRS = BLOCKS.register("royal_stairs",
            () -> new StairBlock(ROYAL_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));
    public static final RegistryObject<SlabBlock> ROYAL_SLAB = BLOCKS.register("royal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));
    public static final RegistryObject<FenceBlock> ROYAL_FENCE = BLOCKS.register("royal_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));
    public static final RegistryObject<FenceGateBlock> ROYAL_FENCE_GATE = BLOCKS.register("royal_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), WoodType.JUNGLE));
    public static final RegistryObject<DoorBlock> ROYAL_DOOR = BLOCKS.register("royal_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<TrapDoorBlock> ROYAL_TRAPDOOR = BLOCKS.register("royal_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<PressurePlateBlock> ROYAL_PRESSURE_PLATE = BLOCKS.register("royal_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), BlockSetType.JUNGLE));
    public static final RegistryObject<ButtonBlock> ROYAL_BUTTON = BLOCKS.register("royal_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, true));
    public static final RegistryObject<RoyalLeavesBlock> ROYAL_LEAVES = BLOCKS.register("royal_leaves",
            () -> new RoyalLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks()));
    public static final RegistryObject<RoyalLeavesBlock> ROYAL_FLOWERING_LEAVES = BLOCKS.register("royal_flowering_leaves",
            () -> new RoyalLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks().lightLevel(state -> 12)));
    public static final RegistryObject<RoyalSaplingBlock> ROYAL_SAPLING = BLOCKS.register("royal_sapling",
            () -> new RoyalSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).noCollission()));
    public static final RegistryObject<FlowerPotBlock> POTTED_ROYAL_SAPLING = BLOCKS.register("potted_royal_sapling",
            () -> new FlowerPotBlock(ROYAL_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<StandingSignBlock> ROYAL_SIGN = BLOCKS.register("royal_sign",
            () -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.ROYAL));
    public static final RegistryObject<WallSignBlock> ROYAL_WALL_SIGN = BLOCKS.register("royal_wall_sign",
            () -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.ROYAL));
    public static final RegistryObject<CeilingHangingSignBlock> ROYAL_HANGING_SIGN = BLOCKS.register("royal_hanging_sign",
            () -> new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.ROYAL));
    public static final RegistryObject<WallHangingSignBlock> ROYAL_WALL_HANGING_SIGN = BLOCKS.register("royal_wall_hanging_sign",
            () -> new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.ROYAL));
    public static final RegistryObject<TruffaloLogBlock> TRUFFALO_LOG = BLOCKS.register("truffalo_log",
            () -> new TruffaloLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)) {
                @Override
                public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction itemAbility, boolean simulate) {
                    if (itemAbility == ToolActions.AXE_STRIP) {
                        return STRIPPED_TRUFFALO_LOG.get().withPropertiesOf(state);
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });
    public static final RegistryObject<RotatedPillarBlock> TRUFFALO_WOOD = BLOCKS.register("truffalo_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)) {
                @Override
                public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction itemAbility, boolean simulate) {
                    if (itemAbility == ToolActions.AXE_STRIP) {
                        return STRIPPED_TRUFFALO_WOOD.get().withPropertiesOf(state);
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });
    public static final RegistryObject<TruffaloLogBlock> STRIPPED_TRUFFALO_LOG = BLOCKS.register("stripped_truffalo_log",
            () -> new TruffaloLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));
    public static final RegistryObject<RotatedPillarBlock> STRIPPED_TRUFFALO_WOOD = BLOCKS.register("stripped_truffalo_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));
    public static final RegistryObject<Block> TRUFFALO_PLANKS = BLOCKS.register("truffalo_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));
    public static final RegistryObject<StairBlock> TRUFFALO_STAIRS = BLOCKS.register("truffalo_stairs",
            () -> new StairBlock(TRUFFALO_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));
    public static final RegistryObject<SlabBlock> TRUFFALO_SLAB = BLOCKS.register("truffalo_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));
    public static final RegistryObject<FenceBlock> TRUFFALO_FENCE = BLOCKS.register("truffalo_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));
    public static final RegistryObject<FenceGateBlock> TRUFFALO_FENCE_GATE = BLOCKS.register("truffalo_fence_gate",
            () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), WoodType.JUNGLE));
    public static final RegistryObject<DoorBlock> TRUFFALO_DOOR = BLOCKS.register("truffalo_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<TrapDoorBlock> TRUFFALO_TRAPDOOR = BLOCKS.register("truffalo_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), BlockSetType.JUNGLE));
    public static final RegistryObject<PressurePlateBlock> TRUFFALO_PRESSURE_PLATE = BLOCKS.register("truffalo_pressure_plate",
            () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), BlockSetType.JUNGLE));
    public static final RegistryObject<ButtonBlock> TRUFFALO_BUTTON = BLOCKS.register("truffalo_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, true));
    public static final RegistryObject<TruffaloSaplingBlock> TRUFFALO_SAPLING = BLOCKS.register("truffalo_sapling",
            () -> new TruffaloSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final RegistryObject<FlowerPotBlock> POTTED_TRUFFALO_SAPLING = BLOCKS.register("potted_truffalo_sapling",
            () -> new FlowerPotBlock(TRUFFALO_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final RegistryObject<StandingSignBlock> TRUFFALO_SIGN = BLOCKS.register("truffalo_sign",
            () -> new StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.TRUFFALO));
    public static final RegistryObject<WallSignBlock> TRUFFALO_WALL_SIGN = BLOCKS.register("truffalo_wall_sign",
            () -> new WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.TRUFFALO));
    public static final RegistryObject<CeilingHangingSignBlock> TRUFFALO_HANGING_SIGN = BLOCKS.register("truffalo_hanging_sign",
            () -> new CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.TRUFFALO));
    public static final RegistryObject<WallHangingSignBlock> TRUFFALO_WALL_HANGING_SIGN = BLOCKS.register("truffalo_wall_hanging_sign",
            () -> new WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.TRUFFALO));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_WHITE = BLOCKS.register("white_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_ORANGE = BLOCKS.register("orange_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.ORANGE_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_MAGENTA = BLOCKS.register("magenta_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.MAGENTA_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_BLUE = BLOCKS.register("light_blue_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_BLUE_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_YELLOW = BLOCKS.register("yellow_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.YELLOW_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_LIME = BLOCKS.register("lime_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.LIME_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_PINK = BLOCKS.register("pink_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.PINK_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_GRAY = BLOCKS.register("gray_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.GRAY_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_LIGHT_GRAY = BLOCKS.register("light_gray_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT_GRAY_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_CYAN = BLOCKS.register("cyan_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.CYAN_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_PURPLE = BLOCKS.register("purple_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.PURPLE_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_BLUE = BLOCKS.register("blue_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.BLUE_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_BROWN = BLOCKS.register("brown_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.BROWN_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_GREEN = BLOCKS.register("green_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.GREEN_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_RED = BLOCKS.register("red_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.RED_WOOL)));
    public static final RegistryObject<TruffaloTuftBlock> TRUFFALO_TUFT_BLACK = BLOCKS.register("black_truffalo_tuft",
            () -> new TruffaloTuftBlock(BlockBehaviour.Properties.copy(Blocks.BLACK_WOOL)));
    public static final RegistryObject<DuctTapeBlock> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new DuctTapeBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()));
    public static final RegistryObject<InfestedRootedDirtBlock> INFESTED_ROOTED_DIRT = BLOCKS.register("infested_rooted_dirt",
            () -> new InfestedRootedDirtBlock(BlockBehaviour.Properties.copy(Blocks.ROOTED_DIRT).randomTicks()));
    public static final RegistryObject<InfestedCoarseDirtBlock> INFESTED_COARSE_DIRT = BLOCKS.register("infested_coarse_dirt",
            () -> new InfestedCoarseDirtBlock(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT).randomTicks()));
    public static final RegistryObject<Block> NYXITE = BLOCKS.register("nyxite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<Block> POLISHED_NYXITE = BLOCKS.register("polished_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<Block> CHISELED_NYXITE = BLOCKS.register("chiseled_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<Block> NYXITE_BRICKS = BLOCKS.register("nyxite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<Block> MOSSY_NYXITE_BRICKS = BLOCKS.register("mossy_nyxite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<StairBlock> NYXITE_STAIRS = BLOCKS.register("nyxite_stairs",
            () -> new StairBlock(NYXITE.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> NYXITE_SLAB = BLOCKS.register("nyxite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> NYXITE_WALL = BLOCKS.register("nyxite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<StairBlock> POLISHED_NYXITE_STAIRS = BLOCKS.register("polished_nyxite_stairs",
            () -> new StairBlock(POLISHED_NYXITE.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> POLISHED_NYXITE_SLAB = BLOCKS.register("polished_nyxite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> POLISHED_NYXITE_WALL = BLOCKS.register("polished_nyxite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock> POLISHED_NYXITE_PRESSURE_PLATE = BLOCKS.register("polished_nyxite_pressure_plate",
            () -> new com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock(BlockSetType.STONE, BlockBehaviour.Properties.copy(Blocks.STONE_PRESSURE_PLATE)));
    public static final RegistryObject<ButtonBlock> POLISHED_NYXITE_BUTTON = BLOCKS.register("polished_nyxite_button",
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON), BlockSetType.STONE, 20, false));
    public static final RegistryObject<StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            () -> new StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<StairBlock> MOSSY_NYXITE_BRICK_STAIRS = BLOCKS.register("mossy_nyxite_brick_stairs",
            () -> new StairBlock(MOSSY_NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> MOSSY_NYXITE_BRICK_SLAB = BLOCKS.register("mossy_nyxite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> MOSSY_NYXITE_BRICK_WALL = BLOCKS.register("mossy_nyxite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<RotatedPillarBlock> NYXITE_PILLAR = BLOCKS.register("nyxite_pillar",
            () -> new RotatedPillarBlock(nyxiteProperties()));
    public static final RegistryObject<Block> SHELLSTONE = BLOCKS.register("shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<Block> POLISHED_SHELLSTONE = BLOCKS.register("polished_shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<Block> SHELLSTONE_BRICKS = BLOCKS.register("shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<Block> CHISELED_SHELLSTONE = BLOCKS.register("chiseled_shellstone",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<Block> MOSSY_SHELLSTONE_BRICKS = BLOCKS.register("mossy_shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<Block> CRACKED_SHELLSTONE_BRICKS = BLOCKS.register("cracked_shellstone_bricks",
            () -> new Block(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<StairBlock> MOSSY_SHELLSTONE_BRICK_STAIRS = BLOCKS.register("mossy_shellstone_brick_stairs",
            () -> new StairBlock(MOSSY_SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<SlabBlock> MOSSY_SHELLSTONE_BRICK_SLAB = BLOCKS.register("mossy_shellstone_brick_slab",
            () -> new SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<WallBlock> MOSSY_SHELLSTONE_BRICK_WALL = BLOCKS.register("mossy_shellstone_brick_wall",
            () -> new WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<StairBlock> SHELLSTONE_STAIRS = BLOCKS.register("shellstone_stairs",
            () -> new StairBlock(SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<SlabBlock> SHELLSTONE_SLAB = BLOCKS.register("shellstone_slab",
            () -> new SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<WallBlock> SHELLSTONE_WALL = BLOCKS.register("shellstone_wall",
            () -> new WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<StairBlock> POLISHED_SHELLSTONE_STAIRS = BLOCKS.register("polished_shellstone_stairs",
            () -> new StairBlock(POLISHED_SHELLSTONE.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<SlabBlock> POLISHED_SHELLSTONE_SLAB = BLOCKS.register("polished_shellstone_slab",
            () -> new SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<WallBlock> POLISHED_SHELLSTONE_WALL = BLOCKS.register("polished_shellstone_wall",
            () -> new WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<StairBlock> SHELLSTONE_BRICK_STAIRS = BLOCKS.register("shellstone_brick_stairs",
            () -> new StairBlock(SHELLSTONE_BRICKS.get().defaultBlockState(), AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<SlabBlock> SHELLSTONE_BRICK_SLAB = BLOCKS.register("shellstone_brick_slab",
            () -> new SlabBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<WallBlock> SHELLSTONE_BRICK_WALL = BLOCKS.register("shellstone_brick_wall",
            () -> new WallBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<RotatedPillarBlock> SHELLSTONE_PILLAR = BLOCKS.register("shellstone_pillar",
            () -> new RotatedPillarBlock(AntarchyObjects.shellstoneProperties()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.FlytrapGooBlock> FLYTRAP_GOO_BLOCK = BLOCKS.register("flytrap_goo_block",
            () -> new com.craisinlord.antarchy.content.block.FlytrapGooBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            () -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyForgeItems::cloudBucketItem,
                    BlockBehaviour.Properties.copy(Blocks.POWDER_SNOW).noLootTable().noOcclusion().emissiveRendering((state, level, pos) -> true)
            ));
    public static final RegistryObject<AntDimensionPortalBlock> ELYTHIA_PORTAL = BLOCKS.register("elythia_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.ELYTHIA, portalProperties()));
    public static final RegistryObject<AntDimensionPortalBlock> THORAXIS_PORTAL = BLOCKS.register("thoraxis_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.THORAXIS, portalProperties()));
    public static final RegistryObject<AntDimensionPortalBlock> CAVARYN_PORTAL = BLOCKS.register("cavaryn_portal",
            () -> new AntDimensionPortalBlock(PermanentPortalType.CAVARYN, portalProperties()));
    public static final RegistryObject<Block> PALE_NYXITE = BLOCKS.register("pale_nyxite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<NyxiteSpikeBlock> NYXITE_SPIKE = BLOCKS.register("nyxite_spike",
            () -> new NyxiteSpikeBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));
    public static final RegistryObject<ChitinSpikeBlock> CHITIN_SPIKE = BLOCKS.register("chitin_spike",
            () -> new ChitinSpikeBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));
    public static final RegistryObject<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            () -> new PotentNyxiteBlock(
                    AntarchyForgeItems::potentNyxiteBlockEntityType,
                    BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 3)
            ));
    public static final RegistryObject<Block> MYRMITE = BLOCKS.register("myrmite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<StairBlock> MYRMITE_STAIRS = BLOCKS.register("myrmite_stairs",
            () -> new StairBlock(MYRMITE.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> MYRMITE_SLAB = BLOCKS.register("myrmite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> MYRMITE_WALL = BLOCKS.register("myrmite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<Block> POLISHED_MYRMITE = BLOCKS.register("polished_myrmite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<StairBlock> POLISHED_MYRMITE_STAIRS = BLOCKS.register("polished_myrmite_stairs",
            () -> new StairBlock(POLISHED_MYRMITE.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> POLISHED_MYRMITE_SLAB = BLOCKS.register("polished_myrmite_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> POLISHED_MYRMITE_WALL = BLOCKS.register("polished_myrmite_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<Block> CHISELED_MYRMITE = BLOCKS.register("chiseled_myrmite",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<Block> MYRMITE_BRICKS = BLOCKS.register("myrmite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<StairBlock> MYRMITE_BRICK_STAIRS = BLOCKS.register("myrmite_brick_stairs",
            () -> new StairBlock(MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> MYRMITE_BRICK_SLAB = BLOCKS.register("myrmite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> MYRMITE_BRICK_WALL = BLOCKS.register("myrmite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<Block> MOSSY_MYRMITE_BRICKS = BLOCKS.register("mossy_myrmite_bricks",
            () -> new Block(nyxiteProperties()));
    public static final RegistryObject<StairBlock> MOSSY_MYRMITE_BRICK_STAIRS = BLOCKS.register("mossy_myrmite_brick_stairs",
            () -> new StairBlock(MOSSY_MYRMITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> MOSSY_MYRMITE_BRICK_SLAB = BLOCKS.register("mossy_myrmite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> MOSSY_MYRMITE_BRICK_WALL = BLOCKS.register("mossy_myrmite_brick_wall",
            () -> new WallBlock(nyxiteProperties()));
    public static final RegistryObject<RotatedPillarBlock> MYRMITE_PILLAR = BLOCKS.register("myrmite_pillar",
            () -> new RotatedPillarBlock(nyxiteProperties()));
    public static final RegistryObject<Block> BIOMITE = BLOCKS.register("biomite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK)));
    public static final RegistryObject<Block> BIOMITE_TURF = BLOCKS.register("biomite_turf",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 2)));
    public static final RegistryObject<BiowartBlock> BIOWART = BLOCKS.register("biowart",
            () -> new BiowartBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK).lightLevel(state -> 2)));
    public static final RegistryObject<BiowartTendrilsBlock> BIOWART_TENDRILS = BLOCKS.register("biowart_tendrils",
            () -> new BiowartTendrilsBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).lightLevel(state -> 2).noCollission().noOcclusion()));
    public static final RegistryObject<Block> MYRMITE_COAL_ORE = BLOCKS.register("myrmite_coal_ore",
            () -> createOre(Blocks.COAL_ORE, 0, 2, MapColor.COLOR_GRAY));
    public static final RegistryObject<Block> BROODSTONE = BLOCKS.register("broodstone",
            () -> new Block(broodstoneProperties()));
    public static final RegistryObject<Block> POLISHED_BROODSTONE = BLOCKS.register("polished_broodstone",
            () -> new Block(broodstoneProperties()));
    public static final RegistryObject<Block> CHISELED_BROODSTONE = BLOCKS.register("chiseled_broodstone",
            () -> new Block(broodstoneProperties()));
    public static final RegistryObject<Block> BROODSTONE_BRICKS = BLOCKS.register("broodstone_bricks",
            () -> new Block(broodstoneProperties()));
    public static final RegistryObject<Block> MOSSY_BROODSTONE_BRICKS = BLOCKS.register("mossy_broodstone_bricks",
            () -> new Block(broodstoneProperties()));
    public static final RegistryObject<StairBlock> BROODSTONE_STAIRS = BLOCKS.register("broodstone_stairs",
            () -> new StairBlock(BROODSTONE.get().defaultBlockState(), broodstoneProperties()));
    public static final RegistryObject<SlabBlock> BROODSTONE_SLAB = BLOCKS.register("broodstone_slab",
            () -> new SlabBlock(broodstoneProperties()));
    public static final RegistryObject<WallBlock> BROODSTONE_WALL = BLOCKS.register("broodstone_wall",
            () -> new WallBlock(broodstoneProperties()));
    public static final RegistryObject<StairBlock> POLISHED_BROODSTONE_STAIRS = BLOCKS.register("polished_broodstone_stairs",
            () -> new StairBlock(POLISHED_BROODSTONE.get().defaultBlockState(), broodstoneProperties()));
    public static final RegistryObject<SlabBlock> POLISHED_BROODSTONE_SLAB = BLOCKS.register("polished_broodstone_slab",
            () -> new SlabBlock(broodstoneProperties()));
    public static final RegistryObject<WallBlock> POLISHED_BROODSTONE_WALL = BLOCKS.register("polished_broodstone_wall",
            () -> new WallBlock(broodstoneProperties()));
    public static final RegistryObject<StairBlock> BROODSTONE_BRICK_STAIRS = BLOCKS.register("broodstone_brick_stairs",
            () -> new StairBlock(BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties()));
    public static final RegistryObject<SlabBlock> BROODSTONE_BRICK_SLAB = BLOCKS.register("broodstone_brick_slab",
            () -> new SlabBlock(broodstoneProperties()));
    public static final RegistryObject<WallBlock> BROODSTONE_BRICK_WALL = BLOCKS.register("broodstone_brick_wall",
            () -> new WallBlock(broodstoneProperties()));
    public static final RegistryObject<StairBlock> MOSSY_BROODSTONE_BRICK_STAIRS = BLOCKS.register("mossy_broodstone_brick_stairs",
            () -> new StairBlock(MOSSY_BROODSTONE_BRICKS.get().defaultBlockState(), broodstoneProperties()));
    public static final RegistryObject<SlabBlock> MOSSY_BROODSTONE_BRICK_SLAB = BLOCKS.register("mossy_broodstone_brick_slab",
            () -> new SlabBlock(broodstoneProperties()));
    public static final RegistryObject<WallBlock> MOSSY_BROODSTONE_BRICK_WALL = BLOCKS.register("mossy_broodstone_brick_wall",
            () -> new WallBlock(broodstoneProperties()));
    public static final RegistryObject<RotatedPillarBlock> BROODSTONE_PILLAR = BLOCKS.register("broodstone_pillar",
            () -> new RotatedPillarBlock(broodstoneProperties()));
    public static final RegistryObject<Block> BROODSTONE_URANIUM_ORE = BLOCKS.register("broodstone_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> BROODSTONE_TITANIUM_ORE = BLOCKS.register("broodstone_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<RotatedPillarBlock> CHITIN_BLOCK = BLOCKS.register("chitin_block",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BONE_BLOCK).requiresCorrectToolForDrops()));
    public static final RegistryObject<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            () -> new UmbralMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));
    public static final RegistryObject<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));
    public static final RegistryObject<AmberMossBlock> AMBER_MOSS_BLOCK = BLOCKS.register("amber_moss_block",
            () -> new AmberMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));
    public static final RegistryObject<UmbralMossCarpetBlock> AMBER_MOSS_CARPET = BLOCKS.register("amber_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));
    public static final RegistryObject<AmberLichenBlock> AMBER_LICHEN = BLOCKS.register("amber_lichen",
            () -> new AmberLichenBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).lightLevel(state -> 4)));
    public static final RegistryObject<Block> BILE_VEIN = BLOCKS.register("bile_vein",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 2)));
    public static final RegistryObject<CreepvineBlock> CREEPVINE = BLOCKS.register("creepvine",
            () -> new CreepvineBlock(BlockBehaviour.Properties.copy(Blocks.VINE)));
    public static final RegistryObject<BlushMossBlock> BLUSH_MOSS_BLOCK = BLOCKS.register("blush_moss_block",
            () -> new BlushMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));
    public static final RegistryObject<BlushMossCarpetBlock> BLUSH_MOSS_CARPET = BLOCKS.register("blush_moss_carpet",
            () -> new BlushMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));
    public static final RegistryObject<DreamTorchBlock> DREAM_TORCH = BLOCKS.register("dream_torch",
            () -> new DreamTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH)));
    public static final RegistryObject<DreamWallTorchBlock> DREAM_WALL_TORCH = BLOCKS.register("dream_wall_torch",
            () -> new DreamWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_WALL_TORCH)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock> DREAM_CEILING_TORCH = BLOCKS.register("dream_ceiling_torch",
            () -> new com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH)));
    public static final RegistryObject<LanternBlock> DREAM_LANTERN = BLOCKS.register("dream_lantern",
            () -> new LanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN)));
    public static final RegistryObject<DreamCampfireBlock> DREAM_CAMPFIRE = BLOCKS.register("dream_campfire",
            () -> new DreamCampfireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_CAMPFIRE)));
    public static final RegistryObject<DreamFireBlock> DREAM_FIRE = BLOCKS.register("dream_fire",
            () -> new DreamFireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_FIRE)));
    public static final RegistryObject<DreamCeilingFireBlock> DREAM_CEILING_FIRE = BLOCKS.register("dream_fire_ceiling",
            () -> new DreamCeilingFireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_FIRE)));
    public static final RegistryObject<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            () -> new BedBugEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));
    public static final RegistryObject<CrawlingBlightEggBlock> CRAWLING_BLIGHT_EGG = BLOCKS.register("crawling_blight_egg",
            () -> new CrawlingBlightEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.PrinceEggBlock> PRINCE_EGG = BLOCKS.register("prince_egg",
            () -> new com.craisinlord.antarchy.content.block.PrinceEggBlock(BlockBehaviour.Properties.copy(Blocks.DRAGON_EGG).randomTicks().noOcclusion()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.PrincessEggBlock> PRINCESS_EGG = BLOCKS.register("princess_egg",
            () -> new com.craisinlord.antarchy.content.block.PrincessEggBlock(BlockBehaviour.Properties.copy(Blocks.DRAGON_EGG).randomTicks().noOcclusion()));
    public static final RegistryObject<SpringbugEggBlock> SPRINGBUG_EGG = BLOCKS.register("springbug_egg",
            () -> new SpringbugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<SpitBugEggBlock> SPIT_BUG_EGG = BLOCKS.register("spit_bug_egg",
            () -> new SpitBugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<JerryEggBlock> JERRY_EGG = BLOCKS.register("jerry_egg",
            () -> new JerryEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).randomTicks()));
    public static final RegistryObject<SkulkingFrightEggBlock> SKULKING_FRIGHT_EGG = BLOCKS.register("skulking_fright_egg",
            () -> new SkulkingFrightEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));

    static {
        BiowartBlock.bindTendrils(BIOWART_TENDRILS);
    }
    public static final RegistryObject<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            () -> new WaspNestBlock(AntarchyForgeItems::waspNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.BEE_NEST)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            () -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT).noLootTable()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock> BRUTALFLY_COCOON_SPAWNER = BLOCKS.register("brutalfly_cocoon_spawner",
            () -> new com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock(BlockBehaviour.Properties.of().noCollission().instabreak().noLootTable()));
    public static final RegistryObject<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            () -> new HushweedBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA).noCollission().noOcclusion()));
    public static final RegistryObject<HangingCreeprootsBlock> HANGING_CREEPROOTS = BLOCKS.register("hanging_creeproots",
            () -> new HangingCreeprootsBlock(BlockBehaviour.Properties.copy(Blocks.HANGING_ROOTS)));
    public static final RegistryObject<MoltingVinesBlock> MOLTING_VINES = BLOCKS.register("molting_vines",
            () -> new MoltingVinesBlock(BlockBehaviour.Properties.copy(Blocks.WEEPING_VINES)));
    public static final RegistryObject<LiquidBlock> BILE_BLOCK = BLOCKS.register("bile",
            () -> new BileLiquidBlock((FlowingFluid) AntarchyForgeMisc.BILE.get(), BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel(state -> 5).noLootTable()));
    public static final RegistryObject<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            () -> new LiquidBlock((FlowingFluid) AntarchyForgeMisc.ICHOR.get(), BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));
    public static final RegistryObject<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            () -> new com.craisinlord.antarchy.content.fluid.AntiwaterLiquidBlock((FlowingFluid) AntarchyForgeMisc.ANTIWATER.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));
    public static final RegistryObject<LiquidBlock> LUMEN_BLOCK = BLOCKS.register("lumen",
            () -> new LumenLiquidBlock((FlowingFluid) AntarchyForgeMisc.LUMEN.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel(state -> 9).noLootTable()));
    public static final RegistryObject<Block> LUMEN_FROGLIGHT = BLOCKS.register("lumen_froglight",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OCHRE_FROGLIGHT)));
    public static final RegistryObject<Block> ROSEATE_FROGLIGHT = BLOCKS.register("roseate_froglight",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OCHRE_FROGLIGHT)));
    public static final RegistryObject<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            () -> new AntNestBlock(
                    AntarchyForgeEntites.RED_ANT,
                    AntarchyForgeItems::antNestBlockEntityType,
                    true,
                    BlockBehaviour.Properties.copy(Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));
    public static final RegistryObject<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            () -> new AntNestBlock(AntarchyForgeEntites.BROWN_ANT, AntarchyForgeItems::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));
    public static final RegistryObject<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            () -> new AntNestBlock(AntarchyForgeEntites.RAINBOW_ANT, AntarchyForgeItems::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));
    public static final RegistryObject<AntNestBlock> TERMITE_NEST = BLOCKS.register("termite_nest",
            () -> new AntNestBlock(AntarchyForgeEntites.TERMITE, AntarchyForgeItems::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));

    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntTrapBlock> ANT_TRAP = BLOCKS.register("ant_trap",
            () -> new com.craisinlord.antarchy.content.block.AntTrapBlock(
                    () -> java.util.List.of(AntarchyForgeEntites.RED_ANT.get(), AntarchyForgeEntites.BROWN_ANT.get(), AntarchyForgeEntites.RAINBOW_ANT.get(), AntarchyForgeEntites.TERMITE.get()),
                    AntarchyForgeItems::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE).noOcclusion()));
    public static final RegistryObject<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> createOre(Blocks.EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> TITANIUM_ORE = BLOCKS.register("titanium_ore",
            () -> createOre(Blocks.DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.register("deepslate_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<BluestoneOreBlock> BLUESTONE_ORE = BLOCKS.register("bluestone_ore",
            () -> new BluestoneOreBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_ORE).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistryObject<BluestoneWireBlock> BLUESTONE_WIRE = BLOCKS.register("bluestone_wire",
            () -> new BluestoneWireBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).mapColor(MapColor.COLOR_BLUE).noCollission().instabreak()));
    public static final RegistryObject<BluestoneBlock> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block",
            () -> new BluestoneBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistryObject<VortexLensBlock> VORTEX_LENS = BLOCKS.register("vortex_lens",
            () -> new VortexLensBlock(AntarchyForgeBlocks::vortexLensBlockEntityType,
                    BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GREEN).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<BluestoneRepeaterBlock> BLUESTONE_REPEATER = BLOCKS.register("bluestone_repeater",
            () -> new BluestoneRepeaterBlock(BlockBehaviour.Properties.copy(Blocks.REPEATER).mapColor(MapColor.COLOR_BLUE).noCollission()));
    public static final RegistryObject<BluestoneComparatorBlock> BLUESTONE_COMPARATOR = BLOCKS.register("bluestone_comparator",
            () -> new BluestoneComparatorBlock(BlockBehaviour.Properties.copy(Blocks.COMPARATOR).mapColor(MapColor.COLOR_BLUE).noCollission(), AntarchyForgeBlocks::bluestoneComparatorBlockEntityType));
    public static final RegistryObject<BluestoneTorchBlock> BLUESTONE_TORCH = BLOCKS.register("bluestone_torch",
            () -> new BluestoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH).mapColor(MapColor.COLOR_BLUE).lightLevel(state -> state.getValue(BluestoneTorchBlock.LIT) ? 7 : 0).noCollission().instabreak()));
    public static final RegistryObject<BluestoneLampBlock> BLUESTONE_LAMP = BLOCKS.register("bluestone_lamp",
            () -> new BluestoneLampBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistryObject<Block> TIGERS_EYE_BLOCK = BLOCKS.register("tigers_eye_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK, MapColor.COLOR_ORANGE));
    public static final RegistryObject<Block> URANIUM_BLOCK = BLOCKS.register("uranium_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> createStorageBlock(Blocks.DIAMOND_BLOCK, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<Block> RAW_URANIUM_BLOCK = BLOCKS.register("raw_uranium_block",
            () -> createRawStorageBlock(Blocks.EMERALD_ORE, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> RAW_TITANIUM_BLOCK = BLOCKS.register("raw_titanium_block",
            () -> createRawStorageBlock(Blocks.DIAMOND_ORE, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<Block> CUT_URANIUM = BLOCKS.register("cut_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> CUT_TITANIUM = BLOCKS.register("cut_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<SlabBlock> CUT_URANIUM_SLAB = BLOCKS.register("cut_uranium_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<SlabBlock> CUT_TITANIUM_SLAB = BLOCKS.register("cut_titanium_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<StairBlock> CUT_URANIUM_STAIRS = BLOCKS.register("cut_uranium_stairs",
            () -> new StairBlock(CUT_URANIUM.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<StairBlock> CUT_TITANIUM_STAIRS = BLOCKS.register("cut_titanium_stairs",
            () -> new StairBlock(CUT_TITANIUM.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<Block> CHISELED_URANIUM = BLOCKS.register("chiseled_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.COPPER_BLOCK, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.COPPER_BLOCK, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).isRedstoneConductor((state, level, pos) -> false).mapColor(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).isRedstoneConductor((state, level, pos) -> false).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<DoorBlock> URANIUM_DOOR = BLOCKS.register("uranium_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_YELLOW), BlockSetType.IRON));
    public static final RegistryObject<DoorBlock> TITANIUM_DOOR = BLOCKS.register("titanium_door",
            () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_LIGHT_BLUE), BlockSetType.IRON));
    public static final RegistryObject<TrapDoorBlock> URANIUM_TRAPDOOR = BLOCKS.register("uranium_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_YELLOW), BlockSetType.IRON));
    public static final RegistryObject<TrapDoorBlock> TITANIUM_TRAPDOOR = BLOCKS.register("titanium_trapdoor",
            () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_LIGHT_BLUE), BlockSetType.IRON));
    public static final RegistryObject<IronBarsBlock> URANIUM_BARS = BLOCKS.register("uranium_bars",
            () -> new IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).mapColor(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<IronBarsBlock> TITANIUM_BARS = BLOCKS.register("titanium_bars",
            () -> new IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<RotatedPillarBlock> ANTIMETAL = BLOCKS.register("antimetal",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<StairBlock> ANTIMETAL_STAIRS = BLOCKS.register("antimetal_stairs",
            () -> new StairBlock(ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<SlabBlock> ANTIMETAL_SLAB = BLOCKS.register("antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<StairBlock> POLISHED_ANTIMETAL_STAIRS = BLOCKS.register("polished_antimetal_stairs",
            () -> new StairBlock(POLISHED_ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<SlabBlock> POLISHED_ANTIMETAL_SLAB = BLOCKS.register("polished_antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT).sound(antimetalSoundType())));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            () -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING).sound(antimetalScaffoldingSoundType())));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalRailBlock> ANTIMETAL_RAIL = BLOCKS.register("antimetal_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalRailBlock(BlockBehaviour.Properties.copy(Blocks.RAIL)));
    public static final RegistryObject<UpperBlock> UPPER = BLOCKS.register("upper",
            () -> new UpperBlock(AntarchyForgeBlocks::upperBlockEntityType, BlockBehaviour.Properties.copy(Blocks.HOPPER)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock> ANTIMETAL_POWERED_RAIL = BLOCKS.register("antimetal_powered_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock> ANTIMETAL_DETECTOR_RAIL = BLOCKS.register("antimetal_detector_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock(BlockBehaviour.Properties.copy(Blocks.DETECTOR_RAIL)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock> ANTIMETAL_ACTIVATOR_RAIL = BLOCKS.register("antimetal_activator_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock(BlockBehaviour.Properties.copy(Blocks.ACTIVATOR_RAIL)));
    public static final RegistryObject<CorneaStalkBlock> CORNEA_STALK = BLOCKS.register("cornea_stalk",
            () -> new CorneaStalkBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH).randomTicks()));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.GlowcapMushroomBlock> GLOWCAP_MUSHROOM = BLOCKS.register("glowcap_mushroom",
            () -> new com.craisinlord.antarchy.content.block.GlowcapMushroomBlock(BlockBehaviour.Properties.copy(Blocks.RED_MUSHROOM).lightLevel(state -> 12).randomTicks()));
    public static final RegistryObject<FlowerPotBlock> POTTED_GLOWCAP_MUSHROOM = BLOCKS.register("potted_glowcap_mushroom",
            () -> new FlowerPotBlock(GLOWCAP_MUSHROOM.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).lightLevel(state -> 12)));
    public static final RegistryObject<net.minecraft.world.level.block.HugeMushroomBlock> GLOWCAP_MUSHROOM_BLOCK = BLOCKS.register("glowcap_mushroom_block",
            () -> new net.minecraft.world.level.block.HugeMushroomBlock(BlockBehaviour.Properties.copy(Blocks.RED_MUSHROOM_BLOCK).lightLevel(state -> 13)));
    public static final RegistryObject<CornCropBlock> CORN_CROP = BLOCKS.register("corn_crop",
            () -> new CornCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).randomTicks().noCollission().noOcclusion()));
    public static final RegistryObject<WildCornBlock> WILD_CORN = BLOCKS.register("wild_corn",
            () -> new WildCornBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noCollission().noOcclusion()));
    public static final RegistryObject<Block> FALLEN_KING_CROWN = BLOCKS.register("fallen_king_crown",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<AmethystClusterBlock> SMALL_BLOOD_CRYSTAL_BUD = BLOCKS.register("small_blood_crystal_bud",
            () -> new AmethystClusterBlock(3, 4, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD)));
    public static final RegistryObject<AmethystClusterBlock> MEDIUM_BLOOD_CRYSTAL_BUD = BLOCKS.register("medium_blood_crystal_bud",
            () -> new AmethystClusterBlock(4, 3, BlockBehaviour.Properties.copy(Blocks.MEDIUM_AMETHYST_BUD)));
    public static final RegistryObject<AmethystClusterBlock> LARGE_BLOOD_CRYSTAL_BUD = BLOCKS.register("large_blood_crystal_bud",
            () -> new AmethystClusterBlock(5, 3, BlockBehaviour.Properties.copy(Blocks.LARGE_AMETHYST_BUD)));
    public static final RegistryObject<Block> BUDDING_BLOOD_CRYSTAL = BLOCKS.register("budding_blood_crystal",
            () -> new BuddingBloodCrystalBlock(
                    BlockBehaviour.Properties.copy(Blocks.BUDDING_AMETHYST),
                    AntarchyForgeBlocks::smallBloodCrystalBudBlock,
                    AntarchyForgeBlocks::mediumBloodCrystalBudBlock,
                    AntarchyForgeBlocks::largeBloodCrystalBudBlock,
                    AntarchyForgeBlocks::bloodCrystalCrystalBlock
            ));
    public static final RegistryObject<Block> BLOOD_CRYSTAL = BLOCKS.register("blood_crystal_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));
    public static final RegistryObject<AmethystClusterBlock> BLOOD_CRYSTAL_CRYSTAL = BLOCKS.register("blood_crystal_cluster",
            () -> new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.copy(Blocks.AMETHYST_CLUSTER)));
    public static final RegistryObject<Block> DREAM_SAND = BLOCKS.register("dream_sand",
            () -> new DreamSandBlock(BlockBehaviour.Properties.copy(Blocks.SAND)));
    public static final RegistryObject<AntigravelBlock> ANTIGRAVEL = BLOCKS.register("antigravel",
            () -> new AntigravelBlock(BlockBehaviour.Properties.copy(Blocks.GRAVEL)));
    public static final RegistryObject<Block> LOAM = BLOCKS.register("loam",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD)));
    public static final RegistryObject<Block> PRIMORDIAL_LOAM = BLOCKS.register("primordial_loam",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD)));
    public static final RegistryObject<Block> PACKED_LOAM = BLOCKS.register("packed_loam",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.PACKED_MUD)));
    public static final RegistryObject<Block> LOAM_BRICKS = BLOCKS.register("loam_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD_BRICKS)));
    public static final RegistryObject<StairBlock> LOAM_BRICK_STAIRS = BLOCKS.register("loam_brick_stairs",
            () -> new StairBlock(LOAM_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.MUD_BRICK_STAIRS)));
    public static final RegistryObject<SlabBlock> LOAM_BRICK_SLAB = BLOCKS.register("loam_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.MUD_BRICK_SLAB)));
    public static final RegistryObject<WallBlock> LOAM_BRICK_WALL = BLOCKS.register("loam_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.MUD_BRICK_WALL)));
    public static final RegistryObject<Block> CHISELED_LOAM_BRICKS = BLOCKS.register("chiseled_loam_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD_BRICKS)));
    public static final RegistryObject<MucusBlock> MUCUS = BLOCKS.register("mucus",
            () -> new MucusBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).friction(0.995F).lightLevel(state -> 0).sound(SoundType.SLIME_BLOCK)));
    public static final RegistryObject<DreamSandstoneBlock> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            () -> new DreamSandstoneBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE)));
    public static final RegistryObject<Block> CHISELED_DREAM_SANDSTONE = BLOCKS.register("chiseled_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CHISELED_SANDSTONE)));
    public static final RegistryObject<Block> CUT_DREAM_SANDSTONE = BLOCKS.register("cut_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE)));
    public static final RegistryObject<Block> SMOOTH_DREAM_SANDSTONE = BLOCKS.register("smooth_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE)));
    public static final RegistryObject<StairBlock> DREAM_SANDSTONE_STAIRS = BLOCKS.register("dream_sandstone_stairs",
            () -> new StairBlock(DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SANDSTONE_STAIRS)));
    public static final RegistryObject<SlabBlock> DREAM_SANDSTONE_SLAB = BLOCKS.register("dream_sandstone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE_SLAB)));
    public static final RegistryObject<WallBlock> DREAM_SANDSTONE_WALL = BLOCKS.register("dream_sandstone_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE_WALL)));
    public static final RegistryObject<StairBlock> SMOOTH_DREAM_SANDSTONE_STAIRS = BLOCKS.register("smooth_dream_sandstone_stairs",
            () -> new StairBlock(SMOOTH_DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE_STAIRS)));
    public static final RegistryObject<SlabBlock> SMOOTH_DREAM_SANDSTONE_SLAB = BLOCKS.register("smooth_dream_sandstone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE_SLAB)));
    public static final RegistryObject<SlabBlock> CUT_DREAM_SANDSTONE_SLAB = BLOCKS.register("cut_dream_sandstone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE_SLAB)));
    public static final RegistryObject<Block> TYPHONITE = BLOCKS.register("typhonite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)));
    public static final RegistryObject<DirectionalNyxiumBlock> NADIR_NYXIUM = BLOCKS.register("nadir_nyxium",
            () -> new DirectionalNyxiumBlock(nyxiteProperties(), DirectionalNyxiumBlock.Type.NADIR));
    public static final RegistryObject<DirectionalNyxiumBlock> VERDANT_NYXIUM = BLOCKS.register("verdant_nyxium",
            () -> new DirectionalNyxiumBlock(nyxiteProperties(), DirectionalNyxiumBlock.Type.VERDANT));
    public static final RegistryObject<RotatedPillarBlock> TYPHONITE_PILLAR = BLOCKS.register("typhonite_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)));
    public static final RegistryObject<Block> TYPHONITE_BRICKS = BLOCKS.register("typhonite_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)));
    public static final RegistryObject<Block> CHISELED_TYPHONITE = BLOCKS.register("chiseled_typhonite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)));
    public static final RegistryObject<Block> POLISHED_TYPHONITE = BLOCKS.register("polished_typhonite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)));
    public static final RegistryObject<Block> VEINED_TYPHONITE = BLOCKS.register("veined_typhonite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE).lightLevel(state -> 8)));
    public static final RegistryObject<TyphoniteSpikeBlock> TYPHONITE_SPIKE = BLOCKS.register("typhonite_spike",
            () -> new TyphoniteSpikeBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));
    public static final RegistryObject<StairBlock> TYPHONITE_STAIRS = BLOCKS.register("typhonite_stairs",
            () -> new StairBlock(TYPHONITE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_STAIRS)));
    public static final RegistryObject<SlabBlock> TYPHONITE_SLAB = BLOCKS.register("typhonite_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_SLAB)));
    public static final RegistryObject<WallBlock> TYPHONITE_WALL = BLOCKS.register("typhonite_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_WALL)));
    public static final RegistryObject<StairBlock> TYPHONITE_BRICK_STAIRS = BLOCKS.register("typhonite_brick_stairs",
            () -> new StairBlock(TYPHONITE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_STAIRS)));
    public static final RegistryObject<SlabBlock> TYPHONITE_BRICK_SLAB = BLOCKS.register("typhonite_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_SLAB)));
    public static final RegistryObject<WallBlock> TYPHONITE_BRICK_WALL = BLOCKS.register("typhonite_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_WALL)));
    public static final RegistryObject<StairBlock> POLISHED_TYPHONITE_STAIRS = BLOCKS.register("polished_typhonite_stairs",
            () -> new StairBlock(POLISHED_TYPHONITE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_STAIRS)));
    public static final RegistryObject<SlabBlock> POLISHED_TYPHONITE_SLAB = BLOCKS.register("polished_typhonite_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_SLAB)));
    public static final RegistryObject<WallBlock> POLISHED_TYPHONITE_WALL = BLOCKS.register("polished_typhonite_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE_WALL)));
    public static final RegistryObject<SpiralingVinesBlock> SPIRALING_VINES = BLOCKS.register("spiraling_vines",
            () -> new SpiralingVinesBlock(BlockBehaviour.Properties.copy(Blocks.TWISTING_VINES)));
    public static final RegistryObject<GorevineBlock> GOREVINE = BLOCKS.register("gorevine",
            () -> new GorevineBlock(BlockBehaviour.Properties.copy(Blocks.WEEPING_VINES).lightLevel(state -> 4)));
    public static final RegistryObject<WhirlflowerBlock> WHIRLFLOWER = BLOCKS.register("whirlflower",
            () -> new WhirlflowerBlock(BlockBehaviour.Properties.copy(Blocks.ALLIUM)));
    public static final RegistryObject<NadirFernBlock> NADIR_FERN = BLOCKS.register("nadir_fern",
            () -> new NadirFernBlock(BlockBehaviour.Properties.copy(Blocks.FERN)));
    public static final RegistryObject<LargeNadirFernBlock> LARGE_NADIR_FERN = BLOCKS.register("large_nadir_fern",
            () -> new LargeNadirFernBlock(BlockBehaviour.Properties.copy(Blocks.LARGE_FERN).noCollission()));
    public static final RegistryObject<DirectionalThoraxisFlowerBlock> DUSKBELL = BLOCKS.register("duskbell",
            () -> new DirectionalThoraxisFlowerBlock(BlockBehaviour.Properties.copy(Blocks.ALLIUM).lightLevel(state -> 4)));
    public static final RegistryObject<DirectionalThoraxisFlowerBlock> NADIR_ROOTS = BLOCKS.register("nadir_roots",
            () -> new DirectionalThoraxisFlowerBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_ROOTS)));
    public static final RegistryObject<DirectionalThoraxisFlowerBlock> VERDANT_ROOTS = BLOCKS.register("verdant_roots",
            () -> new DirectionalThoraxisFlowerBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_ROOTS)));
    public static final RegistryObject<Block> DEAD_STAR_CORAL_BLOCK = BLOCKS.register("dead_star_coral_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_BLOCK)));
    public static final RegistryObject<net.minecraft.world.level.block.BaseCoralPlantBlock> DEAD_STAR_CORAL = BLOCKS.register("dead_star_coral",
            () -> new net.minecraft.world.level.block.BaseCoralPlantBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL)));
    public static final RegistryObject<net.minecraft.world.level.block.BaseCoralFanBlock> DEAD_STAR_CORAL_FAN = BLOCKS.register("dead_star_coral_fan",
            () -> new net.minecraft.world.level.block.BaseCoralFanBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_FAN)));
    public static final RegistryObject<net.minecraft.world.level.block.BaseCoralWallFanBlock> DEAD_STAR_CORAL_WALL_FAN = BLOCKS.register("dead_star_coral_wall_fan",
            () -> new net.minecraft.world.level.block.BaseCoralWallFanBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_WALL_FAN)));
    public static final RegistryObject<Block> STAR_CORAL_BLOCK = BLOCKS.register("star_coral_block",
            () -> new com.craisinlord.antarchy.content.block.StarCoralBlock(DEAD_STAR_CORAL_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_BLOCK).lightLevel(state -> 8)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.StarCoralPlantBlock> STAR_CORAL = BLOCKS.register("star_coral",
            () -> new com.craisinlord.antarchy.content.block.StarCoralPlantBlock(DEAD_STAR_CORAL.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL).lightLevel(state -> 8)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.StarCoralFanBlock> STAR_CORAL_FAN = BLOCKS.register("star_coral_fan",
            () -> new com.craisinlord.antarchy.content.block.StarCoralFanBlock(DEAD_STAR_CORAL_FAN.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_FAN).lightLevel(state -> 8)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.StarCoralWallFanBlock> STAR_CORAL_WALL_FAN = BLOCKS.register("star_coral_wall_fan",
            () -> new com.craisinlord.antarchy.content.block.StarCoralWallFanBlock(DEAD_STAR_CORAL_WALL_FAN.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_WALL_FAN).lightLevel(state -> 8)));

    public static final RegistryObject<BlockEntityType<com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity>> ROYAL_EGG_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("royal_egg",
            () -> BlockEntityType.Builder.of(
                    com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity::new,
                    PRINCE_EGG.get(),
                    PRINCESS_EGG.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> BlockEntityType.Builder.of(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get(),
                    TERMITE_NEST.get(),
                    ANT_TRAP.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<DreamCampfireBlockEntity>> DREAM_CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dream_campfire",
            () -> BlockEntityType.Builder.of(
                    DreamCampfireBlockEntity::new,
                    DREAM_CAMPFIRE.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<WaspNestBlockEntity>> WASP_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("wasp_nest",
            () -> BlockEntityType.Builder.of(
                    WaspNestBlockEntity::new,
                    WASP_NEST.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<HushweedBlockEntity>> HUSHWEED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("hushweed",
            () -> BlockEntityType.Builder.of(
                    HushweedBlockEntity::new,
                    HUSHWEED.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<PotentNyxiteBlockEntity>> POTENT_NYXITE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("potent_nyxite",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new PotentNyxiteBlockEntity(pos, state, AntarchyForgeItems::potentNyxiteBlockEntityType),
                    POTENT_NYXITE.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<SeashellBlockEntity>> SEASHELL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("seashell",
            () -> BlockEntityType.Builder.of(
                    SeashellBlockEntity::new,
                    SEASHELL.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lucid_anchor",
            () -> BlockEntityType.Builder.of(
                    LucidAnchorBlockEntity::new,
                    LUCID_ANCHOR.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("critter_cage_block",
            () -> BlockEntityType.Builder.of(
                    CritterCageBlockEntity::new,
                    CRITTER_CAGE_BLOCK.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity>> BLUESTONE_COMPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("bluestone_comparator",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity(pos, state, AntarchyForgeBlocks::bluestoneComparatorBlockEntityType),
                    BLUESTONE_COMPARATOR.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<UpperBlockEntity>> UPPER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("upper",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new UpperBlockEntity(pos, state, AntarchyForgeBlocks::upperBlockEntityType),
                    UPPER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<VortexLensBlockEntity>> VORTEX_LENS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vortex_lens",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new VortexLensBlockEntity(pos, state, AntarchyForgeBlocks::vortexLensBlockEntityType),
                    VORTEX_LENS.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<PortalGunPortalMasterBlockEntity>> PORTAL_GUN_PORTAL_MASTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_master",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new PortalGunPortalMasterBlockEntity(pos, state, AntarchyForgeBlocks::portalGunPortalMasterBlockEntityType),
                    PORTAL_GUN_PORTAL_MASTER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<PortalGunPortalBaseBlockEntity>> PORTAL_GUN_PORTAL_BASE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("portal_gun_portal_base",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new PortalGunPortalBaseBlockEntity(pos, state, AntarchyForgeBlocks::portalGunPortalBaseBlockEntityType),
                    PORTAL_GUN_PORTAL_BASE.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<DimensionalTearMarkerBlockEntity>> DIMENSIONAL_TEAR_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dimensional_tear_marker",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new DimensionalTearMarkerBlockEntity(pos, state, AntarchyForgeBlocks::dimensionalTearMarkerBlockEntityType),
                    DIMENSIONAL_TEAR_MARKER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> QUEEN_TRAIL_SPAWN_MARKER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("queen_trail_spawn_marker",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new QueenTrailSpawnMarkerBlockEntity(pos, state, AntarchyForgeBlocks::queenTrailSpawnMarkerBlockEntityType),
                    QUEEN_TRAIL_SPAWN_MARKER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<UndertrialSpawnerBlockEntity>> UNDERTRIAL_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undertrial_spawner",
            () -> BlockEntityType.Builder.of((pos, state) -> new UndertrialSpawnerBlockEntity(pos, state, AntarchyForgeBlocks::undertrialSpawnerBlockEntityType), UNDERTRIAL_SPAWNER.get()).build(null));
    public static final RegistryObject<BlockEntityType<UnderVaultBlockEntity>> UNDERVAULT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("undervault",
            () -> BlockEntityType.Builder.of((pos, state) -> new UnderVaultBlockEntity(pos, state, AntarchyForgeBlocks::underVaultBlockEntityType), UNDERVAULT.get()).build(null));

    private AntarchyForgeBlocks() {}

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

    static BlockBehaviour.Properties nyxiteProperties() {
        return BlockBehaviour.Properties.copy(Blocks.NETHERRACK);
    }

    static BlockBehaviour.Properties broodstoneProperties() {
        return BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).requiresCorrectToolForDrops();
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

    private static Block createOre(Block copyFrom, int minExperience, int maxExperience, MapColor mapColor) {
        return new DropExperienceBlock(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops(), UniformInt.of(minExperience, maxExperience));
    }

    static Block createStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    static Block createHorizontalFacingStorageBlock(Block copyFrom, MapColor mapColor) {
        return new SimpleHorizontalFacingBlock(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    static Block createRawStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }

    private static SoundType antimetalSoundType() {
        return new SoundType(1.0F, 1.0F,
                AntarchyForgeSounds.ANTIMETAL_PLACE.get(),
                AntarchyForgeSounds.ANTIMETAL_STEP.get(),
                AntarchyForgeSounds.ANTIMETAL_PLACE.get(),
                AntarchyForgeSounds.ANTIMETAL_STEP.get(),
                AntarchyForgeSounds.ANTIMETAL_PLACE.get());
    }

    private static SoundType antimetalScaffoldingSoundType() {
        SoundEvent scaffold = AntarchyForgeSounds.ANTIMETAL_SCAFFOLD.get();
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
        public BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }
    }
}
