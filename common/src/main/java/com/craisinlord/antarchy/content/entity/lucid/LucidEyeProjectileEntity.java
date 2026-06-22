package com.craisinlord.antarchy.content.entity.lucid;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class LucidEyeProjectileEntity extends ThrowableItemProjectile {
    public static Supplier<Item> defaultItemSupplier;

    
    public static Supplier<Holder<MobEffect>> invertedEffectSupplier;

    public LucidEyeProjectileEntity(EntityType<? extends LucidEyeProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LucidEyeProjectileEntity(EntityType<? extends LucidEyeProjectileEntity> entityType,
            LivingEntity thrower, Level level) {
        super(entityType, thrower, level);
    }

    
    @Override
    protected double getDefaultGravity() {
        return -0.03;
    }

    @Override
    protected Item getDefaultItem() {
        return defaultItemSupplier != null ? defaultItemSupplier.get() : net.minecraft.world.item.Items.ENDER_PEARL;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (this.level() instanceof ServerLevel serverLevel) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
            if (owner != null) {
                Vec3 destination = hitResult.getLocation();
                owner.resetFallDistance();
                owner.teleportTo(destination.x, destination.y, destination.z);
                owner.hurt(owner.damageSources().fall(), 5.0F);
            }

            if (owner != null && invertedEffectSupplier != null) {
                int effectDurationTicks = (int) Math.round(AntarchySettings.lucidPearlInvertedDurationSeconds() * 20.0D);
                owner.addEffect(new MobEffectInstance(
                        invertedEffectSupplier.get(), effectDurationTicks, 0, false, true));
                serverLevel.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                        1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }
        }

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

}
