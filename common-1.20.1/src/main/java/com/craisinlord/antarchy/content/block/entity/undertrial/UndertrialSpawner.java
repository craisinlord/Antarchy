package com.craisinlord.antarchy.content.block.entity.undertrial;

import com.craisinlord.antarchy.content.block.UndertrialSpawnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UndertrialSpawner {
    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    public static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    public static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(MAX_MOB_TRACKING_DISTANCE);
    private final UndertrialSpawnerConfig normalConfig;
    private final UndertrialSpawnerConfig ominousConfig;
    private final UndertrialSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    private final StateAccessor stateAccessor;
    private final UndertrialPlayerDetector playerDetector;
    private final UndertrialPlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public UndertrialSpawner(StateAccessor stateAccessor, UndertrialPlayerDetector playerDetector,
                             UndertrialPlayerDetector.EntitySelector entitySelector) {
        this(UndertrialSpawnerConfig.DEFAULT, UndertrialSpawnerConfig.DEFAULT, new UndertrialSpawnerData(),
                DEFAULT_TARGET_COOLDOWN_LENGTH, DEFAULT_PLAYER_SCAN_RANGE, stateAccessor, playerDetector, entitySelector);
    }

    public UndertrialSpawner(UndertrialSpawnerConfig normalConfig, UndertrialSpawnerConfig ominousConfig, UndertrialSpawnerData data,
                             int targetCooldownLength, int requiredPlayerRange, StateAccessor stateAccessor,
                             UndertrialPlayerDetector playerDetector, UndertrialPlayerDetector.EntitySelector entitySelector) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.data = data;
        this.targetCooldownLength = targetCooldownLength;
        this.requiredPlayerRange = requiredPlayerRange;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public UndertrialSpawner load(CompoundTag tag) {
        UndertrialSpawnerConfig normal = tag.contains(NORMAL_CONFIG_TAG_NAME, Tag.TAG_COMPOUND)
                ? UndertrialSpawnerConfig.load(tag.getCompound(NORMAL_CONFIG_TAG_NAME)) : UndertrialSpawnerConfig.DEFAULT;
        UndertrialSpawnerConfig ominous = tag.contains(OMINOUS_CONFIG_TAG_NAME, Tag.TAG_COMPOUND)
                ? UndertrialSpawnerConfig.load(tag.getCompound(OMINOUS_CONFIG_TAG_NAME)) : UndertrialSpawnerConfig.DEFAULT;
        UndertrialSpawnerData loadedData = UndertrialSpawnerData.load(tag);
        int cooldown = DEFAULT_TARGET_COOLDOWN_LENGTH;
        if (tag.contains("target_cooldown_length", Tag.TAG_ANY_NUMERIC) && tag.getInt("target_cooldown_length") >= 0) {
            cooldown = tag.getInt("target_cooldown_length");
        }
        int range = DEFAULT_PLAYER_SCAN_RANGE;
        if (tag.contains("required_player_range", Tag.TAG_ANY_NUMERIC)) {
            int value = tag.getInt("required_player_range");
            if (value >= 1 && value <= 128) {
                range = value;
            }
        }
        return new UndertrialSpawner(normal, ominous, loadedData, cooldown, range, this.stateAccessor, this.playerDetector, this.entitySelector);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.normalConfig.equals(UndertrialSpawnerConfig.DEFAULT)) {
            tag.put(NORMAL_CONFIG_TAG_NAME, this.normalConfig.save());
        }
        UndertrialSpawnerConfig ominous = this.getOminousConfigForSerialization();
        if (!ominous.equals(UndertrialSpawnerConfig.DEFAULT)) {
            tag.put(OMINOUS_CONFIG_TAG_NAME, ominous.save());
        }
        this.data.save(tag);
        if (this.targetCooldownLength != DEFAULT_TARGET_COOLDOWN_LENGTH) {
            tag.putInt("target_cooldown_length", this.targetCooldownLength);
        }
        if (this.requiredPlayerRange != DEFAULT_PLAYER_SCAN_RANGE) {
            tag.putInt("required_player_range", this.requiredPlayerRange);
        }
        return tag;
    }

    public UndertrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    public UndertrialSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    public UndertrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private UndertrialSpawnerConfig getOminousConfigForSerialization() {
        return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : UndertrialSpawnerConfig.DEFAULT;
    }

    public void applyOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(UndertrialSpawnerBlock.OMINOUS, true), 3);
        UndertrialSpawnerEffects.becomeOminous(level, pos, pos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, level, pos);
    }

    public void removeOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(UndertrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public UndertrialSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public UndertrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level level, UndertrialSpawnerState state) {
        this.stateAccessor.setState(level, state);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public UndertrialPlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public UndertrialPlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(Level level) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
    }

    public Optional<UUID> spawnMob(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        UndertrialSpawnData spawnData = this.data.getOrCreateNextSpawnData(this, level.getRandom());
        CompoundTag entityTag = spawnData.entityToSpawn();
        ListTag posList = entityTag.getList("Pos", Tag.TAG_DOUBLE);
        Optional<EntityType<?>> type = EntityType.by(entityTag);
        if (type.isEmpty()) {
            return Optional.empty();
        }
        int size = posList.size();
        double x = size >= 1 ? posList.getDouble(0)
                : pos.getX() + (random.nextDouble() - random.nextDouble()) * this.getConfig().spawnRange() + 0.5D;
        double y = size >= 2 ? posList.getDouble(1) : pos.getY() + random.nextInt(3) - 1;
        double z = size >= 3 ? posList.getDouble(2)
                : pos.getZ() + (random.nextDouble() - random.nextDouble()) * this.getConfig().spawnRange() + 0.5D;
        if (!level.noCollision(type.get().getAABB(x, y, z))) {
            return Optional.empty();
        }
        Vec3 spawnPos = new Vec3(x, y, z);
        if (!UndertrialPlayerDetector.inLineOfSight(level, pos.getCenter(), spawnPos)) {
            return Optional.empty();
        }
        BlockPos spawnBlockPos = BlockPos.containing(spawnPos);
        if (!SpawnPlacements.checkSpawnRules(type.get(), level, MobSpawnType.SPAWNER, spawnBlockPos, level.getRandom())) {
            return Optional.empty();
        }
        if (spawnData.getCustomSpawnRules().isPresent()
                && !UndertrialSpawnData.isValidPosition(spawnData.getCustomSpawnRules().get(), spawnBlockPos, level)) {
            return Optional.empty();
        }
        Entity entity = EntityType.loadEntityRecursive(entityTag, level, loaded -> {
            loaded.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            return loaded;
        });
        if (entity == null) {
            return Optional.empty();
        }
        if (entity instanceof Mob mob) {
            if (!mob.checkSpawnObstruction(level)) {
                return Optional.empty();
            }
            boolean onlyId = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id", Tag.TAG_STRING);
            if (onlyId) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, null);
            }
            mob.setPersistenceRequired();
            spawnData.getEquipment().ifPresent(table -> table.equip(mob));
        }
        if (!level.tryAddFreshEntityWithPassengers(entity)) {
            return Optional.empty();
        }
        int flame = this.isOminous ? UndertrialSpawnerEffects.FLAME_OMINOUS : UndertrialSpawnerEffects.FLAME_NORMAL;
        UndertrialSpawnerEffects.spawnerSpawn(level, pos, flame);
        UndertrialSpawnerEffects.mobSpawn(level, pos, spawnBlockPos, flame);
        level.gameEvent(entity, GameEvent.ENTITY_PLACE, spawnBlockPos);
        return Optional.of(entity.getUUID());
    }

    public void ejectReward(ServerLevel level, BlockPos pos, ResourceLocation lootTableId) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        List<ItemStack> items = lootTable.getRandomItems(params);
        if (!items.isEmpty()) {
            for (ItemStack stack : items) {
                DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2D));
            }
            UndertrialSpawnerEffects.ejectItem(level, pos);
        }
    }

    public void tickServer(ServerLevel level, BlockPos pos, boolean ominous) {
        this.isOminous = ominous;
        UndertrialSpawnerState state = this.getState();
        if (this.data.currentMobs.removeIf(uuid -> shouldMobBeUntracked(level, pos, uuid))) {
            this.data.nextMobSpawnsAt = level.getGameTime() + this.getConfig().ticksBetweenSpawn();
        }
        UndertrialSpawnerState next = state.tickAndGetNext(pos, this, level);
        if (next != state) {
            this.setState(level, next);
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel level, BlockPos pos, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity == null
                || !entity.isAlive()
                || !entity.level().dimension().equals(level.dimension())
                || entity.blockPosition().distSqr(pos) > MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public interface StateAccessor {
        void setState(Level level, UndertrialSpawnerState state);

        UndertrialSpawnerState getState();

        void markUpdated();
    }
}
