package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
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
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.forge.content.block.WildCornBlock;
import com.craisinlord.antarchy.forge.AntarchyWoodTypes;
import com.craisinlord.antarchy.forge.content.fluid.AntiwaterFluidType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;

public final class AntarchyForgeBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Antarchy.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Antarchy.MODID);
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
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, false));
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
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_BUTTON), BlockSetType.JUNGLE, 30, false));
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
    public static final RegistryObject<DuplicatorSaplingBlock> DUPLICATOR_SAPLING = BLOCKS.register("duplicator_sapling",
            () -> new DuplicatorSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks().noCollission()));
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
            () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON), BlockSetType.STONE, 20, true));
    public static final RegistryObject<StairBlock> NYXITE_BRICK_STAIRS = BLOCKS.register("nyxite_brick_stairs",
            () -> new StairBlock(NYXITE_BRICKS.get().defaultBlockState(), nyxiteProperties()));
    public static final RegistryObject<SlabBlock> NYXITE_BRICK_SLAB = BLOCKS.register("nyxite_brick_slab",
            () -> new SlabBlock(nyxiteProperties()));
    public static final RegistryObject<WallBlock> NYXITE_BRICK_WALL = BLOCKS.register("nyxite_brick_wall",
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
    public static final RegistryObject<com.craisinlord.antarchy.content.block.TriffidGooBlock> TRIFFID_GOO_BLOCK = BLOCKS.register("triffid_goo_block",
            () -> new com.craisinlord.antarchy.content.block.TriffidGooBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.CloudBlock> CLOUD_BLOCK = BLOCKS.register("cloud_block",
            () -> new com.craisinlord.antarchy.content.block.CloudBlock(
                    AntarchyForgeItems::cloudBucketItem,
                    BlockBehaviour.Properties.copy(Blocks.POWDER_SNOW).noLootTable().noOcclusion()
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
    public static final RegistryObject<Block> BROODSTONE_URANIUM_ORE = BLOCKS.register("broodstone_uranium_ore",
            () -> createOre(Blocks.DEEPSLATE_EMERALD_ORE, 4, 8, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> BROODSTONE_TITANIUM_ORE = BLOCKS.register("broodstone_titanium_ore",
            () -> createOre(Blocks.DEEPSLATE_DIAMOND_ORE, 4, 8, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<RotatedPillarBlock> BROODSTONE_PILLAR = BLOCKS.register("broodstone_pillar",
            () -> new RotatedPillarBlock(broodstoneProperties()));
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
    public static final RegistryObject<CreepingHorrorEggBlock> CREEPING_HORROR_EGG = BLOCKS.register("creeping_horror_egg",
            () -> new CreepingHorrorEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<JumpyBugEggBlock> JUMPY_BUG_EGG = BLOCKS.register("jumpy_bug_egg",
            () -> new JumpyBugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<SpitBugEggBlock> SPIT_BUG_EGG = BLOCKS.register("spit_bug_egg",
            () -> new SpitBugEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));
    public static final RegistryObject<JerryEggBlock> JERRY_EGG = BLOCKS.register("jerry_egg",
            () -> new JerryEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG).randomTicks()));
    public static final RegistryObject<LurkingTerrorEggBlock> LURKING_TERROR_EGG = BLOCKS.register("lurking_terror_egg",
            () -> new LurkingTerrorEggBlock(BlockBehaviour.Properties.copy(Blocks.TURTLE_EGG)));

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
    public static final RegistryObject<BluestoneRepeaterBlock> BLUESTONE_REPEATER = BLOCKS.register("bluestone_repeater",
            () -> new BluestoneRepeaterBlock(BlockBehaviour.Properties.copy(Blocks.REPEATER).mapColor(MapColor.COLOR_BLUE).noCollission()));
    public static final RegistryObject<BluestoneComparatorBlock> BLUESTONE_COMPARATOR = BLOCKS.register("bluestone_comparator",
            () -> new BluestoneComparatorBlock(BlockBehaviour.Properties.copy(Blocks.COMPARATOR).mapColor(MapColor.COLOR_BLUE).noCollission(), AntarchyForgeBlocks::bluestoneComparatorBlockEntityType));
    public static final RegistryObject<BluestoneTorchBlock> BLUESTONE_TORCH = BLOCKS.register("bluestone_torch",
            () -> new BluestoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH).mapColor(MapColor.COLOR_BLUE).lightLevel(state -> state.getValue(BluestoneTorchBlock.LIT) ? 7 : 0).noCollission().instabreak()));
    public static final RegistryObject<BluestoneLampBlock> BLUESTONE_LAMP = BLOCKS.register("bluestone_lamp",
            () -> new BluestoneLampBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_BLUE)));
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
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_STONE_BRICKS, MapColor.COLOR_YELLOW));
    public static final RegistryObject<Block> CHISELED_TITANIUM = BLOCKS.register("chiseled_titanium",
            () -> createHorizontalFacingStorageBlock(Blocks.CHISELED_STONE_BRICKS, MapColor.COLOR_LIGHT_BLUE));
    public static final RegistryObject<SignalSavingBulbBlock> URANIUM_BULB = BLOCKS.register("uranium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<SignalSavingBulbBlock> TITANIUM_BULB = BLOCKS.register("titanium_bulb",
            () -> new SignalSavingBulbBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.COLOR_LIGHT_BLUE)));
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
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BASALT)));
    public static final RegistryObject<RotatedPillarBlock> POLISHED_ANTIMETAL = BLOCKS.register("polished_antimetal",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));
    public static final RegistryObject<StairBlock> ANTIMETAL_STAIRS = BLOCKS.register("antimetal_stairs",
            () -> new StairBlock(ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BASALT)));
    public static final RegistryObject<SlabBlock> ANTIMETAL_SLAB = BLOCKS.register("antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BASALT)));
    public static final RegistryObject<StairBlock> POLISHED_ANTIMETAL_STAIRS = BLOCKS.register("polished_antimetal_stairs",
            () -> new StairBlock(POLISHED_ANTIMETAL.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));
    public static final RegistryObject<SlabBlock> POLISHED_ANTIMETAL_SLAB = BLOCKS.register("polished_antimetal_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BASALT)));
    public static final RegistryObject<com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock> ANTIMETAL_SCAFFOLDING = BLOCKS.register("antimetal_scaffolding",
            () -> new com.craisinlord.antarchy.content.block.AntimetalScaffoldingBlock(BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING)));
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
    public static final RegistryObject<MucusBlock> MUCUS = BLOCKS.register("mucus",
            () -> new MucusBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).friction(0.98F).lightLevel(state -> 0).sound(net.minecraft.world.level.block.SoundType.SLIME_BLOCK)));
    public static final RegistryObject<Block> DREAM_SANDSTONE = BLOCKS.register("dream_sandstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SANDSTONE)));
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

    // Block entity types
    public static final RegistryObject<BlockEntityType<AntNestBlockEntity>> ANT_NEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("ant_nest",
            () -> BlockEntityType.Builder.of(
                    AntNestBlockEntity::new,
                    RED_ANT_NEST.get(),
                    BROWN_ANT_NEST.get(),
                    RAINBOW_ANT_NEST.get(),
                    TERMITE_NEST.get()
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
    public static final RegistryObject<BlockEntityType<com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity>> LUCID_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lucid_anchor",
            () -> BlockEntityType.Builder.of(
                    com.craisinlord.antarchy.content.block.entity.LucidAnchorBlockEntity::new,
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

    private AntarchyForgeBlocks() {}

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
