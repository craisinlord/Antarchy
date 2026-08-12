package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DreamCeilingTorchBlock extends TorchBlock {
    private static final ResourceLocation DREAM_FIRE_FLAME_ID = new ResourceLocation(Antarchy.MODID, "dream_fire_flame");
    private static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);

    public DreamCeilingTorchBlock(BlockBehaviour.Properties properties) {
        super(properties, ParticleTypes.SOUL_FIRE_FLAME);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.3D;
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
