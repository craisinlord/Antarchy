package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class NightmarePortalEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CLOSING = SynchedEntityData.defineId(NightmarePortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open").thenLoop("idle");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("close");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int lifetimeTicks;
    private int ageTicks;
    private UUID ownerId;
    private UUID linkedPortalId;
    private Vec3 fallbackDestination = Vec3.ZERO;

    public NightmarePortalEntity(EntityType<? extends NightmarePortalEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static NightmarePortalEntity spawnAt(ServerLevel level, Vec3 pos, NightmareEntity owner, Vec3 fallbackDestination, int lifetimeTicks) {
        NightmarePortalEntity portal = new NightmarePortalEntity(AntarchyObjects.NIGHTMARE_PORTAL.get(), level);
        portal.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
        portal.ownerId = owner.getUUID();
        portal.fallbackDestination = fallbackDestination;
        portal.lifetimeTicks = lifetimeTicks;
        level.addFreshEntity(portal);
        return portal;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CLOSING, false);
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
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        this.ageTicks++;
        if (this.ageTicks >= this.lifetimeTicks - 10) {
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
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
        tag.putInt("AgeTicks", this.ageTicks);
        if (this.ownerId != null) {
            tag.putUUID("OwnerId", this.ownerId);
        }
        if (this.linkedPortalId != null) {
            tag.putUUID("LinkedPortalId", this.linkedPortalId);
        }
        tag.putDouble("FallbackX", this.fallbackDestination.x);
        tag.putDouble("FallbackY", this.fallbackDestination.y);
        tag.putDouble("FallbackZ", this.fallbackDestination.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.lifetimeTicks = tag.getInt("LifetimeTicks");
        this.ageTicks = tag.getInt("AgeTicks");
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
        }
        if (tag.hasUUID("LinkedPortalId")) {
            this.linkedPortalId = tag.getUUID("LinkedPortalId");
        }
        this.fallbackDestination = new Vec3(tag.getDouble("FallbackX"), tag.getDouble("FallbackY"), tag.getDouble("FallbackZ"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "portal_controller", 0, this::portalController));
    }

    private PlayState portalController(AnimationState<NightmarePortalEntity> state) {
        return state.setAndContinue(this.isClosing() ? CLOSE_ANIM : OPEN_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
