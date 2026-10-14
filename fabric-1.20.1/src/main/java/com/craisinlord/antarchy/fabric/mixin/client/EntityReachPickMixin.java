package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class EntityReachPickMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("HEAD"), cancellable = true)
    private void antarchy$pickWithEntityReach(float partialTick, CallbackInfo ci) {
        Entity camera = this.minecraft.getCameraEntity();
        LocalPlayer player = this.minecraft.player;
        if (camera == null || player == null || this.minecraft.level == null || this.minecraft.gameMode == null) {
            return;
        }
        AttributeInstance reach = EntityReach.instance(player);
        if (reach == null || reach.getModifiers().isEmpty()) {
            return;
        }

        this.minecraft.getProfiler().push("pick");
        double blockRange = EntityReach.blockInteractionRange(player);
        double entityRange = EntityReach.entityInteractionRange(player);
        HitResult hitResult = antarchy$pick(camera, blockRange, entityRange, partialTick);
        this.minecraft.hitResult = hitResult;
        this.minecraft.crosshairPickEntity = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
        this.minecraft.getProfiler().pop();
        ci.cancel();
    }

    @Unique
    private HitResult antarchy$pick(Entity camera, double blockRange, double entityRange, float partialTick) {
        double maxRange = Math.max(blockRange, entityRange);
        double maxRangeSq = maxRange * maxRange;
        Vec3 eyePosition = camera.getEyePosition(partialTick);
        HitResult blockHit = camera.pick(maxRange, partialTick, false);
        double blockHitDistanceSq = blockHit.getLocation().distanceToSqr(eyePosition);
        Double swingThroughReachSq = antarchy$swingThroughReachSq(blockHit, maxRangeSq);
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxRangeSq = blockHitDistanceSq;
            maxRange = Math.sqrt(maxRangeSq);
        }

        Vec3 viewVector = camera.getViewVector(1.0F);
        Vec3 reachEnd = eyePosition.add(viewVector.x * maxRange, viewVector.y * maxRange, viewVector.z * maxRange);
        AABB searchBox = camera.getBoundingBox().expandTowards(viewVector.scale(maxRange)).inflate(1.0D, 1.0D, 1.0D);
        double entitySearchSq = swingThroughReachSq != null ? swingThroughReachSq : maxRangeSq;
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(camera, eyePosition, reachEnd, searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(), entitySearchSq);

        if (entityHit != null && swingThroughReachSq != null && antarchy$canSwingThrough(entityHit.getEntity())
                && eyePosition.distanceToSqr(entityHit.getEntity().position()) < swingThroughReachSq) {
            return antarchy$filterHitResult(entityHit, eyePosition, entityRange);
        }
        if (entityHit != null && entityHit.getLocation().distanceToSqr(eyePosition) < blockHitDistanceSq) {
            return antarchy$filterHitResult(entityHit, eyePosition, entityRange);
        }
        return antarchy$filterHitResult(blockHit, eyePosition, blockRange);
    }

    @Unique
    private Double antarchy$swingThroughReachSq(HitResult hitResult, double maxRangeSq) {
        if (!AntarchySettings.swingThroughGrassEnabled()) {
            return null;
        }
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK || this.minecraft.level == null) {
            return null;
        }
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (this.minecraft.level.getBlockState(blockPos).getCollisionShape(this.minecraft.level, blockPos).isEmpty()) {
            return maxRangeSq;
        }
        return null;
    }

    @Unique
    private boolean antarchy$canSwingThrough(Entity hitEntity) {
        return hitEntity instanceof LivingEntity
                && !hitEntity.isSpectator()
                && hitEntity.isAttackable()
                && this.minecraft.player != null
                && hitEntity != this.minecraft.player.getVehicle();
    }

    @Unique
    private static HitResult antarchy$filterHitResult(HitResult hitResult, Vec3 eyePosition, double range) {
        Vec3 location = hitResult.getLocation();
        if (!location.closerThan(eyePosition, range)) {
            Direction direction = Direction.getNearest(location.x - eyePosition.x, location.y - eyePosition.y, location.z - eyePosition.z);
            return BlockHitResult.miss(location, direction, BlockPos.containing(location));
        }
        return hitResult;
    }
}
