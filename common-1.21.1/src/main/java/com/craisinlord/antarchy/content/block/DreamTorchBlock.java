package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DreamTorchBlock extends TorchBlock {
    private static final ResourceLocation DREAM_FIRE_FLAME_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_fire_flame");

    public DreamTorchBlock(BlockBehaviour.Properties properties) {
        super(ParticleTypes.SOUL_FIRE_FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(getDreamFlameParticle(), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static ParticleOptions getDreamFlameParticle() {
        return BuiltInRegistries.PARTICLE_TYPE.get(DREAM_FIRE_FLAME_ID) instanceof ParticleOptions particleOptions
                ? particleOptions
                : ParticleTypes.SOUL_FIRE_FLAME;
    }
}
