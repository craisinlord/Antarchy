package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DreamWallTorchBlock extends WallTorchBlock {
    private static final ResourceLocation DREAM_FIRE_FLAME_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_fire_flame");

    public DreamWallTorchBlock(BlockBehaviour.Properties properties) {
        super(ParticleTypes.SOUL_FIRE_FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5D + 0.27D * direction.getStepX();
        double y = pos.getY() + 0.92D;
        double z = pos.getZ() + 0.5D + 0.27D * direction.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(getDreamFlameParticle(), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static ParticleOptions getDreamFlameParticle() {
        return BuiltInRegistries.PARTICLE_TYPE.get(DREAM_FIRE_FLAME_ID) instanceof ParticleOptions particleOptions
                ? particleOptions
                : ParticleTypes.SOUL_FIRE_FLAME;
    }
}
