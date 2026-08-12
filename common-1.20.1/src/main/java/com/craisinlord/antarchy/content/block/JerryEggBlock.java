package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JerryEggBlock extends Block {
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    private static final ResourceLocation JERRY_ID = new ResourceLocation(Antarchy.MODID, "jerry");
    private static final int HATCH_CHECK_INTERVAL = 1200;
    private static final double HORDE_ACCELERATION_RADIUS = 5.0D;
    private static final VoxelShape SHAPE = Block.box(1.5D, 0.0D, 1.5D, 14.5D, 16.0D, 14.5D);

    public JerryEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATED, context.getHorizontalDirection().getAxis() == Direction.Axis.X);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
                ? state.setValue(ROTATED, !state.getValue(ROTATED))
                : state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!CavarynHordeManager.shouldAccelerateNearbyEggHatching(level, pos, HORDE_ACCELERATION_RADIUS, random)
                && random.nextInt(HATCH_CHECK_INTERVAL) != 0) {
            return;
        }
        this.hatch(level, pos, random);
    }

    public void hatchWithNucleus(ServerLevel level, BlockPos pos) {
        this.hatch(level, pos, level.random);
    }

    private void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(JERRY_ID);
        if (entityType.create(level) instanceof JerryEntity jerry) {
            jerry.setStage(JerryEntity.Stage.INFANT);
            jerry.moveTo(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(jerry);
            level.destroyBlock(pos, false);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.HOSTILE, 0.8F, 0.8F + random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATED);
    }
}
