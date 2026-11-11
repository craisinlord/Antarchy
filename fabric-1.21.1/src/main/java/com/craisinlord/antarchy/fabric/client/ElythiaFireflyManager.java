package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.ElythiaFireflies;
import com.craisinlord.antarchy.content.client.ElythiaGroundMist;
import com.craisinlord.antarchy.content.client.FungalReserveAtmosphere;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricMisc;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class ElythiaFireflyManager {
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final int SOUND_INTERVAL_MIN = 250;
    private static final int SOUND_INTERVAL_MAX = 450;

    private static int nextSoundTick = SOUND_INTERVAL_MIN;

    private ElythiaFireflyManager() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientLevel level = client.level;
            if (level == null || client.player == null) {
                return;
            }
            if (client.isPaused()) {
                return;
            }
            if (!ElythiaSkyRenderer.shouldRender(level)) {
                return;
            }

            ElythiaGroundMist.tick(client, level, AntarchyFabricMisc.GROUND_MIST.get());
            FungalReserveAtmosphere.tick(level, client.player);

            long dayTime = level.getDayTime() % 24000L;
            if (dayTime < NIGHT_START || dayTime > NIGHT_END) {
                return;
            }

            boolean fireflyBiome = level.getBiome(client.player.blockPosition()).is(AntarchyTags.Biomes.ELYTHIA_FIREFLY_PARTICLE_BIOMES);
            if (AntarchySettings.elythiaFireflyParticlesEnabled() && fireflyBiome) {
                ElythiaFireflies.spawn(level, client.player, AntarchyFabricMisc.FIREFLY.get());
            }

            nextSoundTick--;
            if (nextSoundTick <= 0) {
                nextSoundTick = SOUND_INTERVAL_MIN + level.random.nextInt(SOUND_INTERVAL_MAX - SOUND_INTERVAL_MIN + 1);
                Player player = client.player;
                level.playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        AntarchySoundEvents.ELYTHIA_FIREFLY_AMBIENT.get(),
                        SoundSource.AMBIENT,
                        0.6F + level.random.nextFloat() * 0.3F,
                        0.9F + level.random.nextFloat() * 0.2F,
                        false
                );
            }
        });
    }
}
