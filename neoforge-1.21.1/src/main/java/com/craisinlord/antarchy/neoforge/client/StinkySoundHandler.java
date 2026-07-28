package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class StinkySoundHandler {
    private static final int FIRST_SOUND_DELAY = 20;
    private static final int MIN_INTERVAL = 60;
    private static final int MAX_INTERVAL = 120;

    private static int nextSoundTick = FIRST_SOUND_DELAY;

    private StinkySoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            nextSoundTick = FIRST_SOUND_DELAY;
            return;
        }

        if (!mc.player.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
            nextSoundTick = FIRST_SOUND_DELAY;
            return;
        }

        nextSoundTick--;
        if (nextSoundTick > 0) {
            return;
        }

        mc.level.playLocalSound(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                AntarchySoundEvents.STINKY_FLY.get(),
                SoundSource.AMBIENT,
                0.14F + mc.level.random.nextFloat() * 0.06F,
                0.92F + mc.level.random.nextFloat() * 0.16F,
                false
        );
        nextSoundTick = MIN_INTERVAL + mc.level.random.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
    }
}
