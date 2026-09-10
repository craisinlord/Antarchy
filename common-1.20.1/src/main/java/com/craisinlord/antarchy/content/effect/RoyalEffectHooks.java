package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class RoyalEffectHooks {
    private static final ResourceKey<MobEffect> COMMANDED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            new ResourceLocation(Antarchy.MODID, "commanded"));
    private static final ResourceKey<MobEffect> DILATED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            new ResourceLocation(Antarchy.MODID, "dilated"));
    private static final ResourceKey<MobEffect> CONTRACTED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            new ResourceLocation(Antarchy.MODID, "contracted"));

    private RoyalEffectHooks() {
    }

    public static MobEffect commandedHolder() {
        return BuiltInRegistries.MOB_EFFECT.get(COMMANDED_KEY);
    }

    public static MobEffect dilatedHolder() {
        return BuiltInRegistries.MOB_EFFECT.get(DILATED_KEY);
    }

    public static MobEffect contractedHolder() {
        return BuiltInRegistries.MOB_EFFECT.get(CONTRACTED_KEY);
    }
}
