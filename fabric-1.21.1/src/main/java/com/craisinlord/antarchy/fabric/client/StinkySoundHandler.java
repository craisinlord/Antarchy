package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

public final class StinkySoundHandler {
    private static final int FIRST_SOUND_DELAY = 20;
    private static final int MIN_INTERVAL = 60;
    private static final int MAX_INTERVAL = 120;

    private static int nextSoundTick = FIRST_SOUND_DELAY;

    private StinkySoundHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null || client.isPaused()) {
                nextSoundTick = FIRST_SOUND_DELAY;
                return;
            }

            if (!client.player.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
                nextSoundTick = FIRST_SOUND_DELAY;
                return;
            }

            nextSoundTick--;
            if (nextSoundTick > 0) {
                return;
            }

            client.level.playLocalSound(
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ(),
                    AntarchySoundEvents.STINKY_FLY.get(),
                    SoundSource.AMBIENT,
                    0.14F + client.level.random.nextFloat() * 0.06F,
                    0.92F + client.level.random.nextFloat() * 0.16F,
                    false
            );
            nextSoundTick = MIN_INTERVAL + client.level.random.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
        });
    }
}
