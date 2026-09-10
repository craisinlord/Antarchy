package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalBlackHoleEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> COLLAPSING =
            SynchedEntityData.defineId(RoyalBlackHoleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String OWNER_KEY = "OwnerUuid";
    private static final String AGE_KEY = "Age";
    private static final String ACTIVE_KEY = "ActiveTicks";

    private static final double DEFAULT_RADIUS = 14.0D;
    private static final int DEFAULT_ACTIVE_TICKS = 120;
    private static final int COLLAPSE_TICKS = 12;

    @Nullable
    private UUID ownerId;
    private int age;
    private int activeTicks = DEFAULT_ACTIVE_TICKS;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RoyalBlackHoleEntity(EntityType<? extends RoyalBlackHoleEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static RoyalBlackHoleEntity create(ServerLevel level, Vec3 center, double radius, int activeTicks, @Nullable UUID ownerId) {
        RoyalBlackHoleEntity hole = new RoyalBlackHoleEntity(AntarchyObjects.ROYAL_BLACK_HOLE.get(), level);
        hole.setPos(center.x, center.y, center.z);
        hole.setRadius(radius > 0.0D ? radius : AntarchySettings.queenBlackHoleRadius());
        hole.activeTicks = activeTicks > 0 ? Math.max(20, activeTicks) : AntarchySettings.queenBlackHoleActiveTicks();
        hole.ownerId = ownerId;
        return hole;
    }

    public static RoyalBlackHoleEntity create(ServerLevel level, Vec3 center, @Nullable UUID ownerId) {
        return create(level, center, AntarchySettings.queenBlackHoleRadius(), AntarchySettings.queenBlackHoleActiveTicks(), ownerId);
    }

    protected void setActiveTicks(int activeTicks) {
        this.activeTicks = activeTicks > 0 ? Math.max(20, activeTicks) : DEFAULT_ACTIVE_TICKS;
    }

    protected void setOwnerId(@Nullable UUID ownerId) {
        this.ownerId = ownerId;
    }

    protected boolean risesWhileActive() {
        return false;
    }

    protected double timeDilationRadius() {
        return Math.max(2.0D, this.radius() * 0.55D);
    }

    protected boolean hasVisibleTimeDilationField() {
        return true;
    }

    protected int collapseDurationTicks() {
        return COLLAPSE_TICKS;
    }

    protected boolean pullsTerrainBlocks() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, (float) DEFAULT_RADIUS);
        this.entityData.define(COLLAPSING, false);
    }

    public void setRadius(double radius) {
        this.entityData.set(RADIUS, (float) Math.max(1.0D, radius));
    }

    public double radius() {
        return this.entityData.get(RADIUS);
    }

    public boolean isCollapsing() {
        return this.entityData.get(COLLAPSING);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.spawnClientParticles();
            return;
        }
        this.age++;
        if (!this.isCollapsing()) {
            if (this.risesWhileActive()) {
                this.setPos(this.getX(), this.getY() + 0.035D, this.getZ());
            }
            this.applyPull();
            if (this.age % 10 == 0 && this.level() instanceof ServerLevel level) {
                if (this.hasVisibleTimeDilationField()) {
                    TimeDilationApi.createField(level, this.position(), this.timeDilationRadius(), 0.18D, 14, this.ownerId);
                }
                if (this.pullsTerrainBlocks() && this.age % 20 == 0 && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    RoyalBlockDestruction.destroySphere(level, this, this.position(), Math.min(2.0D, this.radius() * 0.2D), 8, 30.0D, 0.0F);
                }
            }
            if (this.age >= this.activeTicks) {
                this.entityData.set(COLLAPSING, true);
                this.age = 0;
            }
            return;
        }
        if (this.age >= this.collapseDurationTicks()) {
            this.collapse();
            this.discard();
        }
    }

    private void applyPull() {
        Vec3 center = this.position();
        double radius = this.radius();
        for (Entity entity : this.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius))) {
            if (entity == this || this.isImmune(entity)) {
                continue;
            }
            Vec3 toCenter = center.subtract(entity.position());
            double distance = toCenter.length();
            if (distance < 0.5D || distance > radius) {
                continue;
            }
            double strength = AntarchySettings.queenBlackHolePullStrength() * (1.0D - distance / radius);
            Vec3 pull = toCenter.scale(strength / distance);
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.86D).add(pull));
            entity.hasImpulse = true;
            entity.fallDistance = 0.0F;
        }
    }

    private void collapse() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = this.position();
        double radius = this.radius();
        DamageSource source = this.ownerEntity() != null
                ? this.damageSources().mobAttack(this.ownerEntity())
                : this.damageSources().magic();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius))) {
            if (this.isImmune(living)) {
                continue;
            }
            double distance = Math.max(1.0D, living.position().distanceTo(center));
            float falloff = (float) Math.max(0.25D, 1.0D - distance / radius);
            living.hurt(source, 26.0F * falloff);
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
        if (entity instanceof RoyalBossEntity) {
            return true;
        }
        if (this.ownerId != null && this.ownerId.equals(entity.getUUID())) {
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
        Vec3 center = this.position();
        for (int i = 0; i < 6; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double r = this.radius() * (0.3D + this.random.nextDouble() * 0.7D);
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
        this.activeTicks = tag.contains(ACTIVE_KEY) ? tag.getInt(ACTIVE_KEY) : DEFAULT_ACTIVE_TICKS;
        this.ownerId = tag.hasUUID(OWNER_KEY) ? tag.getUUID(OWNER_KEY) : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("Radius", this.radius());
        tag.putInt(AGE_KEY, this.age);
        tag.putInt(ACTIVE_KEY, this.activeTicks);
        if (this.ownerId != null) {
            tag.putUUID(OWNER_KEY, this.ownerId);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "black_hole_controller", 0, this::blackHoleController));
    }

    private PlayState blackHoleController(AnimationState<RoyalBlackHoleEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
