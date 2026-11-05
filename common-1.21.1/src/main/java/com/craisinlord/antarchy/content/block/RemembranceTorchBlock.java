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
    private static final ResourceLocation FLAME_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "remembrance_flame");

    public RemembranceTorchBlock(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(flame(), x, y, z, 0.0D, 0.0D, 0.0D);
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

    static ParticleOptions flame() {
        return BuiltInRegistries.PARTICLE_TYPE.get(FLAME_ID) instanceof ParticleOptions particle ? particle : ParticleTypes.FLAME;
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
