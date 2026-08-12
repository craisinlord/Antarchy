package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.portal.PermanentPortalManager;
import com.craisinlord.antarchy.content.portal.PermanentPortalType;
import com.craisinlord.antarchy.content.portal.PermanentPortalTeleporter;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AntDimensionPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    private static final int PORTAL_TRANSITION_TICKS = 20;
    private static final Map<UUID, Integer> PORTAL_ENTRY_TICKS = new HashMap<>();

    private final PermanentPortalType type;

    public AntDimensionPortalBlock(PermanentPortalType type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || !entity.isAlive() || !entity.canChangeDimensions() || entity.isOnPortalCooldown()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int ticks = PORTAL_ENTRY_TICKS.merge(entity.getUUID(), 1, Integer::sum);
        if (ticks < PORTAL_TRANSITION_TICKS) {
            return;
        }

        PORTAL_ENTRY_TICKS.remove(entity.getUUID());
        PermanentPortalTeleporter.teleport(entity, this.type);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, 1);
        }
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
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!PermanentPortalManager.isPortalStillValid(level, pos, this.type, state.getValue(AXIS))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35F, 0.9F + random.nextFloat() * 0.2F, false);
        }

        ParticleOptions particle = switch (this.type) {
            case ELYTHIA -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case THORAXIS -> ParticleTypes.PORTAL;
            case CAVARYN -> ParticleTypes.MYCELIUM;
        };

        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            if (state.getValue(AXIS) == Direction.Axis.X) {
                z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;
            } else {
                x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;
            }
            level.addParticle(
                    particle,
                    x,
                    y,
                    z,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    (random.nextDouble() - 0.5D) * 0.2D,
                    (random.nextDouble() - 0.5D) * 0.2D
            );
        }
    }
}
