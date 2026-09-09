package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class RoyalEffectHooks {
    private static final ResourceKey<MobEffect> COMMANDED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "commanded"));
    private static final ResourceKey<MobEffect> DILATED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dilated"));
    private static final ResourceKey<MobEffect> CONTRACTED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "contracted"));

    private RoyalEffectHooks() {
    }

    public static Holder<MobEffect> commandedHolder() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(COMMANDED_KEY).orElse(null);
    }

    public static Holder<MobEffect> dilatedHolder() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(DILATED_KEY).orElse(null);
    }

    public static Holder<MobEffect> contractedHolder() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(CONTRACTED_KEY).orElse(null);
    }
}
