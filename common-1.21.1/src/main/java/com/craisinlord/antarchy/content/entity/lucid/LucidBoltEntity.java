package com.craisinlord.antarchy.content.entity.lucid;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LucidBoltEntity extends ThrowableProjectile implements GeoEntity {
    private static final int MAX_LIFETIME_TICKS = 40;
    private static final int INVERTED_EFFECT_DURATION = 100;
    private static final double BASE_DAMAGE = 3.0D;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    public static Supplier<Holder<MobEffect>> invertedEffectSupplier;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public LucidBoltEntity(EntityType<? extends LucidBoltEntity> entityType, Level level) {
        super(entityType, level);
        this.configureBolt();
    }

    public LucidBoltEntity(EntityType<? extends LucidBoltEntity> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
        this.configureBolt();
    }

    private void configureBolt() {
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }
        super.tick();
        this.realignToMotion();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            spawnImpactParticles(serverLevel, result.getLocation());
            if (result.getEntity() instanceof LivingEntity target && target.getType().is(AntarchyTags.Entities.LUCID_BOLT_IMMUNE)) {
                this.discard();
                return;
            }

            if (result.getEntity() instanceof LivingEntity target) {
                Entity owner = this.getOwner();
                if (owner instanceof LivingEntity shooter) {
                    target.hurt(this.damageSources().mobAttack(shooter), (float) BASE_DAMAGE);
                } else {
                    target.hurt(this.damageSources().magic(), (float) BASE_DAMAGE);
                }

                if (invertedEffectSupplier != null) {
                    target.addEffect(new MobEffectInstance(invertedEffectSupplier.get(), INVERTED_EFFECT_DURATION, 0, false, true));
                }
            }
        }

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            spawnImpactParticles(serverLevel, result.getLocation());
            this.level().playSound(null, result.getBlockPos(), AntarchySoundEvents.LUCID_BOLT_SOUND.get(), SoundSource.HOSTILE, 0.8F, 1.0F);
        }
        this.discard();
    }

    public static void spawnImpactParticles(ServerLevel level, Vec3 position) {
        level.sendParticles(AntarchyObjects.LUCID_BOLT_IMPACT_LARGE.get(), position.x, position.y, position.z, 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(AntarchyObjects.LUCID_BOLT_IMPACT_SMALL.get(), position.x, position.y, position.z, 10, 0.16D, 0.16D, 0.16D, 0.065D);
    }

    private void realignToMotion() {
        net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6D) {
            return;
        }

        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.setYRot((float) (net.minecraft.util.Mth.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F);
        this.setXRot((float) (-(net.minecraft.util.Mth.atan2(motion.y, horizontal) * (180.0D / Math.PI))));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bolt_controller", 0, this::boltController));
    }

    private PlayState boltController(AnimationState<LucidBoltEntity> state) {
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
