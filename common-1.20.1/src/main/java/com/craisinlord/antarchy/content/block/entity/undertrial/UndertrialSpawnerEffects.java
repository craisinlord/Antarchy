package com.craisinlord.antarchy.content.block.entity.undertrial;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class UndertrialSpawnerEffects {
    public static final int FLAME_NORMAL = 0;
    public static final int FLAME_OMINOUS = 1;
    private static final int SPAWNER_SPAWN = 1;
    private static final int MOB_SPAWN = 2;
    private static final int DETECT_PLAYER = 3;
    private static final int DETECT_PLAYER_OMINOUS = 4;
    private static final int EJECT_ITEM = 5;
    private static final int BECOME_OMINOUS = 6;
    private static final int OFFSET_BITS = 9;
    private static final int OFFSET_MASK = (1 << OFFSET_BITS) - 1;
    private static final int OFFSET_BIAS = 1 << (OFFSET_BITS - 1);
    private static final int FLAG_SHIFT = OFFSET_BITS * 3;

    private UndertrialSpawnerEffects() {
    }

    public static void spawnerSpawn(ServerLevel level, BlockPos spawnerPos, int flame) {
        send(level, spawnerPos, SPAWNER_SPAWN, flame);
    }

    public static void mobSpawn(ServerLevel level, BlockPos spawnerPos, BlockPos mobPos, int flame) {
        send(level, spawnerPos, MOB_SPAWN, packOffset(spawnerPos, mobPos) | (flame << FLAG_SHIFT));
    }

    public static void detectPlayer(ServerLevel level, BlockPos spawnerPos, boolean ominous, int playerCount) {
        send(level, spawnerPos, ominous ? DETECT_PLAYER_OMINOUS : DETECT_PLAYER, playerCount);
    }

    public static void ejectItem(ServerLevel level, BlockPos spawnerPos) {
        send(level, spawnerPos, EJECT_ITEM, 0);
    }

    public static void becomeOminous(ServerLevel level, BlockPos spawnerPos, BlockPos effectPos, int data) {
        send(level, spawnerPos, BECOME_OMINOUS, packOffset(spawnerPos, effectPos) | ((data == 0 ? 0 : 1) << FLAG_SHIFT));
    }

    private static void send(ServerLevel level, BlockPos spawnerPos, int id, int param) {
        BlockState state = level.getBlockState(spawnerPos);
        level.blockEvent(spawnerPos, state.getBlock(), id, param);
    }

    private static int packOffset(BlockPos origin, BlockPos target) {
        int dx = clampOffset(target.getX() - origin.getX());
        int dy = clampOffset(target.getY() - origin.getY());
        int dz = clampOffset(target.getZ() - origin.getZ());
        return (dx + OFFSET_BIAS) | ((dy + OFFSET_BIAS) << OFFSET_BITS) | ((dz + OFFSET_BIAS) << (OFFSET_BITS * 2));
    }

    private static int clampOffset(int value) {
        return Math.max(-OFFSET_BIAS, Math.min(OFFSET_BIAS - 1, value));
    }

    private static BlockPos unpackOffset(BlockPos origin, int param) {
        int dx = (param & OFFSET_MASK) - OFFSET_BIAS;
        int dy = ((param >> OFFSET_BITS) & OFFSET_MASK) - OFFSET_BIAS;
        int dz = ((param >> (OFFSET_BITS * 2)) & OFFSET_MASK) - OFFSET_BIAS;
        return origin.offset(dx, dy, dz);
    }

    public static boolean handle(Level level, BlockPos spawnerPos, int id, int param) {
        if (!level.isClientSide) {
            return id >= SPAWNER_SPAWN && id <= BECOME_OMINOUS;
        }
        RandomSource random = level.random;
        switch (id) {
            case SPAWNER_SPAWN:
                addSpawnParticles(level, spawnerPos, random, flameParticle(param));
                return true;
            case MOB_SPAWN: {
                BlockPos mobPos = unpackOffset(spawnerPos, param);
                UndertrialSounds.playLocal(level, mobPos, UndertrialSounds.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F,
                        (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, true);
                addSpawnParticles(level, mobPos, random, flameParticle((param >> FLAG_SHIFT) & 1));
                return true;
            }
            case DETECT_PLAYER:
            case DETECT_PLAYER_OMINOUS:
                UndertrialSounds.playLocal(level, spawnerPos, UndertrialSounds.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F,
                        (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, true);
                addDetectPlayerParticles(level, spawnerPos, random, param, detectedPlayerParticle(id == DETECT_PLAYER_OMINOUS));
                return true;
            case EJECT_ITEM:
                UndertrialSounds.playLocal(level, spawnerPos, UndertrialSounds.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F,
                        (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, true);
                addEjectItemParticles(level, spawnerPos, random);
                return true;
            case BECOME_OMINOUS: {
                BlockPos effectPos = unpackOffset(spawnerPos, param);
                boolean loud = ((param >> FLAG_SHIFT) & 1) != 0;
                UndertrialSounds.playLocal(level, effectPos, UndertrialSounds.TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundSource.BLOCKS,
                        loud ? 1.0F : 0.3F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, true);
                addDetectPlayerParticles(level, effectPos, random, 0, detectedPlayerParticle(true));
                addBecomeOminousParticles(level, effectPos, random);
                return true;
            }
            default:
                return false;
        }
    }

    private static SimpleParticleType flameParticle(int flame) {
        return flame == FLAME_OMINOUS ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
    }

    private static ParticleOptions detectedPlayerParticle(boolean ominous) {
        SimpleParticleType detection = antarchyParticle("undertrial_spawner_detection");
        if (detection != null) {
            return detection;
        }
        return ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
    }

    private static ParticleOptions omenParticle() {
        SimpleParticleType omen = antarchyParticle("undertrial_omen");
        return omen != null ? omen : ParticleTypes.SOUL;
    }

    private static SimpleParticleType antarchyParticle(String path) {
        return BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(Antarchy.MODID, path)) instanceof SimpleParticleType simple
                ? simple : null;
    }

    public static void addSpawnParticles(Level level, BlockPos pos, RandomSource random, SimpleParticleType flame) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(flame, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void addBecomeOminousParticles(Level level, BlockPos pos, RandomSource random) {
        ParticleOptions omen = omenParticle();
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            double y = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
            double xd = random.nextGaussian() * 0.02D;
            double yd = random.nextGaussian() * 0.02D;
            double zd = random.nextGaussian() * 0.02D;
            level.addParticle(omen, x, y, z, xd, yd, zd);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, xd, yd, zd);
        }
    }

    public static void addDetectPlayerParticles(Level level, BlockPos pos, RandomSource random, int playerCount, ParticleOptions particle) {
        for (int i = 0; i < 30 + Math.min(playerCount, 10) * 5; i++) {
            double dx = (2.0F * random.nextFloat() - 1.0F) * 0.65D;
            double dz = (2.0F * random.nextFloat() - 1.0F) * 0.65D;
            double x = pos.getX() + 0.5D + dx;
            double y = pos.getY() + 0.1D + random.nextFloat() * 0.8D;
            double z = pos.getZ() + 0.5D + dz;
            level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void addEjectItemParticles(Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.4D + random.nextDouble() * 0.2D;
            double y = pos.getY() + 0.4D + random.nextDouble() * 0.2D;
            double z = pos.getZ() + 0.4D + random.nextDouble() * 0.2D;
            double xd = random.nextGaussian() * 0.02D;
            double yd = random.nextGaussian() * 0.02D;
            double zd = random.nextGaussian() * 0.02D;
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, xd, yd, zd * 0.25D);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, xd, yd, zd);
        }
    }
}
