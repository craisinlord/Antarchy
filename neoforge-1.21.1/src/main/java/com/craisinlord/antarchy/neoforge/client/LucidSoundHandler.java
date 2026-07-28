package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class LucidSoundHandler {
    private static final double START_SCAN_RADIUS = 96.0D;
    private static final int ANIM_ATTACK = 2;
    private static final int ANIM_DEATH = 3;
    private static final Map<UUID, LucidLoopSet> ACTIVE_LOOPS = new HashMap<>();

    private LucidSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            resetAll(mc);
            return;
        }

        if (!mc.player.isAlive() || mc.player.isRemoved() || mc.player.isDeadOrDying() || mc.screen instanceof DeathScreen) {
            resetAll(mc);
            return;
        }

        pruneStaleLoops(mc);
        refreshNearbyLucids(mc);
    }

    private static void refreshNearbyLucids(Minecraft mc) {
        List<LucidEntity> lucids = mc.level.getEntitiesOfClass(
                LucidEntity.class,
                mc.player.getBoundingBox().inflate(START_SCAN_RADIUS)
        );

        for (LucidEntity lucid : lucids) {
            if (!lucid.shouldPlayAmbientLoop()) {
                continue;
            }

            LucidLoopSet set = ACTIVE_LOOPS.get(lucid.getUUID());
            if (set == null) {
                set = new LucidLoopSet(lucid);
                ACTIVE_LOOPS.put(lucid.getUUID(), set);
                set.start(mc);
                continue;
            }

            set.refresh(mc, lucid);
        }
    }

    private static void pruneStaleLoops(Minecraft mc) {
        Iterator<Map.Entry<UUID, LucidLoopSet>> iterator = ACTIVE_LOOPS.entrySet().iterator();
        while (iterator.hasNext()) {
            LucidLoopSet set = iterator.next().getValue();
            if (!set.isValid()) {
                set.stop(mc);
                iterator.remove();
                continue;
            }

            if (!set.entity.shouldPlayAmbientLoop()) {
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
            return this.entity != null && this.entity.isAlive() && !this.entity.isRemoved();
        }

        private void start(Minecraft mc) {
            this.playAttackSoundIfNeeded(mc, this.entity);

            if (this.ambient == null || !mc.getSoundManager().isActive(this.ambient)) {
                this.ambient = new LucidLoopSound(
                        this.entity,
                        AntarchyNeoforgeSounds.LUCID_AMBIENT.get(),
                        SoundSource.HOSTILE,
                        0.14F,
                        0.97F,
                        true
                );
                mc.getSoundManager().play(this.ambient);
            }

            if (this.entity.shouldPlayFlyingLoop() && (this.flying == null || !mc.getSoundManager().isActive(this.flying))) {
                this.flying = new LucidLoopSound(
                        this.entity,
                        AntarchyNeoforgeSounds.LUCID_FLYING.get(),
                        SoundSource.HOSTILE,
                        0.18F,
                        1.0F,
                        false
                );
                mc.getSoundManager().play(this.flying);
            }
        }

        private void refresh(Minecraft mc, LucidEntity entity) {
            this.entity = entity;
            this.playAttackSoundIfNeeded(mc, entity);

            if (this.ambient == null || !mc.getSoundManager().isActive(this.ambient)) {
                this.ambient = new LucidLoopSound(
                        entity,
                        AntarchyNeoforgeSounds.LUCID_AMBIENT.get(),
                        SoundSource.HOSTILE,
                        0.14F,
                        0.97F,
                        true
                );
                mc.getSoundManager().play(this.ambient);
            } else {
                this.ambient.setEntity(entity);
            }

            if (entity.shouldPlayFlyingLoop()) {
                if (this.flying == null || !mc.getSoundManager().isActive(this.flying)) {
                    this.flying = new LucidLoopSound(
                            entity,
                            AntarchyNeoforgeSounds.LUCID_FLYING.get(),
                            SoundSource.HOSTILE,
                            0.18F,
                            1.0F,
                            false
                    );
                    mc.getSoundManager().play(this.flying);
                } else {
                    this.flying.setEntity(entity);
                }
            } else if (this.flying != null) {
                mc.getSoundManager().stop(this.flying);
                this.flying = null;
            }
        }

        private void stop(Minecraft mc) {
            if (this.ambient != null) {
                mc.getSoundManager().stop(this.ambient);
                this.ambient = null;
            }
            if (this.flying != null) {
                mc.getSoundManager().stop(this.flying);
                this.flying = null;
            }
            this.lastAnimationState = -1;
        }

        private void playAttackSoundIfNeeded(Minecraft mc, LucidEntity entity) {
            int currentState = entity.getAnimationState();
            if (currentState == ANIM_ATTACK && this.lastAnimationState != ANIM_ATTACK) {
                mc.getSoundManager().play(new LucidOneShotSound(
                        entity,
                        AntarchyNeoforgeSounds.LUCID_ATTACK.get(),
                        SoundSource.HOSTILE,
                        1.0F,
                        1.02F + entity.getRandom().nextFloat() * 0.08F
                ));
            }
            this.lastAnimationState = currentState;
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

        private void setEntity(LucidEntity entity) {
            this.entity = entity;
        }

        @Override
        public void tick() {
            if (this.entity == null
                    || this.entity.isRemoved()
                    || this.entity.isDeadOrDying()
                    || this.entity.getAnimationState() == 3
                    || !this.entity.shouldPlayAmbientLoop()) {
                this.stop();
                return;
            }

            if (!this.ambient && !this.entity.shouldPlayFlyingLoop()) {
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

    private static final class LucidOneShotSound extends AbstractTickableSoundInstance {
        private LucidEntity entity;

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
            if (this.entity == null
                    || this.entity.isRemoved()
                    || this.entity.isDeadOrDying()
                    || this.entity.getAnimationState() == ANIM_DEATH) {
                this.stop();
                return;
            }

            this.x = this.entity.getX();
            this.y = this.entity.getY();
            this.z = this.entity.getZ();
        }
    }
}
