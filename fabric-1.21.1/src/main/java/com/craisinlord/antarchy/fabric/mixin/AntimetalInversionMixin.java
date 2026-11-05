package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityTransition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AntimetalInversionMixin {

    private static final int ANTIMETAL_INVERTED_REFRESH_TICKS = 20;
    private static final int ANTIMETAL_INVERTED_REFRESH_THRESHOLD_TICKS = 12;
    private static final int ANTIMETAL_PLAYER_SCAN_INTERVAL_TICKS = 2;
    private static final int ANTIMETAL_MOB_SCAN_INTERVAL_TICKS = 10;

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickOverheadInversion(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) return;
        if (entity instanceof Player player && player.isSpectator()) return;
        MobEffectInstance existing = entity.getActiveEffectsMap().isEmpty() ? null : entity.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (existing != null) {
            if (existing.getDuration() > ANTIMETAL_INVERTED_REFRESH_THRESHOLD_TICKS) return;
        } else if (!antarchy$isScanPhase(entity)) {
            return;
        }
        if (!antarchy$isDirectlyBelowAntimetal(entity)) return;

        entity.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), ANTIMETAL_INVERTED_REFRESH_TICKS, 0, false, false, false));
        antarchy$spawnInversionParticles(entity);
    }

    private static boolean antarchy$isScanPhase(LivingEntity entity) {
        int interval = entity instanceof Player ? ANTIMETAL_PLAYER_SCAN_INTERVAL_TICKS : ANTIMETAL_MOB_SCAN_INTERVAL_TICKS;
        long phase = (long) entity.getUUID().hashCode() + entity.level().getGameTime();
        return Math.floorMod(phase, interval) == 0;
    }

    private static boolean antarchy$isDirectlyBelowAntimetal(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-3D);
        int minX = Mth.floor(bounds.minX);
        int maxX = Mth.floor(bounds.maxX);
        int minY = Mth.floor(bounds.minY);
        int maxY = Mth.floor(bounds.maxY);
        int minZ = Mth.floor(bounds.minZ);
        int maxZ = Mth.floor(bounds.maxZ);
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    above.set(x, y + 1, z);
                    if (entity.level().getBlockState(above).is(AntarchyTags.Blocks.ANTIMETAL_INVERSION_BLOCKS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Inject(method = "onEffectRemoved", at = @At("HEAD"))
    private void antarchy$onInvertedRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) return;
        if (!effectInstance.is(AntarchyObjects.INVERTED_EFFECT.get())) return;
        GravityApi.setGravityDirection(entity, GravityDirection.DOWN, new GravityTransition(12));
    }

    private static void antarchy$spawnInversionParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        double x = entity.getX();
        double y = entity.getBoundingBox().maxY + 0.1D;
        double z = entity.getZ();
        serverLevel.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.0F),
                x, y, z, 6, 0.2D, 0.05D, 0.2D, 0.0D
        );
    }
}
