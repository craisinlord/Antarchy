package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class NadirVeilBlock extends Block {
    public static final MapCodec<NadirVeilBlock> CODEC = simpleCodec(NadirVeilBlock::new);

    private static final int GLOW_LIGHT_LEVEL = 6;
    private static final DustParticleOptions DRIP_PARTICLE =
            new DustParticleOptions(new Vector3f(0.63F, 0.26F, 0.92F), 1.0F);

    public NadirVeilBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> GLOW_LIGHT_LEVEL));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;
        double y = pos.getY() + 0.15D + random.nextDouble() * 0.4D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.7D;
        level.addParticle(DRIP_PARTICLE, x, y, z, 0.0D, 0.02D + random.nextDouble() * 0.02D, 0.0D);
    }
}
