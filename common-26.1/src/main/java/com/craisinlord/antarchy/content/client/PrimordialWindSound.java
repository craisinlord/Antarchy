package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class PrimordialWindSound {
    private static WindLoop active;

    private PrimordialWindSound() {
    }

    public static void reset(Minecraft minecraft) {
        if (active != null) {
            minecraft.getSoundManager().stop(active);
            active.stopNow();
            active = null;
        }
    }

    public static void tick(Minecraft minecraft, boolean nearGrass) {
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null
                || !AntarchySettings.elythiaGrassWindEnabled() || !AntarchySettings.elythiaWindSoundsEnabled()) {
            if (active != null) active.target = 0.0F;
            return;
        }
        boolean exposed = nearGrass && level.canSeeSky(minecraft.player.blockPosition());
        double gust = PrimordialGrassWind.sample(level.getGameTime(), minecraft.player.getX(), minecraft.player.getZ()).gust();
        float target = exposed ? PrimordialGrassWind.gustLoudness(gust) : 0.0F;
        float targetPitch = 0.9F + 0.2F * (float) gust;
        if (active != null && active.isStopped()) active = null;
        if (active == null && target > 0.0F) {
            active = new WindLoop(level);
            active.target = target;
            active.targetPitch = targetPitch;
            minecraft.getSoundManager().play(active);
        } else if (active != null) {
            active.target = target;
            active.targetPitch = targetPitch;
        }
    }

    private static final class WindLoop extends AbstractTickableSoundInstance {
        private final ClientLevel level;
        private float target;
        private float targetPitch = 0.95F;
        private int silentTicks;

        private WindLoop(ClientLevel level) {
            super(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("antarchy", "ambient.elythia.grass_wind")), SoundSource.AMBIENT, RandomSource.create());
            this.level = level;
            looping = true;
            relative = true;
            attenuation = SoundInstance.Attenuation.NONE;
            volume = 0.0F;
            pitch = 0.95F;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != level || minecraft.player == null) {
                stop();
                return;
            }
            volume += (target - volume) * (target > volume ? 0.12F : 0.06F);
            pitch += (targetPitch - pitch) * 0.05F;
            if (target == 0.0F && volume < 0.001F) {
                if (++silentTicks > 40) stop();
            } else silentTicks = 0;
        }

        private void stopNow() {
            stop();
        }
    }
}
