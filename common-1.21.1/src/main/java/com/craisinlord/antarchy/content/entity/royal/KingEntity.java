package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
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
import com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu;
import com.craisinlord.antarchy.content.menu.RoyalJudgmentState;
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
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
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
    /** Keep the tree-bound patrol safely inside the authored tree footprint. */
    private static final double TREE_ORBIT_RADIUS = 128.0D;
    private static final double TREE_RETURN_RADIUS = 192.0D;
    private static final double TREE_RETURN_RELEASE_RADIUS = 144.0D;
    private static final double TREE_ORBIT_ANGLE_STEP = 0.014D;
    private static final int TREE_ORBIT_PATH_SAMPLES = 16;
    private static final double KING_FOLLOW_RANGE = 192.0D;
    private static final double COME_NO_CLOSER_RADIUS = 16.0D;
    private static final int COME_NO_CLOSER_PARTICLE_INTERVAL = 4;
    private static final int COME_NO_CLOSER_PARTICLE_COUNT = 40;

    @Nullable
    private Vec3 patrolCenter;
    @Nullable
    private Vec3 treePatrolCenter;
    @Nullable
    private Vec3 treePatrolWaypoint;
    private double treePatrolMinimumY;
    private double treePatrolMaximumY;
    private double treePatrolAngle;
    private boolean treePatrolBound;
    private boolean returningToTree;
    private int patrolCooldownTicks;
    private int decreeCooldownTicks;
    private int activeDecreeTicks;
    private int stompCooldownTicks;
    private int wingGustCooldownTicks;
    private int fireballCooldownTicks = 80;
    private int iceballCooldownTicks = 120;
    private int iceSpikeCooldownTicks = 160;
    private boolean decreeRetreatPressure;
    @Nullable
    private ServerPlayer exileTarget;
    private int exileTicks;
    private boolean exileWingAnimationPending;
    @Nullable
    private ServerPlayer titheTarget;
    private int titheTicks;
    @Nullable
    private ServerPlayer targetShotPlayer;
    @Nullable
    private ServerPlayer targetShotVictim;
    private int targetShotTicks;
    @Nullable
    private ServerPlayer sealedTarget;
    private int sealedTicks;
    private long sealUntil;
    private final Map<UUID, Long> judgmentCooldowns = new HashMap<>();
    private final Map<UUID, Long> lastPlayerDamageTime = new HashMap<>();
    private final Map<UUID, Integer> iceBuildup = new HashMap<>();
    private final EnumMap<Behavior, Integer> behaviorScores = new EnumMap<>(Behavior.class);
    @Nullable
    private RoyalDecree activeDecree;
    private static final List<RoyalDecree> DECREES = List.of(
            new SkyIsMineDecree(), new DoNotRunDecree(), new KneelDecree(), new CloseQuartersDecree(),
            new ComeNoCloserDecree(), new FightMeCowardDecree(), new NoRespiteDecree(),
            new StandTallDecree(), new StandYourGroundDecree(),
            new HandsOffTheCrownDecree(), new ShowNoMercyDecree());

    public KingEntity(EntityType<? extends KingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.kingHealth(), AntarchySettings.kingAttackDamage())
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, KING_FOLLOW_RANGE);
    }

    @Override
    protected int maxConcurrentHeadAttacks(Phase phase) {
        return phase.maxConcurrentHeadAttacks();
    }

    public void setTreePatrolHome(Vec3 center, double minimumY, double maximumY, double angle) {
        this.treePatrolCenter = center;
        this.treePatrolMinimumY = minimumY;
        this.treePatrolMaximumY = maximumY;
        this.treePatrolAngle = angle;
        this.treePatrolWaypoint = null;
        this.treePatrolBound = true;
    }

    public boolean isTreePatrolFor(Vec3 center) {
        return this.treePatrolBound && this.treePatrolCenter != null
                && this.treePatrolCenter.distanceToSqr(center) < 1.0D;
    }

    @Override
    protected boolean shouldClearObstruction() {
        return this.getTarget() != null;
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
    protected SoundEvent royalFlyLoopSound() {
        return AntarchySoundEvents.KING_FLY_LOOP.get();
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
                AntarchySettings.kingBeamDurationTicks(), AntarchySettings.kingBeamTravelTicks(), AntarchySettings.kingBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.kingBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.kingBeamTerrainRadius(), 4.0F, AntarchySettings.kingBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.BUILD_ICE;
    }

    @Override
    protected double royalBeamMinimumRange() {
        return AntarchySettings.kingBeamMinimumRange();
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
            case LEFT -> null;
            case CENTER -> ParticleTypes.ELECTRIC_SPARK;
            case RIGHT -> ParticleTypes.SNOWFLAKE;
        };
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (particle != null && distance > 0.001D) {
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
        if (!this.level().isClientSide && this.shouldReturnToTree()) {
            this.returningToTree = true;
            this.setTarget(null);
        }
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.tickComeNoCloserIndicator();
        this.tickRoyalPunishment();
        this.tickPendingExileAnimation();
        long gameTime = this.level().getGameTime();
        this.judgmentCooldowns.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
        this.lastPlayerDamageTime.entrySet().removeIf(entry -> gameTime - entry.getValue() > 600L);
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.iceBuildup.entrySet().removeIf(entry -> serverLevel.getEntity(entry.getKey()) == null);
        if (this.iceBuildup.size() > 16) this.iceBuildup.clear();
        if (this.returningToTree) {
            this.setTarget(null);
            this.decreeRetreatPressure = false;
            if (this.treePatrolBound && this.treePatrolCenter != null) {
                this.setRoyalFlying(true);
                if (this.isWithinTreeReturnReleaseRadius()) {
                    this.returningToTree = false;
                } else {
                    this.tickTreePatrol();
                    return;
                }
            } else {
                this.returningToTree = false;
            }
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
        this.setRoyalFlying(true);
        this.decreeRetreatPressure = false;
        if (this.treePatrolBound && this.treePatrolCenter != null) {
            this.tickTreePatrol();
            return;
        }
        if (this.patrolCenter == null || this.position().distanceToSqr(this.patrolCenter) > 64.0D * 64.0D) {
            this.patrolCenter = this.position();
        }
        if (this.patrolCooldownTicks-- <= 0) {
            this.patrolCooldownTicks = 100 + this.random.nextInt(100);
            this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.9F + this.random.nextFloat() * 0.15F);
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 12.0D + this.random.nextDouble() * 18.0D;
            double px = this.patrolCenter.x + Math.cos(angle) * radius;
            double pz = this.patrolCenter.z + Math.sin(angle) * radius;
            double py = this.groundYBelow(px, pz) + FLYING_PREFERRED_HOVER + this.random.nextDouble() * 5.0D;
            this.getMoveControl().setWantedPosition(px, py, pz, 1.0D);
        }
    }

    private void tickComeNoCloserIndicator() {
        if (!(this.activeDecree instanceof ComeNoCloserDecree)
                || this.tickCount % COME_NO_CLOSER_PARTICLE_INTERVAL != 0
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int index = 0; index < COME_NO_CLOSER_PARTICLE_COUNT; index++) {
            double angle = (Math.PI * 2.0D * index) / COME_NO_CLOSER_PARTICLE_COUNT;
            double x = this.getX() + Math.cos(angle) * COME_NO_CLOSER_RADIUS;
            double z = this.getZ() + Math.sin(angle) * COME_NO_CLOSER_RADIUS;
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, this.getY() + 0.25D, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickTreePatrol() {
        Vec3 center = this.treePatrolCenter;
        if (center == null) {
            return;
        }
        this.treePatrolAngle += TREE_ORBIT_ANGLE_STEP;
        if (this.treePatrolAngle >= Math.PI * 2.0D) {
            this.treePatrolAngle -= Math.PI * 2.0D;
        }
        Vec3 waypoint = this.treePatrolWaypoint;
        if (waypoint == null || this.position().distanceToSqr(waypoint) < 36.0D) {
            waypoint = this.findTreePatrolWaypoint(center, this.treePatrolAngle);
            this.treePatrolWaypoint = waypoint;
        }
        if (waypoint != null) {
            this.getMoveControl().setWantedPosition(waypoint.x, waypoint.y, waypoint.z, 1.0D);
            this.getLookControl().setLookAt(center.x,
                    (this.treePatrolMinimumY + this.treePatrolMaximumY) * 0.5D,
                    center.z, 30.0F, 30.0F);
        }
    }

    private boolean shouldReturnToTree() {
        return this.treePatrolBound && this.treePatrolCenter != null
                && this.horizontalDistanceToTreeSqr() > TREE_RETURN_RADIUS * TREE_RETURN_RADIUS;
    }

    private boolean isWithinTreeReturnReleaseRadius() {
        return this.treePatrolCenter != null
                && this.horizontalDistanceToTreeSqr() <= TREE_RETURN_RELEASE_RADIUS * TREE_RETURN_RELEASE_RADIUS;
    }

    private double horizontalDistanceToTreeSqr() {
        if (this.treePatrolCenter == null) {
            return 0.0D;
        }
        double dx = this.getX() - this.treePatrolCenter.x;
        double dz = this.getZ() - this.treePatrolCenter.z;
        return dx * dx + dz * dz;
    }

    @Nullable
    private Vec3 findTreePatrolWaypoint(Vec3 center, double angle) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double candidateAngle = angle + attempt * 0.22D;
            double x = center.x + Math.cos(candidateAngle) * TREE_ORBIT_RADIUS;
            double z = center.z + Math.sin(candidateAngle) * TREE_ORBIT_RADIUS;
            double vertical = 0.5D + 0.5D * Math.sin(candidateAngle);
            double y = this.treePatrolMinimumY
                    + (this.treePatrolMaximumY - this.treePatrolMinimumY) * vertical;
            Vec3 candidate = new Vec3(x, y, z);
            if (this.clearTreePatrolPath(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean clearTreePatrolPath(Vec3 destination) {
        Vec3 start = this.position();
        for (int sample = 1; sample <= TREE_ORBIT_PATH_SAMPLES; sample++) {
            double progress = sample / (double) TREE_ORBIT_PATH_SAMPLES;
            Vec3 point = start.lerp(destination, progress);
            AABB box = this.getBoundingBox().move(point.x - this.getX(), point.y - this.getY(), point.z - this.getZ());
            if (!this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }

    private void tickRoyalPunishment() {
        if (this.exileTarget == null && this.titheTarget == null && this.targetShotPlayer == null && this.sealedTarget == null) {
            return;
        }
        if (this.exileTarget != null) {
            this.tickRoyalExile();
        }
        if (this.titheTarget != null) {
            this.tickRoyalTithe();
        }
        if (this.targetShotPlayer != null) {
            this.tickRoyalTargetShot();
        }
        if (this.sealedTarget != null) {
            this.tickRoyalSeal();
        }
    }

    private void tickRoyalExile() {
        if (this.exileTicks-- <= 0 || !this.exileTarget.isAlive() || this.exileTarget.level() != this.level()
                || this.isDeadOrDying()) {
            this.clearExile();
            return;
        }
        this.exileTarget.lookAt(EntityAnchorArgument.Anchor.EYES, this.getEyePosition());
        this.exileTarget.setPose(Pose.CROUCHING);
        this.exileTarget.setShiftKeyDown(true);
        if (this.exileTicks == 1) {
            Vec3 away = this.exileTarget.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() < 1.0E-4D) {
                away = this.getLookAngle().multiply(-1.0D, 0.0D, -1.0D);
            }
            away = away.normalize();
            this.exileTarget.setDeltaMovement(away.x * 2.6D, 1.35D, away.z * 2.6D);
            this.exileTarget.hasImpulse = true;
            this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.82F);
            this.exileWingAnimationPending = true;
        }
    }

    private void tickPendingExileAnimation() {
        if (!this.exileWingAnimationPending
                || !this.attackScheduler.ready("exile_wing_animation", RoyalAttackLane.BODY)) {
            return;
        }
        if (this.beginRoyalAttack("exile_wing_animation", RoyalAttackLane.BODY, 1, 0, 1,
                this.animationRecovery(55, 0, 1, 0), new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        KingEntity.this.exileWingAnimationPending = false;
                        KingEntity.this.triggerAnim("wing_action", "wing_gust");
                    }
                })) {
            this.exileWingAnimationPending = false;
        }
    }

    private void tickRoyalTithe() {
        if (!this.titheTarget.isAlive() || this.titheTarget.level() != this.level()
                || this.isDeadOrDying() || !(this.titheTarget.containerMenu instanceof RoyalJudgmentMenu)) {
            this.clearTithe();
            return;
        }
        if (--this.titheTicks <= 0) {
            for (int slot = 0; slot < this.titheTarget.getInventory().getContainerSize(); slot++) {
                ItemStack stack = this.titheTarget.getInventory().getItem(slot);
                if (stack.isEmpty() || !isRoyalTitheItem(stack)) continue;
                ItemStack thrown = stack.split(1);
                this.titheTarget.getInventory().setChanged();
                this.titheTarget.drop(thrown, true);
                this.clearTithe();
                return;
            }
            this.clearTithe();
        }
    }

    private void tickRoyalTargetShot() {
        if (!this.targetShotPlayer.isAlive() || this.targetShotVictim == null || !this.targetShotVictim.isAlive()
                || this.targetShotPlayer.level() != this.level() || this.isDeadOrDying()
                || !(this.targetShotPlayer.containerMenu instanceof RoyalJudgmentMenu)) {
            this.clearTargetShot();
            return;
        }
        this.targetShotPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, this.targetShotVictim.getEyePosition());
        if (--this.targetShotTicks <= 0) {
            ItemStack ranged = findRoyalRangedItem(this.targetShotPlayer);
            if (!ranged.isEmpty()) {
                Arrow arrow = new Arrow((ServerLevel) this.level(), this.targetShotPlayer, ItemStack.EMPTY, ItemStack.EMPTY);
                arrow.setPos(this.targetShotPlayer.getX(), this.targetShotPlayer.getEyeY() - 0.1D, this.targetShotPlayer.getZ());
                Vec3 direction = this.targetShotVictim.getEyePosition().subtract(arrow.position()).normalize();
                arrow.setDeltaMovement(direction.scale(3.0D));
                arrow.setBaseDamage(2.0D);
                ((ServerLevel) this.level()).addFreshEntity(arrow);
                this.targetShotPlayer.displayClientMessage(Component.literal("The King takes your shot."), true);
            }
            this.clearTargetShot();
        }
    }

    private void tickRoyalSeal() {
        if (!this.sealedTarget.isAlive() || this.sealedTarget.level() != this.level() || this.isDeadOrDying()
                || !(this.sealedTarget.containerMenu instanceof RoyalJudgmentMenu) || --this.sealedTicks <= 0) {
            this.clearSeal();
        }
    }

    private void clearExile() {
        if (this.exileTarget != null && this.exileTarget.isAlive()) {
            this.exileTarget.setShiftKeyDown(false);
            this.exileTarget.setPose(Pose.STANDING);
        }
        this.exileTarget = null;
        this.exileTicks = 0;
        this.exileWingAnimationPending = false;
    }

    private void clearTithe() {
        if (this.titheTarget != null && this.titheTarget.containerMenu instanceof RoyalJudgmentMenu) {
            this.titheTarget.closeContainer();
        }
        this.titheTarget = null;
        this.titheTicks = 0;
    }

    private void clearTargetShot() {
        if (this.targetShotPlayer != null && this.targetShotPlayer.containerMenu instanceof RoyalJudgmentMenu) {
            this.targetShotPlayer.closeContainer();
        }
        this.targetShotPlayer = null;
        this.targetShotVictim = null;
        this.targetShotTicks = 0;
    }

    private void clearSeal() {
        if (this.sealedTarget != null) {
            RoyalJudgmentState.clear(this.sealedTarget.getUUID());
            if (this.sealedTarget.containerMenu instanceof RoyalJudgmentMenu) {
                this.sealedTarget.closeContainer();
            }
        }
        this.sealedTarget = null;
        this.sealedTicks = 0;
    }

    private void startTithe(ServerPlayer target) {
        this.clearTithe();
        this.titheTarget = target;
        this.titheTicks = 20;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    private void startTargetShot(ServerPlayer target, ServerPlayer victim) {
        this.clearTargetShot();
        this.targetShotPlayer = target;
        this.targetShotVictim = victim;
        this.targetShotTicks = 20;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    private void startSeal(ServerPlayer target) {
        this.clearSeal();
        java.util.Set<Integer> slots = new java.util.HashSet<>();
        for (int slot = 9; slot < 18; slot++) slots.add(slot);
        RoyalJudgmentState.seal(target.getUUID(), slots);
        this.sealedTarget = target;
        this.sealedTicks = 100;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    public boolean debugActivateDecree(String id, ServerPlayer target) {
        for (RoyalDecree decree : DECREES) {
            String decreeId = decree.translationKey().substring(decree.translationKey().lastIndexOf('.') + 1);
            if (decreeId.equalsIgnoreCase(id.replace('-', '_'))) {
                this.endDecree(this.getTarget());
                this.activeDecree = decree;
                this.activeDecreeTicks = 300;
                this.decreeCooldownTicks = 0;
                this.projectRoyalSound(AntarchySoundEvents.KING_DECREE.get(), 4.0F, 1.0F, target);
                this.sendDecreeTitle(target);
                return true;
            }
        }
        return false;
    }

    public boolean debugTriggerPunishment(RoyalPunishmentType type, ServerPlayer target) {
        if (!(this.level() instanceof ServerLevel level) || !target.isAlive() || target.level() != level) {
            return false;
        }
        switch (type) {
            case ROYAL_EXILE -> {
                this.clearExile();
                this.exileTarget = target;
                this.exileTicks = 25;
                return true;
            }
            case ROYAL_TITHE -> {
                this.startTithe(target);
                return true;
            }
            case KINGS_TARGET -> {
                ServerPlayer victim = level.players().stream()
                        .filter(candidate -> candidate != target && candidate.isAlive() && candidate.distanceToSqr(target) <= 4096.0D)
                        .min((left, right) -> Double.compare(left.distanceToSqr(target), right.distanceToSqr(target)))
                        .orElse(null);
                if (victim == null) return false;
                ItemStack ranged = findRoyalRangedItem(target);
                if (ranged.isEmpty()) return false;
                target.displayClientMessage(Component.literal("The King chooses your target."), true);
                this.startTargetShot(target, victim);
                return true;
            }
            case KINGS_SEAL -> {
                this.sealUntil = level.getGameTime() + 100L;
                target.displayClientMessage(Component.literal("The King seals your inventory."), true);
                level.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0D, target.getZ(),
                        30, 0.6D, 1.0D, 0.6D, 0.1D);
                this.startSeal(target);
                return true;
            }
        }
        return false;
    }

    private static boolean isRoyalTitheItem(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.ARROW)
                || stack.is(net.minecraft.world.item.Items.BREAD)
                || stack.is(net.minecraft.world.item.Items.COBBLESTONE)
                || stack.is(net.minecraft.world.item.Items.POTION);
    }

    private static ItemStack findRoyalRangedItem(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof net.minecraft.world.item.BowItem
                    || stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
        boolean skyIsMine = this.activeDecree instanceof SkyIsMineDecree;
        boolean forceFar = this.decreeRetreatPressure
                || this.activeDecree instanceof ComeNoCloserDecree
                || this.activeDecree instanceof KeepYourDistanceDecree;

        {
            double distance = target.distanceTo(this);
            if (this.attackScheduler.ready("stomp", RoyalAttackLane.BODY)
                    && (distance <= STOMP_RADIUS || skyIsMine)
                    && this.beginRoyalAttack("stomp", RoyalAttackLane.BODY,
                    skyIsMine ? STOMP_COOLDOWN_TICKS / 2 : STOMP_COOLDOWN_TICKS,
                    STOMP_WINDUP_TICKS, 1, 18, new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("body_action", "stomp");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.7F);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performStomp(level, target);
                        }
                    })) {
                if (skyIsMine) {
                    this.getMoveControl().setWantedPosition(this.getX(), target.getY(), this.getZ(), 1.2D);
                }
            } else if (this.attackScheduler.ready("wing_gust", RoyalAttackLane.BODY)
                    && distance <= WING_GUST_RADIUS && (forceFar || this.random.nextInt(3) == 0)
                    && this.beginRoyalAttack("wing_gust", RoyalAttackLane.BODY, WING_GUST_COOLDOWN_TICKS,
                    WING_GUST_WINDUP_TICKS, 1, this.animationRecovery(55, WING_GUST_WINDUP_TICKS, 1, 16), new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("wing_action", "wing_gust");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.85F);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performWingGust(level, target);
                        }
                    })) {
            } else if (this.attackScheduler.ready("royal_muster", RoyalAttackLane.BODY)
                    && !this.findRoyalMusterTargets(level).isEmpty()
                    && (forceFar || this.random.nextInt(4) == 0)
                    && this.beginRoyalAttack("royal_muster", RoyalAttackLane.BODY,
                    AntarchySettings.kingRoyalMusterCooldownTicks(),
                    AntarchySettings.kingRoyalMusterWindupTicks(), 1,
                    this.animationRecovery(59, AntarchySettings.kingRoyalMusterWindupTicks(), 1, 20),
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("body_action", "minion_spawn");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.72F);
                            level.sendParticles(ParticleTypes.ENCHANT,
                                    KingEntity.this.getX(), KingEntity.this.getY() + 2.0D, KingEntity.this.getZ(),
                                    36, 5.0D, 2.0D, 5.0D, 0.12D);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performRoyalMuster(level);
                        }
                        @Override public void onComplete() {
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.62F);
                        }
                    })) {
            }
        }

        if (forceFar && target.distanceTo(this) < 18.0D) {
            Vec3 away = this.position().subtract(target.position()).normalize().scale(24.0D);
            double ax = this.getX() + away.x;
            double az = this.getZ() + away.z;
            double ay = this.groundYBelow(ax, az) + FLYING_PREFERRED_HOVER + 6.0D;
            this.getMoveControl().setWantedPosition(ax, ay, az, 1.3D);
        }

        this.tickKingElementalProjectiles(level, target);
    }

    private List<Mob> findRoyalMusterTargets(ServerLevel level) {
        double radius = AntarchySettings.kingRoyalMusterRadius();
        return level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(radius), mob ->
                mob.isAlive() && mob != this
                        && (mob.getType().getCategory() == MobCategory.MONSTER || mob instanceof Enemy)
                        && !(mob instanceof RoyalBossEntity)
                        && RoyalEffectEligibility.canApplyCommanded(mob));
    }

    private void performRoyalMuster(ServerLevel level) {
        var commanded = RoyalEffectHooks.commandedHolder();
        if (commanded == null) {
            return;
        }
        int applied = 0;
        for (Mob mob : this.findRoyalMusterTargets(level)) {
            if (applied >= AntarchySettings.kingRoyalMusterCap()) {
                break;
            }
            if (mob.addEffect(new MobEffectInstance(commanded, AntarchySettings.kingRoyalMusterDurationTicks()), this)) {
                applied++;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        mob.getX(), mob.getY() + mob.getBbHeight() + 0.3D, mob.getZ(),
                        5, 0.3D, 0.2D, 0.3D, 0.02D);
            }
        }
        if (applied > 0) {
            this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.95F);
        }
    }

    private void tickKingElementalProjectiles(ServerLevel level, LivingEntity target) {
        if (this.fireballCooldownTicks > 0) this.fireballCooldownTicks--;
        if (this.iceballCooldownTicks > 0) this.iceballCooldownTicks--;
        if (this.iceSpikeCooldownTicks > 0) this.iceSpikeCooldownTicks--;

        double distance = this.distanceTo(target);
        String[] candidates = {"fireball", "iceball", "ice_spikes"};
        String selected = this.attackScheduler.chooseWeighted(candidates, id -> {
            boolean ready = switch (id) {
                case "fireball" -> distance >= 14.0D && distance <= 48.0D && this.fireballCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.LEFT).readyToAttack();
                case "iceball" -> distance >= 10.0D && distance <= 42.0D && this.iceballCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.RIGHT).readyToAttack();
                default -> distance <= 36.0D && this.iceSpikeCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.RIGHT).readyToAttack();
            };
            if (!ready) return 0;
            return switch (id) {
                case "fireball" -> this.behaviorScore(Behavior.KEEPING_FAR) + 4;
                case "iceball" -> this.behaviorScore(Behavior.RETREATING) + 4;
                default -> this.behaviorScore(Behavior.HUGGING) + 3;
            };
        }, this.random);
        if (selected == null) return;
        if ("fireball".equals(selected)) {
            if (!this.beginRoyalAttack("fireball", RoyalAttackLane.LEFT_HEAD,
                    AntarchySettings.kingFireballCooldownTicks(), 14, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.LEFT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = KingEntity.this.headAnchor(KingEntity.this.royalHead(RoyalHead.Slot.LEFT));
                            Vec3 predicted = target.position().add(target.getDeltaMovement().scale(10.0D));
                            Vec3 direction = predicted.add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(origin).normalize();
                            RoyalElementalProjectileEntity fireball = RoyalElementalProjectileEntity.create(level, KingEntity.this,
                                    RoyalBeamElement.FIRE, origin, direction);
                            level.addFreshEntity(fireball);
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_FIREBALL_SHOOT.get(), 0.9F);
                        }
                    })) return;
            this.fireballCooldownTicks = AntarchySettings.kingFireballCooldownTicks();
        } else if ("iceball".equals(selected)) {
            if (!this.beginRoyalAttack("iceball", RoyalAttackLane.RIGHT_HEAD,
                    AntarchySettings.kingIceballCooldownTicks(), 16, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.RIGHT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = KingEntity.this.headAnchor(KingEntity.this.royalHead(RoyalHead.Slot.RIGHT));
                            Vec3 direction = target.getEyePosition().subtract(origin).normalize();
                            RoyalElementalProjectileEntity iceball = RoyalElementalProjectileEntity.create(level, KingEntity.this,
                                    RoyalBeamElement.ICE, origin, direction);
                            level.addFreshEntity(iceball);
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_BEAM_SHOOT.get(), 1.2F);
                        }
                    })) return;
            this.iceballCooldownTicks = AntarchySettings.kingIceballCooldownTicks();
        } else {
            if (!this.beginRoyalAttack("ice_spikes", RoyalAttackLane.RIGHT_HEAD,
                    AntarchySettings.kingIceSpikeCooldownTicks(), 20, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.RIGHT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.raiseIceSpikes(level, target);
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ICE_SPIKES.get(), 0.8F);
                        }
                    })) return;
            this.iceSpikeCooldownTicks = AntarchySettings.kingIceSpikeCooldownTicks();
        }
    }

    private void raiseIceSpikes(ServerLevel level, LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.position();
        Vec3 line = end.subtract(start).multiply(1.0D, 0.0D, 1.0D);
        if (line.lengthSqr() < 1.0E-4D) return;
        line = line.normalize();
        Vec3 side = new Vec3(-line.z, 0.0D, line.x);
        for (int i = 1; i <= 5; i++) {
            double distance = i * 4.0D;
            for (int lane = -1; lane <= 1; lane++) {
                Vec3 point = start.add(line.scale(distance)).add(side.scale(lane * (i % 2 == 0 ? 2.0D : 0.0D)));
                double y = this.groundYBelow(point.x, point.z) + 0.1D;
                RoyalIceSpikeEntity spike = RoyalIceSpikeEntity.create(level, new Vec3(point.x, y, point.z));
                level.addFreshEntity(spike);
            }
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
        this.playRoyalSound(AntarchySoundEvents.KING_STOMP.get(), 0.8F);
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
        if (!(target instanceof ServerPlayer player) || !target.isAlive() || target.level() != level) {
            if (this.activeDecree != null) {
                this.endDecree(target);
            }
            this.activeDecree = null;
            this.activeDecreeTicks = 0;
            this.decreeRetreatPressure = false;
            return;
        }
        if (this.activeDecree == null) {
            if (this.decreeCooldownTicks-- > 0) {
                return;
            }
            if (!this.beginRoyalAttack("decree", RoyalAttackLane.DECREE,
                    AntarchySettings.royalDecreeCooldownTicks(), 300)) {
                return;
            }
            this.activeDecreeTicks = 300;
            this.activeDecree = this.pickDecree(target);
            this.decreeRetreatPressure = false;
            this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.9F + this.random.nextFloat() * 0.15F);
            this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.8F + this.random.nextFloat() * 0.12F);
            this.projectRoyalSound(AntarchySoundEvents.KING_DECREE.get(), 4.0F, 1.0F, target);
            this.sendDecreeTitle(player);
        }
        int countdown = this.activeDecree.countdownTicks(target);
        if (countdown > 0 && countdown <= 100 && this.tickCount % 20 == 0) {
            level.playSound(null, target.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 0.8F, 1.0F + countdown / 500.0F);
        }
        this.activeDecree.apply(level, this, target);
        if (this.activeDecree != null
                && this.activeDecree.evaluate(level, this, target) == RoyalDecree.Evaluation.VIOLATED) {
            this.failActiveDecree(target);
            return;
        }
        if (this.activeDecree != null && --this.activeDecreeTicks <= 0) {
            this.projectRoyalSound(AntarchySoundEvents.KING_SUCCESS.get(), 4.0F, 1.0F, target);
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

    private void sendDecreeTitle(ServerPlayer player) {
        if (this.activeDecree == null) return;
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 300, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(this.activeDecree.translationKey())
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true))));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(this.activeDecree.instructionKey())
                .withStyle(style -> style.withColor(ChatFormatting.WHITE))));
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

    public void failActiveDecree(@Nullable LivingEntity target) {
        if (this.activeDecree == null || target == null || !target.isAlive()) {
            return;
        }
        if (target instanceof ServerPlayer player) {
            this.projectRoyalSound(AntarchySoundEvents.KING_JUDGEMENT.get(), 4.0F, 1.0F, target);
            this.debugTriggerPunishment(RoyalPunishmentType.ROYAL_EXILE, player);
        } else {
            this.invokeJudgment(target);
        }
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
            this.projectRoyalSound(AntarchySoundEvents.KING_JUDGEMENT.get(), 4.0F, 1.0F, target);
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
                this.failActiveDecree(attacker);
            } else if (this.activeDecree instanceof ComeNoCloserDecree && !projectile) {
                this.failActiveDecree(attacker);
            } else if (this.activeDecree instanceof HandsOffTheCrownDecree) {
                this.failActiveDecree(attacker);
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TreePatrolBound", this.treePatrolBound);
        if (this.treePatrolCenter != null) {
            tag.putDouble("TreePatrolCenterX", this.treePatrolCenter.x);
            tag.putDouble("TreePatrolCenterY", this.treePatrolCenter.y);
            tag.putDouble("TreePatrolCenterZ", this.treePatrolCenter.z);
            tag.putDouble("TreePatrolMinimumY", this.treePatrolMinimumY);
            tag.putDouble("TreePatrolMaximumY", this.treePatrolMaximumY);
            tag.putDouble("TreePatrolAngle", this.treePatrolAngle);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("TreePatrolBound") && tag.contains("TreePatrolCenterX")
                && tag.contains("TreePatrolCenterY") && tag.contains("TreePatrolCenterZ")) {
            this.setTreePatrolHome(
                    new Vec3(tag.getDouble("TreePatrolCenterX"), tag.getDouble("TreePatrolCenterY"), tag.getDouble("TreePatrolCenterZ")),
                    tag.getDouble("TreePatrolMinimumY"), tag.getDouble("TreePatrolMaximumY"), tag.getDouble("TreePatrolAngle"));
        }
    }

    public static boolean blocksHealingAround(LivingEntity target) {
        if (target.level().isClientSide) return false;
        return target.level().getEntitiesOfClass(KingEntity.class, target.getBoundingBox().inflate(128.0D),
                king -> king.isAlive() && king.blocksHealing()).stream().findFirst().isPresent();
    }
}
