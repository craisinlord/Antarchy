package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JumpyBugEggBlock extends Block {
    public static final MapCodec<JumpyBugEggBlock> CODEC = Block.simpleCodec(JumpyBugEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    private static final int MAX_HATCH = 2;
    private static final double PLAYER_PROXIMITY_RADIUS = 12.0D;
    private static final double HORDE_ACCELERATION_RADIUS = 5.0D;
    private static final float PLAYER_PROXIMITY_HATCH_CHANCE = 0.25F;
    private static final int PLAYER_PROXIMITY_CHECK_INTERVAL = 200;
    private static final VoxelShape FLOOR_SHAPE = Block.box(1.5D, 0.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final VoxelShape CEILING_SHAPE = Block.box(1.5D, 0.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final ResourceLocation JUMPY_BUG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_bug");

    public JumpyBugEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(HANGING, false).setValue(ROTATED, false));
    }

    @Override
    public MapCodec<JumpyBugEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? CEILING_SHAPE : FLOOR_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hanging = context.getClickedFace() == Direction.DOWN
                || (context.getClickedFace() != Direction.UP && !canSupportFloor(context.getLevel(), context.getClickedPos()) && canSupportCeiling(context.getLevel(), context.getClickedPos()));
        boolean rotated = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
        return this.defaultBlockState().setValue(HANGING, hanging).setValue(ROTATED, rotated);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
                ? state.setValue(ROTATED, !state.getValue(ROTATED))
                : state;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(HANGING) ? canSupportCeiling(level, pos) : canSupportFloor(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    private static boolean canSupportFloor(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static boolean canSupportCeiling(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.shouldUpdateHatchLevel(level, pos, state)) {
            return;
        }

        int hatch = state.getValue(HATCH);
        if (hatch < MAX_HATCH) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.95F + random.nextFloat() * 0.1F);
            return;
        }

        this.hatch(level, pos, random);
    }

    private boolean shouldUpdateHatchLevel(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel
                && CavarynHordeManager.shouldAccelerateNearbyEggHatching(serverLevel, pos, HORDE_ACCELERATION_RADIUS, level.random)) {
            return true;
        }

        if (level.random.nextInt(PLAYER_PROXIMITY_CHECK_INTERVAL) == 0) {
            Player nearbyPlayer = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, PLAYER_PROXIMITY_RADIUS, false);
            if (nearbyPlayer != null && level.random.nextFloat() < PLAYER_PROXIMITY_HATCH_CHANCE) {
                return true;
            }
        }

        BlockPos checkPos = state.getValue(HANGING) ? pos.above() : pos.below();
        boolean onAmberMoss = level.getBlockState(checkPos).getBlock() instanceof AmberMossBlock;
        return level.random.nextInt(onAmberMoss ? 150 : 500) == 0;
    }

    private void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.75F, 0.95F + random.nextFloat() * 0.1F);
        @SuppressWarnings("unchecked")
        EntityType<JumpyBugEntity> jumpyBugType = (EntityType<JumpyBugEntity>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getOptional(JUMPY_BUG_ID).orElse(null);
        if (jumpyBugType == null) {
            return;
        }
        JumpyBugEntity entity = jumpyBugType.create(level);
        if (entity == null) {
            return;
        }
        double spawnX = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
        double spawnZ = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
        entity.moveTo(spawnX, pos.getY() + 0.1D, spawnZ, random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity);
    }

    public void hatchWithNucleus(ServerLevel level, BlockPos pos) {
        this.hatch(level, pos, level.random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, HANGING, ROTATED);
    }
}
