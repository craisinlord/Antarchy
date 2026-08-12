package com.craisinlord.antarchy.content.entity.portal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.tags.TagKey;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DimensionalTearEntity extends Entity implements GeoEntity {
    public enum TearState {
        NORMAL,
        UNSTABLE,
        LUCID,
        COLLAPSING
    }

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(DimensionalTearEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final int DEFAULT_LIFETIME_TICKS = 20 * 60 * 20;
    private static final int COLLAPSE_WARNING_TICKS = 20 * 20;
    private static final int EVENT_WARNING_TICKS = 80;
    private static final int TELEPORT_COOLDOWN_TICKS = 60;
    private static final double PORTAL_RADIUS = 1.35D;
    private static final double EXIT_OFFSET = 2.4D;
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private UUID linkedTearId;
    private Vec3 linkedFallbackPos = Vec3.ZERO;
    private int ageTicks;
    private int lifetimeTicks = DEFAULT_LIFETIME_TICKS;
    private int eventTicks;
    private int nextEventTicks;
    private TearState pendingEvent = TearState.NORMAL;

    public DimensionalTearEntity(EntityType<? extends DimensionalTearEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static DimensionalTearEntity create(ServerLevel level, Vec3 pos, float yaw, int lifetimeTicks) {
        DimensionalTearEntity tear = new DimensionalTearEntity(AntarchyObjects.DIMENSIONAL_TEAR.get(), level);
        tear.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
        tear.lifetimeTicks = lifetimeTicks;
        tear.nextEventTicks = tear.randomEventDelay();
        return tear;
    }

    public void linkTo(DimensionalTearEntity other) {
        this.linkedTearId = other.getUUID();
        this.linkedFallbackPos = other.position();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(STATE, TearState.NORMAL.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.tickClientParticles();
            return;
        }

        this.ageTicks++;
        if (this.ageTicks >= this.lifetimeTicks - COLLAPSE_WARNING_TICKS) {
            this.setTearState(TearState.COLLAPSING);
        }
        if (this.ageTicks >= this.lifetimeTicks) {
            this.removeLinkedPair();
            return;
        }

        DimensionalTearEntity linked = this.findLinkedTear();
        if (linked == null || !linked.isAlive()) {
            this.discard();
            return;
        }

        this.tickMobEvent((ServerLevel) this.level());
        this.tickTeleport(linked);
    }

    private void tickMobEvent(ServerLevel level) {
        if (this.getTearState() == TearState.COLLAPSING || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        if (this.eventTicks > 0) {
            this.eventTicks--;
            if (this.eventTicks == 1) {
                this.spawnEmergence(level, this.pendingEvent);
                this.pendingEvent = TearState.NORMAL;
                this.setTearState(TearState.NORMAL);
                this.nextEventTicks = this.randomEventDelay();
            }
            return;
        }

        if (this.nextEventTicks > 0) {
            this.nextEventTicks--;
            return;
        }

        this.pendingEvent = this.random.nextFloat() < AntarchySettings.dimensionalTearLucidEventChance()
                ? TearState.LUCID
                : TearState.UNSTABLE;
        this.eventTicks = EVENT_WARNING_TICKS;
        this.setTearState(this.pendingEvent);
        level.playSound(null, this.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 0.85F, this.pendingEvent == TearState.LUCID ? 1.35F : 0.65F);
    }

    private void spawnEmergence(ServerLevel level, TearState eventState) {
        Vec3 exitPos = this.exitPosition();
        TagKey<EntityType<?>> spawnTag = eventState == TearState.LUCID
                ? AntarchyTags.Entities.DIMENSIONAL_TEAR_COMMON_SPAWNS
                : AntarchyTags.Entities.DIMENSIONAL_TEAR_RARE_SPAWNS;
        Mob mob = this.createTaggedMob(level, spawnTag);
        if (mob == null || !level.noCollision(mob, mob.getType().getDimensions().makeBoundingBox(exitPos))) {
            return;
        }

        mob.moveTo(exitPos.x, exitPos.y, exitPos.z, this.getYRot(), 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(exitPos)), MobSpawnType.MOB_SUMMONED, null, null);
        mob.setDeltaMovement(this.getViewVector(1.0F).scale(0.35D).add(0.0D, 0.08D, 0.0D));
        mob.setNoGravity(true);
        level.addFreshEntity(mob);
        level.playSound(null, this.blockPosition(), eventState == TearState.LUCID ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 0.75F, eventState == TearState.LUCID ? 1.45F : 0.85F);
    }

    @Nullable
    private Mob createTaggedMob(ServerLevel level, TagKey<EntityType<?>> tag) {
        return BuiltInRegistries.ENTITY_TYPE.getTag(tag)
                .flatMap((HolderSet.Named<EntityType<?>> entityTypes) -> entityTypes.getRandomElement(this.random))
                .map(Holder::value)
                .map(entityType -> entityType.create(level))
                .filter(entity -> entity instanceof Mob)
                .map(entity -> (Mob) entity)
                .orElse(null);
    }

    private void tickTeleport(DimensionalTearEntity linked) {
        AABB area = this.getBoundingBox().inflate(PORTAL_RADIUS);
        List<Entity> entities = this.level().getEntities(this, area, this::canTeleportEntity);
        for (Entity entity : entities) {
            teleportEntity(entity, linked);
        }
    }

    private boolean canTeleportEntity(Entity entity) {
        if (!entity.isAlive() || entity instanceof DimensionalTearEntity || entity.isPassenger() || entity.isVehicle()) {
            return false;
        }
        UUID uuid = entity.getUUID();
        long gameTime = this.level().getGameTime();
        long cooldownUntil = TELEPORT_COOLDOWNS.getOrDefault(uuid, 0L);
        if (cooldownUntil <= gameTime) {
            TELEPORT_COOLDOWNS.remove(uuid);
            return true;
        }
        return false;
    }

    private void teleportEntity(Entity entity, DimensionalTearEntity destination) {
        Vec3 motion = entity.getDeltaMovement();
        Vec3 exitPos = destination.exitPosition();
        TELEPORT_COOLDOWNS.put(entity.getUUID(), this.level().getGameTime() + TELEPORT_COOLDOWN_TICKS);

        if (entity instanceof ServerPlayer player && destination.level() instanceof ServerLevel serverLevel) {
            player.teleportTo(serverLevel, exitPos.x, exitPos.y, exitPos.z, player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
        }

        entity.setDeltaMovement(motion);
        entity.hasImpulse = true;
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), AntarchySettings.dimensionalTearInvertedDurationTicks(), 0));
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.7F, 0.65F + this.random.nextFloat() * 0.2F);
    }

    private Vec3 exitPosition() {
        Vec3 facing = this.getViewVector(1.0F);
        if (facing.lengthSqr() < 1.0E-4D) {
            facing = new Vec3(0.0D, 0.0D, 1.0D);
        }
        return this.position().add(facing.normalize().scale(EXIT_OFFSET));
    }

    @Nullable
    private DimensionalTearEntity findLinkedTear() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.linkedTearId == null) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.linkedTearId);
        return entity instanceof DimensionalTearEntity tear ? tear : null;
    }

    private void removeLinkedPair() {
        DimensionalTearEntity linked = this.findLinkedTear();
        if (linked != null && linked.isAlive()) {
            linked.discard();
        }
        this.discard();
    }

    private int randomEventDelay() {
        int min = Math.max(20, AntarchySettings.dimensionalTearEmergenceMinIntervalTicks());
        int max = Math.max(min, AntarchySettings.dimensionalTearEmergenceMaxIntervalTicks());
        return min + this.random.nextInt(max - min + 1);
    }

    public TearState getTearState() {
        int ordinal = this.entityData.get(STATE);
        TearState[] values = TearState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : TearState.NORMAL;
    }

    private void setTearState(TearState state) {
        this.entityData.set(STATE, state.ordinal());
    }

    private void tickClientParticles() {
        TearState state = this.getTearState();
        if (this.random.nextInt(state == TearState.NORMAL ? 5 : 2) != 0) {
            return;
        }
        double spread = state == TearState.NORMAL ? 0.65D : 1.15D;
        this.level().addParticle(
                state == TearState.LUCID ? ParticleTypes.END_ROD : ParticleTypes.PORTAL,
                this.getX() + (this.random.nextDouble() - 0.5D) * spread,
                this.getY() + 1.3D + (this.random.nextDouble() - 0.5D) * spread,
                this.getZ() + (this.random.nextDouble() - 0.5D) * spread,
                (this.random.nextDouble() - 0.5D) * 0.05D,
                (this.random.nextDouble() - 0.5D) * 0.05D,
                (this.random.nextDouble() - 0.5D) * 0.05D
        );
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("AgeTicks", this.ageTicks);
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
        tag.putInt("EventTicks", this.eventTicks);
        tag.putInt("NextEventTicks", this.nextEventTicks);
        tag.putInt("PendingEvent", this.pendingEvent.ordinal());
        if (this.linkedTearId != null) {
            tag.putUUID("LinkedTearId", this.linkedTearId);
        }
        tag.putDouble("LinkedX", this.linkedFallbackPos.x);
        tag.putDouble("LinkedY", this.linkedFallbackPos.y);
        tag.putDouble("LinkedZ", this.linkedFallbackPos.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.ageTicks = tag.getInt("AgeTicks");
        this.lifetimeTicks = tag.getInt("LifetimeTicks");
        this.eventTicks = tag.getInt("EventTicks");
        this.nextEventTicks = tag.getInt("NextEventTicks");
        int event = tag.getInt("PendingEvent");
        TearState[] states = TearState.values();
        this.pendingEvent = event >= 0 && event < states.length ? states[event] : TearState.NORMAL;
        if (tag.hasUUID("LinkedTearId")) {
            this.linkedTearId = tag.getUUID("LinkedTearId");
        }
        this.linkedFallbackPos = new Vec3(tag.getDouble("LinkedX"), tag.getDouble("LinkedY"), tag.getDouble("LinkedZ"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tear_controller", 0, this::tearController));
    }

    private PlayState tearController(AnimationState<DimensionalTearEntity> state) {
        double speed = switch (this.getTearState()) {
            case UNSTABLE, COLLAPSING -> 1.8D;
            case LUCID -> 0.65D;
            default -> 1.0D;
        };
        state.getController().setAnimationSpeed(speed);
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
