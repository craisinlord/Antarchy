package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.WormHookTetherClientState;
import com.craisinlord.antarchy.content.item.WormHookItem;
import com.craisinlord.antarchy.content.util.WormHookRope;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class WormHookTetherRenderHandler {
    private static final Map<Integer, WormHookRope> ROPES = new HashMap<>();

    private WormHookTetherRenderHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            WormHookTetherClientState.clear();
            ROPES.clear();
            return;
        }

        Map<Integer, Integer> tethers = WormHookTetherClientState.snapshot();
        ROPES.keySet().removeIf(playerId -> !tethers.containsKey(playerId));

        for (Map.Entry<Integer, Integer> entry : tethers.entrySet()) {
            Entity source = minecraft.level.getEntity(entry.getKey());
            Entity hook = minecraft.level.getEntity(entry.getValue());
            if (!(source instanceof Player player) || hook == null || hook.isRemoved()) {
                continue;
            }

            Vec3 anchor = hook.position();
            Vec3 playerEye = player.getEyePosition();
            WormHookRope rope = ROPES.computeIfAbsent(entry.getKey(), id -> new WormHookRope(anchor, playerEye));
            rope.update(hook, anchor, playerEye, anchor.distanceTo(playerEye) + 32.0D);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Map<Integer, Integer> tethers = WormHookTetherClientState.snapshot();
        if (tethers.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Matrix4f pose = event.getPoseStack().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<Integer, Integer> entry : tethers.entrySet()) {
            Entity source = minecraft.level.getEntity(entry.getKey());
            Entity hook = minecraft.level.getEntity(entry.getValue());
            if (!(source instanceof Player player) || hook == null || hook.isRemoved()) {
                continue;
            }

            WormHookRope rope = ROPES.get(entry.getKey());
            List<Vec3> points = rope != null ? rope.points() : List.of(hook.position(), player.getEyePosition());

            Vec3 handPos = tetherStart(player, event.getPartialTick().getGameTimeDeltaPartialTick(false));
            for (int i = points.size() - 1; i > 0; i--) {
                Vec3 start = i == points.size() - 1 ? handPos : points.get(i);
                Vec3 end = points.get(i - 1);
                buildTether(buffer, pose, start, end, cameraPos);
            }
        }

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static Vec3 tetherStart(Player player, float partialTick) {
        Vec3 start = player.getEyePosition(partialTick).add(0.0D, -0.45D, 0.0D);
        boolean mainHandHook = player.getMainHandItem().getItem() instanceof WormHookItem;
        boolean offHandHook = player.getOffhandItem().getItem() instanceof WormHookItem;
        if (!mainHandHook && !offHandHook) {
            return start;
        }

        boolean useMainHand = mainHandHook || !offHandHook;
        HumanoidArm arm = useMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double radians = Math.toRadians(bodyYaw + 90.0F);
        double side = arm == HumanoidArm.RIGHT ? -0.28D : 0.28D;
        return start.add(Math.cos(radians) * side, -0.12D, Math.sin(radians) * side);
    }

    private static void buildTether(BufferBuilder buffer, Matrix4f pose, Vec3 start, Vec3 end, Vec3 cameraPos) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.001D) {
            return;
        }

        Vec3 forward = delta.scale(1.0D / length);
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        right = right.normalize();

        int segments = Math.max(4, Mth.ceil(length * 8.0D));
        for (int i = 0; i < segments; i++) {
            double t0 = (double) i / (double) segments;
            double t1 = (double) (i + 1) / (double) segments;
            Vec3 p0 = ropePoint(start, delta, t0, length);
            Vec3 p1 = ropePoint(start, delta, t1, length);
            float width = 0.04F;
            Vec3 offset = right.scale(width);

            Vec3 a = p0.subtract(cameraPos);
            Vec3 b = p1.subtract(cameraPos);
            float shade = i % 2 == 0 ? 0.85F : 0.65F;
            float red = 0.42F * shade;
            float green = 0.36F * shade;
            float blue = 0.24F * shade;

            buffer.addVertex(pose, (float) (a.x + offset.x), (float) (a.y + offset.y), (float) (a.z + offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (a.x - offset.x), (float) (a.y - offset.y), (float) (a.z - offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (b.x - offset.x), (float) (b.y - offset.y), (float) (b.z - offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (b.x + offset.x), (float) (b.y + offset.y), (float) (b.z + offset.z)).setColor(red, green, blue, 1.0F);
        }
    }

    private static Vec3 ropePoint(Vec3 start, Vec3 delta, double t, double length) {
        double sag = Math.sin(t * Math.PI) * Math.min(0.2D, length * 0.03D);
        return start.add(delta.scale(t)).add(0.0D, -sag, 0.0D);
    }
}
