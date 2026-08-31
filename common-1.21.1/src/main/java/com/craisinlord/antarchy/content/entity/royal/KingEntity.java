package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.decree.AdvanceDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.CloseQuartersDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.ComeNoCloserDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.DoNotRunDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.FightMeCowardDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.HandsOffTheCrownDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.KeepYourDistanceDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.KneelDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.NoRespiteDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.NoRetreatDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.RoyalDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.ShowNoMercyDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.SkyIsMineDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.StandTallDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.StandYourGroundDecree;
import java.util.EnumMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KingEntity extends RoyalBossEntity {
    public enum Behavior {
        AIRBORNE, SPRINTING, RETREATING, HUGGING, RANGED, KEEPING_FAR
    }

    private static final int STOMP_COOLDOWN_TICKS = 220;
    private static final int STOMP_WINDUP_TICKS = 18;
    private static final double STOMP_RADIUS = 15.0D;
    private static final int WING_GUST_COOLDOWN_TICKS = 320;
    private static final int WING_GUST_WINDUP_TICKS = 14;
    private static final double WING_GUST_RADIUS = 22.0D;
    private static final int CHAIN_LIGHTNING_INTERVAL = 12;
    private static final int CHAIN_LIGHTNING_JUMPS = 4;
    private static final double CHAIN_LIGHTNING_JUMP_RANGE = 9.0D;
    private static final int ICE_FREEZE_PER_TICK = 7;
    private static final int BEHAVIOR_SCORE_CAP = 12;

    @Nullable
    private Vec3 patrolCenter;
    private int patrolCooldownTicks;
    private int decreeCooldownTicks;
    private int activeDecreeTicks;
    private int stompCooldownTicks;
    private int stompWindupTicks;
    private int wingGustCooldownTicks;
    private int wingGustWindupTicks;
    private boolean decreeRetreatPressure;
    private final Map<UUID, Long> judgmentCooldowns = new HashMap<>();
    private final Map<UUID, Long> lastPlayerDamageTime = new HashMap<>();
    private final Map<UUID, Integer> iceBuildup = new HashMap<>();
    private final EnumMap<Behavior, Integer> behaviorScores = new EnumMap<>(Behavior.class);
    @Nullable
    private RoyalDecree activeDecree;
    private static final List<RoyalDecree> DECREES = List.of(
            new SkyIsMineDecree(), new DoNotRunDecree(), new KneelDecree(), new CloseQuartersDecree(),
            new ComeNoCloserDecree(), new FightMeCowardDecree(), new NoRespiteDecree(), new NoRetreatDecree(),
            new KeepYourDistanceDecree(), new StandTallDecree(), new StandYourGroundDecree(),
            new HandsOffTheCrownDecree(), new AdvanceDecree(), new ShowNoMercyDecree());

    public KingEntity(EntityType<? extends KingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.kingHealth(), AntarchySettings.kingAttackDamage());
    }

    @Override
    protected String geoName() {
        return "king";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.KING_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.KING_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.KING_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.KING_BITE.get();
    }

    @Override
    protected SoundEvent royalBeamShootSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound(RoyalHead head) {
        return head.slot() == RoyalHead.Slot.LEFT
                ? AntarchySoundEvents.KING_FIREBALL_SHOOT.get()
                : AntarchySoundEvents.KING_BEAM_SHOOT.get();
    }

    @Override
    protected SoundEvent royalBeamLoopSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamEndSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected RoyalBeamSettings royalBeamSettings() {
        return new RoyalBeamSettings(AntarchySettings.kingBeamRange(), AntarchySettings.kingBeamTracking(), 7.5D,
                AntarchySettings.kingBeamDurationTicks(), AntarchySettings.kingBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.kingBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.kingBeamTerrainRadius(), 4.0F, AntarchySettings.kingBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.BUILD_ICE;
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode(@Nullable RoyalHead head) {
        return head != null && head.slot() == RoyalHead.Slot.RIGHT ? RoyalBeamTerrainMode.BUILD_ICE : RoyalBeamTerrainMode.NONE;
    }

    @Override
    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        if (head == null) {
            return RoyalBeamElement.GENERIC;
        }
        return switch (head.slot()) {
            case LEFT -> RoyalBeamElement.FIRE;
            case CENTER -> RoyalBeamElement.LIGHTNING;
            case RIGHT -> RoyalBeamElement.ICE;
        };
    }

    @Override
    protected void tickRoyalBeamEffects(RoyalHead head, Vec3 start, Vec3 end) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        var particle = switch (head.slot()) {
            case LEFT -> ParticleTypes.FLAME;
            case CENTER -> ParticleTypes.ELECTRIC_SPARK;
            case RIGHT -> ParticleTypes.SNOWFLAKE;
        };
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (distance > 0.001D) {
            direction = direction.scale(1.0D / distance);
            for (double walked = 0.0D; walked <= distance; walked += 2.5D) {
                Vec3 point = start.add(direction.scale(walked));
                level.sendParticles(particle, point.x, point.y, point.z, 2, 0.12D, 0.12D, 0.12D, 0.01D);
            }
        }
        LivingEntity target = head.target(this.level());
        switch (head.slot()) {
            case LEFT -> {
                if (target != null && target.isAlive()) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 40));
                }
            }
            case CENTER -> {
                if (this.tickCount % CHAIN_LIGHTNING_INTERVAL == 0) {
                    this.chainLightning(level, end);
                }
            }
            case RIGHT -> {
                if (target != null && target.isAlive()) {
                    this.applyIceFreezePressure(target);
                }
            }
        }
    }

    private void chainLightning(ServerLevel level, Vec3 origin) {
        java.util.Set<UUID> struck = new java.util.HashSet<>();
        Vec3 from = origin;
        float damage = (float) AntarchySettings.kingBeamDamage() * 0.6F;
        for (int jump = 0; jump < CHAIN_LIGHTNING_JUMPS; jump++) {
            LivingEntity next = null;
            double bestScore = Double.MAX_VALUE;
            for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(from, from).inflate(CHAIN_LIGHTNING_JUMP_RANGE),
                    e -> e.isAlive() && !(e instanceof RoyalBossEntity) && e.getType() != this.getType())) {
                if (struck.contains(candidate.getUUID())) {
                    continue;
                }
                double score = candidate.position().distanceToSqr(from) - (isConductive(candidate) ? 16.0D : 0.0D);
                if (score < bestScore) {
                    bestScore = score;
                    next = candidate;
                }
            }
            if (next == null) {
                break;
            }
            struck.add(next.getUUID());
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(next.getX(), next.getY(), next.getZ());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            next.hurt(this.damageSources().lightningBolt(), damage);
            from = next.position().add(0.0D, next.getBbHeight() * 0.5D, 0.0D);
            if (isConductive(next)) {
                jump--;
            }
        }
    }

    private static boolean isConductive(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorItem armor) {
                var material = armor.getMaterial();
                if (material == ArmorMaterials.IRON || material == ArmorMaterials.NETHERITE || material == ArmorMaterials.CHAIN) {
                    return true;
                }
            }
        }
        return false;
    }

    private void applyIceFreezePressure(LivingEntity target) {
        int buildup = this.iceBuildup.merge(target.getUUID(), ICE_FREEZE_PER_TICK, Integer::sum);
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + ICE_FREEZE_PER_TICK * 2));
        if (buildup >= 60) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
            this.iceBuildup.put(target.getUUID(), 20);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.iceBuildup.size() > 16) {
            this.iceBuildup.clear();
        }
        LivingEntity target = this.getTarget();
        if (target != null && !this.isDeadOrDying() && this.level() instanceof ServerLevel level) {
            this.trackBehavior(target);
            this.tickDecree(level, target);
            this.tickKingWholeBody(level, target);
            return;
        }

        if (this.isDeadOrDying() || !(this.level() instanceof ServerLevel)) {
            return;
        }
        this.decreeRetreatPressure = false;
        if (this.patrolCenter == null || this.position().distanceToSqr(this.patrolCenter) > 64.0D * 64.0D) {
            this.patrolCenter = this.position();
        }
        if (this.patrolCooldownTicks-- <= 0) {
            this.patrolCooldownTicks = 100 + this.random.nextInt(100);
            this.playSound(AntarchySoundEvents.KING_WING_FLAP.get(), 2.0F, 0.9F + this.random.nextFloat() * 0.15F);
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 12.0D + this.random.nextDouble() * 18.0D;
            double px = this.patrolCenter.x + Math.cos(angle) * radius;
            double pz = this.patrolCenter.z + Math.sin(angle) * radius;
            double py = this.groundYBelow(px, pz) + FLYING_PREFERRED_HOVER + this.random.nextDouble() * 5.0D;
            this.getMoveControl().setWantedPosition(px, py, pz, 1.0D);
        }
    }

    private void trackBehavior(LivingEntity target) {
        double distance = target.distanceTo(this);
        this.bumpBehavior(Behavior.AIRBORNE, !target.onGround() && !target.isPassenger() ? 1 : -1);
        this.bumpBehavior(Behavior.SPRINTING, target.isSprinting() ? 1 : -1);
        this.bumpBehavior(Behavior.HUGGING, distance <= 10.0D ? 1 : -1);
        this.bumpBehavior(Behavior.KEEPING_FAR, distance >= 22.0D ? 1 : -1);
        if (this.patrolCenter != null) {
            this.bumpBehavior(Behavior.RETREATING, distance > 20.0D && target.getDeltaMovement().horizontalDistanceSqr() > 0.02D
                    && target.position().subtract(this.position()).dot(target.getDeltaMovement()) > 0.0D ? 1 : -1);
        }
    }

    private void bumpBehavior(Behavior behavior, int delta) {
        int updated = Math.max(0, Math.min(BEHAVIOR_SCORE_CAP, this.behaviorScores.getOrDefault(behavior, 0) + delta));
        this.behaviorScores.put(behavior, updated);
    }

    public int behaviorScore(Behavior behavior) {
        return this.behaviorScores.getOrDefault(behavior, 0);
    }

    public void setDecreeRetreatPressure(boolean value) {
        this.decreeRetreatPressure = value;
    }

    private void tickKingWholeBody(ServerLevel level, LivingEntity target) {
        if (this.stompCooldownTicks > 0) this.stompCooldownTicks--;
        if (this.wingGustCooldownTicks > 0) this.wingGustCooldownTicks--;

        boolean skyIsMine = this.activeDecree instanceof SkyIsMineDecree;
        boolean forceFar = this.decreeRetreatPressure
                || this.activeDecree instanceof ComeNoCloserDecree
                || this.activeDecree instanceof KeepYourDistanceDecree;

        if (this.stompWindupTicks > 0) {
            if (--this.stompWindupTicks == 0) {
                this.performStomp(level, target);
            }
        } else if (this.wingGustWindupTicks > 0) {
            if (--this.wingGustWindupTicks == 0) {
                this.performWingGust(level, target);
            }
        } else {
            double distance = target.distanceTo(this);
            if (this.stompCooldownTicks <= 0 && (distance <= STOMP_RADIUS || skyIsMine)) {
                this.stompCooldownTicks = skyIsMine ? STOMP_COOLDOWN_TICKS / 2 : STOMP_COOLDOWN_TICKS;
                this.stompWindupTicks = STOMP_WINDUP_TICKS;
                this.triggerAnim("body_action", "stomp");
                this.playSound(AntarchySoundEvents.KING_ROAR.get(), 3.0F, 0.7F);
                if (skyIsMine) {
                    this.getMoveControl().setWantedPosition(this.getX(), target.getY(), this.getZ(), 1.2D);
                }
            } else if (this.wingGustCooldownTicks <= 0 && distance <= WING_GUST_RADIUS && (forceFar || this.random.nextInt(3) == 0)) {
                this.wingGustCooldownTicks = WING_GUST_COOLDOWN_TICKS;
                this.wingGustWindupTicks = WING_GUST_WINDUP_TICKS;
                this.triggerAnim("body_action", "wing_gust");
                this.playSound(AntarchySoundEvents.KING_WING_FLAP.get(), 3.0F, 0.85F);
            }
        }

        if (forceFar && target.distanceTo(this) < 18.0D) {
            Vec3 away = this.position().subtract(target.position()).normalize().scale(24.0D);
            double ax = this.getX() + away.x;
            double az = this.getZ() + away.z;
            double ay = this.groundYBelow(ax, az) + FLYING_PREFERRED_HOVER + 6.0D;
            this.getMoveControl().setWantedPosition(ax, ay, az, 1.3D);
        }
    }

    private void performStomp(ServerLevel level, LivingEntity target) {
        DamageSource source = this.damageSources().mobAttack(this);
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.4F;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(STOMP_RADIUS),
                e -> e.isAlive() && e instanceof Player)) {
            double distance = Math.max(1.0D, living.distanceTo(this));
            float falloff = (float) Math.max(0.2D, 1.0D - distance / (STOMP_RADIUS + 6.0D));
            living.hurt(source, base * falloff);
            Vec3 push = living.position().subtract(this.position()).normalize().scale(2.4D * falloff);
            living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.85D, push.z));
            living.hasImpulse = true;
        }
        RoyalBlockDestruction.destroySphere(level, this,
                new Vec3(this.getX(), this.getY(), this.getZ()), STOMP_RADIUS * 0.5D, 96, 80.0D, 0.15F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 6, 4.0D, 0.5D, 4.0D, 0.0D);
        this.playSound(AntarchySoundEvents.KING_STOMP.get(), 3.5F, 0.8F);
    }

    private void performWingGust(ServerLevel level, LivingEntity target) {
        Vec3 flat = this.getForward().multiply(1.0D, 0.0D, 1.0D);
        Vec3 forward = flat.lengthSqr() < 1.0E-4D
                ? target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize()
                : flat.normalize();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(WING_GUST_RADIUS),
                e -> e.isAlive() && e instanceof Player)) {
            Vec3 toTarget = living.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() < 1.0E-4D || forward.dot(toTarget.normalize()) < 0.1D) {
                continue;
            }
            Vec3 push = toTarget.normalize().scale(3.2D);
            living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.5D, push.z));
            living.hasImpulse = true;
            living.hurt(this.damageSources().mobAttack(this), 6.0F);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + forward.x * 6.0D, this.getY() + 4.0D, this.getZ() + forward.z * 6.0D, 12, 4.0D, 2.0D, 4.0D, 0.0D);
    }

    private void tickDecree(ServerLevel level, LivingEntity target) {
        if (this.activeDecree == null) {
            if (this.decreeCooldownTicks-- > 0) {
                return;
            }
            this.activeDecreeTicks = 300;
            this.activeDecree = this.pickDecree(target);
            this.decreeRetreatPressure = false;
            this.playSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 3.0F, 0.9F + this.random.nextFloat() * 0.15F);
            this.playSound(AntarchySoundEvents.KING_ROAR.get(), 3.0F, 0.8F + this.random.nextFloat() * 0.12F);
            if (target instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 300, 10));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(this.activeDecree.translationKey())
                        .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true))));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(this.activeDecree.instructionKey())
                        .withStyle(style -> style.withColor(ChatFormatting.WHITE))));
            }
        }
        int countdown = this.activeDecree.countdownTicks(target);
        if (countdown > 0 && countdown <= 100 && this.tickCount % 20 == 0) {
            level.playSound(null, target.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 0.8F, 1.0F + countdown / 500.0F);
        }
        this.activeDecree.apply(level, this, target);
        if (this.activeDecree != null && --this.activeDecreeTicks <= 0) {
            this.endDecree(target);
        }
    }

    private RoyalDecree pickDecree(LivingEntity target) {
        int totalWeight = 0;
        int[] weights = new int[DECREES.size()];
        for (int i = 0; i < DECREES.size(); i++) {
            weights[i] = Math.max(1, DECREES.get(i).contextWeight(this, target));
            totalWeight += weights[i];
        }
        int roll = this.random.nextInt(totalWeight);
        for (int i = 0; i < DECREES.size(); i++) {
            roll -= weights[i];
            if (roll < 0) {
                return DECREES.get(i);
            }
        }
        return DECREES.get(this.random.nextInt(DECREES.size()));
    }

    private void endDecree(@Nullable LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            player.connection.send(new ClientboundClearTitlesPacket(false));
        }
        if (this.activeDecree != null) {
            this.activeDecree.onEnded();
        }
        this.activeDecree = null;
        this.activeDecreeTicks = 0;
        this.decreeRetreatPressure = false;
        this.decreeCooldownTicks = AntarchySettings.royalDecreeCooldownTicks();
    }

    public void clearActiveDecree() {
        this.clearActiveDecree(null);
    }

    public void clearActiveDecree(@Nullable LivingEntity target) {
        this.endDecree(target);
    }

    public boolean blocksHealing() {
        return this.activeDecree instanceof NoRespiteDecree;
    }

    public long ticksSincePlayerDamage(LivingEntity target) {
        long last = this.lastPlayerDamageTime.getOrDefault(target.getUUID(), 0L);
        return this.level().getGameTime() - last;
    }

    public void invokeJudgment(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        long now = this.level().getGameTime();
        if (this.judgmentCooldowns.getOrDefault(target.getUUID(), 0L) <= now) {
            this.judgmentCooldowns.put(target.getUUID(), now + 20L);
            target.hurt(this.damageSources().magic(), 6.0F);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            this.lastPlayerDamageTime.put(attacker.getUUID(), this.level().getGameTime());
            boolean projectile = source.getDirectEntity() instanceof Projectile;
            this.bumpBehavior(projectile ? Behavior.RANGED : Behavior.HUGGING, 2);
            if (this.activeDecree instanceof CloseQuartersDecree && projectile) {
                this.invokeJudgment(attacker);
            } else if (this.activeDecree instanceof ComeNoCloserDecree && !projectile) {
                this.invokeJudgment(attacker);
            } else if (this.activeDecree instanceof HandsOffTheCrownDecree) {
                this.invokeJudgment(attacker);
            }
        }
        return super.hurt(source, amount);
    }

    public static boolean blocksHealingAround(LivingEntity target) {
        if (target.level().isClientSide) return false;
        return target.level().getEntitiesOfClass(KingEntity.class, target.getBoundingBox().inflate(128.0D),
                king -> king.isAlive() && king.blocksHealing()).stream().findFirst().isPresent();
    }
}
