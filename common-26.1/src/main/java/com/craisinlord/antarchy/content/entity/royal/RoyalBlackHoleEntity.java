package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.BlockSuction;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.time.TimeDilationAnchor;
import com.craisinlord.anthology.time.TimeDilationApi;
import com.craisinlord.anthology.time.TimeDilationEntityAccess;
import com.craisinlord.anthology.time.TimeDilationFieldEntity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RoyalBlackHoleEntity extends Entity implements GeoEntity, TimeDilationAnchor, GlimmerEcho {
    private record PendingPush(long gameTime, Vec3 impulse) {
    }

    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> COLLAPSING =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> VISUAL_GROWTH =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_RADIUS =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> GLIMMER_ECHO =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> INVERTED =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double GLIMMER_COLLAPSE_RADIUS_SCALE = 1.25D;
    private static final String INVERTED_KEY = "GravityInverted";
    public static final int PLAYER_PUSH_INTERVAL_TICKS = 3;
    private static final double PLAYER_PULL_SCALE = 0.95D;
    private static final double PLAYER_AIRBORNE_PULL_SCALE = 0.3D;
    private static final double PLAYER_GROUND_IMPULSE_CAP = 0.175D;
    private static final double PLAYER_AIR_IMPULSE_CAP = 0.042D;
    private static final double GROUND_FRICTION = 0.546D;
    private static final double AIR_FRICTION = 0.91D;
    private static final double VERTICAL_DRAG = 0.98D;
    private static final int ENTITY_IMPULSE_SYNC_INTERVAL_TICKS = 5;
    private static final String COLLAPSING_KEY = "Collapsing";
    private static final String EFFECT_RADIUS_KEY = "EffectRadius";
    private static final Map<ServerPlayer, PendingPush> PENDING_PUSHES = new WeakHashMap<>();
    private static final double PLAYER_MAX_PULL_SPEED = 1.4D;
    private static final double COLUMN_DANGER_RADIUS = 5.0D;
    private static final int COLUMN_DAMAGE_INTERVAL_TICKS = 10;
    private static final double COLUMN_DAMAGE = 16.0D;
    private static final double COLLAPSE_MAX_DAMAGE = 44.0D;
    private static final double COLLAPSE_MIN_FALLOFF = 0.35D;
    private static final double COLLAPSE_BELOW_ANCHOR = 8.0D;
    private static final int RING_INTERVAL_TICKS = 10;
    private static final int RING_SURFACE_SCAN_BLOCKS = 12;
    private static final String OWNER_KEY = "OwnerUuid";
    private static final String AGE_KEY = "Age";
    private static final String ACTIVE_KEY = "ActiveTicks";
    private static final String TRAVEL_TICKS_KEY = "TravelTicks";
    private static final String DESTINATION_X_KEY = "DestinationX";
    private static final String DESTINATION_Y_KEY = "DestinationY";
    private static final String DESTINATION_Z_KEY = "DestinationZ";
    private static final String ANIMATION_AGE_KEY = "AnimationAge";
    private static final String DILATION_FIELD_KEY = "DilationFieldUuid";
    private static final String SUCKED_BLOCKS_KEY = "SuckedBlocks";

    private static final double DEFAULT_RADIUS = 14.0D;
    private static final int QUEEN_ANIMATION_TICKS = 403;
    private static final int QUEEN_COLLAPSE_TICKS = 25;
    private static final int QUEEN_ACTIVE_TICKS = QUEEN_ANIMATION_TICKS - QUEEN_COLLAPSE_TICKS;
    private static final int QUEEN_PROJECTILE_TRAVEL_TICKS = 20;
    private static final int BLOCK_SUCTION_INTERVAL = 5;
    private static final int BLOCKS_PER_SUCTION = 3;
    private static final int BLOCK_SUCTION_ATTEMPTS = 36;
    private static final double MODEL_RENDER_SCALE = 1.5D;
    private static final double EFFECT_GROWTH_START = 0.45D;
    private static final double EFFECT_GROWTH_END = 1.6D;
    private static final double EFFECT_GROWTH_FRACTION = 0.6D;
    private static final double QUEEN_EFFECT_GROWTH_START = EFFECT_GROWTH_START * 1.5D;
    private static final double QUEEN_PULL_RADIUS_SCALE = 2.0D;
    private static final double MODEL_BASELINE_Y = 24.0D;
    private static final double CORE_CUBE_CENTER_Y = 146.0D;
    private static final double CORE_START_OFFSET_Y = -100.0D;
    private static final double CORE_END_OFFSET_Y = 505.0D;
    private static final double CORE_RISE_END_SECONDS = 19.125D;

    @Nullable
    private UUID ownerId;
    private int age;
    private int activeTicks = QUEEN_ACTIVE_TICKS;
    private int animationAge;
    private int suckedBlocks;
    private int travelTicksRemaining;
    @Nullable
    private UUID dilationFieldId;
    @Nullable
    private Vec3 destination;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RoyalBlackHoleEntity(EntityType<? extends RoyalBlackHoleEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public static RoyalBlackHoleEntity create(ServerLevel level, Vec3 center, double radius, int activeTicks, @Nullable UUID ownerId) {
        RoyalBlackHoleEntity hole = new RoyalBlackHoleEntity(AntarchyObjects.ROYAL_BLACK_HOLE.get(), level);
        hole.configure(center, radius, activeTicks, ownerId);
        return hole;
    }

    public static RoyalBlackHoleEntity create(ServerLevel level, Vec3 center, @Nullable UUID ownerId) {
        return create(level, center, AntarchySettings.queenBlackHoleRadius(), QUEEN_ACTIVE_TICKS, ownerId);
    }

    public static RoyalBlackHoleEntity createProjectile(ServerLevel level, Vec3 origin, Vec3 destination,
                                                         @Nullable UUID ownerId) {
        RoyalBlackHoleEntity hole = create(level, origin, ownerId);
        hole.activeTicks = QUEEN_ACTIVE_TICKS - QUEEN_PROJECTILE_TRAVEL_TICKS;
        hole.destination = destination;
        hole.travelTicksRemaining = QUEEN_PROJECTILE_TRAVEL_TICKS;
        hole.entityData.set(VISUAL_GROWTH, 0.15F);
        return hole;
    }

    protected final void configure(Vec3 center, double radius, int activeTicks, @Nullable UUID ownerId) {
        this.setPos(center.x, center.y, center.z);
        this.setRadius(radius > 0.0D ? radius : AntarchySettings.queenBlackHoleRadius());
        this.activeTicks = activeTicks > 0 ? Math.max(20, activeTicks) : AntarchySettings.queenBlackHoleActiveTicks();
        this.ownerId = ownerId;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, (float) DEFAULT_RADIUS);
        builder.define(COLLAPSING, false);
        builder.define(VISUAL_GROWTH, 1.0F);
        builder.define(EFFECT_RADIUS, (float) (DEFAULT_RADIUS * EFFECT_GROWTH_START));
        builder.define(GLIMMER_ECHO, false);
        builder.define(INVERTED, false);
    }

    public boolean isGravityInverted() {
        return this.entityData.get(INVERTED);
    }

    public void setGravityInverted(boolean inverted) {
        this.entityData.set(INVERTED, inverted);
    }

    private double riseSign() {
        return this.isGravityInverted() ? -1.0D : 1.0D;
    }

    public void dismiss() {
        if (this.level() instanceof ServerLevel level) {
            Vec3 center = this.effectCenter();
            level.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.REVERSE_PORTAL),
                    center.x, center.y, center.z, 60, 3.0D, 3.0D, 3.0D, 0.3D);
        }
        this.discardAttachedDilationField();
        this.discard();
    }

    public static void discardOwnedBy(ServerLevel level, UUID ownerId, AABB bounds) {
        for (RoyalBlackHoleEntity hole : level.getEntitiesOfClass(RoyalBlackHoleEntity.class, bounds,
                hole -> ownerId.equals(hole.ownerId))) {
            hole.dismiss();
        }
    }

    public static void pushPlayer(ServerPlayer player, Vec3 worldImpulse, double maxSpeed) {
        long gameTime = player.level().getGameTime();
        PendingPush pending = PENDING_PUSHES.get(player);
        Vec3 total = pending != null && pending.gameTime() == gameTime ? pending.impulse().add(worldImpulse) : worldImpulse;
        PENDING_PUSHES.put(player, new PendingPush(gameTime, total));
        boolean grounded = player.onGround();
        double cap = grounded ? PLAYER_GROUND_IMPULSE_CAP : PLAYER_AIR_IMPULSE_CAP;
        double horizontal = Math.sqrt(total.x * total.x + total.z * total.z);
        Vec3 capped = horizontal > cap
                ? new Vec3(total.x * cap / horizontal, total.y, total.z * cap / horizontal)
                : total;
        double rate = player instanceof TimeDilationEntityAccess access
                ? Mth.clamp((double) access.anthology$getTimeDilationRate(), 0.1D, 1.0D) : 1.0D;
        double friction = grounded ? GROUND_FRICTION : AIR_FRICTION;
        Vec3 observed = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo)
                .scale(1.0D / rate);
        Vec3 velocity = new Vec3(observed.x * friction, observed.y * VERTICAL_DRAG, observed.z * friction).add(capped);
        if (velocity.lengthSqr() > maxSpeed * maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
        GravityApi.setWorldVelocity(player, velocity);
        player.hurtMarked = true;
    }

    @Override
    public boolean isGlimmerEcho() {
        return this.entityData.get(GLIMMER_ECHO);
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        this.entityData.set(GLIMMER_ECHO, glimmerEcho);
    }

    public void setRadius(double radius) {
        this.entityData.set(RADIUS, (float) Math.max(1.0D, radius));
    }

    public double radius() {
        return this.entityData.get(RADIUS);
    }

    public double effectRadius() {
        return Math.max(1.0D, this.entityData.get(EFFECT_RADIUS));
    }

    private double computeEffectRadius() {
        double progress = this.activeTicks <= 0
                ? 1.0D
                : Mth.clamp(this.age / (this.activeTicks * EFFECT_GROWTH_FRACTION), 0.0D, 1.0D);
        return this.radius() * Mth.lerp(progress, this.effectGrowthStart(), EFFECT_GROWTH_END);
    }

    protected double effectGrowthStart() {
        return QUEEN_EFFECT_GROWTH_START;
    }

    public boolean isCollapsing() {
        return this.entityData.get(COLLAPSING);
    }

    public float visualGrowth() {
        return this.entityData.get(VISUAL_GROWTH);
    }

    @Override
    public void tick() {
        super.tick();
        this.animationAge++;
        if (this.level().isClientSide()) {
            this.spawnClientParticles();
            return;
        }
        if (this.travelTicksRemaining > 0 && this.destination != null) {
            this.tickProjectileTravel();
            return;
        }
        this.age++;
        if (!this.isCollapsing()) {
            if (this.risesWhileActive()) {
                this.setPos(this.getX(), this.getY() + 0.035D, this.getZ());
            }
            this.entityData.set(EFFECT_RADIUS, (float) this.computeEffectRadius());
            this.ensureAttachedDilationField();
            this.applyPull();
            if (this.usesColumnEffects() && this.level() instanceof ServerLevel level) {
                this.tickColumnDanger(level);
                if (this.age % RING_INTERVAL_TICKS == 0) {
                    this.spawnDangerRings(level);
                }
            }
            if (this.pullsTerrainBlocks() && !this.isGlimmerEcho() && this.age % BLOCK_SUCTION_INTERVAL == 0
                    && this.level() instanceof ServerLevel level) {
                this.pullTerrainBlocks(level);
            }
            if (this.age >= this.activeTicks) {
                this.entityData.set(COLLAPSING, true);
                this.age = 0;
                if (this.usesColumnEffects()) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 4.0F, 0.5F);
                }
            }
            return;
        }
        if (this.usesColumnEffects() && this.age % 3 == 0 && this.level() instanceof ServerLevel level) {
            this.spawnDangerRings(level);
        }
        if (this.age >= this.collapseDurationTicks()) {
            this.collapse();
            this.discardAttachedDilationField();
            this.discard();
        }
    }

    private void tickProjectileTravel() {
        Vec3 remaining = this.destination.subtract(this.position());
        double distance = remaining.length();
        if (distance <= 0.5D || this.travelTicksRemaining <= 1) {
            this.setPos(this.destination.x, this.destination.y, this.destination.z);
            this.travelTicksRemaining = 0;
            this.destination = null;
            this.entityData.set(VISUAL_GROWTH, 1.0F);
            return;
        }

        this.setPos(this.position().add(remaining.scale(1.0D / this.travelTicksRemaining)));
        this.travelTicksRemaining--;
        float progress = 1.0F - this.travelTicksRemaining / (float) QUEEN_PROJECTILE_TRAVEL_TICKS;
        this.entityData.set(VISUAL_GROWTH, Mth.lerp(progress, 0.15F, 1.0F));
    }

    protected boolean risesWhileActive() {
        return false;
    }

    protected boolean usesColumnEffects() {
        return true;
    }

    protected double timeDilationRadius() {
        return Math.max(2.0D, this.effectRadius() * 0.55D) * QueenEntity.TIME_FIELD_RADIUS_SCALE;
    }

    protected boolean hasVisibleTimeDilationField() {
        return true;
    }

    protected int collapseDurationTicks() {
        return QUEEN_COLLAPSE_TICKS;
    }

    protected boolean pullsTerrainBlocks() {
        return true;
    }

    protected double pullRadius() {
        return this.effectRadius() * QUEEN_PULL_RADIUS_SCALE;
    }

    protected double pullStrength() {
        return AntarchySettings.queenBlackHolePullStrength();
    }

    protected int blockSuctionCap() {
        return AntarchySettings.queenBlackHoleBlockSuctionCap();
    }

    @Override
    public Vec3 timeDilationCenter() {
        return this.effectCenter();
    }

    public Vec3 effectCenter() {
        double animationSeconds = Math.min(CORE_RISE_END_SECONDS, this.animationAge / 20.0D);
        double riseProgress = animationSeconds / CORE_RISE_END_SECONDS;
        double animatedOffset = Mth.lerp(riseProgress, CORE_START_OFFSET_Y, CORE_END_OFFSET_Y);
        double modelY = CORE_CUBE_CENTER_Y + animatedOffset;
        double worldOffsetY = (modelY - MODEL_BASELINE_Y) / 16.0D * MODEL_RENDER_SCALE;
        return this.position().add(0.0D, worldOffsetY * this.riseSign(), 0.0D);
    }

    private Vec3 nearestColumnPoint(Vec3 point) {
        Vec3 anchor = this.position();
        Vec3 core = this.effectCenter();
        double low = Math.min(anchor.y, core.y);
        double high = Math.max(anchor.y, core.y);
        return new Vec3(anchor.x, Mth.clamp(point.y, low, high), anchor.z);
    }

    private double collapseRadius() {
        return this.effectRadius() * (this.isGlimmerEcho() ? GLIMMER_COLLAPSE_RADIUS_SCALE : 1.0D);
    }

    private boolean withinColumnSpan(LivingEntity living, double margin) {
        double along = (living.getY() + living.getBbHeight() * 0.5D - this.getY()) * this.riseSign();
        double top = (this.effectCenter().y - this.getY()) * this.riseSign();
        return along >= -margin && along <= top + margin;
    }

    private double horizontalDistanceToAnchor(Entity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private DamageSource holeDamageSource() {
        LivingEntity owner = this.ownerEntity();
        if (owner instanceof RoyalBossEntity) {
            return this.damageSources().indirectMagic(this, owner);
        }
        return owner != null ? this.damageSources().mobAttack(owner) : this.damageSources().magic();
    }

    private float scaledHoleDamage(double damage) {
        return this.ownerEntity() instanceof RoyalBossEntity royalBoss
                ? royalBoss.scaleRoyalDamage(damage) : (float) damage;
    }

    private void tickColumnDanger(ServerLevel level) {
        if (this.age % COLUMN_DAMAGE_INTERVAL_TICKS != 0) {
            return;
        }
        AABB bounds = new AABB(this.position(), this.effectCenter()).inflate(COLUMN_DANGER_RADIUS);
        DamageSource source = this.holeDamageSource();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (!this.isImmune(living) && this.horizontalDistanceToAnchor(living) <= COLUMN_DANGER_RADIUS
                    && this.withinColumnSpan(living, COLUMN_DANGER_RADIUS)) {
                living.hurtServer(level, source, this.scaledHoleDamage(COLUMN_DAMAGE));
            }
        }
    }

    private double ringSurfaceY(ServerLevel level) {
        BlockPos.MutableBlockPos pos = this.blockPosition().mutable();
        if (!level.hasChunkAt(pos)) {
            return this.getY();
        }
        int step = this.isGravityInverted() ? 1 : -1;
        for (int offset = 0; offset < RING_SURFACE_SCAN_BLOCKS; offset++) {
            if (!level.getBlockState(pos).isAir()) {
                return pos.getY() + (this.isGravityInverted() ? -0.1D : 1.1D);
            }
            pos.move(0, step, 0);
        }
        return this.getY();
    }

    private void spawnDangerRings(ServerLevel level) {
        double y = this.ringSurfaceY(level);
        this.spawnRing(level, y, this.collapseRadius(), 56, ParticleTypes.REVERSE_PORTAL);
        this.spawnRing(level, y, COLUMN_DANGER_RADIUS, 20, ParticleTypes.WITCH);
    }

    private void spawnRing(ServerLevel level, double y, double radius, int count,
                           ParticleOptions particle) {
        for (int index = 0; index < count; index++) {
            double angle = Mth.TWO_PI * index / count;
            level.sendParticles(GlimmerEchoes.particle(this, particle),
                    this.getX() + Math.cos(angle) * radius, y, this.getZ() + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void ensureAttachedDilationField() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        TimeDilationFieldEntity field = this.dilationFieldId != null
                && level.getEntity(this.dilationFieldId) instanceof TimeDilationFieldEntity existing
                ? existing
                : null;
        if (field == null || !field.isAlive()) {
            int remainingTicks = Math.max(1, this.activeTicks - this.age + this.collapseDurationTicks());
            field = TimeDilationApi.createField(level, this.effectCenter(), this.timeDilationRadius(), 0.18D,
                    remainingTicks, this.ownerId, this.hasVisibleTimeDilationField());
            field.attachTo(this);
            this.dilationFieldId = field.getUUID();
        } else {
            Vec3 center = this.effectCenter();
            field.setPos(center.x, center.y, center.z);
            field.setFieldRadius(this.timeDilationRadius());
        }
    }

    private void discardAttachedDilationField() {
        if (this.dilationFieldId != null && this.level() instanceof ServerLevel level
                && level.getEntity(this.dilationFieldId) instanceof TimeDilationFieldEntity field) {
            field.discard();
        }
        this.dilationFieldId = null;
    }

    private void applyPull() {
        Vec3 center = this.effectCenter();
        boolean column = this.usesColumnEffects();
        boolean terrainAllowed = !this.isGlimmerEcho()
                && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
        boolean pulseTick = this.level().getGameTime() % PLAYER_PUSH_INTERVAL_TICKS == 0L;
        boolean syncTick = this.age % ENTITY_IMPULSE_SYNC_INTERVAL_TICKS == 0;
        double radius = this.pullRadius();
        AABB bounds = column
                ? new AABB(this.position(), center).inflate(radius)
                : new AABB(center, center).inflate(radius);
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, bounds)) {
            if (entity == this || entity instanceof TimeDilationFieldEntity || this.isImmune(entity)) {
                continue;
            }
            boolean falling = entity instanceof FallingBlockEntity;
            if (falling && !terrainAllowed) {
                continue;
            }
            Vec3 pullPoint = column && !falling ? this.nearestColumnPoint(entity.position()) : center;
            Vec3 toCenter = pullPoint.subtract(entity.position());
            double distance = toCenter.length();
            if (distance < 0.75D) {
                if (falling) {
                    entity.discard();
                }
                continue;
            }
            if (distance > radius) {
                continue;
            }
            double strength = this.pullStrength() * (1.0D - distance / radius);
            if (entity instanceof ServerPlayer player) {
                if (pulseTick && !player.isPassenger()) {
                    double scale = PLAYER_PULL_SCALE * (player.onGround() ? 1.0D : PLAYER_AIRBORNE_PULL_SCALE);
                    pushPlayer(player, toCenter.scale(strength * scale / distance), PLAYER_MAX_PULL_SPEED);
                }
                continue;
            }
            Vec3 pull = toCenter.scale(strength / distance);
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.86D).add(pull));
            if (syncTick) {
                entity.needsSync = true;
            }
            entity.fallDistance = 0.0D;
        }
    }

    private void pullTerrainBlocks(ServerLevel level) {
        int cap = this.blockSuctionCap();
        if (cap <= 0 || this.suckedBlocks >= cap
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }

        Vec3 center = this.effectCenter();
        Vec3 terrainOrigin = center;
        double radius = this.pullRadius();
        int pulledThisTick = 0;
        int suctionAttempts = BLOCK_SUCTION_ATTEMPTS * 4;
        for (int attempt = 0; attempt < suctionAttempts && pulledThisTick < BLOCKS_PER_SUCTION
                && this.suckedBlocks < cap; attempt++) {
            BlockPos pos = BlockPos.containing(
                    terrainOrigin.x + (this.random.nextDouble() * 2.0D - 1.0D) * radius,
                    terrainOrigin.y + (this.random.nextDouble() * 2.0D - 1.0D) * radius,
                    terrainOrigin.z + (this.random.nextDouble() * 2.0D - 1.0D) * radius);
            if (pos.distToCenterSqr(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z) > radius * radius
                    || pos.getY() < level.getMinY()
                    || pos.getY() > level.getMaxY()
                    || !level.getWorldBorder().isWithinBounds(pos)) {
                continue;
            }

            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!BlockSuction.canLift(level, pos, state)
                    || BlockSuction.lift(level, pos, state, center, 0.28D) == null) {
                continue;
            }
            pulledThisTick++;
            this.suckedBlocks++;
        }
    }

    private void collapse() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = this.effectCenter();
        double radius = this.collapseRadius();
        if (this.usesColumnEffects()) {
            this.collapseColumn(level, radius);
        } else {
            this.collapseSphere(level, center, radius);
        }
        if (!this.isGlimmerEcho() && level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            RoyalBlockDestruction.destroySphere(level, this, center, radius * 0.4D, 160, 60.0D, 0.1F);
        }
        level.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.EXPLOSION_EMITTER),
                center.x, center.y, center.z, 8, 3.0D, 3.0D, 3.0D, 0.0D);
        level.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.REVERSE_PORTAL),
                center.x, center.y, center.z, 120, 4.0D, 4.0D, 4.0D, 0.6D);
        if (this.isGlimmerEcho()) {
            GlimmerEchoes.ring(level, center, radius, 64);
        }
    }

    private void collapseColumn(ServerLevel level, double radius) {
        double surfaceY = this.ringSurfaceY(level);
        level.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.EXPLOSION_EMITTER),
                this.getX(), surfaceY, this.getZ(), 6, radius * 0.4D, 0.5D, radius * 0.4D, 0.0D);
        this.spawnRing(level, surfaceY, radius, 72, ParticleTypes.EXPLOSION);
        DamageSource source = this.holeDamageSource();
        AABB bounds = new AABB(this.position(), this.effectCenter()).inflate(radius, radius + COLLAPSE_BELOW_ANCHOR, radius);
        double up = this.riseSign();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (this.isImmune(living)) {
                continue;
            }
            double distance = this.horizontalDistanceToAnchor(living);
            if (distance > radius || !this.withinColumnSpan(living, COLLAPSE_BELOW_ANCHOR)) {
                continue;
            }
            double falloff = Math.max(COLLAPSE_MIN_FALLOFF, 1.0D - distance / radius);
            living.hurtServer(level, source, this.scaledHoleDamage(COLLAPSE_MAX_DAMAGE * falloff));
            Vec3 away = new Vec3(living.getX() - this.getX(), 0.0D, living.getZ() - this.getZ());
            Vec3 launch = (away.lengthSqr() > 1.0E-4D ? away.normalize() : Vec3.ZERO).scale(2.2D * falloff)
                    .add(0.0D, up * (0.4D + 0.8D * falloff), 0.0D);
            GravityApi.setWorldVelocity(living, launch);
            living.needsSync = true;
            living.hurtMarked = true;
        }
    }

    private void collapseSphere(ServerLevel level, Vec3 center, double radius) {
        DamageSource source = this.holeDamageSource();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius))) {
            if (this.isImmune(living)) {
                continue;
            }
            double distance = Math.max(1.0D, living.position().distanceTo(center));
            float falloff = (float) Math.max(0.25D, 1.0D - distance / radius);
            living.hurtServer(level, source, this.scaledHoleDamage(26.0D * falloff));
            Vec3 launch = living.position().subtract(center).normalize().scale(2.6D * falloff);
            living.setDeltaMovement(launch.x, 1.1D * falloff + 0.4D, launch.z);
            living.needsSync = true;
            living.hurtMarked = true;
        }
    }

    private boolean isImmune(Entity entity) {
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        if (this.ownerId != null && this.ownerId.equals(entity.getUUID())) {
            return true;
        }
        LivingEntity owner = this.ownerEntity();
        if (owner instanceof QueenEntity) {
            return entity.is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
        }
        if (entity instanceof RoyalBossEntity) {
            return true;
        }
        return entity.is(AntarchyTags.Entities.TIME_DILATION_IMMUNE);
    }

    @Nullable
    private LivingEntity ownerEntity() {
        if (this.ownerId == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        return level.getEntity(this.ownerId) instanceof LivingEntity living ? living : null;
    }

    private void spawnClientParticles() {
        Vec3 center = this.effectCenter();
        for (int i = 0; i < 6; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double r = this.effectRadius() * this.visualGrowth() * (0.3D + this.random.nextDouble() * 0.7D);
            double px = center.x + Math.cos(angle) * r;
            double pz = center.z + Math.sin(angle) * r;
            double py = center.y + (this.random.nextDouble() - 0.5D) * 3.0D;
            this.level().addParticle(GlimmerEchoes.particle(this, ParticleTypes.PORTAL), px, py, pz,
                    (center.x - px) * 0.18D, (center.y - py) * 0.18D, (center.z - pz) * 0.18D);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!this.isInvulnerableToBase(source)) {
            this.markHurt();
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        this.setRadius(tag.getDoubleOr("Radius", 0.0D));
        this.age = tag.getIntOr(AGE_KEY, 0);
        this.activeTicks = tag.getIntOr(ACTIVE_KEY, QUEEN_ACTIVE_TICKS);
        this.animationAge = tag.getIntOr(ANIMATION_AGE_KEY, this.age);
        this.suckedBlocks = tag.getIntOr(SUCKED_BLOCKS_KEY, 0);
        this.entityData.set(EFFECT_RADIUS, (float) this.computeEffectRadius());
        this.ownerId = tag.read(OWNER_KEY, UUIDUtil.CODEC).orElse(null);
        this.setGlimmerEcho(tag.getBooleanOr(GlimmerEchoes.SAVE_KEY, false));
        this.setGravityInverted(tag.getBooleanOr(INVERTED_KEY, false));
        this.entityData.set(COLLAPSING, tag.getBooleanOr(COLLAPSING_KEY, false));
        if (this.isCollapsing()) {
            tag.read(EFFECT_RADIUS_KEY, Codec.FLOAT).ifPresent(radius -> this.entityData.set(EFFECT_RADIUS, radius));
        }
        this.dilationFieldId = tag.read(DILATION_FIELD_KEY, UUIDUtil.CODEC).orElse(null);
        this.travelTicksRemaining = Math.max(0, tag.getIntOr(TRAVEL_TICKS_KEY, 0));
        if (this.travelTicksRemaining > 0 && tag.read(DESTINATION_X_KEY, Codec.DOUBLE).isPresent()) {
            this.destination = new Vec3(tag.getDoubleOr(DESTINATION_X_KEY, 0.0D), tag.getDoubleOr(DESTINATION_Y_KEY, 0.0D),
                    tag.getDoubleOr(DESTINATION_Z_KEY, 0.0D));
            float progress = 1.0F - this.travelTicksRemaining / (float) QUEEN_PROJECTILE_TRAVEL_TICKS;
            this.entityData.set(VISUAL_GROWTH, Mth.lerp(progress, 0.15F, 1.0F));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putDouble("Radius", this.radius());
        tag.putInt(AGE_KEY, this.age);
        tag.putInt(ACTIVE_KEY, this.activeTicks);
        tag.putInt(ANIMATION_AGE_KEY, this.animationAge);
        tag.putInt(SUCKED_BLOCKS_KEY, this.suckedBlocks);
        tag.putBoolean(GlimmerEchoes.SAVE_KEY, this.isGlimmerEcho());
        tag.putBoolean(INVERTED_KEY, this.isGravityInverted());
        tag.putBoolean(COLLAPSING_KEY, this.isCollapsing());
        tag.putFloat(EFFECT_RADIUS_KEY, this.entityData.get(EFFECT_RADIUS));
        tag.storeNullable(OWNER_KEY, UUIDUtil.CODEC, this.ownerId);
        tag.storeNullable(DILATION_FIELD_KEY, UUIDUtil.CODEC, this.dilationFieldId);
        tag.putInt(TRAVEL_TICKS_KEY, this.travelTicksRemaining);
        if (this.destination != null) {
            tag.putDouble(DESTINATION_X_KEY, this.destination.x);
            tag.putDouble(DESTINATION_Y_KEY, this.destination.y);
            tag.putDouble(DESTINATION_Z_KEY, this.destination.z);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RoyalBlackHoleEntity>("black_hole_controller", 0, this::blackHoleController));
    }

    private PlayState blackHoleController(AnimationTest<RoyalBlackHoleEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
