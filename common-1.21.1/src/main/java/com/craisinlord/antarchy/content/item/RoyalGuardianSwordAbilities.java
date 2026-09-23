package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Player-safe kernels for the Royal Guardian Sword's King-derived elemental modes. */
public final class RoyalGuardianSwordAbilities {
    private static final ThreadLocal<Integer> SECONDARY_DAMAGE_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<MeleeAttackContext> MELEE_ATTACK = new ThreadLocal<>();
    private static final DustParticleOptions FIRE_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.55F, 0.08F), 1.6F);
    private static final DustParticleOptions STORM_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.86F, 0.25F), 1.25F);

    private RoyalGuardianSwordAbilities() {
    }

    public static boolean isApplyingSecondaryDamage() {
        return SECONDARY_DAMAGE_DEPTH.get() > 0;
    }

    public static void beginMeleeAttack(Player player, Entity target) {
        boolean charged = player.getMainHandItem().getItem() instanceof RoyalGuardianSwordItem
                && player.getAttackStrengthScale(0.5F) >= 0.9F;
        MELEE_ATTACK.set(new MeleeAttackContext(player.getUUID(), target.getUUID(), charged));
    }

    public static boolean isChargedMeleeAttack(Player player, LivingEntity target) {
        MeleeAttackContext context = MELEE_ATTACK.get();
        return context != null && context.charged && context.playerId.equals(player.getUUID())
                && context.targetId.equals(target.getUUID());
    }

    public static void endMeleeAttack(Player player) {
        MeleeAttackContext context = MELEE_ATTACK.get();
        if (context != null && context.playerId.equals(player.getUUID())) MELEE_ATTACK.remove();
    }

    public static boolean applySecondaryDamage(Player owner, LivingEntity target, float amount) {
        if (amount <= 0.0F || !canDamageTarget(owner, target, false)) return false;
        int depth = SECONDARY_DAMAGE_DEPTH.get();
        SECONDARY_DAMAGE_DEPTH.set(depth + 1);
        try {
            return target.hurt(owner.damageSources().playerAttack(owner), amount);
        } finally {
            if (depth == 0) SECONDARY_DAMAGE_DEPTH.remove();
            else SECONDARY_DAMAGE_DEPTH.set(depth);
        }
    }

    public static void discharge(Player owner, LivingEntity primary, ItemStack swordStack) {
        if (!(owner.level() instanceof ServerLevel level) || !canDamageTarget(owner, primary, true)
                || !(swordStack.getItem() instanceof RoyalGuardianSwordItem)
                || !RoyalGuardianSwordItem.isDischargeReady(swordStack, level.getGameTime())) return;

        RoyalGuardianSwordItem.Mode mode = RoyalGuardianSwordItem.getMode(swordStack);
        if (mode == RoyalGuardianSwordItem.Mode.NONE) return;

        switch (mode) {
            case FIRE -> fireImpact(level, owner, primary);
            case FROST -> frostFan(level, owner, primary);
            case STORM -> chainLightning(level, owner, primary);
            case NONE -> {
            }
        }
        RoyalGuardianSwordItem.startDischargeCooldown(swordStack, level.getGameTime());
    }

    private static void fireImpact(ServerLevel level, Player owner, LivingEntity primary) {
        Vec3 center = primary.position().add(0.0D, primary.getBbHeight() * 0.5D, 0.0D);
        applySecondaryDamage(owner, primary, (float) AntarchySettings.royalGuardianSwordFirePrimaryDamage());
        if (primary.isAlive()) primary.setRemainingFireTicks(Math.max(primary.getRemainingFireTicks(), 100));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(4.0D),
                candidate -> candidate != primary && canDamageTarget(owner, candidate, false))) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            float falloff = (float) Math.max(0.25D, 1.0D - distance / 5.0D);
            applySecondaryDamage(owner, target, (float) AntarchySettings.royalGuardianSwordFireSplashDamage() * falloff);
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
        }
        if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) placeBoundedFire(level, center, 6);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 55, 1.4D, 1.0D, 1.4D, 0.1D);
        level.sendParticles(FIRE_GOLD, center.x, center.y, center.z, 28, 1.1D, 0.8D, 1.1D, 0.04D);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.8D, 0.6D, 0.8D, 0.0D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.05F);
    }

    private static void placeBoundedFire(ServerLevel level, Vec3 center, int cap) {
        int placed = 0;
        BlockPos base = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(2, 2, 2))) {
            if (placed >= cap) return;
            if (pos.distToCenterSqr(center.x, center.y, center.z) > 7.0D || !level.isEmptyBlock(pos)) continue;
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) continue;
            BlockState fire = Blocks.FIRE.defaultBlockState();
            if (fire.canSurvive(level, pos) && level.setBlockAndUpdate(pos, fire)) placed++;
        }
    }

    private static void frostFan(ServerLevel level, Player owner, LivingEntity primary) {
        if (primary.isAlive()) {
            primary.setTicksFrozen(Math.min(primary.getTicksRequiredToFreeze(), primary.getTicksFrozen() + 50));
            primary.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 1), owner);
        }
        Vec3 forward = primary.position().subtract(owner.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-5D) forward = owner.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-5D) forward = new Vec3(0.0D, 0.0D, 1.0D);
        forward = forward.normalize();
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        for (int distanceStep = 1; distanceStep <= 5; distanceStep++) {
            double distance = distanceStep * 4.0D;
            for (int lane = -1; lane <= 1; lane++) {
                Vec3 point = owner.position().add(forward.scale(distance))
                        .add(side.scale(lane * (distanceStep % 2 == 0 ? 2.0D : 0.0D)));
                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        net.minecraft.util.Mth.floor(point.x), net.minecraft.util.Mth.floor(point.z));
                Vec3 spawn = new Vec3(point.x, groundY + 0.1D, point.z);
                RoyalIceSpikeEntity spike = RoyalIceSpikeEntity.create(level, spawn, owner,
                        (float) AntarchySettings.royalGuardianSwordFrostSpikeDamage(), 2.2D, 100, 0, 0);
                level.addFreshEntity(spike);
            }
        }
        Vec3 center = primary.position().add(0.0D, 0.8D, 0.0D);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 70, 1.3D, 1.1D, 1.3D, 0.1D);
        level.sendParticles(ParticleTypes.SNOWFLAKE, owner.getX(), owner.getY() + 1.0D, owner.getZ(), 18, 0.7D, 0.5D, 0.7D, 0.04D);
        level.playSound(null, primary.blockPosition(), AntarchySoundEvents.KING_ICE_SPIKES.get(), SoundSource.PLAYERS, 0.8F, 0.95F);
    }

    private static void chainLightning(ServerLevel level, Player owner, LivingEntity primary) {
        Set<UUID> struck = new HashSet<>();
        struck.add(primary.getUUID());
        strikeVisual(level, primary.position().add(0.0D, primary.getBbHeight() * 0.5D, 0.0D));
        applySecondaryDamage(owner, primary, (float) AntarchySettings.royalGuardianSwordStormPrimaryDamage());
        LivingEntity previous = primary;
        float damage = (float) AntarchySettings.royalGuardianSwordStormJumpDamage();
        double jumpRange = Math.max(0.0D, AntarchySettings.royalGuardianSwordStormJumpRange());
        for (int jump = 0; jump < Math.max(0, AntarchySettings.royalGuardianSwordStormMaxJumps()); jump++) {
            Vec3 origin = previous.position().add(0.0D, previous.getBbHeight() * 0.5D, 0.0D);
            LivingEntity next = level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(jumpRange),
                            candidate -> !struck.contains(candidate.getUUID()) && canDamageTarget(owner, candidate, false)
                                    && candidate.distanceToSqr(origin) <= jumpRange * jumpRange)
                    .stream().min(java.util.Comparator.comparingDouble((LivingEntity candidate) -> candidate.distanceToSqr(origin))
                            .thenComparingInt(LivingEntity::getId)).orElse(null);
            if (next == null) break;
            struck.add(next.getUUID());
            Vec3 end = next.position().add(0.0D, next.getBbHeight() * 0.5D, 0.0D);
            drawLightning(level, origin, end);
            applySecondaryDamage(owner, next, damage);
            next.setRemainingFireTicks(Math.max(next.getRemainingFireTicks(), 20));
            previous = next;
            damage *= 0.76F;
        }
        level.playSound(null, primary.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.45F);
    }

    private static void strikeVisual(ServerLevel level, Vec3 at) {
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        bolt.moveTo(at.x, at.y, at.z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        level.sendParticles(STORM_GOLD, at.x, at.y, at.z, 20, 0.45D, 0.7D, 0.45D, 0.1D);
    }

    private static void drawLightning(ServerLevel level, Vec3 start, Vec3 end) {
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        bolt.moveTo(end.x, end.y, end.z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        if (length > 0.001D) {
            Vec3 step = direction.scale(1.0D / length);
            for (double d = 0.0D; d < length; d += 0.35D) {
                Vec3 point = start.add(step.scale(d));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
                level.sendParticles(STORM_GOLD, point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 18, 0.25D, 0.4D, 0.25D, 0.08D);
    }

    public static boolean canDamageTarget(Player owner, LivingEntity target, boolean primary) {
        if (target == null || target == owner || (!primary && !target.isAlive())) return false;
        if (target.isAlliedTo(owner) || owner.isAlliedTo(target)) return false;
        if (target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())) return false;
        if (target instanceof CommandedEntityAccess access && owner.getUUID().equals(access.antarchy$getCommanderUuid())) return false;
        if (target instanceof Player other) return owner.canHarmPlayer(other);
        if (primary) return true;
        return target instanceof Mob mob && (mob.getType().getCategory() == MobCategory.MONSTER
                || mob.getTarget() == owner || mob.getLastHurtByMob() == owner);
    }

    private record MeleeAttackContext(UUID playerId, UUID targetId, boolean charged) {
    }
}
