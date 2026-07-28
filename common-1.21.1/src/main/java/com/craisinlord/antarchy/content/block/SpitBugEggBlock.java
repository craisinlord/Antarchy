package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SpitBugEntity;
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

public class SpitBugEggBlock extends Block {
    public static final MapCodec<SpitBugEggBlock> CODEC = Block.simpleCodec(SpitBugEggBlock::new);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    private static final int MAX_HATCH = 2;
    private static final double PLAYER_PROXIMITY_RADIUS = 12.0D;
    private static final double HORDE_ACCELERATION_RADIUS = 5.0D;
    private static final float PLAYER_PROXIMITY_HATCH_CHANCE = 0.25F;
    private static final int PLAYER_PROXIMITY_CHECK_INTERVAL = 200;
    private static final VoxelShape SHAPE = Block.box(1.5D, 0.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final ResourceLocation SPIT_BUG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spit_bug");

    public SpitBugEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(ROTATED, false));
    }

    @Override
    public MapCodec<SpitBugEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATED, context.getHorizontalDirection().getAxis() == Direction.Axis.X);
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
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.shouldUpdateHatchLevel(level, pos)) {
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

    private boolean shouldUpdateHatchLevel(Level level, BlockPos pos) {
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

        boolean onAmberMoss = level.getBlockState(pos.below()).getBlock() instanceof AmberMossBlock;
        return level.random.nextInt(onAmberMoss ? 150 : 500) == 0;
    }

    private void hatch(ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.75F, 0.95F + random.nextFloat() * 0.1F);
        @SuppressWarnings("unchecked")
        EntityType<SpitBugEntity> spitBugType = (EntityType<SpitBugEntity>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getOptional(SPIT_BUG_ID).orElse(null);
        if (spitBugType == null) {
            return;
        }
        SpitBugEntity entity = spitBugType.create(level);
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
        builder.add(HATCH, ROTATED);
    }
}
