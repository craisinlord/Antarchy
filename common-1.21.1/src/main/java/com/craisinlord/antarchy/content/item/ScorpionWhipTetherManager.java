package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public final class ScorpionWhipTetherManager {
    private static final int DEFAULT_TETHER_DURATION_TICKS = 200;
    private static final double BREAK_RANGE_MULTIPLIER = 3.0D;
    private static final Map<UUID, TetherState> TETHERS = new ConcurrentHashMap<>();

    private ScorpionWhipTetherManager() {
    }

    public static boolean hasTether(Player player) {
        return TETHERS.containsKey(player.getUUID());
    }

    public static boolean isTetheredTo(Player player, Entity entity) {
        TetherState state = TETHERS.get(player.getUUID());
        return state != null && entity.getUUID().equals(state.targetUuid);
    }

    public static Entity getTarget(ServerPlayer player) {
        TetherState state = TETHERS.get(player.getUUID());
        return state == null ? null : player.serverLevel().getEntity(state.targetUuid);
    }

    public static boolean canTether(LivingEntity target) {
        return !(target instanceof Player)
                && !target.getType().is(AntarchyTags.Entities.SCORPION_WHIP_BLACKLIST)
                && !target.getType().is(AntarchyTags.Entities.SCORPION_WHIP_IMMUNE);
    }

    public static void attach(ServerPlayer player, LivingEntity target) {
        clear(player);
        TetherState state = new TetherState(target.getUUID());
        state.marked = target.hasEffect(MobEffects.POISON);
        TETHERS.put(player.getUUID(), state);
        ScorpionWhipTetherSync.send(player, target.getId());
    }

    public static void clear(ServerPlayer player) {
        TetherState removed = TETHERS.remove(player.getUUID());
        if (removed == null) {
            return;
        }
        ScorpionWhipTetherSync.send(player, -1);
    }

    public static boolean pullAndDetach(ServerPlayer player) {
        TetherState state = TETHERS.get(player.getUUID());
        if (state == null) {
            return false;
        }

        Entity targetEntity = player.serverLevel().getEntity(state.targetUuid);
        if (!(targetEntity instanceof LivingEntity target) || !isValid(player, target, state)) {
            clear(player);
            return false;
        }

        Vec3 toPlayer = player.position().subtract(target.position());
        double distance = toPlayer.length();
        if (distance < 0.001D) {
            return false;
        }

        Vec3 direction = toPlayer.scale(1.0D / distance);
        boolean heavy = isHeavy(target);
        double pullStrength = heavy ? AntarchySettings.scorpionWhipHeavyPullMultiplier() : AntarchySettings.scorpionWhipPullStrength();
        Vec3 targetVelocity = target.getDeltaMovement().add(direction.scale(pullStrength));
        targetVelocity = new Vec3(targetVelocity.x, Mth.clamp(targetVelocity.y + 0.08D, -0.35D, 0.35D), targetVelocity.z);
        target.setDeltaMovement(targetVelocity);
        target.hurtMarked = true;

        if (heavy) {
            Vec3 playerVelocity = player.getDeltaMovement().add(direction.scale(-AntarchySettings.scorpionWhipSelfPullMultiplier()));
            player.setDeltaMovement(playerVelocity.x, Mth.clamp(playerVelocity.y + 0.04D, -0.35D, 0.35D), playerVelocity.z);
            player.hurtMarked = true;
        }

        double damage = AntarchySettings.scorpionWhipSnapBonusDamage();
        if (state.marked) {
            damage += Math.max(2.0D, AntarchySettings.scorpionWhipSnapBonusDamage() * 0.25D);
        }
        target.hurt(player.damageSources().playerAttack(player), (float) damage);

        Vec3 ripBack = direction.scale(-0.4D);
        target.setDeltaMovement(target.getDeltaMovement().add(ripBack.x, 0.12D, ripBack.z));
        target.hurtMarked = true;

        player.level().playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.85F, 0.7F);
        clear(player);
        return true;
    }

    public static void tick(ServerPlayer player) {
        TetherState state = TETHERS.get(player.getUUID());
        if (state == null) {
            return;
        }

        state.remainingTicks--;

        Entity targetEntity = player.serverLevel().getEntity(state.targetUuid);
        if (!(targetEntity instanceof LivingEntity target) || !isValid(player, target, state)) {
            clear(player);
            return;
        }

        applyTautTether(player, target);

        if (state.remainingTicks % 20 == 0) {
            ScorpionWhipTetherSync.send(player, targetEntity.getId());
        }
    }

    private static boolean isValid(ServerPlayer player, LivingEntity target, TetherState state) {
        double maxRange = AntarchySettings.scorpionWhipTetherMaxRange();
        double breakRange = maxRange * BREAK_RANGE_MULTIPLIER;
        return player.isAlive()
                && target.isAlive()
                && !target.isRemoved()
                && player.level() == target.level()
                && state.remainingTicks > 0
                && isHoldingWhip(player)
                && player.distanceToSqr(target) <= breakRange * breakRange
                && !target.getType().is(AntarchyTags.Entities.SCORPION_WHIP_IMMUNE)
                && !target.getType().is(AntarchyTags.Entities.SCORPION_WHIP_BLACKLIST);
    }

    private static void applyTautTether(ServerPlayer player, LivingEntity target) {
        double maxRange = AntarchySettings.scorpionWhipTetherMaxRange();
        Vec3 toPlayer = player.position().subtract(target.position());
        double distance = toPlayer.length();
        if (distance <= maxRange || distance < 0.001D) {
            return;
        }

        Vec3 direction = toPlayer.scale(1.0D / distance);
        double overshoot = distance - maxRange;
        boolean heavy = isHeavy(target);
        double pullStrength = heavy
                ? AntarchySettings.scorpionWhipHeavyPullMultiplier()
                : AntarchySettings.scorpionWhipPullStrength();
        double leashPull = Mth.clamp(overshoot * 0.18D, 0.08D, pullStrength);

        Vec3 targetVelocity = target.getDeltaMovement().add(direction.scale(leashPull));
        targetVelocity = new Vec3(targetVelocity.x, Mth.clamp(targetVelocity.y + 0.04D, -0.3D, 0.3D), targetVelocity.z);
        target.setDeltaMovement(targetVelocity);
        target.hurtMarked = true;

        if (heavy) {
            Vec3 playerVelocity = player.getDeltaMovement().add(direction.scale(-AntarchySettings.scorpionWhipSelfPullMultiplier() * 0.5D));
            player.setDeltaMovement(playerVelocity.x, Mth.clamp(playerVelocity.y + 0.02D, -0.3D, 0.3D), playerVelocity.z);
            player.hurtMarked = true;
        }
    }

    private static boolean isHoldingWhip(Player player) {
        return player.getMainHandItem().getItem() instanceof ScorpionWhipItem
                || player.getOffhandItem().getItem() instanceof ScorpionWhipItem;
    }

    private static boolean isHeavy(LivingEntity target) {
        return target.getMaxHealth() >= 80.0D || target.getBbWidth() >= 2.0F;
    }

    public static final class TetherState {
        private final UUID targetUuid;
        private int remainingTicks = DEFAULT_TETHER_DURATION_TICKS;
        private boolean marked = false;

        private TetherState(UUID targetUuid) {
            this.targetUuid = targetUuid;
        }
    }
}
