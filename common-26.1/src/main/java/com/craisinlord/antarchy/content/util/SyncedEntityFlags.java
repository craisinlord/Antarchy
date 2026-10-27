package com.craisinlord.antarchy.content.util;

import net.minecraft.world.entity.Entity;

public final class SyncedEntityFlags {
    public enum Flag {
        JUDGMENT_MARKED,
        ON_ANTIMETAL_RAIL
    }

    public interface Backend {
        boolean get(Entity entity, Flag flag);

        void set(Entity entity, Flag flag, boolean value);
    }

    private static Backend backend = new Backend() {
        @Override
        public boolean get(Entity entity, Flag flag) {
            throw new IllegalStateException("Antarchy synced entity flags were read before the loader bound a backend");
        }

        @Override
        public void set(Entity entity, Flag flag, boolean value) {
            throw new IllegalStateException("Antarchy synced entity flags were written before the loader bound a backend");
        }
    };

    private SyncedEntityFlags() {
    }

    public static void bind(Backend implementation) {
        backend = implementation;
    }

    public static boolean get(Entity entity, Flag flag) {
        return backend.get(entity, flag);
    }

    public static void set(Entity entity, Flag flag, boolean value) {
        if (backend.get(entity, flag) != value) {
            backend.set(entity, flag, value);
        }
    }
}
