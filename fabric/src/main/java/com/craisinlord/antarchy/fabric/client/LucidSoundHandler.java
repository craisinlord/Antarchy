package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LucidSoundHandler {
    private static final double START_SCAN_RADIUS = 96.0D;
    private static final int ANIM_ATTACK = 2;
    private static final int ANIM_DEATH = 3;
    private static final Map<UUID, LucidLoopSet> ACTIVE_LOOPS = new HashMap<>();

    private LucidSoundHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null || client.isPaused()) {
                resetAll(client);
                return;
            }

            if (!client.player.isAlive() || client.player.isRemoved() || client.player.isDeadOrDying()
                    || client.screen instanceof DeathScreen) {
                resetAll(client);
                return;
            }

            pruneStaleLoops(client);
            refreshNearbyLucids(client);
        });
    }

    private static void refreshNearbyLucids(Minecraft mc) {
        List<LucidEntity> lucids = mc.level.getEntitiesOfClass(
                LucidEntity.class,
                mc.player.getBoundingBox().inflate(START_SCAN_RADIUS)
        );

        for (LucidEntity lucid : lucids) {
            if (!lucid.shouldPlayAmbientLoop()) continue;

            LucidLoopSet set = ACTIVE_LOOPS.get(lucid.getUUID());
            if (set == null) {
                set = new LucidLoopSet(lucid);
                ACTIVE_LOOPS.put(lucid.getUUID(), set);
                set.start(mc);
            } else {
                set.refresh(mc, lucid);
            }
        }
    }

    private static void pruneStaleLoops(Minecraft mc) {
        Iterator<Map.Entry<UUID, LucidLoopSet>> iterator = ACTIVE_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            LucidLoopSet set = iterator.next().getValue();
            if (!set.isValid()) {
                set.stop(mc);
                iterator.remove();
            } else if (!set.entity.shouldPlayAmbientLoop()) {
                set.stop(mc);
                iterator.remove();
            }
        }
    }

    private static void resetAll(Minecraft mc) {
        for (LucidLoopSet set : ACTIVE_LOOPS.values()) {
            set.stop(mc);
        }
        ACTIVE_LOOPS.clear();
    }

    private static final class LucidLoopSet {
        private LucidEntity entity;
        @Nullable private LucidLoopSound ambient;
        @Nullable private LucidLoopSound flying;
        private int lastAnimationState = -1;

        private LucidLoopSet(LucidEntity entity) {
            this.entity = entity;
        }

        private boolean isValid() {
            return entity != null && entity.isAlive() && !entity.isRemoved();
        }

        private void start(Minecraft mc) {
            playAttackSoundIfNeeded(mc, entity);

            if (ambient == null || !mc.getSoundManager().isActive(ambient)) {
                ambient = new LucidLoopSound(entity, AntarchyFabricContent.LUCID_AMBIENT.get(),
                        SoundSource.HOSTILE, 0.14F, 0.97F, true);
                mc.getSoundManager().play(ambient);
            }

            if (entity.shouldPlayFlyingLoop() && (flying == null || !mc.getSoundManager().isActive(flying))) {
                flying = new LucidLoopSound(entity, AntarchyFabricContent.LUCID_FLYING.get(),
                        SoundSource.HOSTILE, 0.18F, 1.0F, false);
                mc.getSoundManager().play(flying);
            }
        }

        private void refresh(Minecraft mc, LucidEntity entity) {
            this.entity = entity;
            playAttackSoundIfNeeded(mc, entity);

            if (ambient == null || !mc.getSoundManager().isActive(ambient)) {
                ambient = new LucidLoopSound(entity, AntarchyFabricContent.LUCID_AMBIENT.get(),
                        SoundSource.HOSTILE, 0.14F, 0.97F, true);
                mc.getSoundManager().play(ambient);
            } else {
                ambient.setEntity(entity);
            }

            if (entity.shouldPlayFlyingLoop()) {
                if (flying == null || !mc.getSoundManager().isActive(flying)) {
                    flying = new LucidLoopSound(entity, AntarchyFabricContent.LUCID_FLYING.get(),
                            SoundSource.HOSTILE, 0.18F, 1.0F, false);
                    mc.getSoundManager().play(flying);
                } else {
                    flying.setEntity(entity);
                }
            } else if (flying != null) {
                mc.getSoundManager().stop(flying);
                flying = null;
            }
        }

        private void stop(Minecraft mc) {
            if (ambient != null) { mc.getSoundManager().stop(ambient); ambient = null; }
            if (flying != null) { mc.getSoundManager().stop(flying); flying = null; }
            lastAnimationState = -1;
        }

        private void playAttackSoundIfNeeded(Minecraft mc, LucidEntity entity) {
            int currentState = entity.getAnimationState();
            if (currentState == ANIM_ATTACK && lastAnimationState != ANIM_ATTACK) {
                mc.getSoundManager().play(new LucidOneShotSound(entity, AntarchyFabricContent.LUCID_ATTACK.get(),
                        SoundSource.HOSTILE, 1.0F, 1.02F + entity.getRandom().nextFloat() * 0.08F));
            }
            lastAnimationState = currentState;
        }
    }

    private static final class LucidLoopSound extends AbstractTickableSoundInstance {
        private LucidEntity entity;
        private final boolean ambient;

        private LucidLoopSound(LucidEntity entity, SoundEvent soundEvent, SoundSource source,
                               float volume, float pitch, boolean ambient) {
            super(soundEvent, source, RandomSource.create());
            this.entity = entity;
            this.ambient = ambient;
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

        private void setEntity(LucidEntity entity) { this.entity = entity; }

        @Override
        public void tick() {
            if (entity == null || entity.isRemoved() || entity.isDeadOrDying()
                    || entity.getAnimationState() == ANIM_DEATH || !entity.shouldPlayAmbientLoop()) {
                stop();
                return;
            }
            if (!ambient && !entity.shouldPlayFlyingLoop()) { stop(); return; }
            x = entity.getX(); y = entity.getY(); z = entity.getZ();
        }

        @Override
        public boolean canStartSilent() { return true; }
    }

    private static final class LucidOneShotSound extends AbstractTickableSoundInstance {
        private final LucidEntity entity;

        private LucidOneShotSound(LucidEntity entity, SoundEvent soundEvent, SoundSource source,
                                  float volume, float pitch) {
            super(soundEvent, source, RandomSource.create());
            this.entity = entity;
            this.looping = false;
            this.delay = 0;
            this.relative = false;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.volume = volume;
            this.pitch = pitch;
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }

        @Override
        public void tick() {
            if (entity == null || entity.isRemoved() || entity.isDeadOrDying()
                    || entity.getAnimationState() == ANIM_DEATH) {
                stop();
                return;
            }
            x = entity.getX(); y = entity.getY(); z = entity.getZ();
        }
    }
}
