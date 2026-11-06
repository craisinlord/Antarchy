package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ElythiaEchoes {
    private static final int SAMPLE_INTERVAL = 4;
    private static final int MAX_SAMPLES = 1800;
    private static final int MIN_LAG_SAMPLES = 600;
    private static final int MAX_LAG_SAMPLES = 1200;
    private static final int REPLAY_SAMPLES = 75;
    private static final int FIRST_COOLDOWN_TICKS = 2400;
    private static final int MIN_COOLDOWN_TICKS = 3600;
    private static final int MAX_COOLDOWN_TICKS = 7200;
    private static final int RETRY_TICKS = 200;
    private static final int PICK_STRIDE = 5;
    private static final double MIN_DISTANCE = 10.0D;
    private static final double MAX_DISTANCE = 64.0D;
    private static final double MAX_HEIGHT_DIFFERENCE = 24.0D;
    private static final double VANISH_DISTANCE = 8.0D;
    private static final float FADE_TICKS = 30.0F;
    private static final float VANISH_TICKS = 10.0F;
    private static final float BASE_ALPHA = 0.3F;
    private static final int TINT = 0xE6DCFF;
    private static final float PLAYER_SCALE = 0.9375F;

    private static final int REASONS = 4;
    private static final int REASON_CHECKED = 0;
    private static final int REASON_DISTANCE = 1;
    private static final int REASON_STILL = 2;
    private static final int REASON_INVALID = 3;

    private static final List<Sample> SAMPLES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static ClientLevel trackedLevel;
    private static int ticks;
    private static int cooldown = FIRST_COOLDOWN_TICKS;
    private static Replay replay;
    private static PlayerModel<AbstractClientPlayer> wideModel;
    private static PlayerModel<AbstractClientPlayer> slimModel;

    private ElythiaEchoes() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.isPaused()) {
            return;
        }
        if (level != trackedLevel) {
            reset();
            trackedLevel = level;
        }
        if (!AntarchySettings.elythiaEchoes() || !level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            replay = null;
            return;
        }
        ticks++;
        if (ticks % SAMPLE_INTERVAL == 0) {
            record(player);
        }
        if (replay != null) {
            tickReplay(player);
        } else if (--cooldown <= 0) {
            replay = pick(player, new int[REASONS]);
            cooldown = replay == null ? RETRY_TICKS : MIN_COOLDOWN_TICKS + RANDOM.nextInt(MAX_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS);
        }
    }

    public static void handleTest(boolean immediate) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal(test(immediate)), false);
        }
    }

    public static String test(boolean immediate) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return "Echoes only appear in Elythia.";
        }
        if (!AntarchySettings.elythiaEchoes()) {
            return "Echoes are turned off in the config.";
        }
        float seconds = SAMPLES.size() * SAMPLE_INTERVAL / 20.0F;
        if (!immediate) {
            int[] reasons = new int[REASONS];
            replay = pick(player, reasons);
            cooldown = replay == null ? RETRY_TICKS : MIN_COOLDOWN_TICKS + RANDOM.nextInt(MAX_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS);
            if (replay != null) {
                return "An echo is replaying now.";
            }
            float needed = (MIN_LAG_SAMPLES + REPLAY_SAMPLES) * SAMPLE_INTERVAL / 20.0F;
            if (seconds < needed) {
                return String.format("No echo yet: %.0fs of path recorded, %.0fs needed. Keep moving around Elythia.", seconds, needed);
            }
            return String.format("No echo: of %d stretches from 2 to 4 minutes ago, %d were too close or too far (needs %.0f to %.0f blocks away), %d had too little walking, %d were while riding or sleeping.",
                    reasons[REASON_CHECKED], reasons[REASON_DISTANCE], MIN_DISTANCE, MAX_DISTANCE, reasons[REASON_STILL], reasons[REASON_INVALID]);
        }
        for (int start = SAMPLES.size() - 1 - REPLAY_SAMPLES; start >= 0; start--) {
            if (suitable(player, start, false, null)) {
                replay = new Replay(start, true);
                return "Replaying your most recent walk as an echo.";
            }
        }
        return String.format("Walk around for at least 15 seconds first (%.0fs recorded).", seconds);
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Replay current = replay;
        if (current == null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        float age = current.age + partialTick;
        float progress = Math.min(age / SAMPLE_INTERVAL, REPLAY_SAMPLES - 1.001F);
        int index = (int) progress;
        float delta = progress - index;
        Sample from = SAMPLES.get(current.start + index);
        Sample to = SAMPLES.get(current.start + index + 1);
        double x = Mth.lerp(delta, from.x, to.x);
        double y = Mth.lerp(delta, from.y, to.y);
        double z = Mth.lerp(delta, from.z, to.z);
        float alpha = alpha(current, age);
        if (alpha <= 0.01F) {
            return;
        }

        PlayerSkin skin = minecraft.player.getSkin();
        PlayerModel<AbstractClientPlayer> model = model(minecraft, skin.model() == PlayerSkin.Model.SLIM);
        float bodyYaw = Mth.rotLerp(delta, from.bodyYaw, to.bodyYaw);
        float headYaw = Mth.rotLerp(delta, from.headYaw, to.headYaw);
        float pitch = Mth.lerp(delta, from.pitch, to.pitch);
        float limbPos = Mth.lerp(delta, from.limbPos, to.limbPos);
        float limbSpeed = Mth.lerp(delta, from.limbSpeed, to.limbSpeed);
        boolean crouching = delta < 0.5F ? from.crouching : to.crouching;

        model.attackTime = 0.0F;
        model.riding = false;
        model.young = false;
        model.crouching = crouching;
        model.swimAmount = 0.0F;
        model.setAllVisible(true);
        model.setupAnim(minecraft.player, limbPos, limbSpeed, age, Mth.wrapDegrees(headYaw - bodyYaw), pitch);

        Vec3 cameraPos = camera.getPosition();
        int light = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(x, y + 1.0D, z));
        int color = Math.round(alpha * 255.0F) << 24 | TINT;
        RenderType renderType = RenderType.entityTranslucent(skin.texture());

        poseStack.pushPose();
        poseStack.translate(x - cameraPos.x, y - cameraPos.y + (crouching ? -0.125D : 0.0D), z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-PLAYER_SCALE, -PLAYER_SCALE, PLAYER_SCALE);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.renderToBuffer(poseStack, buffers.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
        buffers.endBatch(renderType);
    }

    private static void record(LocalPlayer player) {
        Sample previous = SAMPLES.isEmpty() ? null : SAMPLES.get(SAMPLES.size() - 1);
        boolean valid = !player.isPassenger() && !player.isSleeping() && !player.isSpectator();
        double travelled = previous == null ? 0.0D : Math.sqrt(Mth.square(player.getX() - previous.x) + Mth.square(player.getZ() - previous.z));
        float limbSpeed = Math.min(1.0F, (float) travelled);
        float limbPos = previous == null ? 0.0F : previous.limbPos + (float) travelled * 4.0F;
        SAMPLES.add(new Sample(player.getX(), player.getY(), player.getZ(), player.yBodyRot, player.yHeadRot,
                player.getXRot(), player.isCrouching(), limbPos, limbSpeed, valid));
        if (SAMPLES.size() > MAX_SAMPLES) {
            int drop = SAMPLES.size() - MAX_SAMPLES;
            SAMPLES.subList(0, drop).clear();
            if (replay != null) {
                replay.start -= drop;
                if (replay.start < 0) {
                    replay = null;
                }
            }
        }
    }

    private static Replay pick(LocalPlayer player, int[] reasons) {
        int earliest = Math.max(0, SAMPLES.size() - 1 - MAX_LAG_SAMPLES);
        int latest = SAMPLES.size() - 1 - MIN_LAG_SAMPLES;
        List<Integer> candidates = new ArrayList<>();
        for (int start = earliest; start <= latest && start + REPLAY_SAMPLES < SAMPLES.size(); start += PICK_STRIDE) {
            reasons[REASON_CHECKED]++;
            if (suitable(player, start, true, reasons)) {
                candidates.add(start);
            }
        }
        return candidates.isEmpty() ? null : new Replay(candidates.get(RANDOM.nextInt(candidates.size())), false);
    }

    private static boolean suitable(LocalPlayer player, int start, boolean checkDistance, int[] reasons) {
        boolean moved = false;
        Sample first = SAMPLES.get(start);
        for (int i = start; i <= start + REPLAY_SAMPLES; i++) {
            Sample sample = SAMPLES.get(i);
            if (!sample.valid) {
                count(reasons, REASON_INVALID);
                return false;
            }
            double dx = sample.x - player.getX();
            double dz = sample.z - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (checkDistance && (distance < MIN_DISTANCE || distance > MAX_DISTANCE || Math.abs(sample.y - player.getY()) > MAX_HEIGHT_DIFFERENCE)) {
                count(reasons, REASON_DISTANCE);
                return false;
            }
            if (!moved && Mth.square(sample.x - first.x) + Mth.square(sample.z - first.z) > 16.0D) {
                moved = true;
            }
        }
        if (!moved) {
            count(reasons, REASON_STILL);
        }
        return moved;
    }

    private static void count(int[] reasons, int reason) {
        if (reasons != null) {
            reasons[reason]++;
        }
    }

    private static void tickReplay(LocalPlayer player) {
        Replay current = replay;
        current.age++;
        if (!current.test && current.vanishAt < 0) {
            float progress = Math.min((float) current.age / SAMPLE_INTERVAL, REPLAY_SAMPLES - 1.0F);
            Sample sample = SAMPLES.get(current.start + (int) progress);
            if (player.distanceToSqr(sample.x, sample.y, sample.z) < VANISH_DISTANCE * VANISH_DISTANCE) {
                current.vanishAt = current.age;
            }
        }
        boolean finished = current.age >= (REPLAY_SAMPLES - 1) * SAMPLE_INTERVAL;
        boolean vanished = current.vanishAt >= 0 && current.age - current.vanishAt >= VANISH_TICKS;
        if (finished || vanished) {
            replay = null;
        }
    }

    private static float alpha(Replay current, float age) {
        float total = (REPLAY_SAMPLES - 1) * SAMPLE_INTERVAL;
        float fadeIn = Mth.clamp(age / FADE_TICKS, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((total - age) / FADE_TICKS, 0.0F, 1.0F);
        float vanish = current.vanishAt < 0 ? 1.0F : Mth.clamp(1.0F - (age - current.vanishAt) / VANISH_TICKS, 0.0F, 1.0F);
        return BASE_ALPHA * fadeIn * fadeOut * vanish;
    }

    private static PlayerModel<AbstractClientPlayer> model(Minecraft minecraft, boolean slim) {
        if (slim) {
            if (slimModel == null) {
                slimModel = new PlayerModel<>(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
            }
            return slimModel;
        }
        if (wideModel == null) {
            wideModel = new PlayerModel<>(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        }
        return wideModel;
    }

    private static void reset() {
        SAMPLES.clear();
        replay = null;
        ticks = 0;
        cooldown = FIRST_COOLDOWN_TICKS;
        wideModel = null;
        slimModel = null;
    }

    private record Sample(double x, double y, double z, float bodyYaw, float headYaw, float pitch, boolean crouching,
                          float limbPos, float limbSpeed, boolean valid) {
    }

    private static final class Replay {
        private int start;
        private int age;
        private int vanishAt = -1;
        private final boolean test;

        private Replay(int start, boolean test) {
            this.start = start;
            this.test = test;
        }
    }
}
