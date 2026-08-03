package com.craisinlord.antarchy.content.client.hud;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;

public final class CustomBossBarRenderer {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int GLOBAL_Y_OFFSET = 15;
    private static final Map<UUID, Textures> TEXTURES_BY_BOSS_ID = new HashMap<>();

    private static final Map<Class<? extends Entity>, Textures> BY_ENTITY_CLASS = Map.of(
            KrakenEntity.class, textures("blue", "kraken", 204, 56, 4),
            ToreterrorEntity.class, textures("green", "toreterror", 194, 58, 2),
            AlphaMantisEntity.class, textures("green", "alpha_mantis", 205, 49, 6),
            BrutalflyEntity.class, textures("brutalfly", "brutalfly", 198, 38, 3),
            HerculesBeetleEntity.class, textures("beetle", "hercules_beetle", 194, 33, 1),
            EmperorScorpionEntity.class, textures("purple", "scorpion", 218, 34, -2)
    );
    private static final Map<String, Textures> BY_LOCALIZED_NAME_AND_COLOR = Map.of(
            key(Component.translatable("entity.antarchy.kraken").getString(), BossEvent.BossBarColor.BLUE), BY_ENTITY_CLASS.get(KrakenEntity.class),
            key(Component.translatable("entity.antarchy.toreterror").getString(), BossEvent.BossBarColor.GREEN), BY_ENTITY_CLASS.get(ToreterrorEntity.class),
            key(Component.translatable("entity.antarchy.alpha_mantis").getString(), BossEvent.BossBarColor.GREEN), BY_ENTITY_CLASS.get(AlphaMantisEntity.class),
            key(Component.translatable("entity.antarchy.brutalfly").getString(), BossEvent.BossBarColor.YELLOW), BY_ENTITY_CLASS.get(BrutalflyEntity.class),
            key(Component.translatable("entity.antarchy.hercules_beetle").getString(), BossEvent.BossBarColor.RED), BY_ENTITY_CLASS.get(HerculesBeetleEntity.class),
            key(Component.translatable("entity.antarchy.emperor_scorpion").getString(), BossEvent.BossBarColor.PURPLE), BY_ENTITY_CLASS.get(EmperorScorpionEntity.class)
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
        Textures cached = TEXTURES_BY_BOSS_ID.get(bossEvent.getId());
        if (cached != null) {
            return cached;
        }

        Entity entity = findEntity(bossEvent.getId());
        if (entity != null) {
            Textures textures = BY_ENTITY_CLASS.get(entity.getClass());
            if (textures != null) {
                TEXTURES_BY_BOSS_ID.put(bossEvent.getId(), textures);
                return textures;
            }
        }

        Textures fallback = BY_LOCALIZED_NAME_AND_COLOR.get(key(bossEvent.getName().getString(), bossEvent.getColor()));
        if (fallback != null) {
            TEXTURES_BY_BOSS_ID.put(bossEvent.getId(), fallback);
        }
        return fallback;
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
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/boss_bars/healthbars/" + barKey + "_background.png"),
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/boss_bars/healthbars/" + barKey + "_progress.png"),
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/boss_bars/" + overlayKey + ".png"),
                overlayWidth,
                overlayHeight,
                barYOffset
        );
    }

    private static String key(String name, BossEvent.BossBarColor color) {
        return color.name() + "|" + name;
    }

    private record Textures(ResourceLocation background, ResourceLocation progress, ResourceLocation overlay, int overlayWidth, int overlayHeight, int barYOffset) {
    }
}
