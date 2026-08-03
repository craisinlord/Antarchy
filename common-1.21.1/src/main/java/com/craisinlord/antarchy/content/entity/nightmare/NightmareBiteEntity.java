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

public class NightmareBiteEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(NightmareBiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("bite");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int ageTicks;

    public NightmareBiteEntity(EntityType<? extends NightmareBiteEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static NightmareBiteEntity spawnAt(ServerLevel level, Vec3 pos, float yaw, boolean phaseTwo) {
        NightmareBiteEntity bite = new NightmareBiteEntity(AntarchyObjects.NIGHTMARE_BITE.get(), level);
        bite.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
        bite.setPhaseTwo(phaseTwo);
        level.addFreshEntity(bite);
        return bite;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PHASE_TWO, false);
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

    public boolean isPhaseTwo() {
        return this.entityData.get(PHASE_TWO);
    }

    private void setPhaseTwo(boolean phaseTwo) {
        this.getEntityData().set(PHASE_TWO, phaseTwo);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (++this.ageTicks >= 12) {
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("PhaseTwo", this.isPhaseTwo());
        tag.putInt("AgeTicks", this.ageTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.setPhaseTwo(tag.getBoolean("PhaseTwo"));
        this.ageTicks = tag.getInt("AgeTicks");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bite_controller", 0, this::biteController));
    }

    private PlayState biteController(AnimationState<NightmareBiteEntity> state) {
        return state.setAndContinue(BITE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
