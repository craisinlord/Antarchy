package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.UUIDUtil;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class NightmarePortalEntity extends Entity implements GeoEntity {
    private static final int OPEN_TICKS = 10;
    private static final int CLOSE_TICKS = 10;
    private static final EntityDataAccessor<Boolean> CLOSING = SynchedEntityData.defineId(NightmarePortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("close");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int lifetimeTicks;
    private int ageTicks;
    private UUID ownerId;
    private UUID linkedPortalId;
    private Vec3 fallbackDestination = Vec3.ZERO;

    public NightmarePortalEntity(EntityType<? extends NightmarePortalEntity> entityType, Level level) {
        super(entityType, level);
        this.setPermanentlyInvulnerable(true);
        this.noPhysics = true;
    }

    public static NightmarePortalEntity spawnAt(ServerLevel level, Vec3 pos, NightmareEntity owner, Vec3 fallbackDestination, int lifetimeTicks) {
        NightmarePortalEntity portal = new NightmarePortalEntity(AntarchyObjects.NIGHTMARE_PORTAL.get(), level);
        portal.snapTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
        portal.ownerId = owner.getUUID();
        portal.fallbackDestination = fallbackDestination;
        portal.lifetimeTicks = lifetimeTicks;
        level.addFreshEntity(portal);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 0.22F, 0.95F + owner.getRandom().nextFloat() * 0.1F);
        return portal;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CLOSING, false);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
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
    public boolean canBeCollidedWith(Entity other) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        if (this.level().isClientSide()) {
            return;
        }

        if (this.ageTicks >= this.lifetimeTicks - CLOSE_TICKS) {
            this.entityData.set(CLOSING, true);
        }
        if (this.ageTicks >= this.lifetimeTicks) {
            this.discard();
            return;
        }
    }

    public void linkTo(NightmarePortalEntity other) {
        this.linkedPortalId = other.getUUID();
        this.fallbackDestination = other.position();
    }

    public boolean isClosing() {
        return this.getEntityData().get(CLOSING);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        output.putInt("LifetimeTicks", this.lifetimeTicks);
        output.putInt("AgeTicks", this.ageTicks);
        output.storeNullable("OwnerId", UUIDUtil.CODEC, this.ownerId);
        output.storeNullable("LinkedPortalId", UUIDUtil.CODEC, this.linkedPortalId);
        output.putDouble("FallbackX", this.fallbackDestination.x);
        output.putDouble("FallbackY", this.fallbackDestination.y);
        output.putDouble("FallbackZ", this.fallbackDestination.z);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        this.lifetimeTicks = input.getIntOr("LifetimeTicks", 0);
        this.ageTicks = input.getIntOr("AgeTicks", 0);
        input.read("OwnerId", UUIDUtil.CODEC).ifPresent(id -> this.ownerId = id);
        input.read("LinkedPortalId", UUIDUtil.CODEC).ifPresent(id -> this.linkedPortalId = id);
        this.fallbackDestination = new Vec3(input.getDoubleOr("FallbackX", 0.0D), input.getDoubleOr("FallbackY", 0.0D), input.getDoubleOr("FallbackZ", 0.0D));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NightmarePortalEntity>("portal_controller", 0, this::portalController));
    }

    private PlayState portalController(AnimationTest<NightmarePortalEntity> state) {
        state.controller().setAnimationSpeed(this.isClosing() || this.ageTicks < OPEN_TICKS ? 4.0D : 1.0D);
        if (this.isClosing()) {
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
