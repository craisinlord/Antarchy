package com.craisinlord.antarchy.content.block.entity.undertrial;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class UndertrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<UndertrialSpawnData> nextSpawnData;
    protected Optional<ResourceLocation> ejectingLootTable;
    protected Entity displayEntity;
    private SimpleWeightedRandomList<ItemStack> dispensing;

    public UndertrialSpawnerData() {
        this(Set.of(), Set.of(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public UndertrialSpawnerData(Set<UUID> detectedPlayers, Set<UUID> currentMobs, long cooldownEndsAt, long nextMobSpawnsAt,
                                 int totalMobsSpawned, Optional<UndertrialSpawnData> nextSpawnData,
                                 Optional<ResourceLocation> ejectingLootTable) {
        this.detectedPlayers.addAll(detectedPlayers);
        this.currentMobs.addAll(currentMobs);
        this.cooldownEndsAt = cooldownEndsAt;
        this.nextMobSpawnsAt = nextMobSpawnsAt;
        this.totalMobsSpawned = totalMobsSpawned;
        this.nextSpawnData = nextSpawnData;
        this.ejectingLootTable = ejectingLootTable;
    }

    public static UndertrialSpawnerData load(CompoundTag tag) {
        Set<UUID> detected = loadUuidSet(tag.get("registered_players"));
        Set<UUID> mobs = loadUuidSet(tag.get("current_mobs"));
        long cooldownEndsAt = tag.contains("cooldown_ends_at", Tag.TAG_ANY_NUMERIC) ? tag.getLong("cooldown_ends_at") : 0L;
        long nextMobSpawnsAt = tag.contains(TAG_NEXT_MOB_SPAWNS_AT, Tag.TAG_ANY_NUMERIC) ? tag.getLong(TAG_NEXT_MOB_SPAWNS_AT) : 0L;
        int totalMobsSpawned = tag.contains("total_mobs_spawned", Tag.TAG_ANY_NUMERIC) && tag.getInt("total_mobs_spawned") >= 0
                ? tag.getInt("total_mobs_spawned") : 0;
        Optional<UndertrialSpawnData> spawnData = tag.contains(TAG_SPAWN_DATA)
                ? UndertrialSpawnData.load(tag.get(TAG_SPAWN_DATA)) : Optional.empty();
        Optional<ResourceLocation> ejecting = tag.contains("ejecting_loot_table", Tag.TAG_STRING)
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("ejecting_loot_table"))) : Optional.empty();
        return new UndertrialSpawnerData(detected, mobs, cooldownEndsAt, nextMobSpawnsAt, totalMobsSpawned, spawnData, ejecting);
    }

    public void save(CompoundTag tag) {
        if (!this.detectedPlayers.isEmpty()) {
            tag.put("registered_players", saveUuidSet(this.detectedPlayers));
        }
        if (!this.currentMobs.isEmpty()) {
            tag.put("current_mobs", saveUuidSet(this.currentMobs));
        }
        if (this.cooldownEndsAt != 0L) {
            tag.putLong("cooldown_ends_at", this.cooldownEndsAt);
        }
        if (this.nextMobSpawnsAt != 0L) {
            tag.putLong(TAG_NEXT_MOB_SPAWNS_AT, this.nextMobSpawnsAt);
        }
        if (this.totalMobsSpawned != 0) {
            tag.putInt("total_mobs_spawned", this.totalMobsSpawned);
        }
        this.nextSpawnData.ifPresent(data -> tag.put(TAG_SPAWN_DATA, data.save()));
        this.ejectingLootTable.ifPresent(id -> tag.putString("ejecting_loot_table", id.toString()));
    }

    private static Set<UUID> loadUuidSet(Tag tag) {
        Set<UUID> result = new HashSet<>();
        if (!(tag instanceof ListTag list)) {
            return result;
        }
        for (Tag element : list) {
            if (!(element instanceof IntArrayTag array) || array.size() != 4) {
                return new HashSet<>();
            }
            result.add(NbtUtils.loadUUID(array));
        }
        return result;
    }

    private static ListTag saveUuidSet(Set<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) {
            list.add(NbtUtils.createUUID(uuid));
        }
        return list;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(UndertrialSpawner spawner, RandomSource random) {
        boolean hasId = this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn().contains("id", Tag.TAG_STRING);
        return hasId || !spawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(UndertrialSpawnerConfig config, int additionalPlayers) {
        return this.totalMobsSpawned >= config.calculateTargetTotalMobs(additionalPlayers);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel level, UndertrialSpawnerConfig config, int additionalPlayers) {
        return level.getGameTime() >= this.nextMobSpawnsAt
                && this.currentMobs.size() < config.calculateTargetSimultaneousMobs(additionalPlayers);
    }

    public int countAdditionalPlayers(BlockPos pos) {
        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel level, BlockPos pos, UndertrialSpawner spawner) {
        if ((pos.asLong() + level.getGameTime()) % DELAY_BETWEEN_PLAYER_SCANS != 0L) {
            return;
        }
        if (spawner.getState().equals(UndertrialSpawnerState.COOLDOWN) && spawner.isOminous()) {
            return;
        }
        List<UUID> players = spawner.getPlayerDetector()
                .detect(level, spawner.getEntitySelector(), pos, spawner.getRequiredPlayerRange(), true);
        boolean becameOminous;
        if (!spawner.isOminous() && !players.isEmpty()) {
            Optional<Pair<Player, Boolean>> omenPlayer = findPlayerWithOminousEffect(level, players);
            omenPlayer.ifPresent(pair -> {
                Player player = pair.getFirst();
                if (pair.getSecond()) {
                    UndertrialOmen.transformBadOmenIntoTrialOmen(player);
                }
                UndertrialSpawnerEffects.becomeOminous(level, pos, BlockPos.containing(player.getEyePosition()), 0);
                spawner.applyOminous(level, pos);
            });
            becameOminous = omenPlayer.isPresent();
        } else {
            becameOminous = false;
        }
        if (!spawner.getState().equals(UndertrialSpawnerState.COOLDOWN) || becameOminous) {
            boolean firstDetection = spawner.getData().detectedPlayers.isEmpty();
            List<UUID> newPlayers = firstDetection ? players
                    : spawner.getPlayerDetector().detect(level, spawner.getEntitySelector(), pos, spawner.getRequiredPlayerRange(), false);
            if (this.detectedPlayers.addAll(newPlayers)) {
                this.nextMobSpawnsAt = Math.max(level.getGameTime() + UndertrialSpawner.DETECT_PLAYER_SPAWN_BUFFER, this.nextMobSpawnsAt);
                if (!becameOminous) {
                    UndertrialSpawnerEffects.detectPlayer(level, pos, spawner.isOminous(), this.detectedPlayers.size());
                }
            }
        }
    }

    private static Optional<Pair<Player, Boolean>> findPlayerWithOminousEffect(ServerLevel level, List<UUID> players) {
        Player badOmenPlayer = null;
        for (UUID uuid : players) {
            Player player = level.getPlayerByUUID(uuid);
            if (player != null) {
                if (UndertrialOmen.hasTrialOmen(player)) {
                    return Optional.of(Pair.of(player, false));
                }
                if (UndertrialOmen.hasBadOmen(player)) {
                    badOmenPlayer = player;
                }
            }
        }
        return Optional.ofNullable(badOmenPlayer).map(player -> Pair.of(player, true));
    }

    public void resetAfterBecomingOminous(UndertrialSpawner spawner, ServerLevel level, BlockPos spawnerPos) {
        for (UUID uuid : this.currentMobs) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                UndertrialSpawnerEffects.mobSpawn(level, spawnerPos, entity.blockPosition(), UndertrialSpawnerEffects.FLAME_NORMAL);
                if (entity instanceof Mob mob) {
                    dropPreservedEquipment(mob);
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        if (!spawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }
        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenSpawn();
        spawner.markUpdated();
        this.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void dropPreservedEquipment(Mob mob) {
        CompoundTag saved = mob.saveWithoutId(new CompoundTag());
        ListTag hand = saved.getList("HandDropChances", Tag.TAG_FLOAT);
        ListTag armor = saved.getList("ArmorDropChances", Tag.TAG_FLOAT);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ListTag chances = slot.getType() == EquipmentSlot.Type.HAND ? hand : armor;
            double chance = slot.getIndex() < chances.size() ? chances.getFloat(slot.getIndex()) : 0.0D;
            if (chance > 1.0D) {
                mob.setItemSlot(slot, ItemStack.EMPTY);
                mob.spawnAtLocation(stack);
            }
        }
    }

    public boolean isReadyToOpenShutter(ServerLevel level, float delay, int targetCooldownLength) {
        long cooldownStartedAt = this.cooldownEndsAt - targetCooldownLength;
        return (float) level.getGameTime() >= (float) cooldownStartedAt + delay;
    }

    public boolean isReadyToEjectItems(ServerLevel level, float interval, int targetCooldownLength) {
        long cooldownStartedAt = this.cooldownEndsAt - targetCooldownLength;
        return (float) (level.getGameTime() - cooldownStartedAt) % interval == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel level) {
        return level.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(UndertrialSpawner spawner, RandomSource random, EntityType<?> entityType) {
        this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn()
                .putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    protected UndertrialSpawnData getOrCreateNextSpawnData(UndertrialSpawner spawner, RandomSource random) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        }
        SimpleWeightedRandomList<UndertrialSpawnData> potentials = spawner.getConfig().spawnPotentialsDefinition();
        Optional<UndertrialSpawnData> picked = potentials.isEmpty()
                ? this.nextSpawnData
                : potentials.getRandom(random).map(wrapper -> wrapper.getData());
        this.nextSpawnData = Optional.of(picked.orElseGet(UndertrialSpawnData::new));
        spawner.markUpdated();
        return this.nextSpawnData.get();
    }

    public Entity getOrCreateDisplayEntity(UndertrialSpawner spawner, Level level, UndertrialSpawnerState state) {
        if (!state.hasSpinningMob()) {
            return null;
        }
        if (this.displayEntity == null) {
            CompoundTag entityTag = this.getOrCreateNextSpawnData(spawner, level.getRandom()).getEntityToSpawn();
            if (entityTag.contains("id", Tag.TAG_STRING)) {
                this.displayEntity = EntityType.loadEntityRecursive(entityTag, level, Function.identity());
            }
        }
        return this.displayEntity;
    }

    public CompoundTag getUpdateTag(UndertrialSpawnerState state) {
        CompoundTag tag = new CompoundTag();
        if (state == UndertrialSpawnerState.ACTIVE) {
            tag.putLong(TAG_NEXT_MOB_SPAWNS_AT, this.nextMobSpawnsAt);
        }
        this.nextSpawnData.ifPresent(data -> tag.put(TAG_SPAWN_DATA, data.save()));
        return tag;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel level, UndertrialSpawnerConfig config, BlockPos pos) {
        if (this.dispensing != null) {
            return this.dispensing;
        }
        LootTable lootTable = level.getServer().getLootData().getLootTable(config.itemsToDropWhenOminous());
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> items = lootTable.getRandomItems(params, lowResolutionPosition(level, pos));
        if (items.isEmpty()) {
            return SimpleWeightedRandomList.empty();
        }
        SimpleWeightedRandomList.Builder<ItemStack> builder = SimpleWeightedRandomList.builder();
        for (ItemStack stack : items) {
            builder.add(stack.copyWithCount(1), stack.getCount());
        }
        this.dispensing = builder.build();
        return this.dispensing;
    }

    private static long lowResolutionPosition(ServerLevel level, BlockPos pos) {
        BlockPos lowRes = new BlockPos(Mth.floor(pos.getX() / 30.0F), Mth.floor(pos.getY() / 20.0F), Mth.floor(pos.getZ() / 30.0F));
        return level.getSeed() + lowRes.asLong();
    }
}
