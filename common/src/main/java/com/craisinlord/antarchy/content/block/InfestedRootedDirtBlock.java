package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InfestedRootedDirtBlock extends Block {
    private final MapCodec<InfestedRootedDirtBlock> codec;

    public InfestedRootedDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.codec = Block.simpleCodec(ignored -> this);
    }

    @Override
    public MapCodec<InfestedRootedDirtBlock> codec() {
        return this.codec;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.35F) {
            this.tryReleaseMoleworm(level, pos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof Player && level.random.nextFloat() < 0.22F) {
            this.tryReleaseMoleworm((ServerLevel) level, pos);
        }
    }

    private void tryReleaseMoleworm(ServerLevel level, BlockPos pos) {
        Player nearbyPlayer = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 4.5D, false);
        if (nearbyPlayer == null) {
            return;
        }

        BlockPos emergencePos = findEmergencePos(level, pos);
        if (emergencePos == null) {
            return;
        }

        if (!(AntarchyObjects.MOLEWORM.get().create(level) instanceof MolewormEntity moleworm)) {
            return;
        }

        moleworm.moveTo(emergencePos.getX() + 0.5D, emergencePos.getY() + 0.05D, emergencePos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(moleworm)) {
            moleworm.discard();
            return;
        }

        level.setBlock(pos, net.minecraft.world.level.block.Blocks.ROOTED_DIRT.defaultBlockState(), 3);
        level.addFreshEntity(moleworm);
        level.playSound(null, emergencePos, SoundEvents.SILVERFISH_STEP, net.minecraft.sounds.SoundSource.BLOCKS, 0.45F, 0.8F + level.random.nextFloat() * 0.25F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D, 8, 0.18D, 0.08D, 0.18D, 0.01D);
    }

    @Nullable
    private static BlockPos findEmergencePos(BlockGetter level, BlockPos origin) {
        BlockPos[] candidates = new BlockPos[] {
                origin.above(),
                origin.north(),
                origin.south(),
                origin.east(),
                origin.west(),
                origin.north().above(),
                origin.south().above(),
                origin.east().above(),
                origin.west().above()
        };

        for (BlockPos candidate : candidates) {
            if (isOpenStandingSpot(level, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isOpenStandingSpot(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        return state.isAir()
                && above.isAir()
                && below.isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
