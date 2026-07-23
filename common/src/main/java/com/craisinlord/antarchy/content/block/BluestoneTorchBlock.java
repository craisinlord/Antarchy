package com.craisinlord.antarchy.content.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class BluestoneTorchBlock extends AbstractCeilingBluestoneBlock implements BluestoneSignalSource {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final DustParticleOptions BLUESTONE_PARTICLE = new DustParticleOptions(new Vector3f(0.18F, 0.54F, 1.0F), 1.0F);
    private static final Map<Level, List<Toggle>> RECENT_TOGGLES = new WeakHashMap<>();

    public BluestoneTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN) {
            return 0;
        }
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide) {
            boolean powered = this.hasInputSignal(level, pos);
            if (state.getValue(LIT) == powered) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = this.hasInputSignal(level, pos);
        boolean lit = state.getValue(LIT);

        if (lit) {
            if (powered) {
                level.setBlock(pos, state.setValue(LIT, false), 3);
                if (this.isToggledTooFrequently(level, pos, true)) {
                    level.levelEvent(1502, pos, 0);
                    level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                    level.scheduleTick(pos, this, 160);
                }
            }
        } else if (!powered && !this.isToggledTooFrequently(level, pos, false)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.3D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(BLUESTONE_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public int getBluestoneSignal(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        return direction == Direction.DOWN ? 0 : state.getValue(LIT) ? 15 : 0;
    }

    private boolean hasInputSignal(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos.above());
    }

    private boolean isToggledTooFrequently(Level level, BlockPos pos, boolean addNew) {
        List<Toggle> toggles = RECENT_TOGGLES.computeIfAbsent(level, ignored -> new ArrayList<>());
        if (addNew) {
            toggles.add(new Toggle(pos.immutable(), level.getGameTime()));
        }

        long cutoff = level.getGameTime() - 60L;
        toggles.removeIf(toggle -> toggle.gameTime < cutoff);

        int count = 0;
        for (Toggle toggle : toggles) {
            if (toggle.pos.equals(pos)) {
                count++;
                if (count >= 8) {
                    return true;
                }
            }
        }
        return false;
    }

    private record Toggle(BlockPos pos, long gameTime) {
    }
}
