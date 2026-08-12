package com.craisinlord.antarchy.compat.geckolib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;

public final class AutoPlayingSoundKeyframeHandlerCompat {
    private AutoPlayingSoundKeyframeHandlerCompat() {
    }

    public static <A extends LivingEntity & GeoAnimatable> AnimationController.SoundKeyframeHandler<A> create() {
        return event -> {
            String[] parts = event.getKeyframeData().getSound().split("\\|");
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(parts[0]));
            if (sound == null) {
                return;
            }
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1f;
            A animatable = event.getAnimatable();
            animatable.level().playSound(null, animatable.getX(), animatable.getY(), animatable.getZ(), sound, animatable.getSoundSource(), volume, pitch);
        };
    }
}
