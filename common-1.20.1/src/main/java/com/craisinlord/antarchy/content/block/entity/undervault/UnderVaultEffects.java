package com.craisinlord.antarchy.content.block.entity.undervault;

import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSounds;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class UnderVaultEffects {
    private static final int DEACTIVATE = 1;
    private static final int EJECT_ITEM = 2;

    private UnderVaultEffects() {
    }

    public static void deactivate(ServerLevel level, BlockPos pos, boolean ominous) {
        level.blockEvent(pos, level.getBlockState(pos).getBlock(), DEACTIVATE, ominous ? 1 : 0);
    }

    public static void ejectItem(ServerLevel level, BlockPos pos) {
        level.blockEvent(pos, level.getBlockState(pos).getBlock(), EJECT_ITEM, 0);
    }

    public static boolean handle(Level level, BlockPos pos, int id, int param) {
        if (!level.isClientSide) {
            return id == DEACTIVATE || id == EJECT_ITEM;
        }
        RandomSource random = level.random;
        if (id == DEACTIVATE) {
            UnderVaultLogic.Client.emitDeactivationParticles(level, pos, param == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
            UndertrialSounds.playLocal(level, pos, UndertrialSounds.VAULT_DEACTIVATE, SoundSource.BLOCKS, 1.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, true);
            return true;
        }
        if (id == EJECT_ITEM) {
            UndertrialSpawnerEffects.addEjectItemParticles(level, pos, random);
            return true;
        }
        return false;
    }
}
