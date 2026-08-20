package com.craisinlord.antarchy.content.portalgun;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PortalGunBlackHoleEntity extends Entity implements GeoEntity {
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("close");
    private static final int OPEN_TICKS = 10;
    private static final int CLOSE_TICKS = 12;
    private static final int LIFETIME_TICKS = 20 * 12;
    private static final double PULL_RADIUS = 6.0D;
    private static final double CONSUME_RADIUS = 0.6D;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerId;
    private int ageTicks;
    private boolean closing;

    public PortalGunBlackHoleEntity(EntityType<? extends PortalGunBlackHoleEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    public void configure(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        this.setYRot(this.getYRot() + 6.0F);
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.ageTicks >= LIFETIME_TICKS - CLOSE_TICKS) {
            this.closing = true;
        }
        if (this.ageTicks >= LIFETIME_TICKS) {
            this.discard();
            return;
        }
        this.pullNearby(serverLevel);
        if (this.ageTicks % 10 == 0) {
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.5F, 0.6F);
        }
    }

    private void pullNearby(ServerLevel level) {
        AABB pullBox = this.getBoundingBox().inflate(PULL_RADIUS);
        List<Entity> nearby = level.getEntities(this, pullBox, this::canAffect);
        for (Entity entity : nearby) {
            Vec3 toCenter = this.position().subtract(entity.position());
            double distance = toCenter.length();
            if (distance < 1.0E-4D) {
                continue;
            }
            if (distance <= CONSUME_RADIUS) {
                this.consume(entity);
                continue;
            }
            double strength = 0.09D * (1.0D - Math.min(distance / PULL_RADIUS, 1.0D)) + 0.02D;
            Vec3 pull = toCenter.normalize().scale(strength);
            entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
            entity.hasImpulse = true;
            entity.fallDistance = 0.0F;
        }
    }

    private boolean canAffect(Entity entity) {
        if (!entity.isAlive() || entity instanceof PortalGunPortalEntity || entity instanceof PortalGunBlackHoleEntity) {
            return false;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return false;
        }
        return entity instanceof ItemEntity || entity instanceof LivingEntity;
    }

    private void consume(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            itemEntity.discard();
        } else if (entity instanceof Player) {
            entity.teleportTo(this.getX(), this.getY(), this.getZ());
            entity.setDeltaMovement(Vec3.ZERO);
        } else if (entity instanceof LivingEntity living) {
            living.hurt(this.damageSources().magic(), 4.0F);
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.1D));
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(2.0D);
    }

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(this.getX() - 0.6D, this.getY() - 0.6D, this.getZ() - 0.6D, this.getX() + 0.6D, this.getY() + 0.6D, this.getZ() + 0.6D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerId != null) {
            tag.putUUID("OwnerId", this.ownerId);
        }
        tag.putInt("AgeTicks", this.ageTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
        }
        this.ageTicks = tag.getInt("AgeTicks");
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "black_hole_controller", 0, this::blackHoleController));
    }

    private PlayState blackHoleController(AnimationState<PortalGunBlackHoleEntity> state) {
        if (this.closing) {
            return state.setAndContinue(CLOSE_ANIM);
        }
        if (this.ageTicks < OPEN_TICKS) {
            return state.setAndContinue(OPEN_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
