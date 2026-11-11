package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.ElythiaFireflies;
import com.craisinlord.antarchy.content.client.ElythiaGroundMist;
import com.craisinlord.antarchy.content.client.FungalReserveAtmosphere;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class ElythiaFireflyManager {
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final int SOUND_INTERVAL_MIN = 250;
    private static final int SOUND_INTERVAL_MAX = 450;

    private static int nextSoundTick = SOUND_INTERVAL_MIN;

    private ElythiaFireflyManager() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        if (!ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }

        ElythiaGroundMist.tick(mc, level, AntarchyNeoforgeMisc.GROUND_MIST.get());
        FungalReserveAtmosphere.tick(level, mc.player);

        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < NIGHT_START || dayTime > NIGHT_END) {
            return;
        }

        boolean fireflyBiome = level.getBiome(mc.player.blockPosition()).is(AntarchyTags.Biomes.ELYTHIA_FIREFLY_PARTICLE_BIOMES);
        if (AntarchySettings.elythiaFireflyParticlesEnabled() && fireflyBiome) {
            ElythiaFireflies.spawn(level, mc.player, AntarchyNeoforgeMisc.FIREFLY.get());
        }

        nextSoundTick--;
        if (nextSoundTick <= 0) {
            nextSoundTick = SOUND_INTERVAL_MIN + level.random.nextInt(SOUND_INTERVAL_MAX - SOUND_INTERVAL_MIN + 1);
            Player player = mc.player;
            level.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    AntarchySoundEvents.ELYTHIA_FIREFLY_AMBIENT.get(),
                    SoundSource.AMBIENT,
                    0.6F + level.random.nextFloat() * 0.3F,
                    0.9F + level.random.nextFloat() * 0.2F,
                    false
            );
        }
    }
}
