package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeSounds;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class WaspSoundHandler {
    private static final double START_SCAN_RADIUS = 96.0D;
    private static final Map<UUID, WaspLoopSound> ACTIVE_LOOPS = new HashMap<>();

    private WaspSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Ambient buzzing now comes from the wasp entity itself.
    }

    private static void refreshNearbyWasps(Minecraft mc) {
        for (WaspEntity wasp : mc.level.getEntitiesOfClass(WaspEntity.class, mc.player.getBoundingBox().inflate(START_SCAN_RADIUS))) {
            WaspLoopSound loopSound = ACTIVE_LOOPS.get(wasp.getUUID());
            if (loopSound == null || !mc.getSoundManager().isActive(loopSound)) {
                loopSound = new WaspLoopSound(
                        wasp,
                        AntarchyNeoforgeSounds.WASP_IDLE.get(),
                        SoundSource.HOSTILE,
                        0.12F,
                        1.0F
                );
                ACTIVE_LOOPS.put(wasp.getUUID(), loopSound);
                mc.getSoundManager().play(loopSound);
            } else {
                loopSound.setEntity(wasp);
            }
        }
    }

    private static void pruneStaleLoops(Minecraft mc) {
        Iterator<Map.Entry<UUID, WaspLoopSound>> iterator = ACTIVE_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            WaspLoopSound loopSound = iterator.next().getValue();
            if (!loopSound.isValid()) {
                loopSound.stop(mc);
                iterator.remove();
            }
        }
    }

    private static void resetAll(Minecraft mc) {
        for (WaspLoopSound loopSound : ACTIVE_LOOPS.values()) {
            loopSound.stop(mc);
        }
        ACTIVE_LOOPS.clear();
    }

    private static final class WaspLoopSound extends AbstractTickableSoundInstance {
        @Nullable
        private WaspEntity entity;

        private WaspLoopSound(WaspEntity entity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
            super(soundEvent, source, RandomSource.create());
            this.entity = entity;
            this.looping = true;
            this.delay = 0;
            this.relative = false;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.volume = volume;
            this.pitch = pitch;
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }

        private void setEntity(WaspEntity entity) {
            this.entity = entity;
        }

        private boolean isValid() {
            return this.entity != null && this.entity.isAlive() && !this.entity.isRemoved();
        }

        private void stop(Minecraft mc) {
            mc.getSoundManager().stop(this);
        }

        @Override
        public void tick() {
            if (this.entity == null || this.entity.isRemoved() || this.entity.isDeadOrDying() || !this.entity.isAlive()) {
                this.stop();
                return;
            }

            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
