package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum GlimmerVariant {
    APPLE_COW(new GlimmerAppleCowBehavior(), "apple_of_my_eye"),
    OURANWOOD_DEER(new GlimmerOuranwoodDeerBehavior(), "deerly_beloved"),
    FROG(new GlimmerFrogBehavior(), "hoppy_together"),
    ANT(new GlimmerAntBehavior(), "spirant_animal"),
    ELKA(new GlimmerElkaBehavior(), "elkcellent_choice");

    public static final Codec<GlimmerVariant> CODEC = Codec.STRING.xmap(GlimmerVariant::byName, GlimmerVariant::name);

    private final GlimmerVariantBehavior behavior;
    private final ResourceLocation tameAdvancementId;

    GlimmerVariant(GlimmerVariantBehavior behavior, String tameAdvancementPath) {
        this.behavior = behavior;
        this.tameAdvancementId = new ResourceLocation(Antarchy.MODID, tameAdvancementPath);
    }

    public GlimmerVariantBehavior getBehavior() {
        return this.behavior;
    }

    public ResourceLocation tameAdvancementId() {
        return this.tameAdvancementId;
    }

    public Component displayName() {
        return Component.translatable("glimmer_variant.antarchy." + this.name().toLowerCase(Locale.ROOT));
    }

    public static GlimmerVariant random(net.minecraft.util.RandomSource random) {
        GlimmerVariant[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static GlimmerVariant byName(String name) {
        try {
            return GlimmerVariant.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return APPLE_COW;
        }
    }
}
