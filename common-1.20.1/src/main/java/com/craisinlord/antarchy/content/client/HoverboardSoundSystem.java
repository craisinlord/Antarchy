package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.craisinlord.antarchy.mixins.LivingEntityJumpingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class HoverboardSoundSystem {
    private static final float MIN_PITCH = 0.75F;
    private static final float MAX_PITCH = 1.6F;
    private static final float MIN_VOLUME = 0.45F;
    private static final float MAX_VOLUME = 0.8F;

    private static final Map<Integer, IdleLoopSound> ACTIVE = new HashMap<>();

    private HoverboardSoundSystem() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stopAll(mc);
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        Set<Integer> activeIds = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof HoverboardEntity hoverboard) || !hoverboard.isVehicle()) {
                continue;
            }

            int id = hoverboard.getId();
            activeIds.add(id);
            IdleLoopSound sound = ACTIVE.get(id);
            if (sound == null || !mc.getSoundManager().isActive(sound)) {
                sound = new IdleLoopSound(hoverboard);
                ACTIVE.put(id, sound);
                mc.getSoundManager().play(sound);
            }
        }

        ACTIVE.entrySet().removeIf(entry -> {
            if (activeIds.contains(entry.getKey())) {
                return false;
            }
            mc.getSoundManager().stop(entry.getValue());
            return true;
        });
    }

    private static void stopAll(Minecraft mc) {
        ACTIVE.values().forEach(mc.getSoundManager()::stop);
        ACTIVE.clear();
    }

    private static final class IdleLoopSound extends AbstractTickableSoundInstance {
        private final HoverboardEntity hoverboard;
        private double lastX;
        private double lastZ;

        private IdleLoopSound(HoverboardEntity hoverboard) {
            super(AntarchySoundEvents.HOVERBOARD_IDLE.get(), SoundSource.NEUTRAL, RandomSource.create());
            this.hoverboard = hoverboard;
            this.looping = true;
            this.delay = 0;
            this.relative = false;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.volume = MIN_VOLUME;
            this.pitch = MIN_PITCH;
            this.x = hoverboard.getX();
            this.y = hoverboard.getY();
            this.z = hoverboard.getZ();
            this.lastX = this.x;
            this.lastZ = this.z;
        }

        @Override
        public void tick() {
            if (this.hoverboard.isRemoved() || !this.hoverboard.isVehicle()) {
                this.stop();
                return;
            }

            double dx = this.hoverboard.getX() - this.lastX;
            double dz = this.hoverboard.getZ() - this.lastZ;
            double speed = Math.sqrt(dx * dx + dz * dz);
            this.lastX = this.hoverboard.getX();
            this.lastZ = this.hoverboard.getZ();

            this.x = this.hoverboard.getX();
            this.y = this.hoverboard.getY();
            this.z = this.hoverboard.getZ();

            double maxSpeed = Math.max(AntarchySettings.hoverboardMaxSpeed(), 0.0001D);
            float speedRatio = (float) Mth.clamp(speed / maxSpeed, 0.0D, 1.0D);
            boolean jumping = false;
            Entity passenger = this.hoverboard.getControllingPassenger();
            if (passenger instanceof net.minecraft.world.entity.player.Player player) {
                jumping = ((LivingEntityJumpingAccessor) player).antarchy$isJumping();
            }
            boolean movingUpward = this.hoverboard.getDeltaMovement().y > 0.02D;
            float liftRatio = jumping || movingUpward ? 1.0F : 0.0F;
            this.volume = Mth.lerp(speedRatio, MIN_VOLUME, MAX_VOLUME);
            this.pitch = Mth.lerp(speedRatio, MIN_PITCH, MAX_PITCH)
                    + Mth.lerp(liftRatio, 0.0F, 0.2F);
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
