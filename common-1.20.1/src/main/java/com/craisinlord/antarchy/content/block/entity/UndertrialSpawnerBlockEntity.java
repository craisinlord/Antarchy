package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.UndertrialSpawnerBlock;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialPlayerDetector;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawner;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerConfig;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerEffects;
import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialSpawnerState;
import com.craisinlord.anthology.effect.AnthologyEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class UndertrialSpawnerBlockEntity extends BlockEntity implements UndertrialSpawner.StateAccessor {
    private static final String SPAWNER_TAG = "trial_spawner";
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int EFFECT_DURATION = 3600;
    private UndertrialSpawner trialSpawner;
    private UndertrialSpawnerState previousClientState;
    private double clientSpin;
    private double clientOldSpin;
    private SimpleWeightedRandomList<ResourceLocation> normalLootTables = SimpleWeightedRandomList.empty();
    private SimpleWeightedRandomList<ResourceLocation> ominousLootTables = SimpleWeightedRandomList.empty();
    private Tag normalLootTablesTag;
    private Tag ominousLootTablesTag;

    public UndertrialSpawnerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<UndertrialSpawnerBlockEntity>> type) {
        super(type.get(), pos, state);
        this.trialSpawner = new UndertrialSpawner(this, UndertrialPlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                UndertrialPlayerDetector.EntitySelector.SELECT_FROM_LEVEL);
        this.trialSpawner.overridePeacefulAndMobSpawnRule();
        this.previousClientState = state.getValue(UndertrialSpawnerBlock.STATE);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        Set<UUID> before = new HashSet<>();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D))) {
            before.add(mob.getUUID());
        }
        UndertrialSpawnerState previousState = blockEntity.getState();
        boolean ominous = state.getValue(UndertrialSpawnerBlock.OMINOUS);
        boolean rewardReady = previousState == UndertrialSpawnerState.EJECTING_REWARD
                && blockEntity.trialSpawner.getData().isReadyToEjectItems(level, 30.0F, blockEntity.trialSpawner.getTargetCooldownLength());
        try (UndertrialSpawnContext ignored = UndertrialSpawnContext.enter()) {
            blockEntity.trialSpawner.tickServer(level, pos, ominous);
        }
        if (rewardReady && blockEntity.getState() == UndertrialSpawnerState.EJECTING_REWARD) {
            blockEntity.ejectReward(level, pos, ominous);
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D))) {
            if (!before.contains(mob.getUUID())) {
                blockEntity.applyUndertrialEffects(mob);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        UndertrialSpawnerState currentState = state.getValue(UndertrialSpawnerBlock.STATE);
        blockEntity.clientOldSpin = blockEntity.clientSpin;
        if (currentState == UndertrialSpawnerState.ACTIVE || currentState == UndertrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
            blockEntity.clientSpin = (blockEntity.clientSpin + 4.0D) % 360.0D;
        }
        if (currentState == UndertrialSpawnerState.ACTIVE || currentState == UndertrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
            if (level.random.nextInt(2) == 0) {
                level.addParticle(blockEntity.particle("undertrial_omen"), pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D,
                        pos.getY() + 0.45D + level.random.nextDouble() * 0.35D,
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.7D, 0.0D, 0.01D, 0.0D);
            }
        } else if (currentState == UndertrialSpawnerState.WAITING_FOR_PLAYERS && blockEntity.previousClientState != currentState) {
            ResourceLocation detectionSound = new ResourceLocation(Antarchy.MODID,
                    level.random.nextBoolean() ? "undertrial_detect_player1" : "undertrial_detect_player2");
            level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.get(detectionSound),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            for (int i = 0; i < 12; i++) {
                level.addParticle(blockEntity.particle("undertrial_spawner_detection"), pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 1.4D,
                        pos.getY() + 0.15D + level.random.nextDouble() * 0.7D,
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 1.4D, 0.0D, 0.0D, 0.0D);
            }
        }
        blockEntity.previousClientState = currentState;
    }

    private SimpleParticleType particle(String id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(Antarchy.MODID, id));
    }

    private void applyUndertrialEffects(Mob mob) {
        mob.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), EFFECT_DURATION, 0, false, true, true));
        int roll = mob.getRandom().nextInt(100);
        if (roll < 30) {
            mob.addEffect(new MobEffectInstance(AnthologyEffects.contracted(), EFFECT_DURATION, 0, false, true, true));
        } else if (roll < 50) {
            mob.addEffect(new MobEffectInstance(effect("shrinking"), EFFECT_DURATION, 0, false, true, true));
        } else if (roll < 65) {
            mob.addEffect(new MobEffectInstance(effect("growth"), EFFECT_DURATION, 0, false, true, true));
        }
    }

    private static MobEffect effect(String path) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(Antarchy.MODID, path));
        if (effect == null) {
            throw new IllegalStateException("Missing mob effect " + Antarchy.MODID + ":" + path);
        }
        return effect;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Tag spawnerTag = tag.contains(SPAWNER_TAG) ? tag.get(SPAWNER_TAG) : tag;
        if (spawnerTag instanceof CompoundTag compound) {
            UndertrialSpawner decoded = trialSpawner.load(compound);
            captureLootTables(decoded, spawnerTag);
            trialSpawner = createRewardlessSpawner(decoded);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag spawnerTag = trialSpawner.save();
        if (normalLootTablesTag != null) {
            spawnerTag.getCompound(UndertrialSpawner.NORMAL_CONFIG_TAG_NAME).put("loot_tables_to_eject", normalLootTablesTag.copy());
        }
        if (ominousLootTablesTag != null) {
            spawnerTag.getCompound(UndertrialSpawner.OMINOUS_CONFIG_TAG_NAME).put("loot_tables_to_eject", ominousLootTablesTag.copy());
        }
        tag.put(SPAWNER_TAG, spawnerTag);
    }

    @Override
    public void setState(Level level, UndertrialSpawnerState state) {
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(UndertrialSpawnerBlock.STATE, state));
    }

    @Override
    public UndertrialSpawnerState getState() {
        return getBlockState().getValue(UndertrialSpawnerBlock.STATE);
    }

    @Override
    public void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UndertrialSpawner getTrialSpawner() {
        return trialSpawner;
    }

    public void setSpawnEntity(EntityType<?> entityType) {
        if (this.level == null) {
            return;
        }
        this.trialSpawner.getData().setEntityId(this.trialSpawner, this.level.random, entityType);
        this.markUpdated();
    }

    public double getClientSpin(float partialTick) {
        return this.clientOldSpin + (this.clientSpin - this.clientOldSpin) * partialTick;
    }

    private void captureLootTables(UndertrialSpawner source, Tag spawnerTag) {
        normalLootTables = source.getNormalConfig().lootTablesToEject();
        ominousLootTables = source.getOminousConfig().lootTablesToEject();
        if (spawnerTag instanceof CompoundTag compound) {
            CompoundTag normalConfig = compound.getCompound(UndertrialSpawner.NORMAL_CONFIG_TAG_NAME);
            CompoundTag ominousConfig = compound.getCompound(UndertrialSpawner.OMINOUS_CONFIG_TAG_NAME);
            normalLootTablesTag = normalConfig.get("loot_tables_to_eject");
            ominousLootTablesTag = ominousConfig.get("loot_tables_to_eject");
        }
    }

    private UndertrialSpawner createRewardlessSpawner(UndertrialSpawner source) {
        UndertrialSpawner rewardless = new UndertrialSpawner(withoutLootTables(source.getNormalConfig()), withoutLootTables(source.getOminousConfig()),
                source.getData(), targetCooldownLength(source), source.getRequiredPlayerRange(), this,
                source.getPlayerDetector(), source.getEntitySelector());
        rewardless.overridePeacefulAndMobSpawnRule();
        return rewardless;
    }

    private static int targetCooldownLength(UndertrialSpawner source) {
        int targetCooldownLength = source.getTargetCooldownLength();
        return targetCooldownLength > 0 ? targetCooldownLength : DEFAULT_TARGET_COOLDOWN_LENGTH;
    }

    private static UndertrialSpawnerConfig withoutLootTables(UndertrialSpawnerConfig config) {
        return new UndertrialSpawnerConfig(config.spawnRange(), config.totalMobs(), config.simultaneousMobs(),
                config.totalMobsAddedPerPlayer(), config.simultaneousMobsAddedPerPlayer(), config.ticksBetweenSpawn(),
                config.spawnPotentialsDefinition(), SimpleWeightedRandomList.empty(), config.itemsToDropWhenOminous());
    }

    private void ejectReward(ServerLevel level, BlockPos pos, boolean ominous) {
        SimpleWeightedRandomList<ResourceLocation> lootTables = ominous ? ominousLootTables : normalLootTables;
        lootTables.getRandomValue(level.getRandom()).ifPresent(key -> {
            LootTable lootTable = level.getServer().getLootData().getLootTable(key);
            LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
            for (ItemStack stack : lootTable.getRandomItems(params)) {
                DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.DOWN,
                        Vec3.atBottomCenterOf(pos).relative(Direction.DOWN, 1.2D));
            }
            UndertrialSpawnerEffects.ejectItem(level, pos);
        });
    }
}
