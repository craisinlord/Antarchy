package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.UndertrialSpawnerBlock;
import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class UndertrialSpawnerBlockEntity extends BlockEntity implements TrialSpawner.StateAccessor {
    private static final String SPAWNER_TAG = "trial_spawner";
    private static final int EFFECT_DURATION = 3600;
    private TrialSpawner trialSpawner;
    private TrialSpawnerState previousClientState;
    private double clientSpin;
    private double clientOldSpin;

    public UndertrialSpawnerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends net.minecraft.world.level.block.entity.BlockEntityType<UndertrialSpawnerBlockEntity>> type) {
        super(type.get(), pos, state);
        this.trialSpawner = new TrialSpawner(this, net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.EntitySelector.SELECT_FROM_LEVEL);
        this.previousClientState = state.getValue(UndertrialSpawnerBlock.STATE);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        Set<UUID> before = new HashSet<>();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D))) {
            before.add(mob.getUUID());
        }
        blockEntity.trialSpawner.tickServer(level, pos, state.getValue(UndertrialSpawnerBlock.OMINOUS));
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D))) {
            if (!before.contains(mob.getUUID())) {
                blockEntity.applyUndertrialEffects(mob);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        TrialSpawnerState currentState = state.getValue(UndertrialSpawnerBlock.STATE);
        blockEntity.clientOldSpin = blockEntity.clientSpin;
        if (currentState == TrialSpawnerState.ACTIVE || currentState == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
            blockEntity.clientSpin = (blockEntity.clientSpin + 4.0D) % 360.0D;
        }
        if (currentState == TrialSpawnerState.ACTIVE || currentState == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
            if (level.random.nextInt(2) == 0) {
                level.addParticle(blockEntity.particle("undertrial_omen"), pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D,
                        pos.getY() + 0.45D + level.random.nextDouble() * 0.35D,
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D, 0.0D, 0.01D, 0.0D);
            }
        } else if (currentState == TrialSpawnerState.WAITING_FOR_PLAYERS && blockEntity.previousClientState != currentState) {
            for (int i = 0; i < 12; i++) {
                level.addParticle(blockEntity.particle("undertrial_spawner_detection"), pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 1.4D,
                        pos.getY() + 0.15D + level.random.nextDouble() * 0.7D,
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 1.4D, 0.0D, 0.0D, 0.0D);
            }
        }
        blockEntity.previousClientState = currentState;
    }

    private SimpleParticleType particle(String id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, id));
    }

    private void applyUndertrialEffects(Mob mob) {
        mob.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), EFFECT_DURATION, 0, false, true, true));
        int roll = mob.getRandom().nextInt(100);
        if (roll < 30) {
            mob.addEffect(new MobEffectInstance(effect("contracted"), EFFECT_DURATION, 0, false, true, true));
        } else if (roll < 50) {
            mob.addEffect(new MobEffectInstance(effect("shrinking"), EFFECT_DURATION, 0, false, true, true));
        } else if (roll < 65) {
            mob.addEffect(new MobEffectInstance(effect("growth"), EFFECT_DURATION, 0, false, true, true));
        }
    }

    private static net.minecraft.core.Holder<MobEffect> effect(String path) {
        return BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(SPAWNER_TAG)) {
            trialSpawner.codec().parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag.get(SPAWNER_TAG)).result().ifPresent(decoded -> {
                trialSpawner = decoded;
            });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        trialSpawner.codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), trialSpawner).result()
                .ifPresent(encoded -> tag.put(SPAWNER_TAG, encoded));
    }

    @Override
    public void setState(Level level, TrialSpawnerState state) {
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(UndertrialSpawnerBlock.STATE, state));
    }

    @Override
    public TrialSpawnerState getState() {
        return getBlockState().getValue(UndertrialSpawnerBlock.STATE);
    }

    @Override
    public void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public TrialSpawner getTrialSpawner() {
        return trialSpawner;
    }

    public double getClientSpin(float partialTick) {
        return this.clientOldSpin + (this.clientSpin - this.clientOldSpin) * partialTick;
    }
}
