package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
public abstract class GameRendererSwingThroughGrassMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void antarchy$preferAttackableEntityBehindGrass(float partialTick, CallbackInfo ci) {
        if (!AntarchySettings.swingThroughGrassEnabled()) {
            return;
        }
        Entity cameraEntity = this.minecraft.getCameraEntity();
        ClientLevel level = this.minecraft.level;
        if (cameraEntity == null || level == null || this.minecraft.gameMode == null) {
            return;
        }
        if (!(this.minecraft.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()) {
            return;
        }

        double blockInteractionRange = this.minecraft.gameMode.getPickRange();
        double entityInteractionRange = antarchy$entityInteractionRange(cameraEntity);
        double maxRange = Math.max(blockInteractionRange, entityInteractionRange);
        double maxRangeSq = maxRange * maxRange;
        Vec3 eyePosition = cameraEntity.getEyePosition(partialTick);
        Vec3 viewVector = cameraEntity.getViewVector(1.0F);
        Vec3 reachEnd = eyePosition.add(viewVector.x * maxRange, viewVector.y * maxRange, viewVector.z * maxRange);
        AABB searchBox = cameraEntity.getBoundingBox().expandTowards(viewVector.scale(maxRange)).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(cameraEntity, eyePosition, reachEnd, searchBox,
                candidate -> !candidate.isSpectator() && candidate.isPickable(), maxRangeSq);
        if (entityHitResult == null) {
            return;
        }

        Entity hitEntity = entityHitResult.getEntity();
        if (!(hitEntity instanceof LivingEntity)
                || hitEntity.isSpectator()
                || !hitEntity.isAttackable()
                || this.minecraft.player == null
                || hitEntity == this.minecraft.player.getVehicle()) {
            return;
        }

        if (eyePosition.distanceToSqr(hitEntity.position()) < maxRangeSq) {
            Vec3 location = entityHitResult.getLocation();
            if (!location.closerThan(eyePosition, entityInteractionRange)) {
                Vec3 direction = location.subtract(eyePosition);
                this.minecraft.hitResult = BlockHitResult.miss(location, Direction.getNearest(direction.x, direction.y, direction.z), BlockPos.containing(location));
                this.minecraft.crosshairPickEntity = null;
            } else {
                this.minecraft.hitResult = entityHitResult;
                this.minecraft.crosshairPickEntity = hitEntity;
            }
        }
    }

    @Unique
    private double antarchy$entityInteractionRange(Entity cameraEntity) {
        if (this.minecraft.gameMode != null && this.minecraft.gameMode.hasFarPickRange()) {
            return 6.0D;
        }
        if (cameraEntity instanceof Player player) {
            return EntityReach.entityInteractionRange(player);
        }
        return EntityReach.DEFAULT_ENTITY_INTERACTION_RANGE;
    }
}
