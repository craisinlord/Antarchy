package com.craisinlord.antarchy.content.horde;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
import com.craisinlord.antarchy.content.network.HordeIntensitySync;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class CavarynHordeManager {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static final ResourceLocation JUMPY_BUG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_bug");
    private static final ResourceLocation SPIT_BUG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spit_bug");
    private static final ResourceLocation CREEPING_HORROR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "creeping_horror");
    private static final ResourceLocation LURKING_TERROR_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lurking_terror");
    private static final ResourceLocation HERCULES_BEETLE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle");
    private static final String DATA_ID = "antarchy_cavaryn_hordes";

    private static final int GROUP_RADIUS_BLOCKS = 128;
    private static final int ATTENTION_THRESHOLD = 100;
    private static final int STALE_ATTENTION_PRUNE_TICKS = 20 * 60 * 20;
    private static final int PLAYER_CHECK_INTERVAL = 20;
    private static final int AREA_ATTENTION_INTERVAL = 20 * 45;
    private static final int DORMANT_MIN_SPAWN_INTERVAL = 20 * 50;
    private static final int DORMANT_RANDOM_SPAWN_INTERVAL = 20 * 35;
    private static final int AWAKENING_SKIRMISH_INTERVAL = 20 * 8;
    private static final int AWAKENING_TICKS = 20 * 25;
    private static final int PULSE_INTERVAL_TICKS = 20 * 18;
    private static final int RECOVERY_TICKS = 20 * 60;
    private static final int ABANDON_TIMEOUT_TICKS = 20 * 20;
    private static final int HERCULES_TIMEOUT_TICKS = 20 * 180;
    private static final int MAX_HORDE_MOBS_PER_ENCOUNTER = 48;
    private static final int MAX_DORMANT_MOBS_PER_AREA = 8;
    private static final int MAX_SPAWN_ATTEMPTS_PER_MOB = 18;
    private static final double MIN_SPAWN_DISTANCE = 18.0D;
    private static final double MAX_SPAWN_DISTANCE = 36.0D;

    private CavarynHordeManager() {
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(CAVARYN)) {
            return;
        }

        HordeData data = get(level);
        long gameTime = level.getGameTime();
        if (gameTime % PLAYER_CHECK_INTERVAL == 0L) {
            tickPlayers(level, data, gameTime);
        }
        tickEncounters(level, data, gameTime);
        if (gameTime % 100L == 0L) {
            data.prune(level, gameTime);
        }
    }

    public static void recordMobKill(ServerPlayer player, LivingEntity killed) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(CAVARYN)) {
            return;
        }
        if (!isHordeBiome(level, player.blockPosition())) {
            return;
        }
        if (isPlayerInActiveEncounter(level, player)) {
            return;
        }
        addAttention(level, player, killed instanceof Enemy || killed.getType().getCategory() == MobCategory.MONSTER ? 18 : 8);
    }

    public static void recordBlockBreak(ServerPlayer player, BlockState brokenState, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(CAVARYN)) {
            return;
        }
        if (!isHordeBiome(level, pos) || isPlayerInActiveEncounter(level, player)) {
            return;
        }
        addAttention(level, player, isEggOrNest(brokenState) ? 22 : 2);
    }

    public static boolean isHordeBiome(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(AntarchyTags.Biomes.USES_HORDE_SYSTEM);
    }

    public static int maxHordeLevel() {
        return ATTENTION_THRESHOLD;
    }

    public static int getHordeLevel(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(CAVARYN)) {
            return -1;
        }

        PlayerAttention attention = get(level).players.get(player.getUUID());
        return attention != null ? attention.attention : 0;
    }

    public static int setHordeLevel(ServerPlayer player, int levelValue) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(CAVARYN)) {
            return -1;
        }

        HordeData data = get(level);
        PlayerAttention attention = data.players.computeIfAbsent(player.getUUID(), ignored -> new PlayerAttention());
        attention.attention = Mth.clamp(levelValue, 0, ATTENTION_THRESHOLD);
        attention.areaKey = areaKey(player.blockPosition());
        attention.areaTicks = 0;
        attention.lastSeenGameTime = level.getGameTime();
        if (attention.attention < ATTENTION_THRESHOLD) {
            data.encounters.removeIf(encounter -> encounter.center.distSqr(player.blockPosition()) <= GROUP_RADIUS_BLOCKS * GROUP_RADIUS_BLOCKS);
        }
        data.setDirty();
        HordeIntensitySync.send(player, attention.attention / (float) ATTENTION_THRESHOLD);
        return attention.attention;
    }

    private static void tickPlayers(ServerLevel level, HordeData data, long gameTime) {
        Set<UUID> seen = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }

            UUID uuid = player.getUUID();
            seen.add(uuid);
            PlayerAttention attention = data.players.computeIfAbsent(uuid, ignored -> new PlayerAttention());
            boolean inHordeBiome = isHordeBiome(level, player.blockPosition());
            if (!inHordeBiome) {
                attention.fade(6);
                data.setDirty();
                HordeIntensitySync.send(player, 0.0F);
                continue;
            }

            attention.lastSeenGameTime = gameTime;
            BlockPos pos = player.blockPosition();
            long currentArea = areaKey(pos);
            if (attention.areaKey != currentArea) {
                attention.areaKey = currentArea;
                attention.areaTicks = 0;
                attention.fade(4);
                data.setDirty();
            } else if (!isPlayerInActiveEncounter(level, player)) {
                attention.areaTicks += PLAYER_CHECK_INTERVAL;
                if (attention.areaTicks >= AREA_ATTENTION_INTERVAL) {
                    attention.areaTicks = 0;
                    attention.add(6);
                    data.setDirty();
                }
            }

            Encounter encounter = findNearbyEncounter(data, player.blockPosition());
            if (encounter == null && attention.attention >= ATTENTION_THRESHOLD) {
                startEncounter(level, data, player);
                encounter = findNearbyEncounter(data, player.blockPosition());
            }
            if (encounter == null) {
                tickDormantPressure(level, data, player, attention, currentArea, gameTime);
            }
            HordeIntensitySync.send(player, encounter != null ? encounter.intensity(gameTime) : attention.attention / (float) ATTENTION_THRESHOLD);
        }

        for (Map.Entry<UUID, PlayerAttention> entry : data.players.entrySet()) {
            if (!seen.contains(entry.getKey())) {
                entry.getValue().fade(2);
            }
        }
    }

    private static void tickEncounters(ServerLevel level, HordeData data, long gameTime) {
        Iterator<Encounter> iterator = data.encounters.iterator();
        while (iterator.hasNext()) {
            Encounter encounter = iterator.next();
            List<ServerPlayer> targets = targetsFor(level, encounter);
            if (targets.isEmpty()) {
                if (encounter.abandonedSince < 0L) {
                    encounter.abandonedSince = gameTime;
                }
                if (gameTime - encounter.abandonedSince >= ABANDON_TIMEOUT_TICKS) {
                    iterator.remove();
                    data.setDirty();
                }
                continue;
            }
            encounter.abandonedSince = -1L;
            encounter.center = averageBlockPos(targets);

            if (encounter.phase == Phase.AWAKENING && gameTime >= encounter.nextSkirmishTime) {
                spawnAwakeningSkirmish(level, encounter, targets);
                encounter.nextSkirmishTime = gameTime + AWAKENING_SKIRMISH_INTERVAL;
                data.setDirty();
            }
            if (encounter.phase == Phase.AWAKENING && gameTime >= encounter.nextPulseTime) {
                encounter.phase = Phase.SWARMING;
                encounter.nextPulseTime = gameTime;
            }
            if (encounter.phase == Phase.SWARMING && gameTime >= encounter.nextPulseTime) {
                spawnPulse(level, encounter, targets);
                encounter.wave++;
                encounter.nextPulseTime = gameTime + PULSE_INTERVAL_TICKS;
                if (encounter.wave >= 4) {
                    encounter.phase = Phase.HERCULES;
                }
                data.setDirty();
            }
            if (encounter.phase == Phase.HERCULES && herculesCleared(level, encounter, gameTime)) {
                encounter.phase = Phase.RECOVERY;
                encounter.recoveryEndsAt = gameTime + RECOVERY_TICKS;
                data.setDirty();
            }
            if (encounter.phase == Phase.RECOVERY && gameTime >= encounter.recoveryEndsAt) {
                fadeAttentionNear(data, encounter.center, 70);
                iterator.remove();
                data.setDirty();
            }
        }
    }

    private static void tickDormantPressure(ServerLevel level, HordeData data, ServerPlayer player, PlayerAttention attention, long areaKey, long gameTime) {
        long nextSpawnTime = data.areaSpawnCooldowns.getOrDefault(areaKey, 0L);
        if (gameTime < nextSpawnTime || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        int existing = countNearbyDirectHordeMobs(level, player.blockPosition(), 48.0D);
        if (existing >= scaleSpawnCount(level, MAX_DORMANT_MOBS_PER_AREA)) {
            data.areaSpawnCooldowns.put(areaKey, gameTime + DORMANT_MIN_SPAWN_INTERVAL);
            data.setDirty();
            return;
        }

        int count = scaleSpawnCount(level, attention.attention >= ATTENTION_THRESHOLD * 2 / 3 ? 2 : 1);
        for (int i = 0; i < count; i++) {
            EntityType<?> type = level.random.nextBoolean() ? creepingHorrorType() : lurkingTerrorType();
            if (type != null) {
                spawnDirectMob(level, null, type, player);
            }
        }
        data.areaSpawnCooldowns.put(areaKey, gameTime + DORMANT_MIN_SPAWN_INTERVAL + level.random.nextInt(DORMANT_RANDOM_SPAWN_INTERVAL));
        data.setDirty();
    }

    private static boolean herculesCleared(ServerLevel level, Encounter encounter, long gameTime) {
        if (encounter.herculesIds.isEmpty() || gameTime >= encounter.herculesTimeoutAt) {
            return true;
        }
        for (UUID uuid : encounter.herculesIds) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity living && living.isAlive() && !living.isRemoved()) {
                return false;
            }
        }
        return true;
    }

    private static void addAttention(ServerLevel level, ServerPlayer player, int amount) {
        HordeData data = get(level);
        PlayerAttention attention = data.players.computeIfAbsent(player.getUUID(), ignored -> new PlayerAttention());
        attention.lastSeenGameTime = level.getGameTime();
        attention.areaKey = areaKey(player.blockPosition());
        attention.add(amount);
        data.setDirty();
    }

    private static void startEncounter(ServerLevel level, HordeData data, ServerPlayer seedPlayer) {
        Encounter existing = findNearbyEncounter(data, seedPlayer.blockPosition());
        if (existing != null) {
            return;
        }

        Encounter encounter = new Encounter();
        encounter.id = UUID.randomUUID();
        encounter.center = seedPlayer.blockPosition();
        encounter.phase = Phase.AWAKENING;
        encounter.startedAt = level.getGameTime();
        encounter.nextPulseTime = level.getGameTime() + AWAKENING_TICKS;
        encounter.nextSkirmishTime = level.getGameTime() + AWAKENING_SKIRMISH_INTERVAL;
        encounter.recoveryEndsAt = -1L;
        encounter.abandonedSince = -1L;
        encounter.herculesTimeoutAt = level.getGameTime() + AWAKENING_TICKS + PULSE_INTERVAL_TICKS * 4L + HERCULES_TIMEOUT_TICKS;
        for (ServerPlayer player : targetsFor(level, encounter)) {
            encounter.targets.add(player.getUUID());
        }
        if (encounter.targets.isEmpty()) {
            encounter.targets.add(seedPlayer.getUUID());
        }
        data.encounters.add(encounter);
        data.setDirty();
    }

    private static void spawnAwakeningSkirmish(ServerLevel level, Encounter encounter, List<ServerPlayer> targets) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        int players = Math.max(1, targets.size());
        int capacity = Math.max(0, scaleSpawnCount(level, MAX_HORDE_MOBS_PER_ENCOUNTER) - encounter.hordeMobIds.size());
        if (capacity <= 0) {
            return;
        }

        List<EntityType<?>> types = new ArrayList<>();
        addCommon(types, scaleSpawnCount(level, 2 + players));
        addRepeated(types, spitBugType(), scaleSpawnCount(level, Math.max(1, players)));
        int spawned = 0;
        for (EntityType<?> type : types) {
            if (spawned >= capacity || type == null) {
                break;
            }
            ServerPlayer target = targets.get(level.random.nextInt(targets.size()));
            if (spawnDirectMob(level, encounter, type, target)) {
                spawned++;
            }
        }
    }

    private static void spawnPulse(ServerLevel level, Encounter encounter, List<ServerPlayer> targets) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        int players = Math.max(1, targets.size());
        int capacity = Math.max(0, scaleSpawnCount(level, MAX_HORDE_MOBS_PER_ENCOUNTER) - encounter.hordeMobIds.size());
        if (capacity <= 0) {
            return;
        }

        List<EntityType<?>> types = new ArrayList<>();
        switch (encounter.wave) {
            case 0 -> addCommon(types, scaleSpawnCount(level, 12 + players * 3));
            case 1 -> {
                addCommon(types, scaleSpawnCount(level, 14 + players * 3));
                addRepeated(types, spitBugType(), scaleSpawnCount(level, 2 + players));
            }
            case 2 -> {
                addCommon(types, scaleSpawnCount(level, 16 + players * 4));
                addRepeated(types, spitBugType(), scaleSpawnCount(level, 3 + players));
                addRepeated(types, jumpyBugType(), scaleSpawnCount(level, 1 + players));
            }
            default -> {
                int beetles = Math.min(scaleSpawnCount(level, 1 + players / 3), 3);
                addRepeated(types, herculesBeetleType(), beetles);
                addCommon(types, scaleSpawnCount(level, 8 + players * 2));
                warnHercules(level, encounter.center, targets);
            }
        }

        int spawned = 0;
        for (EntityType<?> type : types) {
            if (spawned >= capacity || type == null) {
                break;
            }
            ServerPlayer target = targets.get(level.random.nextInt(targets.size()));
            if (spawnDirectMob(level, encounter, type, target)) {
                spawned++;
            }
        }
    }

    private static boolean spawnDirectMob(ServerLevel level, @Nullable Encounter encounter, EntityType<?> type, ServerPlayer target) {
        BlockPos spawnPos = findSpawnPos(level, target, type, level.random);
        if (spawnPos == null) {
            return false;
        }
        Entity entity = type.create(level);
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        mob.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
        mob.setTarget(target);
        if (!level.addFreshEntity(mob)) {
            return false;
        }
        if (encounter != null) {
            encounter.hordeMobIds.add(mob.getUUID());
        }
        if (type == herculesBeetleType()) {
            if (encounter != null) {
                encounter.herculesIds.add(mob.getUUID());
            }
            level.playSound(null, spawnPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 0.65F);
            HerculesBeetleImpactShakeSync.send(target, 28);
        } else {
            level.playSound(null, spawnPos, SoundEvents.GRAVEL_BREAK, SoundSource.HOSTILE, 0.7F, 0.75F + level.random.nextFloat() * 0.25F);
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.2D, spawnPos.getZ() + 0.5D, 8, 0.4D, 0.2D, 0.4D, 0.03D);
        return true;
    }

    private static int countNearbyDirectHordeMobs(ServerLevel level, BlockPos center, double radius) {
        AABB box = new AABB(center).inflate(radius);
        EntityType<?> creepingHorror = creepingHorrorType();
        EntityType<?> lurkingTerror = lurkingTerrorType();
        EntityType<?> spitBug = spitBugType();
        EntityType<?> jumpyBug = jumpyBugType();
        return level.getEntitiesOfClass(Mob.class, box, mob ->
                mob.getType() == creepingHorror
                        || mob.getType() == lurkingTerror
                        || mob.getType() == spitBug
                        || mob.getType() == jumpyBug
        ).size();
    }

    @Nullable
    private static BlockPos findSpawnPos(ServerLevel level, ServerPlayer target, EntityType<?> type, RandomSource random) {
        BlockPos origin = target.blockPosition();
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS_PER_MOB; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            BlockPos base = origin.offset(Mth.floor(Math.cos(angle) * distance), random.nextInt(13) - 6, Mth.floor(Math.sin(angle) * distance));
            if (!level.hasChunkAt(base) || !isHordeBiome(level, base)) {
                continue;
            }
            BlockPos candidate = findFloor(level, base);
            if (candidate == null || !level.hasChunkAt(candidate) || !isHordeBiome(level, candidate)) {
                continue;
            }
            if (candidate.distSqr(target.blockPosition()) < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE) {
                continue;
            }
            if (!level.noCollision(type.getSpawnAABB(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D))) {
                continue;
            }
            if (target.hasLineOfSight(new net.minecraft.world.entity.item.ItemEntity(level, candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D, net.minecraft.world.item.ItemStack.EMPTY))) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    @Nullable
    private static BlockPos findFloor(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int dy = 0; dy <= 8; dy++) {
            cursor.set(start.getX(), start.getY() - dy, start.getZ());
            if (isValidFloorSpawn(level, cursor)) {
                return cursor.immutable();
            }
            cursor.set(start.getX(), start.getY() + dy, start.getZ());
            if (isValidFloorSpawn(level, cursor)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isValidFloorSpawn(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above());
    }

    private static void addCommon(List<EntityType<?>> types, int count) {
        EntityType<?> creepingHorror = creepingHorrorType();
        EntityType<?> lurkingTerror = lurkingTerrorType();
        for (int i = 0; i < count; i++) {
            EntityType<?> type = i % 2 == 0 ? creepingHorror : lurkingTerror;
            if (type != null) {
                types.add(type);
            }
        }
    }

    private static void addRepeated(List<EntityType<?>> types, @Nullable EntityType<?> type, int count) {
        if (type == null) {
            return;
        }
        for (int i = 0; i < count; i++) {
            types.add(type);
        }
    }

    private static int scaleSpawnCount(ServerLevel level, int baseCount) {
        if (baseCount <= 0 || level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0;
        }
        float multiplier = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 0.65F;
            case NORMAL -> 1.0F;
            case HARD -> 1.35F;
        };
        return Math.max(1, Mth.ceil(baseCount * multiplier));
    }

    @Nullable
    private static EntityType<?> spitBugType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(SPIT_BUG_ID).orElse(null);
    }

    @Nullable
    private static EntityType<?> jumpyBugType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(JUMPY_BUG_ID).orElse(null);
    }

    @Nullable
    private static EntityType<?> creepingHorrorType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(CREEPING_HORROR_ID).orElse(null);
    }

    @Nullable
    private static EntityType<?> lurkingTerrorType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(LURKING_TERROR_ID).orElse(null);
    }

    @Nullable
    private static EntityType<?> herculesBeetleType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(HERCULES_BEETLE_ID).orElse(null);
    }

    private static void warnHercules(ServerLevel level, BlockPos center, List<ServerPlayer> targets) {
        level.playSound(null, center, AntarchySoundEvents.HERCULES_BEETLE_CRY.get(), SoundSource.HOSTILE, 2.0F, 0.65F);
        for (ServerPlayer player : targets) {
            HerculesBeetleImpactShakeSync.send(player, 35);
        }
    }

    private static boolean isPlayerInActiveEncounter(ServerLevel level, ServerPlayer player) {
        HordeData data = get(level);
        Encounter encounter = findNearbyEncounter(data, player.blockPosition());
        return encounter != null && encounter.phase != Phase.RECOVERY;
    }

    @Nullable
    private static Encounter findNearbyEncounter(HordeData data, BlockPos pos) {
        for (Encounter encounter : data.encounters) {
            if (encounter.center.distSqr(pos) <= GROUP_RADIUS_BLOCKS * GROUP_RADIUS_BLOCKS) {
                return encounter;
            }
        }
        return null;
    }

    private static List<ServerPlayer> targetsFor(ServerLevel level, Encounter encounter) {
        List<ServerPlayer> targets = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !isHordeBiome(level, player.blockPosition())) {
                continue;
            }
            if (player.blockPosition().distSqr(encounter.center) <= GROUP_RADIUS_BLOCKS * GROUP_RADIUS_BLOCKS) {
                targets.add(player);
                encounter.targets.add(player.getUUID());
            }
        }
        return targets;
    }

    private static BlockPos averageBlockPos(List<ServerPlayer> players) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (ServerPlayer player : players) {
            x += player.blockPosition().getX();
            y += player.blockPosition().getY();
            z += player.blockPosition().getZ();
        }
        int count = Math.max(1, players.size());
        return new BlockPos(x / count, y / count, z / count);
    }

    private static void fadeAttentionNear(HordeData data, BlockPos center, int amount) {
        for (PlayerAttention attention : data.players.values()) {
            if (BlockPos.of(attention.areaKey).distSqr(center) <= GROUP_RADIUS_BLOCKS * GROUP_RADIUS_BLOCKS) {
                attention.attention = Math.max(0, attention.attention - amount);
                attention.areaTicks = 0;
            }
        }
    }

    private static long areaKey(BlockPos pos) {
        return BlockPos.asLong(
                Math.floorDiv(pos.getX(), GROUP_RADIUS_BLOCKS) * GROUP_RADIUS_BLOCKS,
                Math.floorDiv(pos.getY(), GROUP_RADIUS_BLOCKS) * GROUP_RADIUS_BLOCKS,
                Math.floorDiv(pos.getZ(), GROUP_RADIUS_BLOCKS) * GROUP_RADIUS_BLOCKS
        );
    }

    private static boolean isEggOrNest(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        return id.getNamespace().equals(Antarchy.MODID) && (path.contains("egg") || path.contains("nest"));
    }

    private static HordeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<HordeData>(HordeData::create, HordeData::load, null),
                DATA_ID
        );
    }

    private enum Phase {
        AWAKENING,
        SWARMING,
        HERCULES,
        RECOVERY
    }

    public static final class HordeData extends SavedData {
        private static final String PLAYERS_KEY = "Players";
        private static final String ENCOUNTERS_KEY = "Encounters";
        private static final String AREA_SPAWN_COOLDOWNS_KEY = "AreaSpawnCooldowns";

        private final Map<UUID, PlayerAttention> players = new HashMap<>();
        private final List<Encounter> encounters = new ArrayList<>();
        private final Map<Long, Long> areaSpawnCooldowns = new HashMap<>();

        static HordeData create() {
            return new HordeData();
        }

        static HordeData load(CompoundTag tag, HolderLookup.Provider registries) {
            HordeData data = new HordeData();
            for (net.minecraft.nbt.Tag rawEntry : tag.getList(PLAYERS_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) rawEntry;
                data.players.put(entry.getUUID("Player"), PlayerAttention.load(entry));
            }
            for (net.minecraft.nbt.Tag rawEntry : tag.getList(ENCOUNTERS_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                data.encounters.add(Encounter.load((CompoundTag) rawEntry));
            }
            for (net.minecraft.nbt.Tag rawEntry : tag.getList(AREA_SPAWN_COOLDOWNS_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) rawEntry;
                data.areaSpawnCooldowns.put(entry.getLong("Area"), entry.getLong("NextSpawnTime"));
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag playerList = new ListTag();
            for (Map.Entry<UUID, PlayerAttention> entry : this.players.entrySet()) {
                CompoundTag playerTag = entry.getValue().save();
                playerTag.putUUID("Player", entry.getKey());
                playerList.add(playerTag);
            }
            tag.put(PLAYERS_KEY, playerList);

            ListTag encounterList = new ListTag();
            for (Encounter encounter : this.encounters) {
                encounterList.add(encounter.save());
            }
            tag.put(ENCOUNTERS_KEY, encounterList);

            ListTag areaCooldownList = new ListTag();
            for (Map.Entry<Long, Long> entry : this.areaSpawnCooldowns.entrySet()) {
                CompoundTag areaTag = new CompoundTag();
                areaTag.putLong("Area", entry.getKey());
                areaTag.putLong("NextSpawnTime", entry.getValue());
                areaCooldownList.add(areaTag);
            }
            tag.put(AREA_SPAWN_COOLDOWNS_KEY, areaCooldownList);
            return tag;
        }

        private void prune(ServerLevel level, long gameTime) {
            this.players.values().removeIf(attention -> attention.attention <= 0 && gameTime - attention.lastSeenGameTime > STALE_ATTENTION_PRUNE_TICKS);
            this.areaSpawnCooldowns.entrySet().removeIf(entry -> gameTime - entry.getValue() > STALE_ATTENTION_PRUNE_TICKS);
            for (Encounter encounter : this.encounters) {
                encounter.hordeMobIds.removeIf(uuid -> {
                    Entity entity = level.getEntity(uuid);
                    return entity == null || entity.isRemoved();
                });
            }
            this.setDirty();
        }
    }

    private static final class PlayerAttention {
        private int attention;
        private int areaTicks;
        private long areaKey;
        private long lastSeenGameTime;

        private void add(int amount) {
            this.attention = Mth.clamp(this.attention + amount, 0, ATTENTION_THRESHOLD);
        }

        private void fade(int amount) {
            this.attention = Math.max(0, this.attention - amount);
            this.areaTicks = Math.max(0, this.areaTicks - PLAYER_CHECK_INTERVAL);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Attention", this.attention);
            tag.putInt("AreaTicks", this.areaTicks);
            tag.putLong("AreaKey", this.areaKey);
            tag.putLong("LastSeenGameTime", this.lastSeenGameTime);
            return tag;
        }

        private static PlayerAttention load(CompoundTag tag) {
            PlayerAttention attention = new PlayerAttention();
            attention.attention = tag.getInt("Attention");
            attention.areaTicks = tag.getInt("AreaTicks");
            attention.areaKey = tag.getLong("AreaKey");
            attention.lastSeenGameTime = tag.getLong("LastSeenGameTime");
            return attention;
        }
    }

    private static final class Encounter {
        private UUID id = UUID.randomUUID();
        private Phase phase = Phase.AWAKENING;
        private BlockPos center = BlockPos.ZERO;
        private int wave;
        private long startedAt;
        private long nextPulseTime;
        private long recoveryEndsAt;
        private long abandonedSince;
        private long herculesTimeoutAt;
        private long nextSkirmishTime;
        private final Set<UUID> targets = new HashSet<>();
        private final Set<UUID> hordeMobIds = new HashSet<>();
        private final Set<UUID> herculesIds = new HashSet<>();

        private float intensity(long gameTime) {
            return switch (this.phase) {
                case AWAKENING -> Mth.clamp((gameTime - this.startedAt) / (float) AWAKENING_TICKS, 0.25F, 1.0F);
                case SWARMING, HERCULES -> 1.0F;
                case RECOVERY -> Mth.clamp((this.recoveryEndsAt - gameTime) / (float) RECOVERY_TICKS, 0.0F, 0.75F);
            };
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", this.id);
            tag.putString("Phase", this.phase.name());
            tag.putLong("Center", this.center.asLong());
            tag.putInt("Wave", this.wave);
            tag.putLong("StartedAt", this.startedAt);
            tag.putLong("NextPulseTime", this.nextPulseTime);
            tag.putLong("RecoveryEndsAt", this.recoveryEndsAt);
            tag.putLong("AbandonedSince", this.abandonedSince);
            tag.putLong("HerculesTimeoutAt", this.herculesTimeoutAt);
            tag.putLong("NextSkirmishTime", this.nextSkirmishTime);
            tag.put("Targets", uuidList(this.targets));
            tag.put("HordeMobs", uuidList(this.hordeMobIds));
            tag.put("Hercules", uuidList(this.herculesIds));
            return tag;
        }

        private static Encounter load(CompoundTag tag) {
            Encounter encounter = new Encounter();
            encounter.id = tag.getUUID("Id");
            encounter.phase = Phase.valueOf(tag.getString("Phase"));
            encounter.center = BlockPos.of(tag.getLong("Center"));
            encounter.wave = tag.getInt("Wave");
            encounter.startedAt = tag.getLong("StartedAt");
            encounter.nextPulseTime = tag.getLong("NextPulseTime");
            encounter.recoveryEndsAt = tag.getLong("RecoveryEndsAt");
            encounter.abandonedSince = tag.getLong("AbandonedSince");
            encounter.herculesTimeoutAt = tag.getLong("HerculesTimeoutAt");
            encounter.nextSkirmishTime = tag.getLong("NextSkirmishTime");
            readUuidList(tag.getList("Targets", net.minecraft.nbt.Tag.TAG_COMPOUND), encounter.targets);
            readUuidList(tag.getList("HordeMobs", net.minecraft.nbt.Tag.TAG_COMPOUND), encounter.hordeMobIds);
            readUuidList(tag.getList("Hercules", net.minecraft.nbt.Tag.TAG_COMPOUND), encounter.herculesIds);
            return encounter;
        }

        private static ListTag uuidList(Set<UUID> uuids) {
            ListTag list = new ListTag();
            for (UUID uuid : uuids) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", uuid);
                list.add(tag);
            }
            return list;
        }

        private static void readUuidList(ListTag list, Set<UUID> uuids) {
            for (net.minecraft.nbt.Tag rawEntry : list) {
                uuids.add(((CompoundTag) rawEntry).getUUID("Id"));
            }
        }
    }
}
