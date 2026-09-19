package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationFieldEntity;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalBlackHoleEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> COLLAPSING =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> VISUAL_GROWTH =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_RADIUS =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
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
        this.noCulling = true;
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
        if (this.level().isClientSide) {
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
            if (this.pullsTerrainBlocks() && this.age % BLOCK_SUCTION_INTERVAL == 0
                    && this.level() instanceof ServerLevel level) {
                this.pullTerrainBlocks(level);
            }
            if (this.age >= this.activeTicks) {
                this.entityData.set(COLLAPSING, true);
                this.age = 0;
            }
            return;
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

    /** Returns the center of the animated black-hole cube in world coordinates. */
    public Vec3 effectCenter() {
        double animationSeconds = Math.min(CORE_RISE_END_SECONDS, this.animationAge / 20.0D);
        double riseProgress = animationSeconds / CORE_RISE_END_SECONDS;
        double animatedOffset = Mth.lerp(riseProgress, CORE_START_OFFSET_Y, CORE_END_OFFSET_Y);
        double modelY = CORE_CUBE_CENTER_Y + animatedOffset;
        double worldOffsetY = (modelY - MODEL_BASELINE_Y) / 16.0D * MODEL_RENDER_SCALE;
        return this.position().add(0.0D, worldOffsetY, 0.0D);
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
        double radius = this.pullRadius();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius))) {
            if (entity == this || entity instanceof TimeDilationFieldEntity || this.isImmune(entity)) {
                continue;
            }
            Vec3 toCenter = center.subtract(entity.position());
            double distance = toCenter.length();
            if (distance < 0.75D) {
                if (entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof FallingBlockEntity) {
                    entity.discard();
                }
                continue;
            }
            if (distance > radius) {
                continue;
            }
            double strength = AntarchySettings.queenBlackHolePullStrength() * (1.0D - distance / radius);
            Vec3 pull = toCenter.scale(strength / distance);
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.86D).add(pull));
            entity.hasImpulse = true;
            entity.fallDistance = 0.0F;
        }
    }

    private void pullTerrainBlocks(ServerLevel level) {
        int cap = AntarchySettings.queenBlackHoleBlockSuctionCap();
        if (cap <= 0 || this.suckedBlocks >= cap
                || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
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
                    || pos.getY() < level.getMinBuildHeight()
                    || pos.getY() >= level.getMaxBuildHeight()
                    || !level.getWorldBorder().isWithinBounds(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir() || level.getBlockEntity(pos) != null
                    || state.getDestroySpeed(level, pos) < 0.0F
                    || state.getBlock().getExplosionResistance() > 30.0F
                    || state.getCollisionShape(level, pos).isEmpty()) {
                continue;
            }

            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            if (falling == null) {
                continue;
            }
            Vec3 towardCenter = center.subtract(falling.position());
            if (towardCenter.lengthSqr() > 0.001D) {
                falling.setDeltaMovement(towardCenter.normalize().scale(0.28D));
            }
            falling.hasImpulse = true;
            falling.hurtMarked = true;
            pulledThisTick++;
            this.suckedBlocks++;
        }
    }

    private void collapse() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = this.effectCenter();
        double radius = this.effectRadius();
        DamageSource source = this.ownerEntity() != null
                ? this.damageSources().mobAttack(this.ownerEntity())
                : this.damageSources().magic();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius))) {
            if (this.isImmune(living)) {
                continue;
            }
            double distance = Math.max(1.0D, living.position().distanceTo(center));
            float falloff = (float) Math.max(0.25D, 1.0D - distance / radius);
            double damage = 26.0D * falloff;
            living.hurt(source, this.ownerEntity() instanceof RoyalBossEntity royalBoss
                    ? royalBoss.scaleRoyalDamage(damage) : (float) damage);
            Vec3 launch = living.position().subtract(center).normalize().scale(2.6D * falloff);
            living.setDeltaMovement(launch.x, 1.1D * falloff + 0.4D, launch.z);
            living.hasImpulse = true;
        }
        if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            RoyalBlockDestruction.destroySphere(level, this, center, radius * 0.4D, 160, 60.0D, 0.1F);
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 8, 3.0D, 3.0D, 3.0D, 0.0D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 120, 4.0D, 4.0D, 4.0D, 0.6D);
    }

    private boolean isImmune(Entity entity) {
        if (this.ownerId != null && this.ownerId.equals(entity.getUUID())) {
            return true;
        }
        LivingEntity owner = this.ownerEntity();
        if (owner instanceof QueenEntity) {
            return entity.getType().is(com.craisinlord.antarchy.content.AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
        }
        if (entity instanceof RoyalBossEntity) {
            return true;
        }
        return entity.getType().is(com.craisinlord.antarchy.content.AntarchyTags.Entities.TIME_DILATION_IMMUNE);
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
            this.level().addParticle(ParticleTypes.PORTAL, px, py, pz,
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
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRadius(tag.getDouble("Radius"));
        this.age = tag.getInt(AGE_KEY);
        this.activeTicks = tag.contains(ACTIVE_KEY) ? tag.getInt(ACTIVE_KEY) : QUEEN_ACTIVE_TICKS;
        this.animationAge = tag.contains(ANIMATION_AGE_KEY) ? tag.getInt(ANIMATION_AGE_KEY) : this.age;
        this.suckedBlocks = tag.getInt(SUCKED_BLOCKS_KEY);
        this.entityData.set(EFFECT_RADIUS, (float) this.computeEffectRadius());
        this.ownerId = tag.hasUUID(OWNER_KEY) ? tag.getUUID(OWNER_KEY) : null;
        this.dilationFieldId = tag.hasUUID(DILATION_FIELD_KEY) ? tag.getUUID(DILATION_FIELD_KEY) : null;
        this.travelTicksRemaining = Math.max(0, tag.getInt(TRAVEL_TICKS_KEY));
        if (this.travelTicksRemaining > 0 && tag.contains(DESTINATION_X_KEY)) {
            this.destination = new Vec3(tag.getDouble(DESTINATION_X_KEY), tag.getDouble(DESTINATION_Y_KEY),
                    tag.getDouble(DESTINATION_Z_KEY));
            float progress = 1.0F - this.travelTicksRemaining / (float) QUEEN_PROJECTILE_TRAVEL_TICKS;
            this.entityData.set(VISUAL_GROWTH, Mth.lerp(progress, 0.15F, 1.0F));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("Radius", this.radius());
        tag.putInt(AGE_KEY, this.age);
        tag.putInt(ACTIVE_KEY, this.activeTicks);
        tag.putInt(ANIMATION_AGE_KEY, this.animationAge);
        tag.putInt(SUCKED_BLOCKS_KEY, this.suckedBlocks);
        if (this.ownerId != null) {
            tag.putUUID(OWNER_KEY, this.ownerId);
        }
        if (this.dilationFieldId != null) {
            tag.putUUID(DILATION_FIELD_KEY, this.dilationFieldId);
        }
        tag.putInt(TRAVEL_TICKS_KEY, this.travelTicksRemaining);
        if (this.destination != null) {
            tag.putDouble(DESTINATION_X_KEY, this.destination.x);
            tag.putDouble(DESTINATION_Y_KEY, this.destination.y);
            tag.putDouble(DESTINATION_Z_KEY, this.destination.z);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "black_hole_controller", 0, this::blackHoleController));
    }

    private PlayState blackHoleController(AnimationState<RoyalBlackHoleEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
