package com.craisinlord.antarchy.content.entity.lucid;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Supplier;

public class LucidBoltEntity extends ThrowableProjectile {
    private static final int MAX_LIFETIME_TICKS = 40;
    private static final int INVERTED_EFFECT_DURATION = 100;
    private static final double BASE_DAMAGE = 3.0D;

    public static Supplier<Holder<MobEffect>> invertedEffectSupplier;

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
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target
                && target.getType().is(AntarchyTags.Entities.LUCID_BOLT_IMMUNE)) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
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

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide()) {
            this.level().playSound(null, result.getBlockPos(), AntarchySoundEvents.LUCID_BOLT_SOUND.get(), SoundSource.HOSTILE, 0.8F, 1.0F);
        }
        this.discard();
    }
}
