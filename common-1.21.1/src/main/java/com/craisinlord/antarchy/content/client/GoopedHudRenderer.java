package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public final class GoopedHudRenderer {
    private GoopedHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (player == null || player.isDeadOrDying() || gooped == null) {
            return;
        }

        MobEffectInstance effect = player.getEffect(gooped);
        if (effect == null) {
            return;
        }

        float durationFade = effect.isInfiniteDuration()
                ? 1.0F
                : Mth.clamp(effect.getDuration() / 40.0F, 0.25F, 1.0F);
        float strength = Mth.clamp((effect.getAmplifier() + 1) / 3.0F, 0.0F, 1.0F);
        int alpha = Mth.clamp((int) ((0x38 + 0x20 * strength) * durationFade), 0, 0x70);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, (alpha << 24) | 0x16B846);
    }
}
