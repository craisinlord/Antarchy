package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class SizeRaySoundHandler {
    private static SizeRayItem activeItem;
    private static SizeRayUseSound chargeSound;
    private static SizeRayUseSound loopSound;

    private SizeRaySoundHandler() {
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null || !AntarchySettings.sizeChangingRaysEnabled()) {
            stopActive(minecraft);
            return;
        }

        if (!(player.getUseItem().getItem() instanceof SizeRayItem sizeRayItem)) {
            stopActive(minecraft);
            return;
        }

        if (activeItem != sizeRayItem) {
            stopActive(minecraft);
            activeItem = sizeRayItem;
        }

        int useTicks = sizeRayItem.getUseDuration(player.getUseItem()) - player.getUseItemRemainingTicks();
        if (useTicks >= SizeRayItem.LOOP_SOUND_START_TICKS) {
            stopCharge(minecraft);
            if (loopSound == null || !minecraft.getSoundManager().isActive(loopSound)) {
                loopSound = new SizeRayUseSound(player, sizeRayItem.getLoopSound(), true);
                minecraft.getSoundManager().play(loopSound);
            }
            return;
        }

        stopLoop(minecraft);
        if (chargeSound == null) {
            chargeSound = new SizeRayUseSound(player, sizeRayItem.getChargeSound(), false);
            minecraft.getSoundManager().play(chargeSound);
        }
    }

    private static void stopActive(Minecraft minecraft) {
        stopCharge(minecraft);
        stopLoop(minecraft);
        activeItem = null;
    }

    private static void stopCharge(Minecraft minecraft) {
        if (chargeSound != null) {
            chargeSound.stopNow(minecraft);
            chargeSound = null;
        }
    }

    private static void stopLoop(Minecraft minecraft) {
        if (loopSound != null) {
            loopSound.stopNow(minecraft);
            loopSound = null;
        }
    }

    private static final class SizeRayUseSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        private SizeRayUseSound(LocalPlayer player, SoundEvent soundEvent, boolean loop) {
            super(soundEvent, SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            this.looping = loop;
            this.delay = 0;
            this.relative = false;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        private void stopNow(Minecraft minecraft) {
            minecraft.getSoundManager().stop(this);
            this.stop();
        }

        @Override
        public void tick() {
            if (this.player.isRemoved()
                    || !this.player.isAlive()
                    || !(this.player.getUseItem().getItem() instanceof SizeRayItem)) {
                this.stop();
                return;
            }

            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
