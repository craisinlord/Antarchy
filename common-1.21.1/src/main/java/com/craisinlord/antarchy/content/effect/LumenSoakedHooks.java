package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import com.craisinlord.antarchy.mixins.LivingEntityEffectParticlesAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LumenSoakedHooks {
    public static final int DURATION_TICKS = 20 * 30;
    public static final ResourceLocation DRIP_PARTICLE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lumen_drip");
    private static final ResourceKey<MobEffect> LUMEN_SOAKED_KEY = ResourceKey.create(
            Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lumen_soaked")
    );

    private LumenSoakedHooks() {
    }

    @Nullable
    public static Holder<MobEffect> holder() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(LUMEN_SOAKED_KEY).orElse(null);
    }

    @Nullable
    public static ParticleType<?> dripParticleType() {
        return BuiltInRegistries.PARTICLE_TYPE.getOptional(DRIP_PARTICLE_ID).orElse(null);
    }

    public static void soak(LivingEntity entity) {
        if (entity.level().isClientSide() || entity instanceof GlimmerEntity || !entity.isAlive()) {
            return;
        }
        Holder<MobEffect> soaked = holder();
        if (soaked == null) {
            return;
        }
        MobEffectInstance current = entity.getEffect(soaked);
        if (current != null && current.getDuration() > DURATION_TICKS - 20) {
            return;
        }
        entity.addEffect(new MobEffectInstance(soaked, DURATION_TICKS, 0, false, true, false));
    }

    public static boolean isSoaked(LivingEntity entity) {
        Holder<MobEffect> soaked = holder();
        if (soaked != null && entity.hasEffect(soaked)) {
            return true;
        }
        ParticleType<?> drip = dripParticleType();
        if (drip == null) {
            return false;
        }
        List<ParticleOptions> particles = entity.getEntityData().get(LivingEntityEffectParticlesAccessor.antarchy$getEffectParticlesData());
        for (ParticleOptions particle : particles) {
            if (particle.getType() == drip) {
                return true;
            }
        }
        return false;
    }
}
