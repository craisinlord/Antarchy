package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.BluestoneComparatorBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public class BluestoneComparatorBlock extends ComparatorBlock {
    public static final MapCodec<BluestoneComparatorBlock> CODEC = simpleCodec(BluestoneComparatorBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final DustParticleOptions BLUESTONE_PARTICLE = new DustParticleOptions(new Vector3f(0.18F, 0.54F, 1.0F), 1.0F);
    private final Supplier<? extends BlockEntityType<BluestoneComparatorBlockEntity>> blockEntityTypeSupplier;

    public BluestoneComparatorBlock(BlockBehaviour.Properties properties) {
        this(properties, null);
    }

    public BluestoneComparatorBlock(BlockBehaviour.Properties properties, Supplier<? extends BlockEntityType<BluestoneComparatorBlockEntity>> blockEntityTypeSupplier) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
    }

    @Override
    public MapCodec<ComparatorBlock> codec() {
        return (MapCodec<ComparatorBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BluestonePlacementHelper.hasCeilingSupport(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return BluestonePlacementHelper.canPlaceOnCeiling(context) ? super.getStateForPlacement(context) : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BluestoneComparatorBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public void animateTick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            return;
        }
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        double y = pos.getY() + 0.8D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
        level.addParticle(BLUESTONE_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
