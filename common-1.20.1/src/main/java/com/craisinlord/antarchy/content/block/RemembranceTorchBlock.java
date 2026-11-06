package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.RemembranceTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RemembranceTorchBlock extends TorchBlock implements EntityBlock {
    private static final ResourceLocation FLAME_ID = new ResourceLocation(Antarchy.MODID, "remembrance_flame");
    private static final ResourceLocation MOTE_ID = new ResourceLocation(Antarchy.MODID, "remembrance_mote");

    public RemembranceTorchBlock(BlockBehaviour.Properties properties) {
        super(properties, ParticleTypes.FLAME);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        spawnParticles(level, random, x, y, z);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemembranceTorchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return ticker(level, type);
    }

    static void spawnParticles(Level level, RandomSource random, double x, double y, double z) {
        level.addParticle(particle(FLAME_ID), x, y, z, 0.0D, 0.0D, 0.0D);
        int motes = 1 + random.nextInt(2);
        for (int i = 0; i < motes; i++) {
            double spreadX = (random.nextDouble() - 0.5D) * 0.35D;
            double spreadZ = (random.nextDouble() - 0.5D) * 0.35D;
            level.addParticle(particle(MOTE_ID), x + spreadX, y + random.nextDouble() * 0.2D, z + spreadZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private static ParticleOptions particle(ResourceLocation id) {
        return BuiltInRegistries.PARTICLE_TYPE.get(id) instanceof ParticleOptions particle ? particle : ParticleTypes.FLAME;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> BlockEntityTicker<T> ticker(Level level, BlockEntityType<T> type) {
        if (level.isClientSide || type != AntarchyObjects.REMEMBRANCE_TORCH_BLOCK_ENTITY.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<RemembranceTorchBlockEntity>) (tickLevel, tickPos, tickState, torch) -> {
            if (tickLevel instanceof ServerLevel serverLevel) {
                RemembranceTorchBlockEntity.serverTick(serverLevel, tickPos, torch);
            }
        };
    }
}
