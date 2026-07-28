package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

public class BluestoneOreBlock extends RedStoneOreBlock {
    private static final DustParticleOptions BLUESTONE_PARTICLE = new DustParticleOptions(new Vector3f(0.18F, 0.54F, 1.0F), 1.0F);

    public BluestoneOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        activate(state, level, pos);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        activate(state, level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        activate(state, level, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BlockStateProperties.LIT)) {
            spawnBluestoneParticles(level, pos);
        }
    }

    private static void activate(BlockState state, Level level, BlockPos pos) {
        spawnBluestoneParticles(level, pos);
        if (!state.getValue(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
        }
    }

    private static void spawnBluestoneParticles(Level level, BlockPos pos) {
        RandomSource random = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) {
                continue;
            }
            Direction.Axis axis = direction.getAxis();
            double x = axis == Direction.Axis.X ? 0.5D + 0.5625D * direction.getStepX() : random.nextFloat();
            double y = axis == Direction.Axis.Y ? 0.5D + 0.5625D * direction.getStepY() : random.nextFloat();
            double z = axis == Direction.Axis.Z ? 0.5D + 0.5625D * direction.getStepZ() : random.nextFloat();
            level.addParticle(BLUESTONE_PARTICLE, pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0.0D, 0.0D, 0.0D);
        }
    }
}
