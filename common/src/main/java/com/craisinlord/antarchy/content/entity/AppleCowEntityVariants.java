package com.craisinlord.antarchy.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

public final class AppleCowEntityVariants {
    private AppleCowEntityVariants() {
    }

    public static class AppleCow extends AppleCowEntity {
        public AppleCow(EntityType<? extends Cow> entityType, Level level) {
            super(entityType, level);
        }
    }

    public static class GoldenAppleCow extends AppleCowEntity {
        public GoldenAppleCow(EntityType<? extends Cow> entityType, Level level) {
            super(entityType, level);
        }
    }

    public static class EnchantedGoldenAppleCow extends AppleCowEntity {
        public EnchantedGoldenAppleCow(EntityType<? extends Cow> entityType, Level level) {
            super(entityType, level);
        }
    }

    public static class HoneyedAppleCow extends AppleCowEntity {
        public HoneyedAppleCow(EntityType<? extends Cow> entityType, Level level) {
            super(entityType, level);
        }
    }
}
