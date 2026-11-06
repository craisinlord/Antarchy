package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class TearIdleSoundHandler {
    private static final double START_SCAN_RADIUS = 64.0D;
    private static final int DISCOVERY_SCAN_INTERVAL_TICKS = 10;
    private static final Map<UUID, TearIdleSound> ACTIVE_LOOPS = new HashMap<>();
    private static long nextDiscoveryScanTick;

    private TearIdleSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            resetAll(mc);
            return;
        }

        pruneStaleLoops(mc);

        long gameTime = mc.level.getGameTime();
        if (gameTime >= nextDiscoveryScanTick) {
            nextDiscoveryScanTick = gameTime + DISCOVERY_SCAN_INTERVAL_TICKS;
            discoverNearbyTears(mc);
        }
    }

    private static void discoverNearbyTears(Minecraft mc) {
        List<DimensionalTearEntity> tears = mc.level.getEntitiesOfClass(
                DimensionalTearEntity.class,
                mc.player.getBoundingBox().inflate(START_SCAN_RADIUS)
        );

        for (DimensionalTearEntity tear : tears) {
            if (!tear.shouldPlayIdleSound() || ACTIVE_LOOPS.containsKey(tear.getUUID())) {
                continue;
            }
            TearIdleSound sound = new TearIdleSound(tear);
            ACTIVE_LOOPS.put(tear.getUUID(), sound);
            mc.getSoundManager().play(sound);
        }
    }

    private static void pruneStaleLoops(Minecraft mc) {
        Iterator<Map.Entry<UUID, TearIdleSound>> iterator = ACTIVE_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            TearIdleSound sound = iterator.next().getValue();
            if (!sound.isValid()) {
                mc.getSoundManager().stop(sound);
                iterator.remove();
            }
        }
    }

    private static void resetAll(Minecraft mc) {
        for (TearIdleSound sound : ACTIVE_LOOPS.values()) {
            mc.getSoundManager().stop(sound);
        }
        ACTIVE_LOOPS.clear();
        nextDiscoveryScanTick = 0L;
    }

    private static final class TearIdleSound extends AbstractTickableSoundInstance {
        private final DimensionalTearEntity tear;

        private TearIdleSound(DimensionalTearEntity tear) {
            super(AntarchySoundEvents.DIMENSIONAL_TEAR_IDLE.get(), SoundSource.HOSTILE, tear.getRandom());
            this.tear = tear;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.6F;
            this.pitch = 1.0F;
            this.x = tear.getX();
            this.y = tear.getY();
            this.z = tear.getZ();
        }

        private boolean isValid() {
            return !this.tear.isRemoved() && this.tear.shouldPlayIdleSound();
        }

        @Override
        public void tick() {
            if (!this.isValid()) {
                this.stop();
                return;
            }
            this.x = this.tear.getX();
            this.y = this.tear.getY();
            this.z = this.tear.getZ();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
