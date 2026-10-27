package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.UndertrialSpawnerBlock;
import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.effect.AnthologyEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class UndertrialSpawnerBlockEntity extends BlockEntity implements TrialSpawner.StateAccessor {
    private static final String SPAWNER_TAG = "trial_spawner";
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int EFFECT_DURATION = 3600;
    private TrialSpawner trialSpawner;
    private TrialSpawnerState previousClientState;
    private double clientSpin;
    private double clientOldSpin;
    private WeightedList<ResourceKey<LootTable>> normalLootTables = WeightedList.of();
    private WeightedList<ResourceKey<LootTable>> ominousLootTables = WeightedList.of();
    private boolean hasNormalLootTablesTag;
    private boolean hasOminousLootTablesTag;

    public UndertrialSpawnerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends net.minecraft.world.level.block.entity.BlockEntityType<UndertrialSpawnerBlockEntity>> type) {
        super(type.get(), pos, state);
        this.trialSpawner = new TrialSpawner(TrialSpawner.FullConfig.DEFAULT, this, net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                net.minecraft.world.level.block.entity.trialspawner.PlayerDetector.EntitySelector.SELECT_FROM_LEVEL);
        this.trialSpawner.overridePeacefulAndMobSpawnRule();
        this.previousClientState = state.getValue(UndertrialSpawnerBlock.STATE);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, UndertrialSpawnerBlockEntity blockEntity) {
        Set<UUID> before = new HashSet<>();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(16.0D))) {
            before.add(mob.getUUID());
        }
        TrialSpawnerState previousState = blockEntity.getState();
        boolean ominous = state.getValue(UndertrialSpawnerBlock.OMINOUS);
        boolean rewardReady = previousState == TrialSpawnerState.EJECTING_REWARD
                && blockEntity.trialSpawner.getStateData().isReadyToEjectItems(level, 30.0F, blockEntity.trialSpawner.getTargetCooldownLength());
        try (UndertrialSpawnContext ignored = UndertrialSpawnContext.enter()) {
            blockEntity.trialSpawner.tickServer(level, pos, ominous);
        }
        if (rewardReady && blockEntity.getState() == TrialSpawnerState.EJECTING_REWARD) {
            blockEntity.ejectReward(level, pos, ominous);
        }
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
            if (level.getRandom().nextInt(2) == 0) {
                level.addParticle(blockEntity.particle("undertrial_omen"), pos.getX() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.7D,
                        pos.getY() + 0.45D + level.getRandom().nextDouble() * 0.35D,
                        pos.getZ() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 0.7D, 0.0D, 0.01D, 0.0D);
            }
        } else if (currentState == TrialSpawnerState.WAITING_FOR_PLAYERS && blockEntity.previousClientState != currentState) {
            Identifier detectionSound = Identifier.fromNamespaceAndPath(Antarchy.MODID,
                    level.getRandom().nextBoolean() ? "undertrial_detect_player1" : "undertrial_detect_player2");
            level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.getValue(detectionSound),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            for (int i = 0; i < 12; i++) {
                level.addParticle(blockEntity.particle("undertrial_spawner_detection"), pos.getX() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 1.4D,
                        pos.getY() + 0.15D + level.getRandom().nextDouble() * 0.7D,
                        pos.getZ() + 0.5D + (level.getRandom().nextDouble() - 0.5D) * 1.4D, 0.0D, 0.0D, 0.0D);
            }
        }
        blockEntity.previousClientState = currentState;
    }

    private SimpleParticleType particle(String id) {
        return (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, id));
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

    private static net.minecraft.core.Holder<MobEffect> effect(String path) {
        return BuiltInRegistries.MOB_EFFECT.getOrThrow(ResourceKey.create(Registries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(Antarchy.MODID, path)));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ValueInput spawnerInput = input.child(SPAWNER_TAG).orElse(input);
        TrialSpawner decoded = new TrialSpawner(TrialSpawner.FullConfig.DEFAULT, this, trialSpawner.getPlayerDetector(), trialSpawner.getEntitySelector());
        decoded.load(spawnerInput);
        captureLootTables(decoded, spawnerInput);
        trialSpawner = createRewardlessSpawner(decoded);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.trialSpawner.getStateData().getUpdateTag(this.getState());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput spawnerOutput = output.child(SPAWNER_TAG);
        TrialSpawnerConfig normalConfig = hasNormalLootTablesTag
                ? withLootTables(trialSpawner.normalConfig(), normalLootTables) : trialSpawner.normalConfig();
        TrialSpawnerConfig ominousConfig = hasOminousLootTablesTag
                ? withLootTables(trialSpawner.ominousConfig(), ominousLootTables) : trialSpawner.ominousConfig();
        spawnerOutput.store(TrialSpawnerStateData.Packed.MAP_CODEC, trialSpawner.getStateData().pack());
        spawnerOutput.store(TrialSpawner.FullConfig.MAP_CODEC, new TrialSpawner.FullConfig(Holder.direct(normalConfig),
                Holder.direct(ominousConfig), trialSpawner.getTargetCooldownLength(), trialSpawner.getRequiredPlayerRange()));
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

    public void setSpawnEntity(EntityType<?> entityType) {
        if (this.level == null) {
            return;
        }
        this.trialSpawner.overrideEntityToSpawn(entityType, this.level);
        this.markUpdated();
    }

    public double getClientSpin(float partialTick) {
        return this.clientOldSpin + (this.clientSpin - this.clientOldSpin) * partialTick;
    }

    private void captureLootTables(TrialSpawner source, ValueInput spawnerInput) {
        normalLootTables = source.normalConfig().lootTablesToEject();
        ominousLootTables = source.ominousConfig().lootTablesToEject();
        hasNormalLootTablesTag = spawnerInput.read("normal_config", CompoundTag.CODEC)
                .map(config -> config.contains("loot_tables_to_eject")).orElse(false);
        hasOminousLootTablesTag = spawnerInput.read("ominous_config", CompoundTag.CODEC)
                .map(config -> config.contains("loot_tables_to_eject")).orElse(false);
    }

    private TrialSpawner createRewardlessSpawner(TrialSpawner source) {
        TrialSpawner rewardless = new TrialSpawner(new TrialSpawner.FullConfig(
                Holder.direct(withoutLootTables(source.normalConfig())), Holder.direct(withoutLootTables(source.ominousConfig())),
                targetCooldownLength(source), source.getRequiredPlayerRange()), this,
                source.getPlayerDetector(), source.getEntitySelector());
        rewardless.getStateData().apply(source.getStateData().pack());
        rewardless.overridePeacefulAndMobSpawnRule();
        return rewardless;
    }

    private static int targetCooldownLength(TrialSpawner source) {
        int targetCooldownLength = source.getTargetCooldownLength();
        return targetCooldownLength > 0 ? targetCooldownLength : DEFAULT_TARGET_COOLDOWN_LENGTH;
    }

    private static TrialSpawnerConfig withoutLootTables(TrialSpawnerConfig config) {
        return new TrialSpawnerConfig(config.spawnRange(), config.totalMobs(), config.simultaneousMobs(),
                config.totalMobsAddedPerPlayer(), config.simultaneousMobsAddedPerPlayer(), config.ticksBetweenSpawn(),
                config.spawnPotentialsDefinition(), WeightedList.of(), config.itemsToDropWhenOminous());
    }

    private static TrialSpawnerConfig withLootTables(TrialSpawnerConfig config, WeightedList<ResourceKey<LootTable>> lootTables) {
        return new TrialSpawnerConfig(config.spawnRange(), config.totalMobs(), config.simultaneousMobs(),
                config.totalMobsAddedPerPlayer(), config.simultaneousMobsAddedPerPlayer(), config.ticksBetweenSpawn(),
                config.spawnPotentialsDefinition(), lootTables, config.itemsToDropWhenOminous());
    }

    private void ejectReward(ServerLevel level, BlockPos pos, boolean ominous) {
        WeightedList<ResourceKey<LootTable>> lootTables = ominous ? ominousLootTables : normalLootTables;
        lootTables.getRandom(level.getRandom()).ifPresent(key -> {
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(key);
            LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
            for (ItemStack stack : lootTable.getRandomItems(params)) {
                DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.DOWN,
                        Vec3.atBottomCenterOf(pos).relative(Direction.DOWN, 1.2D));
            }
            level.levelEvent(3014, pos, 0);
        });
    }
}
