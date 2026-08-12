package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

public final class GoopedEffectHooks {
    public static final int PROJECTILE_DURATION_TICKS = 20 * 8;
    private static final ResourceKey<MobEffect> GOOPED_KEY = ResourceKey.create(
            Registries.MOB_EFFECT,
            new ResourceLocation(Antarchy.MODID, "gooped")
    );

    private GoopedEffectHooks() {
    }

    @Nullable
    public static Holder<MobEffect> holder() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(GOOPED_KEY).orElse(null);
    }
}
