package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.Comparator;

public final class QueenMusicHandler {
    private static final double START_RADIUS = 256.0D;
    private static final double STOP_RADIUS = 320.0D;
    private static final float MUSIC_VOLUME_SCALE = 0.7F;
    private static QueenMusicSound music;

    private QueenMusicHandler() {
    }

    public static boolean isPlaying() {
        return music != null;
    }

    public static void tick(Minecraft minecraft, SoundEvent soundEvent) {
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()
                || !minecraft.player.isAlive() || minecraft.player.isRemoved()
                || minecraft.player.isDeadOrDying() || minecraft.screen instanceof DeathScreen) {
            reset(minecraft);
            return;
        }

        QueenEntity queen = minecraft.level.getEntitiesOfClass(
                        QueenEntity.class,
                        minecraft.player.getBoundingBox().inflate(STOP_RADIUS),
                        entity -> entity.isQueenMusicActive() || entity.shouldPlayQueenMusic()
                ).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(minecraft.player)))
                .orElse(null);

        if (queen == null) {
            stop(minecraft);
            return;
        }

        if (music != null && !minecraft.getSoundManager().isActive(music)) {
            music = null;
        }

        if (music == null) {
            minecraft.getMusicManager().stopPlaying();
            music = new QueenMusicSound(soundEvent);
            minecraft.getSoundManager().play(music);
        }

        if (music != null) {
            double distance = Math.sqrt(queen.distanceToSqr(minecraft.player));
            float volume = distance <= START_RADIUS
                    ? 1.0F
                    : (float) Mth.clamp(1.0D - (distance - START_RADIUS) / (STOP_RADIUS - START_RADIUS), 0.0D, 1.0D);
            music.setVolume(volume * MUSIC_VOLUME_SCALE);
        }
    }

    private static void stop(Minecraft minecraft) {
        if (music != null) {
            minecraft.getSoundManager().stop(music);
            music = null;
        }
    }

    private static void reset(Minecraft minecraft) {
        stop(minecraft);
    }

    private static final class QueenMusicSound extends AbstractTickableSoundInstance {
        private QueenMusicSound(SoundEvent soundEvent) {
            super(soundEvent, SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 1.0F;
            this.pitch = 1.0F;
        }

        private void setVolume(float volume) {
            this.volume = volume;
        }

        @Override
        public void tick() {
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
