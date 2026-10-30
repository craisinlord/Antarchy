package com.craisinlord.antarchy.fabric.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class ElythiaFireflyManager {
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final int SPAWN_INTERVAL = 3;
    private static final int SPAWN_RADIUS_H = 20;
    private static final int SPAWN_Y_MIN = -2;
    private static final int SPAWN_Y_MAX = 10;
    private static final int SOUND_INTERVAL_MIN = 250;
    private static final int SOUND_INTERVAL_MAX = 450;

    private static int nextSoundTick = SOUND_INTERVAL_MIN;

    private ElythiaFireflyManager() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientLevel level = client.level;
            if (level == null || client.player == null) return;
            if (client.isPaused()) return;
            if (!ElythiaSkyRenderer.shouldRender(level)) return;
            com.craisinlord.antarchy.content.client.ElythiaGroundMist.tick(Minecraft.getInstance(), level, AntarchyFabricMisc.GROUND_MIST.get());

            long dayTime = level.getOverworldClockTime() % 24000L;
            if (dayTime < NIGHT_START || dayTime > NIGHT_END) return;

            long gameTime = level.getGameTime();
            boolean fireflyBiome = level.getBiome(client.player.blockPosition()).is(AntarchyTags.Biomes.ELYTHIA_FIREFLY_PARTICLE_BIOMES);

            if (AntarchySettings.elythiaFireflyParticlesEnabled() && fireflyBiome) {
                if (gameTime % SPAWN_INTERVAL == 0) {
                    Player player = client.player;
                    int px = (int) player.getX();
                    int py = (int) player.getY();
                    int pz = (int) player.getZ();

                    int attempts = 1 + level.getRandom().nextInt(3);
                    for (int i = 0; i < attempts; i++) {
                        int ox = level.getRandom().nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;
                        int oy = SPAWN_Y_MIN + level.getRandom().nextInt(SPAWN_Y_MAX - SPAWN_Y_MIN + 1);
                        int oz = level.getRandom().nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;

                        BlockPos candidate = new BlockPos(px + ox, py + oy, pz + oz);
                        if (!level.getBlockState(candidate).isAir()) continue;

                        level.addParticle(
                                AntarchyFabricMisc.FIREFLY.get(),
                                candidate.getX() + level.getRandom().nextDouble(),
                                candidate.getY() + level.getRandom().nextDouble(),
                                candidate.getZ() + level.getRandom().nextDouble(),
                                0.0, 0.0, 0.0
                        );
                    }
                }
            }

            nextSoundTick--;
            if (nextSoundTick <= 0) {
                nextSoundTick = SOUND_INTERVAL_MIN + level.getRandom().nextInt(SOUND_INTERVAL_MAX - SOUND_INTERVAL_MIN + 1);
                Player player = client.player;
                level.playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        AntarchySoundEvents.ELYTHIA_FIREFLY_AMBIENT.get(),
                        SoundSource.AMBIENT,
                        0.6f + level.getRandom().nextFloat() * 0.3f,
                        0.9f + level.getRandom().nextFloat() * 0.2f,
                        false
                );
            }
        });
    }
}
