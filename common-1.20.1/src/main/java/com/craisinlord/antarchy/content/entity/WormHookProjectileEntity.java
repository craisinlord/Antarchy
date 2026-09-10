package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.WormHookTetherManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown out by the worm hook item. Sticks into the first block it hits and stays there,
 * anchoring a swing rope back to the thrower until the player detaches.
 */
public class WormHookProjectileEntity extends ThrowableItemProjectile {
    private boolean stuck = false;

    public WormHookProjectileEntity(EntityType<? extends WormHookProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public WormHookProjectileEntity(EntityType<? extends WormHookProjectileEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return AntarchyObjects.WORM_HOOK.get();
    }

    @Override
    public void tick() {
        if (this.stuck) {
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
        super.tick();

        if (!this.level().isClientSide && !this.stuck && this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide || this.stuck) {
            return;
        }

        if (hitResult instanceof EntityHitResult) {
            this.discard();
            return;
        }

        if (!(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        if (!(this.getOwner() instanceof ServerPlayer player)) {
            this.discard();
            return;
        }

        this.stuck = true;
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.setNoGravity(true);
        this.setPos(blockHit.getLocation());

        boolean attached = WormHookTetherManager.attach(player, this);
        if (!attached) {
            this.discard();
            return;
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.8F, 1.4F);
    }

    public boolean isStuck() {
        return this.stuck;
    }

    public void removeHook() {
        this.discard();
    }
}
