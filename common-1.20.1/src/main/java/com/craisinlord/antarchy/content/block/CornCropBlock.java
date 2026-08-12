package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CornCropBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final int MAX_AGE = 3;
    private static final int TALL_AGE = 2;
    private static final VoxelShape[] LOWER_SHAPES = new VoxelShape[] {
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)
    };
    private static final VoxelShape[] UPPER_SHAPES = new VoxelShape[] {
            Block.box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
            Block.box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)
    };
    private static final ResourceLocation CORN_SEEDS_ID = new ResourceLocation("antarchy", "corn_seeds");

    public CornCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPES[age] : LOWER_SHAPES[age];
    }

    @Override
    public boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);
        int age = state.getValue(AGE);
        if (half == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE) >= TALL_AGE;
        }

        if (!super.canSurvive(state, level, pos)) {
            return false;
        }

        return age < TALL_AGE || level.getBlockState(pos.above()).is(this)
                && level.getBlockState(pos.above()).getValue(HALF) == DoubleBlockHalf.UPPER
                && level.getBlockState(pos.above()).getValue(AGE) == age;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9 || random.nextInt(5) != 0) {
            return;
        }

        this.grow(level, pos, state);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        if (!lowerState.is(this) || lowerState.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return false;
        }

        int age = lowerState.getValue(AGE);
        return age < MAX_AGE && (age >= TALL_AGE || level.getBlockState(lowerPos.above()).canBeReplaced());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        if (lowerState.is(this)) {
            this.grow(level, lowerPos, lowerState);
        }
    }

    private void grow(ServerLevel level, BlockPos lowerPos, BlockState lowerState) {
        int age = lowerState.getValue(AGE);
        if (age >= MAX_AGE) {
            return;
        }

        if (age + 1 >= TALL_AGE) {
            BlockPos upperPos = lowerPos.above();
            if (!level.getBlockState(upperPos).canBeReplaced() && !level.getBlockState(upperPos).is(this)) {
                return;
            }
        }

        int newAge = age + 1;
        level.setBlock(lowerPos, lowerState.setValue(AGE, newAge), Block.UPDATE_ALL);
        if (newAge >= TALL_AGE) {
            level.setBlock(lowerPos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, newAge), Block.UPDATE_ALL);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this.getBaseSeedId());
    }

    protected ItemLike getBaseSeedId() {
        Optional<net.minecraft.world.item.Item> item = BuiltInRegistries.ITEM.getOptional(CORN_SEEDS_ID);
        return item.orElse(Blocks.AIR.asItem());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }
}
