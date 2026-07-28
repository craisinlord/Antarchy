package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.worldgen.elythia.OuranwoodTreeGrowers;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OuranwoodAcornBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<OuranwoodAcornBlock> CODEC = Block.simpleCodec(OuranwoodAcornBlock::new);
    public static final int MAX_STAGE = 2;
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);
    private static final VoxelShape[] SAPLING_SHAPES = new VoxelShape[] {
            Block.box(5.5D, 0.0D, 5.5D, 10.5D, 4.0D, 10.5D),
            Block.box(3.5D, 0.0D, 3.5D, 12.5D, 8.0D, 12.5D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D)
    };
    private static final VoxelShape HANGING_SHAPE = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public OuranwoodAcornBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false).setValue(STAGE, 0));
    }

    @Override
    public MapCodec<OuranwoodAcornBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int stage = state.getValue(STAGE);
        return state.getValue(HANGING) ? HANGING_SHAPE : SAPLING_SHAPES[stage];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HANGING)) {
            return level.getBlockState(pos.above()).is(BlockTags.LEAVES);
        }

        return super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING)) {
            return;
        }

        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.advanceGrowth(level, pos, state, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(HANGING);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return !state.getValue(HANGING) && random.nextFloat() < 0.75F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(HANGING)) {
            return;
        }

        this.advanceGrowth(level, pos, state, random);
    }

    private void advanceGrowth(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) < MAX_STAGE) {
            level.setBlock(pos, state.setValue(STAGE, state.getValue(STAGE) + 1), Block.UPDATE_ALL);
            return;
        }

        this.growConfiguredTree(level, pos, state, random);
    }

    private void growConfiguredTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Footprint largeFootprint = this.findFootprint(level, pos, 3);
        if (largeFootprint != null) {
            if (largeFootprint.fullyMatured()) {
                this.placeConfiguredTree(level, largeFootprint, OuranwoodTreeGrowers.OURANWOOD_LARGE_TREE, random);
            }
            return;
        }

        Footprint mediumFootprint = this.findFootprint(level, pos, 2);
        if (mediumFootprint != null) {
            if (mediumFootprint.fullyMatured()) {
                this.placeConfiguredTree(level, mediumFootprint, OuranwoodTreeGrowers.OURANWOOD_MEDIUM_TREE, random);
            }
            return;
        }

        this.placeConfiguredTree(level, new Footprint(pos, Map.of(pos, state)), OuranwoodTreeGrowers.OURANWOOD_YOUNG_TREE, random);
    }

    private Footprint findFootprint(ServerLevel level, BlockPos pos, int size) {
        for (int offsetX = 0; offsetX < size; offsetX++) {
            for (int offsetZ = 0; offsetZ < size; offsetZ++) {
                BlockPos origin = pos.offset(-offsetX, 0, -offsetZ);
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                boolean matches = true;
                boolean fullyMatured = true;

                for (int x = 0; x < size && matches; x++) {
                    for (int z = 0; z < size; z++) {
                        BlockPos checkPos = origin.offset(x, 0, z);
                        BlockState checkState = level.getBlockState(checkPos);
                        if (!checkState.is(this) || checkState.getValue(HANGING)) {
                            matches = false;
                            break;
                        }

                        fullyMatured &= checkState.getValue(STAGE) >= MAX_STAGE;
                        blocks.put(checkPos, checkState);
                    }
                }

                if (matches) {
                    return new Footprint(origin, blocks, fullyMatured);
                }
            }
        }

        return null;
    }

    private void placeConfiguredTree(ServerLevel level, Footprint footprint, ResourceKey<ConfiguredFeature<?, ?>> featureKey, RandomSource random) {
        Holder.Reference<ConfiguredFeature<?, ?>> feature = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(featureKey);

        for (BlockPos saplingPos : footprint.blocks().keySet()) {
            level.removeBlock(saplingPos, false);
        }

        if (feature.value().place(level, level.getChunkSource().getGenerator(), random, footprint.origin())) {
            return;
        }

        for (Map.Entry<BlockPos, BlockState> entry : footprint.blocks().entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING, STAGE);
    }

    private record Footprint(BlockPos origin, Map<BlockPos, BlockState> blocks, boolean fullyMatured) {
        private Footprint(BlockPos origin, Map<BlockPos, BlockState> blocks) {
            this(origin, blocks, true);
        }
    }
}
