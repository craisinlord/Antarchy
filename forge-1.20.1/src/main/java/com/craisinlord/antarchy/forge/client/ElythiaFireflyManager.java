package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.forge.registry.AntarchyForgeMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (mc.isPaused()) return;
        if (!ElythiaSkyRenderer.shouldRender(level)) return;

        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < NIGHT_START || dayTime > NIGHT_END) return;

        long gameTime = level.getGameTime();
        boolean fireflyBiome = level.getBiome(mc.player.blockPosition()).is(AntarchyTags.Biomes.ELYTHIA_FIREFLY_PARTICLE_BIOMES);

        if (AntarchySettings.elythiaFireflyParticlesEnabled() && fireflyBiome) {
            if (gameTime % SPAWN_INTERVAL == 0) {
                Player player = mc.player;
                int px = (int) player.getX();
                int py = (int) player.getY();
                int pz = (int) player.getZ();

                int attempts = 1 + level.random.nextInt(3);
                for (int i = 0; i < attempts; i++) {
                    int ox = level.random.nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;
                    int oy = SPAWN_Y_MIN + level.random.nextInt(SPAWN_Y_MAX - SPAWN_Y_MIN + 1);
                    int oz = level.random.nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;

                    BlockPos candidate = new BlockPos(px + ox, py + oy, pz + oz);
                    if (!level.getBlockState(candidate).isAir()) continue;

                    level.addParticle(
                            AntarchyForgeMisc.FIREFLY.get(),
                            candidate.getX() + level.random.nextDouble(),
                            candidate.getY() + level.random.nextDouble(),
                            candidate.getZ() + level.random.nextDouble(),
                            0.0, 0.0, 0.0
                    );
                }
            }
        }

        nextSoundTick--;
        if (nextSoundTick <= 0) {
            nextSoundTick = SOUND_INTERVAL_MIN + level.random.nextInt(SOUND_INTERVAL_MAX - SOUND_INTERVAL_MIN + 1);
            Player player = mc.player;
            level.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    AntarchySoundEvents.ELYTHIA_FIREFLY_AMBIENT.get(),
                    SoundSource.AMBIENT,
                    0.6f + level.random.nextFloat() * 0.3f,
                    0.9f + level.random.nextFloat() * 0.2f,
                    false
            );
        }
    }
}
