package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.block.ElythiaPalette;
import com.craisinlord.anthology.shake.CameraShakeSync;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class KrakenRitualManager {
    private static final TagKey<Biome> ELYTHIA_OCEANS = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "is_elythia_ocean"));

    private static final int ANSWER_START = 60;
    private static final int RECKONING_START = 150;
    private static final int DARKNESS_START = 205;
    private static final int HUSH_START = 220;
    private static final int FINAL_HEARTBEAT = 232;
    private static final int TOTAL_TICKS = 240;

    private static final double VIEW_RANGE = 128.0D;
    private static final double OCCUPIED_RANGE = 96.0D;
    private static final double OUTER_RADIUS = 24.0D;
    private static final double INNER_RADIUS = 6.0D;
    private static final double PUSH_RADIUS = 18.0D;
    private static final float SHAKE_RADIUS = 72.0F;
    private static final int STORM_TICKS = 600;
    private static final int SPOUTS = 8;
    private static final int WHIRLPOOL_ARMS = 3;
    private static final int WHIRLPOOL_STEPS = 4;
    private static final int[] SPAWN_RADII = {0, 8, 14, 20};

    private static final Map<ServerLevel, List<Ritual>> RITUALS = new WeakHashMap<>();

    private KrakenRitualManager() {
    }

    public static InteractionResult tryStart(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (!AntarchySettings.krakenRitualEnabled() || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.INK_SAC) || !level.getBlockState(pos).is(ElythiaPalette.block("chiseled_tidestone"))) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        String failure = failure(serverLevel, pos, stack, serverPlayer);
        if (failure != null) {
            fail(serverLevel, serverPlayer, failure);
            return InteractionResult.CONSUME;
        }
        int cost = AntarchySettings.krakenRitualInkCost();
        int spent = serverPlayer.getAbilities().instabuild ? 0 : cost;
        stack.consume(cost, serverPlayer);
        int surface = Math.max(pos.getY() + 1, serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()));
        Vec3 center = new Vec3(pos.getX() + 0.5D, surface, pos.getZ() + 0.5D);
        RITUALS.computeIfAbsent(serverLevel, key -> new ArrayList<>())
                .add(new Ritual(pos.immutable(), center, serverPlayer.getUUID(), spent));
        return InteractionResult.CONSUME;
    }

    public static void tick(ServerLevel level) {
        List<Ritual> rituals = RITUALS.get(level);
        if (rituals == null) {
            return;
        }
        Iterator<Ritual> iterator = rituals.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick(level)) {
                iterator.remove();
            }
        }
        if (rituals.isEmpty()) {
            RITUALS.remove(level);
        }
    }

    private static String failure(ServerLevel level, BlockPos pos, ItemStack stack, ServerPlayer player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return "peaceful";
        }
        if (!level.getBiome(pos).is(ELYTHIA_OCEANS)) {
            return "wrong_site";
        }
        if (!touchesWater(level, pos)) {
            return "no_water";
        }
        if (isOccupied(level, pos)) {
            return "occupied";
        }
        if (!player.getAbilities().instabuild && stack.getCount() < AntarchySettings.krakenRitualInkCost()) {
            return "ink";
        }
        return null;
    }

    private static boolean touchesWater(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOccupied(ServerLevel level, BlockPos pos) {
        List<Ritual> rituals = RITUALS.get(level);
        if (rituals != null) {
            double rangeSqr = OCCUPIED_RANGE * OCCUPIED_RANGE;
            for (Ritual ritual : rituals) {
                if (ritual.altar.distSqr(pos) <= rangeSqr) {
                    return true;
                }
            }
        }
        AABB area = new AABB(pos).inflate(OCCUPIED_RANGE);
        return !level.getEntities(AntarchyObjects.KRAKEN.get(), area, kraken -> kraken.isAlive()).isEmpty();
    }

    private static void fail(ServerLevel level, ServerPlayer player, String key) {
        Component message = "ink".equals(key)
                ? Component.translatable("message.antarchy.kraken_ritual.ink", AntarchySettings.krakenRitualInkCost())
                : Component.translatable("message.antarchy.kraken_ritual." + key);
        player.sendOverlayMessage(message.copy().withStyle(ChatFormatting.DARK_AQUA));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private static final class Ritual {
        private final BlockPos altar;
        private final Vec3 center;
        private final UUID summoner;
        private final int spentInk;
        private int age;
        private int nextHeartbeat;
        private int nextBolt = ANSWER_START;

        private Ritual(BlockPos altar, Vec3 center, UUID summoner, int spentInk) {
            this.altar = altar;
            this.center = center;
            this.summoner = summoner;
            this.spentInk = spentInk;
        }

        private boolean tick(ServerLevel level) {
            if (!level.hasChunkAt(this.altar)) {
                return true;
            }
            List<ServerPlayer> watchers = watchers(level);
            if (watchers.isEmpty() || !level.getBlockState(this.altar).is(ElythiaPalette.block("chiseled_tidestone"))) {
                this.fizzle(level, watchers);
                return true;
            }
            this.age++;
            if (this.age >= TOTAL_TICKS) {
                this.emerge(level, watchers);
                return true;
            }
            if (this.age < ANSWER_START) {
                this.tickOffering(level, watchers);
            } else if (this.age < RECKONING_START) {
                this.tickAnswer(level, watchers);
            } else if (this.age < HUSH_START) {
                this.tickReckoning(level, watchers);
            } else {
                this.tickHush(level, watchers);
            }
            return false;
        }

        private List<ServerPlayer> watchers(ServerLevel level) {
            List<ServerPlayer> watchers = new ArrayList<>();
            double rangeSqr = VIEW_RANGE * VIEW_RANGE;
            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator() && player.distanceToSqr(this.center) <= rangeSqr) {
                    watchers.add(player);
                }
            }
            return watchers;
        }

        private void tickOffering(ServerLevel level, List<ServerPlayer> watchers) {
            if (this.age == 1) {
                this.sound(level, SoundEvents.CONDUIT_ACTIVATE, 5.0F, 0.5F);
                this.sound(level, SoundEvents.SQUID_SQUIRT, 3.0F, 0.5F);
                narrate(watchers, "begin");
            }
            if (this.age % 2 == 0) {
                send(level, watchers, ParticleTypes.SQUID_INK, this.altar.getX() + 0.5D, this.altar.getY() + 1.1D,
                        this.altar.getZ() + 0.5D, 8, 0.35D, 0.2D, 0.35D, 0.12D);
                double bloom = 1.0D + this.age / 8.0D;
                send(level, watchers, ParticleTypes.SQUID_INK, this.center.x, this.center.y, this.center.z,
                        10, bloom, 0.1D, bloom, 0.02D);
                this.drawPillar(level, watchers, 8.0D, 8);
            }
            this.heartbeat(level, 30, 0.6F);
        }

        private void tickAnswer(ServerLevel level, List<ServerPlayer> watchers) {
            float progress = (this.age - ANSWER_START) / (float) (RECKONING_START - ANSWER_START);
            if (this.age == ANSWER_START) {
                level.getServer().setWeatherParameters(0, STORM_TICKS, true, true);
                this.sound(level, SoundEvents.ELDER_GUARDIAN_CURSE, 4.0F, 0.5F);
                narrate(watchers, "answer");
            }
            double radius = Mth.lerp(progress, 4.0D, OUTER_RADIUS);
            if (this.age % 2 == 0) {
                this.drawWhirlpool(level, watchers, radius, 0.22F);
                this.drawPillar(level, watchers, 14.0D, 12);
            }
            if (this.age % 20 == 0) {
                this.sound(level, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 5.0F, 0.6F);
            }
            if (this.age % 10 == 0) {
                CameraShakeSync.sendNearby(level, this.center, 0.2F + progress * 0.5F, 14, SHAKE_RADIUS);
            }
            this.boltOnSchedule(level, radius, Mth.lerpInt(progress, 40, 14));
            this.heartbeat(level, Mth.lerpInt(progress, 22, 10), 0.6F + progress * 0.2F);
        }

        private void tickReckoning(ServerLevel level, List<ServerPlayer> watchers) {
            float progress = (this.age - RECKONING_START) / (float) (HUSH_START - RECKONING_START);
            if (this.age == RECKONING_START) {
                this.sound(level, AntarchySoundEvents.KRAKEN_ROAR.get(), 6.0F, 0.45F);
                narrate(watchers, "turn");
            }
            if (this.age == RECKONING_START + 40) {
                this.sound(level, AntarchySoundEvents.KRAKEN_ROAR.get(), 7.0F, 0.6F);
            }
            if (this.age == DARKNESS_START) {
                for (ServerPlayer player : watchers) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, false, false));
                }
            }
            double spoutRadius = Mth.lerp(progress, OUTER_RADIUS, INNER_RADIUS);
            if (this.age % 2 == 0) {
                this.drawWhirlpool(level, watchers, OUTER_RADIUS, 0.34F);
                this.drawSpouts(level, watchers, spoutRadius, 6.0D + progress * 22.0D);
                this.drawPillar(level, watchers, 20.0D, 16);
            }
            if (this.age % 15 == 0) {
                this.sound(level, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 6.0F, 0.5F);
            }
            if (this.age % 8 == 0) {
                CameraShakeSync.sendNearby(level, this.center, 0.7F + progress * 0.5F, 12, SHAKE_RADIUS);
            }
            this.boltOnSchedule(level, spoutRadius, 10);
            this.heartbeat(level, 6, 0.8F + progress * 0.3F);
        }

        private void tickHush(ServerLevel level, List<ServerPlayer> watchers) {
            if (this.age == HUSH_START) {
                narrate(watchers, "hush");
            }
            if (this.age == FINAL_HEARTBEAT) {
                this.sound(level, SoundEvents.WARDEN_HEARTBEAT, 8.0F, 0.5F);
            }
        }

        private void emerge(ServerLevel level, List<ServerPlayer> watchers) {
            KrakenEntity kraken = AntarchyObjects.KRAKEN.get().create(level, EntitySpawnReason.TRIGGERED);
            if (kraken == null) {
                this.fizzle(level, watchers);
                return;
            }
            this.placeKraken(level, kraken);
            kraken.finalizeSpawn(level, level.getCurrentDifficultyAt(this.altar), EntitySpawnReason.TRIGGERED, null);
            kraken.setPersistenceRequired();
            if (level.getPlayerByUUID(this.summoner) instanceof ServerPlayer player && player.isAlive()
                    && !player.isCreative() && !player.isSpectator()) {
                kraken.setTarget(player);
            }
            level.addFreshEntity(kraken);

            this.sound(level, AntarchySoundEvents.KRAKEN_ROAR.get(), 8.0F, 0.8F);
            this.sound(level, SoundEvents.LIGHTNING_BOLT_THUNDER, 8.0F, 0.6F);
            this.sound(level, SoundEvents.GENERIC_EXPLODE.value(), 6.0F, 0.5F);
            send(level, watchers, ParticleTypes.EXPLOSION_EMITTER, this.center.x, this.center.y, this.center.z,
                    4, 6.0D, 2.0D, 6.0D, 0.0D);
            for (int ring = 1; ring <= 4; ring++) {
                double radius = ring * 7.0D;
                for (int point = 0; point < 8; point++) {
                    double angle = point * Mth.TWO_PI / 8.0D + ring;
                    send(level, watchers, ParticleTypes.SPLASH, this.center.x + Math.cos(angle) * radius, this.center.y + 1.0D,
                            this.center.z + Math.sin(angle) * radius, 30, 3.0D, 1.5D, 3.0D, 0.4D);
                }
            }
            for (int bolt = 0; bolt < 4; bolt++) {
                double angle = bolt * Mth.TWO_PI / 4.0D;
                this.strike(level, this.center.x + Math.cos(angle) * 16.0D, this.center.z + Math.sin(angle) * 16.0D);
            }
            CameraShakeSync.sendNearby(level, this.center, 1.6F, 40, SHAKE_RADIUS);
            Component title = Component.translatable("message.antarchy.kraken_ritual.title")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);
            for (ServerPlayer player : watchers) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 50, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                this.push(player);
            }
        }

        private void placeKraken(ServerLevel level, KrakenEntity kraken) {
            int surface = (int) this.center.y;
            float yaw = level.getRandom().nextFloat() * 360.0F;
            for (int radius : SPAWN_RADII) {
                int samples = radius == 0 ? 1 : 8;
                for (int sample = 0; sample < samples; sample++) {
                    double angle = sample * Mth.TWO_PI / samples;
                    double x = this.center.x + Math.cos(angle) * radius;
                    double z = this.center.z + Math.sin(angle) * radius;
                    if (!level.hasChunkAt(BlockPos.containing(x, surface, z))) {
                        continue;
                    }
                    int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, Mth.floor(x), Mth.floor(z));
                    for (int y = Math.max(floor, surface - 24); y <= surface + 4; y += 4) {
                        kraken.snapTo(x, y, z, yaw, 0.0F);
                        if (level.noCollision(kraken)) {
                            return;
                        }
                    }
                }
            }
            kraken.snapTo(this.center.x, surface + 2.0D, this.center.z, yaw, 0.0F);
        }

        private void push(ServerPlayer player) {
            Vec3 away = player.position().subtract(this.center).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > PUSH_RADIUS * PUSH_RADIUS || away.lengthSqr() < 1.0E-4D) {
                return;
            }
            player.setDeltaMovement(player.getDeltaMovement().add(away.normalize().scale(1.2D)).add(0.0D, 0.6D, 0.0D));
            player.hurtMarked = true;
        }

        private void fizzle(ServerLevel level, List<ServerPlayer> watchers) {
            narrate(watchers, "fizzle");
            this.sound(level, SoundEvents.FIRE_EXTINGUISH, 2.0F, 0.6F);
            if (this.spentInk > 0) {
                Containers.dropItemStack(level, this.altar.getX() + 0.5D, this.altar.getY() + 1.0D, this.altar.getZ() + 0.5D,
                        new ItemStack(Items.INK_SAC, this.spentInk));
            }
        }

        private void heartbeat(ServerLevel level, int interval, float pitch) {
            if (this.age >= this.nextHeartbeat) {
                this.sound(level, SoundEvents.WARDEN_HEARTBEAT, 6.0F, pitch);
                this.nextHeartbeat = this.age + interval;
            }
        }

        private void boltOnSchedule(ServerLevel level, double radius, int interval) {
            if (this.age >= this.nextBolt) {
                double angle = level.getRandom().nextDouble() * Mth.TWO_PI;
                this.strike(level, this.center.x + Math.cos(angle) * radius, this.center.z + Math.sin(angle) * radius);
                this.nextBolt = this.age + interval;
            }
        }

        private void strike(ServerLevel level, double x, double z) {
            BlockPos column = BlockPos.containing(x, this.center.y, z);
            if (!level.hasChunkAt(column)) {
                return;
            }
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt == null) {
                return;
            }
            bolt.snapTo(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()), z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        private void drawPillar(ServerLevel level, List<ServerPlayer> watchers, double height, int count) {
            send(level, watchers, ParticleTypes.SQUID_INK, this.center.x, this.center.y + height * 0.5D, this.center.z,
                    count, 0.4D, height * 0.35D, 0.4D, 0.02D);
            double depth = this.center.y - this.altar.getY();
            if (depth > 1.0D) {
                send(level, watchers, ParticleTypes.GLOW_SQUID_INK, this.center.x, this.altar.getY() + depth * 0.5D, this.center.z,
                        count, 0.3D, depth * 0.35D, 0.3D, 0.02D);
            }
        }

        private void drawWhirlpool(ServerLevel level, List<ServerPlayer> watchers, double radius, float spin) {
            for (int arm = 0; arm < WHIRLPOOL_ARMS; arm++) {
                for (int step = 1; step <= WHIRLPOOL_STEPS; step++) {
                    double distance = radius * step / WHIRLPOOL_STEPS;
                    double angle = this.age * spin + arm * Mth.TWO_PI / WHIRLPOOL_ARMS + distance * 0.3D;
                    ParticleOptions particle = step % 2 == 0 ? ParticleTypes.GLOW_SQUID_INK : ParticleTypes.SPLASH;
                    send(level, watchers, particle, this.center.x + Math.cos(angle) * distance, this.center.y + 0.2D,
                            this.center.z + Math.sin(angle) * distance, 3, 0.5D, 0.05D, 0.5D, 0.02D);
                }
            }
        }

        private void drawSpouts(ServerLevel level, List<ServerPlayer> watchers, double radius, double height) {
            for (int spout = 0; spout < SPOUTS; spout++) {
                double angle = spout * Mth.TWO_PI / SPOUTS + this.age * 0.03D;
                double x = this.center.x + Math.cos(angle) * radius;
                double z = this.center.z + Math.sin(angle) * radius;
                send(level, watchers, ParticleTypes.CLOUD, x, this.center.y + height * 0.5D, z,
                        5, 0.6D, height * 0.35D, 0.6D, 0.01D);
                send(level, watchers, ParticleTypes.SPLASH, x, this.center.y + height * 0.5D, z,
                        8, 0.8D, height * 0.35D, 0.8D, 0.1D);
            }
        }

        private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
            level.playSound(null, this.center.x, this.center.y, this.center.z, sound, SoundSource.HOSTILE, volume, pitch);
        }

        private static void narrate(List<ServerPlayer> watchers, String key) {
            Component message = Component.translatable("message.antarchy.kraken_ritual." + key)
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC);
            for (ServerPlayer player : watchers) {
                player.sendOverlayMessage(message);
            }
        }

        private static void send(ServerLevel level, List<ServerPlayer> watchers, ParticleOptions particle,
                double x, double y, double z, int count, double spreadX, double spreadY, double spreadZ, double speed) {
            for (ServerPlayer player : watchers) {
                level.sendParticles(player, particle, true, false, x, y, z, count, spreadX, spreadY, spreadZ, speed);
            }
        }
    }
}
