package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.entity.cloud_shark.CloudSharkEntity;
import com.craisinlord.antarchy.content.entity.cloud_shark.SharknadoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.UUID;

public final class ThoraxisWeatherManager {
    private static final ResourceLocation THORAXIS_DIMENSION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis");
    private static final ResourceKey<Biome> THORAXIS_WASTES = ResourceKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_wastes")
    );
    private static final ResourceKey<Biome> DREAM_DUNES = ResourceKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes")
    );
    private static final ResourceKey<Biome> LUCID_POOLS = ResourceKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lucid_pools")
    );
    private static final ResourceKey<Biome> CLOUD_SEA = ResourceKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cloud_sea")
    );
    private static final int WEATHER_RECHECK_MIN_TICKS = 400;
    private static final int WEATHER_RECHECK_MAX_TICKS = 900;
    private static final int STORM_MIN_TICKS = 600;
    private static final int STORM_MAX_TICKS = 1800;
    private static final int INVERSION_STRIKE_INTERVAL = 600;
    private static final int INVERSION_GRAVITY_TICKS = 220;
    private static final int INVERSION_EFFECT_TICKS = 200;
    private static final int WEATHER_PARTICLE_INTERVAL = 4;
    private static final double WEATHER_RADIUS = 48.0D;

    private static final WeakHashMap<ServerLevel, WeatherState> STATES = new WeakHashMap<>();

    private ThoraxisWeatherManager() {
    }

    public static ThoraxisWeatherSnapshot tick(ServerLevel level) {
        if (!isThoraxis(level)) {
            return null;
        }

        WeatherState state = STATES.computeIfAbsent(level, ignored -> new WeatherState());
        long now = level.getGameTime();
        RandomSource random = level.getRandom();

        if (state.kind.isActive()) {
            runActiveWeather(level, state, now);
            if (now >= state.expiresAt) {
                state.clear(now + randomCooldown(random));
                return snapshot(level, state);
            }
            return null;
        }

        if (now < state.nextDecisionAt) {
            return null;
        }

        WeatherKindSelection selection = chooseWeather(level, random);
        if (selection.kind == ThoraxisWeatherKind.NONE) {
            state.nextDecisionAt = now + randomCooldown(random);
            return null;
        }

        state.start(selection.kind, selection.anchor, now, now + randomDuration(random));
        runActiveWeather(level, state, now);
        return snapshot(level, state);
    }

    public static ThoraxisWeatherSnapshot snapshot(ServerLevel level) {
        WeatherState state = STATES.get(level);
        if (state == null) {
            return new ThoraxisWeatherSnapshot(level.dimension().location(), ThoraxisWeatherKind.NONE, 0L, BlockPos.ZERO);
        }

        return snapshot(level, state);
    }

    public static ThoraxisWeatherSnapshot force(ServerLevel level, ThoraxisWeatherKind kind, BlockPos anchor, long durationTicks) {
        if (!isThoraxis(level)) {
            return new ThoraxisWeatherSnapshot(level.dimension().location(), ThoraxisWeatherKind.NONE, 0L, BlockPos.ZERO);
        }

        WeatherState state = STATES.computeIfAbsent(level, ignored -> new WeatherState());
        long now = level.getGameTime();
        if (kind == null || !kind.isActive()) {
            state.clear(now + randomCooldown(level.getRandom()));
            return snapshot(level, state);
        }

        long expiresAt = durationTicks <= 0L ? now + randomDuration(level.getRandom()) : now + durationTicks;
        state.start(kind, anchor == null ? BlockPos.ZERO : anchor, now, expiresAt);
        runActiveWeather(level, state, now);
        return snapshot(level, state);
    }

    public static ThoraxisWeatherSnapshot clear(ServerLevel level) {
        if (!isThoraxis(level)) {
            return new ThoraxisWeatherSnapshot(level.dimension().location(), ThoraxisWeatherKind.NONE, 0L, BlockPos.ZERO);
        }

        WeatherState state = STATES.computeIfAbsent(level, ignored -> new WeatherState());
        state.clear(level.getGameTime() + randomCooldown(level.getRandom()));
        return snapshot(level, state);
    }

    private static ThoraxisWeatherSnapshot snapshot(ServerLevel level, WeatherState state) {
        return new ThoraxisWeatherSnapshot(level.dimension().location(), state.kind, state.expiresAt, state.anchor);
    }

    private static void runActiveWeather(ServerLevel level, WeatherState state, long now) {
        switch (state.kind) {
            case INVERSION_STORM -> tickInversionStorm(level, state, now);
            case BLOOD_RAIN -> tickBloodRain(level, state, now);
            case SHARKNADO -> tickSharknadoWeather(level, state, now);
            case SANDSTORM -> tickSandstorm(level, state, now);
            default -> {
            }
        }
    }

    private static void tickInversionStorm(ServerLevel level, WeatherState state, long now) {
        cleanupExpiredForcedInversions(level, state, now);
        if (now % INVERSION_STRIKE_INTERVAL != 0L) {
            return;
        }

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(state.anchor).inflate(WEATHER_RADIUS),
                entity -> entity.isAlive() && !entity.isSpectator()
        );

        if (targets.isEmpty()) {
            return;
        }

        LivingEntity target = targets.get(level.getRandom().nextInt(targets.size()));
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        if (!AntarchyGravityApi.isGravityForced(target)) {
            AntarchyGravityApi.setForcedGravityDirection(target, AntarchyGravityDirection.UP, new AntarchyGravityTransition(12));
            state.forcedInversionExpiry.put(target.getUUID(), now + INVERSION_GRAVITY_TICKS);
        }

        target.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), INVERSION_EFFECT_TICKS, 0, false, true, true));
    }

    private static void cleanupExpiredForcedInversions(ServerLevel level, WeatherState state, long now) {
        Iterator<Map.Entry<UUID, Long>> iterator = state.forcedInversionExpiry.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() > now) {
                continue;
            }

            LivingEntity entity = level.getEntity(entry.getKey()) instanceof LivingEntity living ? living : null;
            if (entity != null && entity.isAlive() && AntarchyGravityApi.isGravityForced(entity)) {
                AntarchyGravityApi.clearForcedGravity(entity);
            }
            iterator.remove();
        }
    }

    private static void tickBloodRain(ServerLevel level, WeatherState state, long now) {
        if (now % WEATHER_PARTICLE_INTERVAL != 0L) {
            return;
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RED_SAND.defaultBlockState()),
                state.anchor.getX() + 0.5D,
                state.anchor.getY() + 28.0D,
                state.anchor.getZ() + 0.5D,
                26,
                18.0D,
                12.0D,
                18.0D,
                0.12D
        );
        level.playSound(null, state.anchor, SoundEvents.LIGHTNING_BOLT_THUNDER, net.minecraft.sounds.SoundSource.WEATHER, 0.2F, 0.8F);
    }

    private static void tickSandstorm(ServerLevel level, WeatherState state, long now) {
        if (now % WEATHER_PARTICLE_INTERVAL != 0L) {
            return;
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                state.anchor.getX() + 0.5D,
                state.anchor.getY() + 12.0D,
                state.anchor.getZ() + 0.5D,
                40,
                14.0D,
                6.0D,
                14.0D,
                0.10D
        );
    }

    private static void tickSharknadoWeather(ServerLevel level, WeatherState state, long now) {
        if (now == state.startedAt) {
            seedSharknado(level, state.anchor);
        }

        if (now % WEATHER_PARTICLE_INTERVAL != 0L) {
            return;
        }

        level.sendParticles(
                ParticleTypes.CLOUD,
                state.anchor.getX() + 0.5D,
                state.anchor.getY() + 8.0D,
                state.anchor.getZ() + 0.5D,
                26,
                10.0D,
                8.0D,
                10.0D,
                0.12D
        );
    }

    private static void seedSharknado(ServerLevel level, BlockPos anchor) {
        SharknadoManager manager = SharknadoManager.get(level);
        for (int i = 0; i < 4; i++) {
            CloudSharkEntity shark = createCloudShark(level);
            if (shark == null) {
                continue;
            }

            double x = anchor.getX() + level.getRandom().nextDouble() * 10.0D - 5.0D;
            double y = anchor.getY() + 6.0D + level.getRandom().nextDouble() * 6.0D;
            double z = anchor.getZ() + level.getRandom().nextDouble() * 10.0D - 5.0D;
            shark.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(shark);
            manager.tryFormOrJoin(shark, level);
        }
    }

    private static CloudSharkEntity createCloudShark(ServerLevel level) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cloud_shark"))
                .orElse(null);
        if (entityType == null) {
            return null;
        }

        return entityType.create(level) instanceof CloudSharkEntity cloudShark ? cloudShark : null;
    }

    private static WeatherKindSelection chooseWeather(ServerLevel level, RandomSource random) {
        var players = level.players();
        if (players.isEmpty()) {
            return WeatherKindSelection.none();
        }

        LivingEntity anchorEntity = players.get(random.nextInt(players.size()));
        BlockPos anchor = anchorEntity.blockPosition();
        var biome = level.getBiome(anchor);

        if (biome.is(DREAM_DUNES)) {
            return new WeatherKindSelection(ThoraxisWeatherKind.SANDSTORM, anchor);
        }

        if (biome.is(CLOUD_SEA)) {
            return new WeatherKindSelection(ThoraxisWeatherKind.SHARKNADO, anchor);
        }

        if (biome.is(LUCID_POOLS)) {
            return random.nextFloat() < 0.35F
                    ? new WeatherKindSelection(ThoraxisWeatherKind.BLOOD_RAIN, anchor)
                    : WeatherKindSelection.none();
        }

        if (biome.is(THORAXIS_WASTES)) {
            float roll = random.nextFloat();
            if (roll < 0.58F) {
                return new WeatherKindSelection(ThoraxisWeatherKind.INVERSION_STORM, anchor);
            }
            if (roll < 0.88F) {
                return new WeatherKindSelection(ThoraxisWeatherKind.BLOOD_RAIN, anchor);
            }
        }

        return WeatherKindSelection.none();
    }

    private static int randomDuration(RandomSource random) {
        return STORM_MIN_TICKS + random.nextInt(STORM_MAX_TICKS - STORM_MIN_TICKS + 1);
    }

    private static int randomCooldown(RandomSource random) {
        return WEATHER_RECHECK_MIN_TICKS + random.nextInt(WEATHER_RECHECK_MAX_TICKS - WEATHER_RECHECK_MIN_TICKS + 1);
    }

    private static boolean isThoraxis(ServerLevel level) {
        return level.dimension().location().equals(THORAXIS_DIMENSION);
    }

    private record WeatherKindSelection(ThoraxisWeatherKind kind, BlockPos anchor) {
        private static WeatherKindSelection none() {
            return new WeatherKindSelection(ThoraxisWeatherKind.NONE, BlockPos.ZERO);
        }
    }

    private static final class WeatherState {
        private ThoraxisWeatherKind kind = ThoraxisWeatherKind.NONE;
        private long expiresAt;
        private long nextDecisionAt;
        private long startedAt;
        private BlockPos anchor = BlockPos.ZERO;
        private final Map<UUID, Long> forcedInversionExpiry = new java.util.HashMap<>();

        private void start(ThoraxisWeatherKind kind, BlockPos anchor, long startedAt, long expiresAt) {
            this.kind = kind;
            this.anchor = anchor;
            this.startedAt = startedAt;
            this.expiresAt = expiresAt;
            this.nextDecisionAt = expiresAt;
        }

        private void clear(long nextDecisionAt) {
            this.kind = ThoraxisWeatherKind.NONE;
            this.anchor = BlockPos.ZERO;
            this.expiresAt = 0L;
            this.startedAt = 0L;
            this.nextDecisionAt = nextDecisionAt;
            this.forcedInversionExpiry.clear();
        }
    }
}
