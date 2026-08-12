package com.craisinlord.antarchy.content.client.hud;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;

public final class CustomBossBarRenderer {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int GLOBAL_Y_OFFSET = 15;

    private static final Map<Class<? extends Entity>, Textures> BY_ENTITY_CLASS = Map.of(
            KrakenEntity.class, textures("blue", "kraken", 204, 56, 4),
            ToreterrorEntity.class, textures("green", "toreterror", 194, 58, 2),
            AlphaMantisEntity.class, textures("green", "alpha_mantis", 205, 49, 6),
            BrutalflyEntity.class, textures("brutalfly", "brutalfly", 198, 38, 3),
            HerculesBeetleEntity.class, textures("beetle", "hercules_beetle", 194, 33, 1),
            EmperorScorpionEntity.class, textures("purple", "scorpion", 218, 34, -2)
    );

    private CustomBossBarRenderer() {
    }

    public static boolean render(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent) {
        Textures textures = lookup(bossEvent);
        if (textures == null) {
            return false;
        }

        int baseY = y + GLOBAL_Y_OFFSET;
        int barY = baseY + textures.barYOffset;

        guiGraphics.blit(textures.background, x, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        int filledWidth = Math.round(BAR_WIDTH * Math.max(0.0F, Math.min(1.0F, bossEvent.getProgress())));
        if (filledWidth > 0) {
            guiGraphics.blit(textures.progress, x, barY, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        int overlayX = x + (BAR_WIDTH - textures.overlayWidth) / 2;
        int overlayY = baseY + (BAR_HEIGHT - textures.overlayHeight) / 2;
        guiGraphics.blit(textures.overlay, overlayX, overlayY, 0, 0, textures.overlayWidth, textures.overlayHeight, textures.overlayWidth, textures.overlayHeight);

        return true;
    }

    public static boolean isCustomBoss(BossEvent bossEvent) {
        return lookup(bossEvent) != null;
    }

    private static Textures lookup(BossEvent bossEvent) {
        Entity entity = findEntity(bossEvent.getId());
        if (entity == null) {
            return null;
        }
        return BY_ENTITY_CLASS.get(entity.getClass());
    }

    private static Entity findEntity(UUID id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity.getUUID().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    private static Textures textures(String barKey, String overlayKey, int overlayWidth, int overlayHeight, int barYOffset) {
        return new Textures(
                new ResourceLocation(Antarchy.MODID, "textures/gui/boss_bars/healthbars/" + barKey + "_background.png"),
                new ResourceLocation(Antarchy.MODID, "textures/gui/boss_bars/healthbars/" + barKey + "_progress.png"),
                new ResourceLocation(Antarchy.MODID, "textures/gui/boss_bars/" + overlayKey + ".png"),
                overlayWidth,
                overlayHeight,
                barYOffset
        );
    }

    private record Textures(ResourceLocation background, ResourceLocation progress, ResourceLocation overlay, int overlayWidth, int overlayHeight, int barYOffset) {
    }
}
