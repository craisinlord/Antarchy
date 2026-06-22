package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickOverheadInversion(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) return;
        if (entity instanceof Player player && player.isSpectator()) return;
        if (!antarchy$isDirectlyBelowAntimetal(entity)) return;

        MobEffectInstance existing = entity.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (existing == null || existing.getDuration() <= 5) {
            entity.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), ANTIMETAL_INVERTED_REFRESH_TICKS, 0, false, false, false));
            antarchy$spawnInversionParticles(entity);
        }
    }

    private static boolean antarchy$isDirectlyBelowAntimetal(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-3D);
        int minX = Mth.floor(bounds.minX);
        int maxX = Mth.floor(bounds.maxX);
        int minY = Mth.floor(bounds.minY);
        int maxY = Mth.floor(bounds.maxY);
        int minZ = Mth.floor(bounds.minZ);
        int maxZ = Mth.floor(bounds.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos above = new BlockPos(x, y + 1, z);
                    if (entity.level().getBlockState(above).is(AntarchyObjects.ANTIMETAL.get())
                            || entity.level().getBlockState(above).is(AntarchyObjects.POLISHED_ANTIMETAL.get())) {
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
        AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, new AntarchyGravityTransition(12));
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
