package com.craisinlord.antarchy.content.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;

public class LumenLiquidBlock extends LiquidBlock {
    private static final int SOURCE_PARTICLE_INTERVAL = 8;
    private static final int FLOW_PARTICLE_INTERVAL = 20;

    public LumenLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        scheduleCurrentLumenTick(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        scheduleCurrentLumenTick(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickAccess.scheduleTick(pos, this.fluid, this.fluid.getTickDelay(level));
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        scheduleDependentLumenTicks(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!isSurfaceExposed(level, pos)) {
            return;
        }

        int chance = state.getValue(LEVEL) == 0 ? SOURCE_PARTICLE_INTERVAL : FLOW_PARTICLE_INTERVAL;
        if (random.nextInt(chance) != 0) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 0.76D + random.nextDouble() * 0.18D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0D, 0.02D, 0.0D);

        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.ENCHANT, x, y + 0.02D, z, 0.0D, 0.015D, 0.0D);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (!level.isClientSide() || !(entity instanceof LivingEntity) || !isSurfaceExposed(level, pos)) {
            return;
        }

        RandomSource random = level.getRandom();
        if (random.nextInt(10) != 0) {
            return;
        }

        double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
        double y = entity.getY() + 0.1D + random.nextDouble() * Math.max(0.2D, entity.getBbHeight() * 0.2D);
        double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
        level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0D, 0.03D, 0.0D);
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.ENCHANT, x, y + 0.05D, z, 0.0D, 0.04D, 0.0D);
        }
    }

    private static boolean isSurfaceExposed(Level level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() || !above.canOcclude();
    }

    private void scheduleCurrentLumenTick(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos, this.fluid, this.fluid.getTickDelay(level));
    }

    private void scheduleDependentLumenTicks(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos.above(), this.fluid, this.fluid.getTickDelay(level));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.scheduleTick(pos.relative(direction), this.fluid, this.fluid.getTickDelay(level));
        }
    }
}
