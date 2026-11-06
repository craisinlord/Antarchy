package com.craisinlord.antarchy.content.effect;

import com.craisinlord.anthology.effect.AnthologyEffects;
import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public final class RoyalEffectHooks {
    private static final ResourceKey<MobEffect> COMMANDED_KEY = ResourceKey.create(Registries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "commanded"));

    private RoyalEffectHooks() {
    }

    public static Holder<MobEffect> commandedHolder() {
        return BuiltInRegistries.MOB_EFFECT.get(COMMANDED_KEY).orElse(null);
    }

    public static Holder<MobEffect> dilatedHolder() {
        return AnthologyEffects.dilated();
    }

    public static Holder<MobEffect> contractedHolder() {
        return AnthologyEffects.contracted();
    }
}
