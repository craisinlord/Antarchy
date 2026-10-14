package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public final class UndertrialSounds {
    public static final Entry TRIAL_SPAWNER_SPAWN_MOB = new Entry("block.trial_spawner.spawn_mob", () -> SoundEvents.FIRECHARGE_USE);
    public static final Entry TRIAL_SPAWNER_DETECT_PLAYER = new Entry("block.trial_spawner.detect_player", () -> SoundEvents.RESPAWN_ANCHOR_CHARGE);
    public static final Entry TRIAL_SPAWNER_EJECT_ITEM = new Entry("block.trial_spawner.eject_item", () -> SoundEvents.DISPENSER_DISPENSE);
    public static final Entry TRIAL_SPAWNER_OPEN_SHUTTER = new Entry("block.trial_spawner.open_shutter", () -> SoundEvents.IRON_TRAPDOOR_OPEN);
    public static final Entry TRIAL_SPAWNER_CLOSE_SHUTTER = new Entry("block.trial_spawner.close_shutter", () -> SoundEvents.IRON_TRAPDOOR_CLOSE);
    public static final Entry TRIAL_SPAWNER_OMINOUS_ACTIVATE = new Entry("block.trial_spawner.ominous_activate", () -> SoundEvents.RESPAWN_ANCHOR_SET_SPAWN);
    public static final Entry TRIAL_SPAWNER_SPAWN_ITEM_BEGIN = new Entry("block.trial_spawner.spawn_item_begin", () -> SoundEvents.AMETHYST_BLOCK_CHIME);
    public static final Entry VAULT_AMBIENT = new Entry("block.vault.ambient", () -> null);
    public static final Entry VAULT_DEACTIVATE = new Entry("block.vault.deactivate", () -> SoundEvents.IRON_DOOR_CLOSE);
    public static final Entry VAULT_INSERT_ITEM = new Entry("block.vault.insert_item", () -> SoundEvents.CHAIN_PLACE);
    public static final Entry VAULT_INSERT_ITEM_FAIL = new Entry("block.vault.insert_item_fail", () -> SoundEvents.DISPENSER_FAIL);
    public static final Entry VAULT_REJECT_REWARDED_PLAYER = new Entry("block.vault.reject_rewarded_player", () -> SoundEvents.DISPENSER_FAIL);
    public static final Entry VAULT_OPEN_SHUTTER = new Entry("block.vault.open_shutter", () -> SoundEvents.IRON_TRAPDOOR_OPEN);
    public static final Entry VAULT_CLOSE_SHUTTER = new Entry("block.vault.close_shutter", () -> SoundEvents.IRON_TRAPDOOR_CLOSE);
    public static final Entry VAULT_EJECT_ITEM = new Entry("block.vault.eject_item", () -> SoundEvents.DISPENSER_DISPENSE);

    private UndertrialSounds() {
    }

    public static void playServer(ServerLevel level, BlockPos pos, Entry entry, SoundSource source, float volume, float pitch) {
        SoundEvent sound = entry.get();
        if (sound != null) {
            level.playSound(null, pos, sound, source, volume, pitch);
        }
    }

    public static void playLocal(Level level, BlockPos pos, Entry entry, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        SoundEvent sound = entry.get();
        if (sound != null) {
            level.playLocalSound(pos, sound, source, volume, pitch, distanceDelay);
        }
    }

    public static final class Entry {
        private final ResourceLocation id;
        private final Supplier<SoundEvent> fallback;

        private Entry(String path, Supplier<SoundEvent> fallback) {
            this.id = new ResourceLocation("minecraft", path);
            this.fallback = fallback;
        }

        public SoundEvent get() {
            SoundEvent registered = BuiltInRegistries.SOUND_EVENT.getOptional(this.id).orElse(null);
            return registered != null ? registered : this.fallback.get();
        }
    }
}
