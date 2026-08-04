package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.fabric.AntarchyWoodTypes;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.*;
import com.craisinlord.antarchy.content.block.entity.AntNestBlockEntity;
import com.craisinlord.antarchy.content.block.entity.CritterCageBlockEntity;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.craisinlord.antarchy.content.block.entity.HushweedBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.craisinlord.antarchy.content.block.entity.UpperBlockEntity;
import com.craisinlord.antarchy.content.block.entity.WaspNestBlockEntity;
import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.LumenLiquidBlock;
import com.craisinlord.antarchy.fabric.content.fluid.AntiwaterLiquidBlock;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public final class AntarchyFabricBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);


    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);


    public static final DeferredBlock<DuplicatorLogBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new DuplicatorLogBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).randomTicks()));


    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_LOG = BLOCKS.register("ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));


    public static final DeferredBlock<OuranwoodLogBlock> OURANWOOD_WOOD = BLOCKS.register("ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));


    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_LOG = BLOCKS.register("mossy_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));


    public static final DeferredBlock<OuranwoodLogBlock> MOSSY_OURANWOOD_WOOD = BLOCKS.register("mossy_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));


    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_LOG = BLOCKS.register("stripped_ouranwood_log",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));


    public static final DeferredBlock<OuranwoodLogBlock> STRIPPED_OURANWOOD_WOOD = BLOCKS.register("stripped_ouranwood_wood",
            () -> new OuranwoodLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));


    public static final DeferredBlock<Block> OURANWOOD_PLANKS = BLOCKS.register("ouranwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> OURANWOOD_STAIRS = BLOCKS.register("ouranwood_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(OURANWOOD_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> OURANWOOD_SLAB = BLOCKS.register("ouranwood_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> OURANWOOD_FENCE = BLOCKS.register("ouranwood_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> OURANWOOD_FENCE_GATE = BLOCKS.register("ouranwood_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), net.minecraft.world.level.block.state.properties.WoodType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> OURANWOOD_DOOR = BLOCKS.register("ouranwood_door",
            () -> new net.minecraft.world.level.block.DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> OURANWOOD_TRAPDOOR = BLOCKS.register("ouranwood_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> OURANWOOD_PRESSURE_PLATE = BLOCKS.register("ouranwood_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> OURANWOOD_BUTTON = BLOCKS.register("ouranwood_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, false));


    public static final DeferredBlock<OuranwoodLeavesBlock> OURANWOOD_LEAVES = BLOCKS.register("ouranwood_leaves",
            () -> new OuranwoodLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks()));


    public static final DeferredBlock<OuranwoodAcornBlock> OURANWOOD_ACORN_BLOCK = BLOCKS.register("ouranwood_acorn",
            () -> new OuranwoodAcornBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_OURANWOOD_ACORN = BLOCKS.register("potted_ouranwood_acorn",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(OURANWOOD_ACORN_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<MilkweedBlock> ORANGE_MILKWEED = BLOCKS.register("orange_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));


    public static final DeferredBlock<MilkweedBlock> PINK_MILKWEED = BLOCKS.register("pink_milkweed",
            () -> new MilkweedBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CamelliaBlock> CAMELLIA = BLOCKS.register("camellia",
            () -> new com.craisinlord.antarchy.content.block.CamelliaBlock(BlockBehaviour.Properties.copy(Blocks.PEONY)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.SpiderLilyBlock> SPIDER_LILY = BLOCKS.register("spider_lily",
            () -> new com.craisinlord.antarchy.content.block.SpiderLilyBlock(BlockBehaviour.Properties.copy(Blocks.POPPY)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_SPIDER_LILY = BLOCKS.register("potted_spider_lily",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(SPIDER_LILY.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<GiantLilyPadBlock> GIANT_LILY_PAD = BLOCKS.register("giant_lily_pad",
            () -> new GiantLilyPadBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.LotusBlock> LOTUS = BLOCKS.register("lotus",
            () -> new com.craisinlord.antarchy.content.block.LotusBlock(BlockBehaviour.Properties.copy(Blocks.SPORE_BLOSSOM)));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_LOTUS = BLOCKS.register("potted_lotus",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(LOTUS.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));
    public static final DeferredBlock<SeashellBlock> SEASHELL = BLOCKS.register("seashell",
            () -> new SeashellBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).noOcclusion()));
    public static final DeferredBlock<LucidAnchorBlock> LUCID_ANCHOR = BLOCKS.register("lucid_anchor",
            () -> new LucidAnchorBlock(BlockBehaviour.Properties.copy(Blocks.BEACON).strength(3.0F)));
    public static final DeferredBlock<CritterCageBlock> CRITTER_CAGE_BLOCK = BLOCKS.register("critter_cage_block",
            () -> new CritterCageBlock(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));


    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> OURANWOOD_SIGN = BLOCKS.register("ouranwood_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.OURANWOOD));


    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> OURANWOOD_WALL_SIGN = BLOCKS.register("ouranwood_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.OURANWOOD));


    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> OURANWOOD_HANGING_SIGN = BLOCKS.register("ouranwood_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.OURANWOOD));


    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> OURANWOOD_WALL_HANGING_SIGN = BLOCKS.register("ouranwood_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.OURANWOOD));


    public static final DeferredBlock<RotatedPillarBlock> PEACH_LOG = BLOCKS.register("peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG)));


    public static final DeferredBlock<RotatedPillarBlock> PEACH_WOOD = BLOCKS.register("peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WOOD)));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_LOG = BLOCKS.register("stripped_peach_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG)));


    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PEACH_WOOD = BLOCKS.register("stripped_peach_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_WOOD)));


    public static final DeferredBlock<Block> PEACH_PLANKS = BLOCKS.register("peach_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.JUNGLE_PLANKS)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> PEACH_STAIRS = BLOCKS.register("peach_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(PEACH_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.JUNGLE_STAIRS)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> PEACH_SLAB = BLOCKS.register("peach_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SLAB)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> PEACH_FENCE = BLOCKS.register("peach_fence",
            () -> new net.minecraft.world.level.block.FenceBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE)));


    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> PEACH_FENCE_GATE = BLOCKS.register("peach_fence_gate",
            () -> new net.minecraft.world.level.block.FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_FENCE_GATE), net.minecraft.world.level.block.state.properties.WoodType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> PEACH_DOOR = BLOCKS.register("peach_door",
            () -> new net.minecraft.world.level.block.DoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_DOOR), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> PEACH_TRAPDOOR = BLOCKS.register("peach_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_TRAPDOOR), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> PEACH_PRESSURE_PLATE = BLOCKS.register("peach_pressure_plate",
            () -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.JUNGLE_PRESSURE_PLATE), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE));


    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> PEACH_BUTTON = BLOCKS.register("peach_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), net.minecraft.world.level.block.state.properties.BlockSetType.JUNGLE, 30, false));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachLeavesBlock> PEACH_LEAVES = BLOCKS.register("peach_leaves",
            () -> new com.craisinlord.antarchy.content.block.PeachLeavesBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LEAVES).randomTicks()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.HangingPeachBlock> PEACH_HANGING_PEACH = BLOCKS.register("hanging_peach",
            () -> new com.craisinlord.antarchy.content.block.HangingPeachBlock(BlockBehaviour.Properties.of().noOcclusion().instabreak()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PeachSaplingBlock> PEACH_SAPLING = BLOCKS.register("peach_sapling",
            () -> new com.craisinlord.antarchy.content.block.PeachSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_PEACH_SAPLING = BLOCKS.register("potted_peach_sapling",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(PEACH_SAPLING.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION)));


    public static final DeferredBlock<net.minecraft.world.level.block.StandingSignBlock> PEACH_SIGN = BLOCKS.register("peach_sign",
            () -> new net.minecraft.world.level.block.StandingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_SIGN), AntarchyWoodTypes.PEACH));


    public static final DeferredBlock<net.minecraft.world.level.block.WallSignBlock> PEACH_WALL_SIGN = BLOCKS.register("peach_wall_sign",
            () -> new net.minecraft.world.level.block.WallSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_SIGN), AntarchyWoodTypes.PEACH));


    public static final DeferredBlock<net.minecraft.world.level.block.CeilingHangingSignBlock> PEACH_HANGING_SIGN = BLOCKS.register("peach_hanging_sign",
            () -> new net.minecraft.world.level.block.CeilingHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_HANGING_SIGN), AntarchyWoodTypes.PEACH));


    public static final DeferredBlock<net.minecraft.world.level.block.WallHangingSignBlock> PEACH_WALL_HANGING_SIGN = BLOCKS.register("peach_wall_hanging_sign",
            () -> new net.minecraft.world.level.block.WallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_WALL_HANGING_SIGN), AntarchyWoodTypes.PEACH));


    public static final DeferredBlock<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            () -> new DuplicatorSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));


    public static final DeferredBlock<DuctTapeBlock> DUCT_TAPE = BLOCKS.register("duct_tape",
            () -> new DuctTapeBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()));


    public static final DeferredBlock<InfestedRootedDirtBlock> INFESTED_ROOTED_DIRT = BLOCKS.register("infested_rooted_dirt",
            () -> new InfestedRootedDirtBlock(BlockBehaviour.Properties.copy(Blocks.ROOTED_DIRT).randomTicks()));


    public static final DeferredBlock<InfestedCoarseDirtBlock> INFESTED_COARSE_DIRT = BLOCKS.register("infested_coarse_dirt",
            () -> new InfestedCoarseDirtBlock(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT).randomTicks()));


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

    public static final DeferredBlock<com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock> POLISHED_NYXITE_PRESSURE_PLATE = BLOCKS.register("polished_nyxite_pressure_plate",
            () -> new com.craisinlord.antarchy.content.block.PolishedNyxitePressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.STONE, BlockBehaviour.Properties.copy(Blocks.STONE_PRESSURE_PLATE)));
    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> POLISHED_NYXITE_BUTTON = BLOCKS.register("polished_nyxite_button",
            () -> new net.minecraft.world.level.block.ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON), net.minecraft.world.level.block.state.properties.BlockSetType.STONE, 20, false));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(nyxiteProperties()));


    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
            () -> new net.minecraft.world.level.block.WallBlock(nyxiteProperties()));

    public static final DeferredBlock<RotatedPillarBlock> NYXITE_PILLAR = BLOCKS.register("nyxite_pillar",
            () -> new RotatedPillarBlock(nyxiteProperties()));


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


    public static final DeferredBlock<RotatedPillarBlock> SHELLSTONE_PILLAR = BLOCKS.register("shellstone_pillar",
            () -> new RotatedPillarBlock(AntarchyObjects.shellstoneProperties()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.TriffidGooBlock> TRIFFID_GOO_BLOCK = BLOCKS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.block.TriffidGooBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            () -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyFabricItems::cloudBucketItem,
                    BlockBehaviour.Properties.copy(Blocks.POWDER_SNOW).noLootTable().noOcclusion()
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
            () -> new NyxiteSpikeBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));


    public static final DeferredBlock<ChitinSpikeBlock> CHITIN_SPIKE = BLOCKS.register("chitin_spike",
            () -> new ChitinSpikeBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)));


    public static final DeferredBlock<PotentNyxiteBlock> POTENT_NYXITE = BLOCKS.register("potent_nyxite",
            () -> new PotentNyxiteBlock(
                    AntarchyFabricBlocks::potentNyxiteBlockEntityType,
                    BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 3)
            ));


    public static final DeferredBlock<Block> MYRMITE = BLOCKS.register("myrmite",
            () -> new Block(nyxiteProperties()));


    public static final DeferredBlock<Block> BIOMITE = BLOCKS.register("biomite",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK)));


    public static final DeferredBlock<Block> BIOMITE_TURF = BLOCKS.register("biomite_turf",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 2)));


    public static final DeferredBlock<BiowartBlock> BIOWART = BLOCKS.register("biowart",
            () -> new BiowartBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK).lightLevel(state -> 2)));


    public static final DeferredBlock<BiowartTendrilsBlock> BIOWART_TENDRILS = BLOCKS.register("biowart_tendrils",
            () -> new BiowartTendrilsBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).lightLevel(state -> 2).noCollission().noOcclusion()));


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


    public static final DeferredBlock<Block> BROODSTONE_URANIUM_ORE = BLOCKS.register("broodstone_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> BROODSTONE_TITANIUM_ORE = BLOCKS.register("broodstone_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<RotatedPillarBlock> BROODSTONE_PILLAR = BLOCKS.register("broodstone_pillar",
            () -> new RotatedPillarBlock(broodstoneProperties()));


    public static final DeferredBlock<RotatedPillarBlock> CHITIN_BLOCK = BLOCKS.register("chitin_block",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BONE_BLOCK).requiresCorrectToolForDrops()));


    public static final DeferredBlock<UmbralMossBlock> UMBRAL_MOSS_BLOCK = BLOCKS.register("umbral_moss_block",
            () -> new UmbralMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));


    public static final DeferredBlock<UmbralMossCarpetBlock> UMBRAL_MOSS_CARPET = BLOCKS.register("umbral_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));


    public static final DeferredBlock<AmberMossBlock> AMBER_MOSS_BLOCK = BLOCKS.register("amber_moss_block",
            () -> new AmberMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));


    public static final DeferredBlock<UmbralMossCarpetBlock> AMBER_MOSS_CARPET = BLOCKS.register("amber_moss_carpet",
            () -> new UmbralMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));


    public static final DeferredBlock<AmberLichenBlock> AMBER_LICHEN = BLOCKS.register("amber_lichen",
            () -> new AmberLichenBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).lightLevel(state -> 4)));


    public static final DeferredBlock<Block> BILE_VEIN = BLOCKS.register("bile_vein",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERRACK).lightLevel(state -> 2)));


    public static final DeferredBlock<CreepvineBlock> CREEPVINE = BLOCKS.register("creepvine",
            () -> new CreepvineBlock(BlockBehaviour.Properties.copy(Blocks.VINE)));


    public static final DeferredBlock<BlushMossBlock> BLUSH_MOSS_BLOCK = BLOCKS.register("blush_moss_block",
            () -> new BlushMossBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));


    public static final DeferredBlock<BlushMossCarpetBlock> BLUSH_MOSS_CARPET = BLOCKS.register("blush_moss_carpet",
            () -> new BlushMossCarpetBlock(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noOcclusion()));


    public static final DeferredBlock<DreamTorchBlock> DREAM_TORCH = BLOCKS.register("dream_torch",
            () -> new DreamTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH)));


    public static final DeferredBlock<DreamWallTorchBlock> DREAM_WALL_TORCH = BLOCKS.register("dream_wall_torch",
            () -> new DreamWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_WALL_TORCH)));
    public static final DeferredBlock<com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock> DREAM_CEILING_TORCH = BLOCKS.register("dream_ceiling_torch",
            () -> new com.craisinlord.antarchy.content.block.DreamCeilingTorchBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH)));


    public static final DeferredBlock<net.minecraft.world.level.block.LanternBlock> DREAM_LANTERN = BLOCKS.register("dream_lantern",
            () -> new net.minecraft.world.level.block.LanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN)));


    public static final DeferredBlock<DreamCampfireBlock> DREAM_CAMPFIRE = BLOCKS.register("dream_campfire",
            () -> new DreamCampfireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_CAMPFIRE)));


    public static final DeferredBlock<DreamFireBlock> DREAM_FIRE = BLOCKS.register("dream_fire",
            () -> new DreamFireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_FIRE)));


    public static final DeferredBlock<DreamCeilingFireBlock> DREAM_CEILING_FIRE = BLOCKS.register("dream_fire_ceiling",
            () -> new DreamCeilingFireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_FIRE)));


    public static final DeferredBlock<BedBugEggBlock> BED_BUG_EGG = BLOCKS.register("bed_bug_egg",
            () -> new BedBugEggBlock(BlockBehaviour.Properties.of()
                    .strength(0.15F)
                    .sound(SoundType.METAL)
                    .randomTicks()
                    .noOcclusion()
                    .noCollission()
                    .replaceable()));


    public static final DeferredBlock<CreepingHorrorEggBlock> CREEPING_HORROR_EGG = BLOCKS.register("creeping_horror_egg",
            () -> new CreepingHorrorEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));


    public static final DeferredBlock<JumpyBugEggBlock> JUMPY_BUG_EGG = BLOCKS.register("jumpy_bug_egg",
            () -> new JumpyBugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));


    public static final DeferredBlock<SpitBugEggBlock> SPIT_BUG_EGG = BLOCKS.register("spit_bug_egg",
            () -> new SpitBugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));


    public static final DeferredBlock<JerryEggBlock> JERRY_EGG = BLOCKS.register("jerry_egg",
            () -> new JerryEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).randomTicks()));


    public static final DeferredBlock<LurkingTerrorEggBlock> LURKING_TERROR_EGG = BLOCKS.register("lurking_terror_egg",
            () -> new LurkingTerrorEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));


    static {
        BiowartBlock.bindTendrils(BIOWART_TENDRILS);
    }


    public static final DeferredBlock<WaspNestBlock> WASP_NEST = BLOCKS.register("wasp_nest",
            () -> new WaspNestBlock(AntarchyFabricBlocks::waspNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.BEE_NEST)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock> OURANWOOD_SQUIRREL_NEST = BLOCKS.register("ouranwood_squirrel_nest",
            () -> new com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT).noLootTable()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock> BRUTALFLY_COCOON_SPAWNER = BLOCKS.register("brutalfly_cocoon_spawner",
            () -> new com.craisinlord.antarchy.content.block.BrutalflyCocoonSpawnerBlock(BlockBehaviour.Properties.of().noCollission().instabreak().noLootTable()));


    public static final DeferredBlock<HushweedBlock> HUSHWEED = BLOCKS.register("hushweed",
            () -> new HushweedBlock(BlockBehaviour.Properties.copy(Blocks.AZALEA).noCollission().noOcclusion()));


    public static final DeferredBlock<HangingCreeprootsBlock> HANGING_CREEPROOTS = BLOCKS.register("hanging_creeproots",
            () -> new HangingCreeprootsBlock(BlockBehaviour.Properties.copy(Blocks.HANGING_ROOTS)));


    public static final DeferredBlock<MoltingVinesBlock> MOLTING_VINES = BLOCKS.register("molting_vines",
            () -> new MoltingVinesBlock(BlockBehaviour.Properties.copy(Blocks.WEEPING_VINES)));


    public static final DeferredBlock<LiquidBlock> BILE_BLOCK = BLOCKS.register("bile",
            () -> new BileLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.BILE.get(), BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel(state -> 5).noLootTable()));


    public static final DeferredBlock<LiquidBlock> ICHOR_BLOCK = BLOCKS.register("ichor",
            () -> new LiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ICHOR.get(), BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));


    public static final DeferredBlock<LiquidBlock> ANTIWATER_BLOCK = BLOCKS.register("antiwater",
            () -> new AntiwaterLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.ANTIWATER.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));


    public static final DeferredBlock<LiquidBlock> LUMEN_BLOCK = BLOCKS.register("lumen",
            () -> new LumenLiquidBlock((net.minecraft.world.level.material.FlowingFluid) AntarchyFabricMisc.LUMEN.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel(state -> 9).noLootTable()));


    public static final DeferredBlock<Block> LUMEN_FROGLIGHT = BLOCKS.register("lumen_froglight",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OCHRE_FROGLIGHT)));


    public static final DeferredBlock<Block> ROSEATE_FROGLIGHT = BLOCKS.register("roseate_froglight",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OCHRE_FROGLIGHT)));


    public static final DeferredBlock<AntNestBlock> RED_ANT_NEST = BLOCKS.register("red_ant_nest",
            () -> new AntNestBlock(
                    AntarchyFabricEntities.RED_ANT,
                    AntarchyFabricBlocks::antNestBlockEntityType,
                    true,
                    BlockBehaviour.Properties.copy(Blocks.MAGMA_BLOCK).lightLevel(state -> 3).noLootTable()
            ));


    public static final DeferredBlock<AntNestBlock> BROWN_ANT_NEST = BLOCKS.register("brown_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.BROWN_ANT, AntarchyFabricBlocks::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));


    public static final DeferredBlock<AntNestBlock> RAINBOW_ANT_NEST = BLOCKS.register("rainbow_ant_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.RAINBOW_ANT, AntarchyFabricBlocks::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));


    public static final DeferredBlock<AntNestBlock> TERMITE_NEST = BLOCKS.register("termite_nest",
            () -> new AntNestBlock(AntarchyFabricEntities.TERMITE, AntarchyFabricBlocks::antNestBlockEntityType, BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));


    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> createOre(Blocks.EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.register("titanium_ore",
            () -> createOre(Blocks.DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.register("deepslate_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<BluestoneOreBlock> BLUESTONE_ORE = BLOCKS.register("bluestone_ore",
            () -> new BluestoneOreBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_ORE).mapColor(MapColor.COLOR_BLUE)));


    public static final DeferredBlock<BluestoneWireBlock> BLUESTONE_WIRE = BLOCKS.register("bluestone_wire",
            () -> new BluestoneWireBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).mapColor(MapColor.COLOR_BLUE).noCollission().instabreak()));


    public static final DeferredBlock<BluestoneBlock> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block",
            () -> new BluestoneBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK).mapColor(MapColor.COLOR_BLUE)));
    public static final DeferredBlock<BluestoneRepeaterBlock> BLUESTONE_REPEATER = BLOCKS.register("bluestone_repeater",
            () -> new BluestoneRepeaterBlock(BlockBehaviour.Properties.copy(Blocks.REPEATER).mapColor(MapColor.COLOR_BLUE).noCollission()));
    public static final DeferredBlock<BluestoneComparatorBlock> BLUESTONE_COMPARATOR = BLOCKS.register("bluestone_comparator",
            () -> new BluestoneComparatorBlock(BlockBehaviour.Properties.copy(Blocks.COMPARATOR).mapColor(MapColor.COLOR_BLUE).noCollission(), AntarchyFabricBlocks::bluestoneComparatorBlockEntityType));
    public static final DeferredBlock<BluestoneTorchBlock> BLUESTONE_TORCH = BLOCKS.register("bluestone_torch",
            () -> new BluestoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH).mapColor(MapColor.COLOR_BLUE).lightLevel(state -> state.getValue(BluestoneTorchBlock.LIT) ? 7 : 0).noCollission().instabreak()));
    public static final DeferredBlock<BluestoneLampBlock> BLUESTONE_LAMP = BLOCKS.register("bluestone_lamp",
            () -> new BluestoneLampBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_BLUE)));


    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.register("uranium_block",
            () -> createStorageBlock(Blocks.EMERALD_BLOCK, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> createStorageBlock(Blocks.DIAMOND_BLOCK, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<Block> RAW_URANIUM_BLOCK = BLOCKS.register("raw_uranium_block",
            () -> createRawStorageBlock(Blocks.EMERALD_ORE, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = BLOCKS.register("raw_titanium_block",
            () -> createRawStorageBlock(Blocks.DIAMOND_ORE, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<Block> CUT_URANIUM = BLOCKS.register("cut_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> CUT_TITANIUM = BLOCKS.register("cut_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CUT_COPPER, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_URANIUM_SLAB = BLOCKS.register("cut_uranium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_YELLOW)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_TITANIUM_SLAB = BLOCKS.register("cut_titanium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_SLAB).mapColor(MapColor.COLOR_LIGHT_BLUE)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_URANIUM_STAIRS = BLOCKS.register("cut_uranium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_URANIUM.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_YELLOW)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> CUT_TITANIUM_STAIRS = BLOCKS.register("cut_titanium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(CUT_TITANIUM.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CUT_COPPER_STAIRS).mapColor(MapColor.COLOR_LIGHT_BLUE)));


    public static final DeferredBlock<Block> CHISELED_URANIUM = BLOCKS.register("chiseled_uranium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_STONE_BRICKS, MapColor.COLOR_YELLOW));


    public static final DeferredBlock<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_STONE_BRICKS, MapColor.COLOR_LIGHT_BLUE));


    public static final DeferredBlock<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_YELLOW)));


    public static final DeferredBlock<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_LIGHT_BLUE)));


    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> URANIUM_DOOR = BLOCKS.register("uranium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_YELLOW), net.minecraft.world.level.block.state.properties.BlockSetType.IRON));


    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> TITANIUM_DOOR = BLOCKS.register("titanium_door",
            () -> new net.minecraft.world.level.block.DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).mapColor(MapColor.COLOR_LIGHT_BLUE), net.minecraft.world.level.block.state.properties.BlockSetType.IRON));


    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> URANIUM_TRAPDOOR = BLOCKS.register("uranium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_YELLOW), net.minecraft.world.level.block.state.properties.BlockSetType.IRON));


    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> TITANIUM_TRAPDOOR = BLOCKS.register("titanium_trapdoor",
            () -> new net.minecraft.world.level.block.TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_TRAPDOOR).mapColor(MapColor.COLOR_LIGHT_BLUE), net.minecraft.world.level.block.state.properties.BlockSetType.IRON));


    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> URANIUM_BARS = BLOCKS.register("uranium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).mapColor(MapColor.COLOR_YELLOW)));


    public static final DeferredBlock<net.minecraft.world.level.block.IronBarsBlock> TITANIUM_BARS = BLOCKS.register("titanium_bars",
            () -> new net.minecraft.world.level.block.IronBarsBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).mapColor(MapColor.COLOR_LIGHT_BLUE)));


    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> ANTIMETAL = BLOCKS.register("antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BASALT)));


    public static final DeferredBlock<net.minecraft.world.level.block.RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));
    public static final DeferredBlock<StairBlock> ANTIMETAL_STAIRS = BLOCKS.register("antimetal_stairs",
            () -> new StairBlock(ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BASALT)));
    public static final DeferredBlock<SlabBlock> ANTIMETAL_SLAB = BLOCKS.register("antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BASALT)));
    public static final DeferredBlock<StairBlock> POLISHED_ANTIMETAL_STAIRS = BLOCKS.register("polished_antimetal_stairs",
            () -> new StairBlock(POLISHED_ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));
    public static final DeferredBlock<SlabBlock> POLISHED_ANTIMETAL_SLAB = BLOCKS.register("polished_antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            () -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalRailBlock> ANTIMETAL_RAIL = BLOCKS.register("antimetal_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalRailBlock(BlockBehaviour.Properties.copy(Blocks.RAIL)));
    public static final DeferredBlock<UpperBlock> UPPER = BLOCKS.register("upper",
            () -> new UpperBlock(AntarchyFabricBlocks::upperBlockEntityType, BlockBehaviour.Properties.copy(Blocks.HOPPER)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock> ANTIMETAL_POWERED_RAIL = BLOCKS.register("antimetal_powered_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock> ANTIMETAL_DETECTOR_RAIL = BLOCKS.register("antimetal_detector_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalDetectorRailBlock(BlockBehaviour.Properties.copy(Blocks.DETECTOR_RAIL)));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock> ANTIMETAL_ACTIVATOR_RAIL = BLOCKS.register("antimetal_activator_rail",
            () -> new com.craisinlord.antarchy.content.block.AntimetalActivatorRailBlock(BlockBehaviour.Properties.copy(Blocks.ACTIVATOR_RAIL)));


    public static final DeferredBlock<CorneaStalkBlock> CORNEA_STALK = BLOCKS.register("cornea_stalk",
            () -> new CorneaStalkBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH).randomTicks()));


    public static final DeferredBlock<com.craisinlord.antarchy.content.block.GlowcapMushroomBlock> GLOWCAP_MUSHROOM = BLOCKS.register("glowcap_mushroom",
            () -> new com.craisinlord.antarchy.content.block.GlowcapMushroomBlock(BlockBehaviour.Properties.copy(Blocks.RED_MUSHROOM).lightLevel(state -> 12).randomTicks()));
    public static final DeferredBlock<net.minecraft.world.level.block.FlowerPotBlock> POTTED_GLOWCAP_MUSHROOM = BLOCKS.register("potted_glowcap_mushroom",
            () -> new net.minecraft.world.level.block.FlowerPotBlock(GLOWCAP_MUSHROOM.get(), BlockBehaviour.Properties.copy(Blocks.POTTED_DANDELION).lightLevel(state -> 12)));


    public static final DeferredBlock<net.minecraft.world.level.block.HugeMushroomBlock> GLOWCAP_MUSHROOM_BLOCK = BLOCKS.register("glowcap_mushroom_block",
            () -> new net.minecraft.world.level.block.HugeMushroomBlock(BlockBehaviour.Properties.copy(Blocks.RED_MUSHROOM_BLOCK).lightLevel(state -> 13)));


    public static final DeferredBlock<CornCropBlock> CORN_CROP = BLOCKS.register("corn_crop",
            () -> new CornCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).randomTicks().noCollission().noOcclusion()));


    public static final DeferredBlock<WildCornBlock> WILD_CORN = BLOCKS.register("wild_corn",
            () -> new WildCornBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noCollission().noOcclusion()));


    public static final DeferredBlock<Block> FALLEN_KING_CROWN_BLOCK = BLOCKS.register("fallen_king_crown",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));


    public static final DeferredBlock<AmethystClusterBlock> SMALL_BLOOD_CRYSTAL_BUD = BLOCKS.register("small_blood_crystal_bud",
            () -> new AmethystClusterBlock(3, 4, BlockBehaviour.Properties.copy(Blocks.SMALL_AMETHYST_BUD)));


    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_BLOOD_CRYSTAL_BUD = BLOCKS.register("medium_blood_crystal_bud",
            () -> new AmethystClusterBlock(4, 3, BlockBehaviour.Properties.copy(Blocks.MEDIUM_AMETHYST_BUD)));


    public static final DeferredBlock<AmethystClusterBlock> LARGE_BLOOD_CRYSTAL_BUD = BLOCKS.register("large_blood_crystal_bud",
            () -> new AmethystClusterBlock(5, 3, BlockBehaviour.Properties.copy(Blocks.LARGE_AMETHYST_BUD)));


    public static final DeferredBlock<Block> BUDDING_BLOOD_CRYSTAL = BLOCKS.register("budding_blood_crystal",
            () -> new BuddingBloodCrystalBlock(
                    BlockBehaviour.Properties.copy(Blocks.BUDDING_AMETHYST),
                    AntarchyFabricBlocks::smallBloodCrystalBudBlock,
                    AntarchyFabricBlocks::mediumBloodCrystalBudBlock,
                    AntarchyFabricBlocks::largeBloodCrystalBudBlock,
                    AntarchyFabricBlocks::bloodCrystalCrystalBlock
            ));


    public static final DeferredBlock<Block> BLOOD_CRYSTAL = BLOCKS.register("blood_crystal_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK)));


    public static final DeferredBlock<AmethystClusterBlock> BLOOD_CRYSTAL_CRYSTAL = BLOCKS.register("blood_crystal_cluster",
            () -> new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.copy(Blocks.AMETHYST_CLUSTER)));


    public static final DeferredBlock<Block> DREAM_SAND = BLOCKS.register("dream_sand",
            () -> new DreamSandBlock(BlockBehaviour.Properties.copy(Blocks.SAND)));
    public static final DeferredBlock<AntigravelBlock> ANTIGRAVEL = BLOCKS.register("antigravel",
            () -> new AntigravelBlock(BlockBehaviour.Properties.copy(Blocks.GRAVEL)));
    public static final DeferredBlock<Block> LOAM = BLOCKS.register("loam",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD)));
    public static final DeferredBlock<MucusBlock> MUCUS = BLOCKS.register("mucus",
            () -> new MucusBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).friction(0.98F).lightLevel(state -> 0).sound(net.minecraft.world.level.block.SoundType.SLIME_BLOCK)));


    public static final DeferredBlock<Block> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SANDSTONE)));


    public static final DeferredBlock<Block> CHISELED_DREAM_SANDSTONE = BLOCKS.register("chiseled_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CHISELED_SANDSTONE)));


    public static final DeferredBlock<Block> CUT_DREAM_SANDSTONE = BLOCKS.register("cut_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE)));


    public static final DeferredBlock<Block> SMOOTH_DREAM_SANDSTONE = BLOCKS.register("smooth_dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> DREAM_SANDSTONE_STAIRS = BLOCKS.register("dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SANDSTONE_STAIRS)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> DREAM_SANDSTONE_SLAB = BLOCKS.register("dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE_SLAB)));


    public static final DeferredBlock<net.minecraft.world.level.block.WallBlock> DREAM_SANDSTONE_WALL = BLOCKS.register("dream_sandstone_wall",
            () -> new net.minecraft.world.level.block.WallBlock(BlockBehaviour.Properties.copy(Blocks.SANDSTONE_WALL)));


    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> SMOOTH_DREAM_SANDSTONE_STAIRS = BLOCKS.register("smooth_dream_sandstone_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(SMOOTH_DREAM_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE_STAIRS)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> SMOOTH_DREAM_SANDSTONE_SLAB = BLOCKS.register("smooth_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_SANDSTONE_SLAB)));


    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> CUT_DREAM_SANDSTONE_SLAB = BLOCKS.register("cut_dream_sandstone_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE_SLAB)));


    public static final DeferredBlock<Block> DEAD_STAR_CORAL_BLOCK = BLOCKS.register("dead_star_coral_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_BLOCK)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralPlantBlock> DEAD_STAR_CORAL = BLOCKS.register("dead_star_coral",
            () -> new net.minecraft.world.level.block.BaseCoralPlantBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralFanBlock> DEAD_STAR_CORAL_FAN = BLOCKS.register("dead_star_coral_fan",
            () -> new net.minecraft.world.level.block.BaseCoralFanBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_FAN)));


    public static final DeferredBlock<net.minecraft.world.level.block.BaseCoralWallFanBlock> DEAD_STAR_CORAL_WALL_FAN = BLOCKS.register("dead_star_coral_wall_fan",
            () -> new net.minecraft.world.level.block.BaseCoralWallFanBlock(BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_WALL_FAN)));


    public static final DeferredBlock<Block> STAR_CORAL_BLOCK = BLOCKS.register("star_coral_block",
            () -> new StarCoralBlock(DEAD_STAR_CORAL_BLOCK.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_BLOCK).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralPlantBlock> STAR_CORAL = BLOCKS.register("star_coral",
            () -> new StarCoralPlantBlock(DEAD_STAR_CORAL.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralFanBlock> STAR_CORAL_FAN = BLOCKS.register("star_coral_fan",
            () -> new StarCoralFanBlock(DEAD_STAR_CORAL_FAN.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_FAN).lightLevel(state -> 8)));


    public static final DeferredBlock<StarCoralWallFanBlock> STAR_CORAL_WALL_FAN = BLOCKS.register("star_coral_wall_fan",
            () -> new StarCoralWallFanBlock(DEAD_STAR_CORAL_WALL_FAN.get(), BlockBehaviour.Properties.copy(Blocks.TUBE_CORAL_WALL_FAN).lightLevel(state -> 8)));


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> BlockEntityType.Builder.of(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get(),
                    TERMITE_NEST.get()
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lucid_anchor",
            () -> BlockEntityType.Builder.of(
                    com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity::new,
                    LUCID_ANCHOR.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterCageBlockEntity>> CRITTER_CAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("critter_cage_block",
            () -> BlockEntityType.Builder.of(
                    CritterCageBlockEntity::new,
                    CRITTER_CAGE_BLOCK.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity>> BLUESTONE_COMPARATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("bluestone_comparator",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity(pos, state, AntarchyFabricBlocks::bluestoneComparatorBlockEntityType),
                    BLUESTONE_COMPARATOR.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpperBlockEntity>> UPPER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("upper",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new UpperBlockEntity(pos, state, AntarchyFabricBlocks::upperBlockEntityType),
                    UPPER.get()
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

    private static BlockEntityType<com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity> bluestoneComparatorBlockEntityType() {
        return BLUESTONE_COMPARATOR_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<UpperBlockEntity> upperBlockEntityType() {
        return UPPER_BLOCK_ENTITY.get();
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
        return new DropExperienceBlock(dirtOreProperties(), UniformInt.of(minExperience, maxExperience));
    }



    private static BlockBehaviour.Properties dirtOreProperties() {
        return BlockBehaviour.Properties.copy(Blocks.DIRT)
                .sound(SoundType.ROOTED_DIRT)
                .strength(0.65F, 0.8F)
                .requiresCorrectToolForDrops();
    }



    private static BlockBehaviour.Properties nyxiteProperties() {
        return BlockBehaviour.Properties.copy(Blocks.NETHERRACK);
    }

    private static BlockBehaviour.Properties broodstoneProperties() {
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



    private static Block createOre(Block copyFrom, MapColor mapColor) {
        return new Block(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }



    private static Block createOre(Block copyFrom, int minExperience, int maxExperience, MapColor mapColor) {
        return new DropExperienceBlock(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops(), UniformInt.of(minExperience, maxExperience));
    }



    private static Block createStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }



    private static Block createHorizontalFacingStorageBlock(Block copyFrom, MapColor mapColor) {
        return new SimpleHorizontalFacingBlock(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
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



    private static Block createRawStorageBlock(Block copyFrom, MapColor mapColor) {
        return new Block(BlockBehaviour.Properties.copy(copyFrom).mapColor(mapColor).requiresCorrectToolForDrops());
    }


    public static void register() {
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();
    }

}
