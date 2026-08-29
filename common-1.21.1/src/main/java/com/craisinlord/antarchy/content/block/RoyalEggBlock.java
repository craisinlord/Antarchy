package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.entity.RoyalEggBlockEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public abstract class RoyalEggBlock extends BaseEntityBlock {
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    private static final int MAX_HATCH = 2;

    protected RoyalEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
    }

    protected abstract EntityType<? extends RoyalMountEntity> mountType();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RoyalEggBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof RoyalEggBlockEntity egg) {
            egg.setPlacerUuid(player.getUUID());
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            this.teleport(state, serverLevel, pos);
        }
    }

    private void teleport(BlockState state, ServerLevel level, BlockPos pos) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < 32; attempt++) {
            BlockPos target = pos.offset(
                    random.nextInt(16) - random.nextInt(16),
                    random.nextInt(8) - random.nextInt(8),
                    random.nextInt(16) - random.nextInt(16));
            if (level.getBlockState(target).isAir() && target.getY() > level.getMinBuildHeight()) {
                RoyalEggBlockEntity source = level.getBlockEntity(pos) instanceof RoyalEggBlockEntity be ? be : null;
                UUID placer = source == null ? null : source.getPlacerUuid();
                int hatch = state.getValue(HATCH);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(target, state.setValue(HATCH, Math.max(0, hatch - 1)), 2);
                if (level.getBlockEntity(target) instanceof RoyalEggBlockEntity moved) {
                    moved.setPlacerUuid(placer);
                }
                level.levelEvent(2003, pos, 0);
                level.playSound(null, pos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.BLOCKS, 0.6F, 1.4F);
                return;
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(Math.max(1, AntarchySettings.royalEggHatchChance())) != 0) {
            return;
        }
        int hatch = state.getValue(HATCH);
        if (hatch < MAX_HATCH) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
            level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            return;
        }
        this.hatch(level, pos);
    }

    private void hatch(ServerLevel level, BlockPos pos) {
        UUID placer = level.getBlockEntity(pos) instanceof RoyalEggBlockEntity be ? be.getPlacerUuid() : null;
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.9F, 1.0F);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        RoyalMountEntity mount = this.mountType().create(level);
        if (mount == null) {
            return;
        }
        mount.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mount.setBaby(true);
        mount.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.BREEDING, null);
        if (placer != null) {
            mount.setTame(true, true);
            mount.setOwnerUUID(placer);
            if (level.getPlayerByUUID(placer) instanceof ServerPlayer owner) {
                mount.tame(owner);
            }
        }
        level.addFreshEntity(mount);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 24, 0.4D, 0.4D, 0.4D, 0.0D);
    }
}
