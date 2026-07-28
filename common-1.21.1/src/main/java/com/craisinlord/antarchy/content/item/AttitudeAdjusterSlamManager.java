package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.network.ImpactShakePayload;
import com.craisinlord.antarchy.content.network.ImpactShakeSync;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class AttitudeAdjusterSlamManager {
    private static final int SLAM_TIMEOUT_TICKS = 40;
    private static final int PLAYER_SLAM_COOLDOWN_TICKS = 28;
    private static final double GROUNDED_VERTICAL_KNOCKBACK = 1.35D;
    private static final double GROUNDED_HORIZONTAL_KNOCKBACK = 0.5D;
    private static final double AIRBORNE_SLAM_DOWNWARD_VELOCITY = -1.8D;
    private static final double SLAM_BONUS_DAMAGE_SCALE = 10.0D;
    private static final double SLAM_MAX_BONUS_DAMAGE = 18.0D;
    private static final double SLAM_IMPACT_RADIUS = 3.75D;
    private static final double SLAM_RADIAL_KNOCKBACK = 0.55D;
    private static final double PLAYER_SLAM_START_VELOCITY = -2.15D;
    private static final double PLAYER_SLAM_RADIUS = 4.75D;
    private static final double PLAYER_SLAM_BASE_DAMAGE = 12.0D;
    private static final double PLAYER_SLAM_DAMAGE_SCALE = 0.85D;
    private static final double PLAYER_SLAM_MAX_DAMAGE = 24.0D;
    private static final double PLAYER_SLAM_UPWARD_KNOCKBACK = 0.9D;
    private static final double PLAYER_SLAM_HORIZONTAL_KNOCKBACK = 0.9D;
    private static final float NORMAL_SHAKE_INTENSITY = 0.5F;
    private static final int NORMAL_SHAKE_DURATION = 10;
    private static final float ENEMY_SLAM_SHAKE_INTENSITY = 0.95F;
    private static final int ENEMY_SLAM_SHAKE_DURATION = 16;
    private static final float PLAYER_SLAM_SHAKE_INTENSITY = 1.2F;
    private static final int PLAYER_SLAM_SHAKE_DURATION = 20;
    private static final float IMPACT_SHAKE_RADIUS = 24.0F;
    private static final float NORMAL_BREAK_HARDNESS_LIMIT = 6.0F;
    private static final int MAX_BROKEN_BLOCKS = 9;
    private static final Map<UUID, SlammedTargetState> SLAMMED_TARGETS = new HashMap<>();
    private static final Map<UUID, PlayerSlamState> PLAYER_SLAMS = new HashMap<>();
    private static final Map<UUID, Long> SPECIAL_HIT_MARKS = new HashMap<>();

    private AttitudeAdjusterSlamManager() {
    }

    public static void markSpecialHit(Player player) {
        SPECIAL_HIT_MARKS.put(player.getUUID(), player.level().getGameTime());
    }

    public static boolean consumeSpecialHit(Player player) {
        Long markedTime = SPECIAL_HIT_MARKS.get(player.getUUID());
        if (markedTime == null || markedTime != player.level().getGameTime()) {
            return false;
        }
        SPECIAL_HIT_MARKS.remove(player.getUUID());
        return true;
    }

    public static void onGroundedHit(ServerLevel level, Player attacker, LivingEntity target) {
        Vec3 push = horizontalAway(attacker.position(), target.position(), attacker.getLookAngle()).scale(GROUNDED_HORIZONTAL_KNOCKBACK);
        double resistanceScale = resistanceScale(target);
        Vec3 motion = target.getDeltaMovement().add(push.x * resistanceScale, Math.max(GROUNDED_VERTICAL_KNOCKBACK * resistanceScale, 0.45D), push.z * resistanceScale);
        target.setDeltaMovement(motion);
        target.hurtMarked = true;
        playNormalImpact(level, target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), target.getOnPosLegacy());
    }

    public static void onAirborneHit(ServerLevel level, Player attacker, LivingEntity target) {
        Vec3 push = horizontalAway(attacker.position(), target.position(), attacker.getLookAngle()).scale(0.18D);
        target.setDeltaMovement(push.x, Math.min(target.getDeltaMovement().y, AIRBORNE_SLAM_DOWNWARD_VELOCITY), push.z);
        target.hurtMarked = true;
        SLAMMED_TARGETS.put(target.getUUID(), new SlammedTargetState(
                target.getUUID(),
                attacker.getUUID(),
                level.dimension().location().toString(),
                level.getGameTime() + SLAM_TIMEOUT_TICKS,
                target.getDeltaMovement().y,
                target.fallDistance
        ));
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    public static boolean tryStartPlayerSlam(ServerPlayer player, net.minecraft.world.InteractionHand hand) {
        if (player.onGround() || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying || PLAYER_SLAMS.containsKey(player.getUUID())) {
            return false;
        }
        PLAYER_SLAMS.put(player.getUUID(), new PlayerSlamState(player.getUUID(), player.serverLevel().dimension().location().toString(), player.serverLevel().getGameTime() + SLAM_TIMEOUT_TICKS, player.fallDistance));
        AntarchyGravityDirection gravityDirection = AntarchyGravityRotationUtil.getGravityDownDirection(player) == Direction.UP ? AntarchyGravityDirection.UP : AntarchyGravityDirection.DOWN;
        Vec3 motion = player.getDeltaMovement();
        Vec3 localMotion = AntarchyGravityRotationUtil.vecWorldToPlayer(motion, gravityDirection);
        player.setDeltaMovement(AntarchyGravityRotationUtil.vecPlayerToWorld(new Vec3(localMotion.x * 0.25D, PLAYER_SLAM_START_VELOCITY, localMotion.z * 0.25D), gravityDirection));
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        player.swing(hand, true);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    public static void tick(ServerLevel level) {
        tickSlammedTargets(level);
        tickPlayerSlams(level);
    }

    private static void tickSlammedTargets(ServerLevel level) {
        Iterator<SlammedTargetState> iterator = SLAMMED_TARGETS.values().iterator();
        while (iterator.hasNext()) {
            SlammedTargetState state = iterator.next();
            if (!state.dimensionId.equals(level.dimension().location().toString())) {
                continue;
            }
            Entity entity = level.getEntity(state.targetId);
            if (!(entity instanceof LivingEntity target) || !target.isAlive() || level.getGameTime() > state.expiresAt || target.isInWaterOrBubble() || target.isInLava() || target.isFallFlying() || (target instanceof Player player && player.getAbilities().flying)) {
                iterator.remove();
                continue;
            }
            if (!target.onGround()) {
                continue;
            }
            Player attacker = level.getPlayerByUUID(state.attackerId);
            double impactSpeed = Math.max(Math.abs(Math.min(target.getDeltaMovement().y, state.startDownwardVelocity)), Math.max(0.0D, target.fallDistance * 0.125D));
            float bonusDamage = (float) Mth.clamp(impactSpeed * SLAM_BONUS_DAMAGE_SCALE, 4.0D, SLAM_MAX_BONUS_DAMAGE);
            if (attacker != null) {
                target.hurt(level.damageSources().playerAttack(attacker), bonusDamage);
            } else {
                target.hurt(level.damageSources().generic(), bonusDamage);
            }
            Vec3 impactPos = target.position();
            BlockPos groundPos = target.getOnPosLegacy();
            playSlamImpact(level, impactPos, groundPos, true);
            knockbackNearby(level, impactPos, target, SLAM_IMPACT_RADIUS, SLAM_RADIAL_KNOCKBACK, 0.3D);
            iterator.remove();
        }
    }

    private static void tickPlayerSlams(ServerLevel level) {
        Iterator<PlayerSlamState> iterator = PLAYER_SLAMS.values().iterator();
        while (iterator.hasNext()) {
            PlayerSlamState state = iterator.next();
            if (!state.dimensionId.equals(level.dimension().location().toString())) {
                continue;
            }
            Player entity = level.getPlayerByUUID(state.playerId);
            if (!(entity instanceof ServerPlayer player) || !player.isAlive() || level.getGameTime() > state.expiresAt || player.isInWaterOrBubble() || player.isInLava() || player.isFallFlying() || player.getAbilities().flying) {
                iterator.remove();
                continue;
            }
            player.fallDistance = 0.0F;
            if (!player.onGround()) {
                continue;
            }
            Direction gravityDown = AntarchyGravityRotationUtil.getGravityDownDirection(player);
            Vec3 impactPos = player.position();
            BlockPos groundPos = getSlamSurfacePos(player, gravityDown);
            double localVerticalSpeed = AntarchyGravityRotationUtil.vecWorldToPlayer(player.getDeltaMovement(), gravityDown == Direction.UP ? AntarchyGravityDirection.UP : AntarchyGravityDirection.DOWN).y;
            double impactStrength = Math.max(Math.abs(Math.min(localVerticalSpeed, PLAYER_SLAM_START_VELOCITY)), Math.max(state.startFallDistance, player.fallDistance) * 0.15D);
            float damage = (float) Mth.clamp(PLAYER_SLAM_BASE_DAMAGE + impactStrength * PLAYER_SLAM_DAMAGE_SCALE * 10.0D, PLAYER_SLAM_BASE_DAMAGE, PLAYER_SLAM_MAX_DAMAGE);
            AABB area = new AABB(impactPos.x - PLAYER_SLAM_RADIUS, impactPos.y - 1.5D, impactPos.z - PLAYER_SLAM_RADIUS, impactPos.x + PLAYER_SLAM_RADIUS, impactPos.y + 2.5D, impactPos.z + PLAYER_SLAM_RADIUS);
            Vec3 awayFromSurface = new Vec3(-gravityDown.getStepX(), -gravityDown.getStepY(), -gravityDown.getStepZ());
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, candidate -> candidate.isAlive() && candidate != player)) {
                if (!target.hurt(level.damageSources().playerAttack(player), damage)) {
                    continue;
                }
                Vec3 away = horizontalAway(impactPos, target.position(), player.getLookAngle()).scale(PLAYER_SLAM_HORIZONTAL_KNOCKBACK);
                double resistanceScale = resistanceScale(target);
                double surfaceKnockback = Math.max(PLAYER_SLAM_UPWARD_KNOCKBACK * resistanceScale, 0.35D);
                target.setDeltaMovement(target.getDeltaMovement().add(
                        away.x * resistanceScale + awayFromSurface.x * surfaceKnockback,
                        away.y * resistanceScale + awayFromSurface.y * surfaceKnockback,
                        away.z * resistanceScale + awayFromSurface.z * surfaceKnockback
                ));
                target.hurtMarked = true;
            }
            playSlamImpact(level, impactPos, groundPos, false);
            if (AntarchySettings.attitudeAdjusterBreaksBlocks()) {
                breakBlocks(level, player, groundPos, gravityDown);
            }
            if (player.getMainHandItem().getItem() instanceof AttitudeAdjusterItem item) {
                player.getCooldowns().addCooldown(item, PLAYER_SLAM_COOLDOWN_TICKS);
            }
            iterator.remove();
        }
    }

    private static BlockPos getSlamSurfacePos(Entity entity, Direction gravityDown) {
        if (gravityDown == Direction.UP) {
            return BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY + 0.05D, entity.getZ());
        }
        return entity.getOnPosLegacy();
    }

    private static void breakBlocks(ServerLevel level, ServerPlayer player, BlockPos center, Direction gravityDown) {
        int broken = 0;
        int minY = gravityDown == Direction.UP ? 0 : -1;
        int maxY = gravityDown == Direction.UP ? 1 : 0;
        Direction supportFace = gravityDown.getOpposite();
        for (int y = minY; y <= maxY && broken < MAX_BROKEN_BLOCKS; y++) {
            for (int x = -1; x <= 1 && broken < MAX_BROKEN_BLOCKS; x++) {
                for (int z = -1; z <= 1 && broken < MAX_BROKEN_BLOCKS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (state.isAir() || blockEntity != null || state.getDestroySpeed(level, pos) < 0.0F || state.getDestroySpeed(level, pos) > NORMAL_BREAK_HARDNESS_LIMIT) {
                        continue;
                    }
                    if (!state.isFaceSturdy(level, pos, supportFace) && pos.getY() != center.getY()) {
                        continue;
                    }
                    if (player.gameMode.destroyBlock(pos)) {
                        broken++;
                    }
                }
            }
        }
    }

    private static void playNormalImpact(ServerLevel level, Vec3 impactPos, BlockPos groundPos) {
        level.playSound(null, impactPos.x, impactPos.y, impactPos.z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 0.95F, 0.75F);
        spawnImpactParticles(level, impactPos, groundPos, 10, 20);
        shake(level, impactPos, NORMAL_SHAKE_INTENSITY, NORMAL_SHAKE_DURATION);
    }

    private static void playSlamImpact(ServerLevel level, Vec3 impactPos, BlockPos groundPos, boolean enemySlam) {
        level.playSound(null, impactPos.x, impactPos.y, impactPos.z, enemySlam ? SoundEvents.GENERIC_EXPLODE.value() : SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, enemySlam ? 1.2F : 1.35F, enemySlam ? 0.65F : 0.55F);
        spawnImpactParticles(level, impactPos, groundPos, enemySlam ? 20 : 28, enemySlam ? 32 : 42);
        spawnShockwaveParticles(level, impactPos, enemySlam ? 18 : 28);
        shake(level, impactPos, enemySlam ? ENEMY_SLAM_SHAKE_INTENSITY : PLAYER_SLAM_SHAKE_INTENSITY, enemySlam ? ENEMY_SLAM_SHAKE_DURATION : PLAYER_SLAM_SHAKE_DURATION);
        level.gameEvent(GameEvent.EXPLODE, impactPos, GameEvent.Context.of(level.getBlockState(groundPos)));
    }

    private static void spawnImpactParticles(ServerLevel level, Vec3 impactPos, BlockPos groundPos, int dustCount, int debrisCount) {
        level.sendParticles(new DustParticleOptions(new Vector3f(1.00F, 0.92F, 0.18F), 1.35F), impactPos.x, impactPos.y + 0.1D, impactPos.z, dustCount, 0.45D, 0.15D, 0.45D, 0.035D);
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState), impactPos.x, impactPos.y + 0.05D, impactPos.z, debrisCount, 0.7D, 0.15D, 0.7D, 0.06D);
        }
        level.sendParticles(ParticleTypes.POOF, impactPos.x, impactPos.y + 0.2D, impactPos.z, 8, 0.45D, 0.05D, 0.45D, 0.02D);
    }

    private static void spawnShockwaveParticles(ServerLevel level, Vec3 impactPos, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0D * i) / count;
            double x = impactPos.x + Math.cos(angle) * 0.6D;
            double z = impactPos.z + Math.sin(angle) * 0.6D;
            double dx = Math.cos(angle) * 0.18D;
            double dz = Math.sin(angle) * 0.18D;
            level.sendParticles(new DustParticleOptions(new Vector3f(1.00F, 0.85F, 0.22F), 0.95F), x, impactPos.y + 0.05D, z, 1, dx, 0.01D, dz, 0.0D);
        }
    }

    private static void knockbackNearby(ServerLevel level, Vec3 center, LivingEntity ignored, double radius, double horizontalStrength, double verticalStrength) {
        AABB area = new AABB(center.x - radius, center.y - 1.5D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, candidate -> candidate.isAlive() && candidate != ignored)) {
            Vec3 away = horizontalAway(center, entity.position(), entity.getLookAngle()).scale(horizontalStrength);
            double resistanceScale = resistanceScale(entity);
            entity.setDeltaMovement(entity.getDeltaMovement().add(away.x * resistanceScale, Math.max(verticalStrength * resistanceScale, 0.1D), away.z * resistanceScale));
            entity.hurtMarked = true;
        }
    }

    private static void shake(ServerLevel level, Vec3 pos, float intensity, int durationTicks) {
        ImpactShakePayload payload = new ImpactShakePayload(pos.x, pos.y, pos.z, intensity, durationTicks, IMPACT_SHAKE_RADIUS);
        double radiusSqr = IMPACT_SHAKE_RADIUS * IMPACT_SHAKE_RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.x, pos.y, pos.z) <= radiusSqr) {
                ImpactShakeSync.send(player, payload);
            }
        }
    }

    private static Vec3 horizontalAway(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 horizontal = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = fallback.multiply(1.0D, 0.0D, 1.0D);
        }
        return horizontal.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : horizontal.normalize();
    }

    private static double resistanceScale(LivingEntity entity) {
        return Mth.clamp(1.0D - entity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE), 0.15D, 1.0D);
    }

    private static final class SlammedTargetState {
        private final UUID targetId;
        private final UUID attackerId;
        private final String dimensionId;
        private final long expiresAt;
        private final double startDownwardVelocity;
        private final float startFallDistance;

        private SlammedTargetState(UUID targetId, UUID attackerId, String dimensionId, long expiresAt, double startDownwardVelocity, float startFallDistance) {
            this.targetId = targetId;
            this.attackerId = attackerId;
            this.dimensionId = dimensionId;
            this.expiresAt = expiresAt;
            this.startDownwardVelocity = startDownwardVelocity;
            this.startFallDistance = startFallDistance;
        }
    }

    private static final class PlayerSlamState {
        private final UUID playerId;
        private final String dimensionId;
        private final long expiresAt;
        private final float startFallDistance;

        private PlayerSlamState(UUID playerId, String dimensionId, long expiresAt, float startFallDistance) {
            this.playerId = playerId;
            this.dimensionId = dimensionId;
            this.expiresAt = expiresAt;
            this.startFallDistance = startFallDistance;
        }
    }
}
