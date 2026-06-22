package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class DreadClientHandler {
    private static final int FOOTSTEP_MIN_INTERVAL_TICKS = 10;
    private static final int FOOTSTEP_MAX_INTERVAL_TICKS = 22;
    private static final int APPARITION_MIN_DURATION_TICKS = 16;
    private static final int APPARITION_MAX_DURATION_TICKS = 34;
    private static final int LUNGE_MIN_DURATION_TICKS = 10;
    private static final int LUNGE_MAX_DURATION_TICKS = 16;

    private static int nextSoundTick = 0;
    private static int nextApparitionTick = 0;
    private static int nextFootstepTick = 0;
    private static final List<ApparitionEntry> ACTIVE_APPARITIONS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private DreadClientHandler() {
    }

    private enum ApparitionType {
        PERIPHERAL_SILHOUETTE,
        CROSSING_SHADOW,
        LUNGE_FLASH
    }

    private static final class ApparitionEntry {
        private final ApparitionType type;
        private final float scale;
        private final int lifetime;
        private final int baseColor;
        private float xNorm;
        private float yNorm;
        private float xVelocity;
        private float yVelocity;
        private int age;

        private ApparitionEntry(ApparitionType type, float xNorm, float yNorm, float xVelocity, float yVelocity, float scale, int lifetime, int baseColor) {
            this.type = type;
            this.xNorm = xNorm;
            this.yNorm = yNorm;
            this.xVelocity = xVelocity;
            this.yVelocity = yVelocity;
            this.scale = scale;
            this.lifetime = lifetime;
            this.baseColor = baseColor;
        }

        private void tick() {
            this.age++;
            this.xNorm += this.xVelocity;
            this.yNorm += this.yVelocity;
        }

        private boolean expired() {
            return this.age >= this.lifetime;
        }

        private float alpha() {
            float progress = this.lifetime <= 0 ? 1.0F : (float) this.age / (float) this.lifetime;
            return Mth.clamp(Mth.sin(progress * (float) Math.PI), 0.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            resetState();
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        Player player = mc.player;
        if (!player.isAlive() || player.isRemoved() || player.deathTime > 0 || player.isDeadOrDying() || mc.screen instanceof DeathScreen) {
            resetState();
            return;
        }

        tickApparitions();

        if (!player.hasEffect(AntarchyNeoforgeMisc.DREAD)) {
            resetState();
            return;
        }

        if (AntarchySettings.dreadHallucinationSoundsEnabled()) {
            nextSoundTick--;
            if (nextSoundTick <= 0) {
                playHallucinationSound(mc, player);
                nextSoundTick = randomIntervalTicks(
                        AntarchySettings.dreadHallucinationSoundMinInterval(),
                        AntarchySettings.dreadHallucinationSoundMaxInterval()
                );
            }
        } else {
            nextSoundTick = 0;
        }

        nextFootstepTick--;
        if (nextFootstepTick <= 0) {
            if (player.onGround() && player.getDeltaMovement().horizontalDistanceSqr() > 0.0025D) {
                playFootstepSound(mc, player);
                nextFootstepTick = randomTickInterval(FOOTSTEP_MIN_INTERVAL_TICKS, FOOTSTEP_MAX_INTERVAL_TICKS);
            } else {
                nextFootstepTick = 4;
            }
        }

        if (AntarchySettings.dreadHallucinationMobsEnabled()) {
            nextApparitionTick--;
            if (nextApparitionTick <= 0) {
                spawnApparition(mc, player);
                nextApparitionTick = randomIntervalTicks(
                        AntarchySettings.dreadHallucinationMobMinInterval(),
                        AntarchySettings.dreadHallucinationMobMaxInterval()
                );
            }
        } else {
            nextApparitionTick = 0;
            ACTIVE_APPARITIONS.clear();
        }
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.hasEffect(AntarchyNeoforgeMisc.DREAD) || ACTIVE_APPARITIONS.isEmpty()) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        for (ApparitionEntry entry : ACTIVE_APPARITIONS) {
            float alpha = entry.alpha();
            if (alpha <= 0.01F) {
                continue;
            }

            switch (entry.type) {
                case PERIPHERAL_SILHOUETTE -> renderPeripheralSilhouette(guiGraphics, width, height, entry, alpha);
                case CROSSING_SHADOW -> renderCrossingShadow(guiGraphics, width, height, entry, alpha);
                case LUNGE_FLASH -> renderLungeFlash(guiGraphics, width, height, entry, alpha);
            }
        }
    }

    private static void tickApparitions() {
        Iterator<ApparitionEntry> iterator = ACTIVE_APPARITIONS.iterator();
        while (iterator.hasNext()) {
            ApparitionEntry entry = iterator.next();
            entry.tick();
            if (entry.expired()) {
                iterator.remove();
            }
        }
    }

    private static void playHallucinationSound(Minecraft mc, Player player) {
        Optional<HolderSet.Named<SoundEvent>> tagOpt =
                BuiltInRegistries.SOUND_EVENT.getTag(AntarchyTags.SoundEvents.DREAD_HALLUCINATION_SOUNDS);
        if (tagOpt.isEmpty()) {
            return;
        }

        List<Holder<SoundEvent>> sounds = tagOpt.get().stream().toList();
        if (sounds.isEmpty()) {
            return;
        }

        SoundEvent soundEvent = sounds.get(RANDOM.nextInt(sounds.size())).value();

        Vec3 look = player.getLookAngle();
        double baseAngle = Math.atan2(-look.x, -look.z);
        double spread = (RANDOM.nextDouble() - 0.5D) * Math.PI;
        double angle = baseAngle + spread;
        double distance = 10.0D + RANDOM.nextDouble() * 15.0D;

        double soundX = player.getX() + Math.sin(angle) * distance;
        double soundY = player.getY() + RANDOM.nextDouble() * 4.0D - 1.0D;
        double soundZ = player.getZ() + Math.cos(angle) * distance;

        mc.level.playLocalSound(
                soundX,
                soundY,
                soundZ,
                soundEvent,
                SoundSource.HOSTILE,
                1.0F,
                0.85F + RANDOM.nextFloat() * 0.3F,
                false
        );
    }

    private static void spawnApparition(Minecraft mc, Player player) {
        double roll = RANDOM.nextDouble();
        if (roll < 0.5D) {
            spawnPeripheralSilhouette();
            return;
        }
        if (roll < 0.82D) {
            spawnCrossingShadow();
            return;
        }
        spawnLungeFlash(mc, player);
    }

    private static void spawnPeripheralSilhouette() {
        boolean leftSide = RANDOM.nextBoolean();
        float xNorm = leftSide ? 0.08F + RANDOM.nextFloat() * 0.08F : 0.84F + RANDOM.nextFloat() * 0.08F;
        float yNorm = 0.48F + (RANDOM.nextFloat() - 0.5F) * 0.26F;
        float scale = 0.85F + RANDOM.nextFloat() * 0.4F;
        int lifetime = randomTickInterval(APPARITION_MIN_DURATION_TICKS, APPARITION_MAX_DURATION_TICKS);
        ACTIVE_APPARITIONS.add(new ApparitionEntry(
                ApparitionType.PERIPHERAL_SILHOUETTE,
                xNorm,
                yNorm,
                leftSide ? -0.0015F : 0.0015F,
                0.0F,
                scale,
                lifetime,
                0x160000
        ));
    }

    private static void spawnCrossingShadow() {
        boolean leftToRight = RANDOM.nextBoolean();
        float xNorm = leftToRight ? -0.18F : 1.18F;
        float yNorm = 0.38F + RANDOM.nextFloat() * 0.36F;
        float velocity = leftToRight ? 0.04F + RANDOM.nextFloat() * 0.015F : -(0.04F + RANDOM.nextFloat() * 0.015F);
        float scale = 0.9F + RANDOM.nextFloat() * 0.45F;
        int lifetime = randomTickInterval(APPARITION_MIN_DURATION_TICKS, APPARITION_MAX_DURATION_TICKS);
        ACTIVE_APPARITIONS.add(new ApparitionEntry(
                ApparitionType.CROSSING_SHADOW,
                xNorm,
                yNorm,
                velocity,
                0.0F,
                scale,
                lifetime,
                0x1A0000
        ));
    }

    private static void spawnLungeFlash(Minecraft mc, Player player) {
        float xNorm = 0.5F + (RANDOM.nextBoolean() ? -1.0F : 1.0F) * (0.12F + RANDOM.nextFloat() * 0.12F);
        float yNorm = 0.58F + (RANDOM.nextFloat() - 0.5F) * 0.12F;
        float scale = 0.95F + RANDOM.nextFloat() * 0.35F;
        int lifetime = randomTickInterval(LUNGE_MIN_DURATION_TICKS, LUNGE_MAX_DURATION_TICKS);
        ACTIVE_APPARITIONS.add(new ApparitionEntry(
                ApparitionType.LUNGE_FLASH,
                xNorm,
                yNorm,
                0.0F,
                -0.002F,
                scale,
                lifetime,
                0x2A0000
        ));
        mc.level.playLocalSound(
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                AntarchyNeoforgeSounds.NIGHTMARE_BITE.get(),
                SoundSource.HOSTILE,
                0.4F,
                0.88F + RANDOM.nextFloat() * 0.12F,
                false
        );
    }

    private static void playFootstepSound(Minecraft mc, Player player) {
        double soundX = player.getX() + (RANDOM.nextDouble() - 0.5D) * 1.2D;
        double soundY = player.getY() - 0.95D;
        double soundZ = player.getZ() + (RANDOM.nextDouble() - 0.5D) * 1.2D;

        mc.level.playLocalSound(
                soundX,
                soundY,
                soundZ,
                SoundEvents.SILVERFISH_STEP,
                SoundSource.HOSTILE,
                0.45F,
                0.85F + RANDOM.nextFloat() * 0.25F,
                false
        );
    }

    private static void renderPeripheralSilhouette(GuiGraphics guiGraphics, int width, int height, ApparitionEntry entry, float alpha) {
        int centerX = Math.round(entry.xNorm * width);
        int baseY = Math.round(entry.yNorm * height);
        int bodyWidth = Math.round(width * 0.045F * entry.scale);
        int bodyHeight = Math.round(height * 0.24F * entry.scale);
        int headSize = Math.round(bodyWidth * 0.78F);
        int x0 = centerX - bodyWidth / 2;
        int y0 = baseY - bodyHeight / 2;

        int outerColor = alphaColor(entry.baseColor, alpha * 0.8F);
        int innerColor = alphaColor(0x5A0000, alpha * 0.22F);

        guiGraphics.fill(x0, y0, x0 + bodyWidth, y0 + bodyHeight, outerColor);
        guiGraphics.fill(x0 - headSize / 2 + bodyWidth / 2, y0 - headSize / 2, x0 + bodyWidth / 2 + headSize / 2, y0 + headSize / 2, outerColor);
        guiGraphics.fill(x0 + bodyWidth / 6, y0 + bodyHeight / 6, x0 + bodyWidth - bodyWidth / 6, y0 + bodyHeight - bodyHeight / 7, innerColor);
    }

    private static void renderCrossingShadow(GuiGraphics guiGraphics, int width, int height, ApparitionEntry entry, float alpha) {
        int centerX = Math.round(entry.xNorm * width);
        int centerY = Math.round(entry.yNorm * height);
        int shadowWidth = Math.round(width * 0.22F * entry.scale);
        int shadowHeight = Math.round(height * 0.035F * entry.scale);

        int outerColor = alphaColor(entry.baseColor, alpha * 0.65F);
        int innerColor = alphaColor(0x340000, alpha * 0.28F);

        guiGraphics.fill(centerX - shadowWidth / 2, centerY - shadowHeight / 2, centerX + shadowWidth / 2, centerY + shadowHeight / 2, outerColor);
        guiGraphics.fill(centerX - shadowWidth / 3, centerY - shadowHeight, centerX + shadowWidth / 3, centerY + shadowHeight, innerColor);
    }

    private static void renderLungeFlash(GuiGraphics guiGraphics, int width, int height, ApparitionEntry entry, float alpha) {
        int centerX = Math.round(entry.xNorm * width);
        int centerY = Math.round(entry.yNorm * height);
        int faceWidth = Math.round(width * 0.11F * entry.scale);
        int faceHeight = Math.round(height * 0.12F * entry.scale);

        int shadowColor = alphaColor(entry.baseColor, alpha * 0.82F);
        int glowColor = alphaColor(0x4A0000, alpha * 0.25F);
        int eyeColor = alphaColor(0xB02020, alpha * 0.8F);

        guiGraphics.fill(centerX - faceWidth / 2, centerY - faceHeight / 2, centerX + faceWidth / 2, centerY + faceHeight / 2, shadowColor);
        guiGraphics.fill(centerX - faceWidth, centerY - faceHeight / 3, centerX + faceWidth, centerY + faceHeight, glowColor);

        int eyeWidth = Math.max(2, faceWidth / 7);
        int eyeHeight = Math.max(2, faceHeight / 12);
        int eyeY = centerY - faceHeight / 8;
        int eyeOffset = faceWidth / 4;
        guiGraphics.fill(centerX - eyeOffset - eyeWidth, eyeY, centerX - eyeOffset + eyeWidth, eyeY + eyeHeight, eyeColor);
        guiGraphics.fill(centerX + eyeOffset - eyeWidth, eyeY, centerX + eyeOffset + eyeWidth, eyeY + eyeHeight, eyeColor);
    }

    private static void resetState() {
        ACTIVE_APPARITIONS.clear();
        nextSoundTick = 0;
        nextApparitionTick = 0;
        nextFootstepTick = 0;
    }

    private static int randomIntervalTicks(double minSeconds, double maxSeconds) {
        double seconds = minSeconds + RANDOM.nextDouble() * Math.max(0.0D, maxSeconds - minSeconds);
        return Math.max(1, (int) (seconds * 20.0D));
    }

    private static int randomTickInterval(int minTicks, int maxTicks) {
        return minTicks + RANDOM.nextInt(Math.max(1, maxTicks - minTicks + 1));
    }

    private static int alphaColor(int rgb, float alpha) {
        int alphaByte = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        return (alphaByte << 24) | rgb;
    }
}
