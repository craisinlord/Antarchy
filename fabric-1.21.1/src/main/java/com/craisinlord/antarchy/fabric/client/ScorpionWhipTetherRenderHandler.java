package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.item.ScorpionWhipItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.joml.Matrix4f;

public final class ScorpionWhipTetherRenderHandler {
    private ScorpionWhipTetherRenderHandler() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ScorpionWhipTetherClientState.clear();
        }
    }

    public static void render(WorldRenderContext context) {
        if (context.world() == null || context.matrixStack() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Map<Integer, Integer> tethers = ScorpionWhipTetherClientState.snapshot();
        if (tethers.isEmpty()) {
            return;
        }

        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        Matrix4f pose = context.matrixStack().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<Integer, Integer> entry : tethers.entrySet()) {
            Entity source = minecraft.level.getEntity(entry.getKey());
            Entity target = minecraft.level.getEntity(entry.getValue());
            if (!(source instanceof Player player) || target == null || target.isRemoved()) {
                continue;
            }

            buildTether(
                    buffer,
                    pose,
                    tetherStart(player, context.tickCounter().getGameTimeDeltaPartialTick(false)),
                    target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                    cameraPos
            );
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
        boolean mainHandWhip = player.getMainHandItem().getItem() instanceof ScorpionWhipItem;
        boolean offHandWhip = player.getOffhandItem().getItem() instanceof ScorpionWhipItem;
        if (!mainHandWhip && !offHandWhip) {
            return start;
        }

        boolean useMainHand = mainHandWhip || !offHandWhip;
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

        int segments = Math.max(16, Mth.ceil(length * 8.0D));
        for (int i = 0; i < segments; i++) {
            double t0 = (double) i / (double) segments;
            double t1 = (double) (i + 1) / (double) segments;
            Vec3 p0 = ropePoint(start, delta, t0, length);
            Vec3 p1 = ropePoint(start, delta, t1, length);
            float width = 0.045F;
            Vec3 offset = right.scale(width);

            Vec3 a = p0.subtract(cameraPos);
            Vec3 b = p1.subtract(cameraPos);
            float shade = i % 2 == 0 ? 0.82F : 0.62F;
            float red = 0.36F * shade;
            float green = 0.20F * shade;
            float blue = 0.11F * shade;

            buffer.addVertex(pose, (float) (a.x + offset.x), (float) (a.y + offset.y), (float) (a.z + offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (a.x - offset.x), (float) (a.y - offset.y), (float) (a.z - offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (b.x - offset.x), (float) (b.y - offset.y), (float) (b.z - offset.z)).setColor(red, green, blue, 1.0F);
            buffer.addVertex(pose, (float) (b.x + offset.x), (float) (b.y + offset.y), (float) (b.z + offset.z)).setColor(red, green, blue, 1.0F);
        }
    }

    private static Vec3 ropePoint(Vec3 start, Vec3 delta, double t, double length) {
        double sag = Math.sin(t * Math.PI) * Math.min(0.45D, length * 0.07D);
        return start.add(delta.scale(t)).add(0.0D, -sag, 0.0D);
    }
}
