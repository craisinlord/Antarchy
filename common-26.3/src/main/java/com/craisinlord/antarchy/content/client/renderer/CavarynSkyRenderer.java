package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;

public final class CavarynSkyRenderer {
    public static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static final Map<SkyRenderState, State> STATES = new WeakHashMap<>();

    @Nullable
    private static AntarchySkyDraw.StaticMesh orangeStarMesh;

    private CavarynSkyRenderer() {
    }

    public static boolean shouldRender(ClientLevel level) {
        return level.dimension().equals(CAVARYN);
    }

    public static boolean shouldRender(SkyRenderState skyState) {
        return STATES.containsKey(skyState);
    }

    public static void extract(ClientLevel level, Camera camera, float partialTick, SkyRenderState skyState) {
        if (!shouldRender(level)) {
            STATES.remove(skyState);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        State state = new State();
        state.skip = minecraft.gui.hud.getBossOverlay().shouldCreateWorldFog() || minecraft.gameRenderer.mainCamera() != camera;
        state.intensity = HordeClientState.intensity();
        Entity cameraEntity = camera.entity();
        state.darkDisc = cameraEntity != null
                && cameraEntity.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < 0.0D;
        STATES.put(skyState, state);
    }

    public static void render(SkyRenderState skyState) {
        State state = STATES.get(skyState);
        if (state == null || state.skip) {
            return;
        }

        float intensity = state.intensity;
        float red = 0.015F + 0.28F * intensity;
        float green = 0.085F + 0.18F * intensity;
        float blue = 0.035F + 0.02F * intensity;

        Matrix4f modelView = AntarchySkyDraw.modelView();
        AntarchySkyDraw.draw("Antarchy Cavaryn sky disc", RenderPipelines.SKY, AntarchySkyDraw.topSkyDisc(), modelView,
                new Vector4f(red, green, blue, 1.0F), null);

        float starBrightness = 0.8F + 0.45F * intensity;
        if (orangeStarMesh == null) {
            orangeStarMesh = buildOrangeStarMesh();
        }

        AntarchySkyDraw.draw("Antarchy Cavaryn stars", RenderPipelines.DEBUG_QUADS, orangeStarMesh, modelView,
                new Vector4f(starBrightness, starBrightness, starBrightness, starBrightness), null);

        if (state.darkDisc) {
            Matrix4f darkModelView = new Matrix4f(modelView).translate(0.0F, 12.0F, 0.0F);
            AntarchySkyDraw.draw("Antarchy Cavaryn dark disc", RenderPipelines.SKY, AntarchySkyDraw.bottomSkyDisc(), darkModelView,
                    AntarchySkyDraw.WHITE, null);
        }
    }

    private static AntarchySkyDraw.StaticMesh buildOrangeStarMesh() {
        Random random = new Random(548721L);
        BufferBuilder builder = AntarchySkyDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 1800; i++) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double size = 0.12F + random.nextFloat() * 0.12F;
            double lenSq = x * x + y * y + z * z;
            if (lenSq < 1.0D && lenSq > 0.01D) {
                double len = 1.0D / Math.sqrt(lenSq);
                x *= len;
                y *= len;
                z *= len;
                double sx = x * 100.0D;
                double sy = y * 100.0D;
                double sz = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double rot = random.nextDouble() * Math.PI * 2.0D;
                double sinRot = Math.sin(rot);
                double cosRot = Math.cos(rot);

                float[] color = pickStarColor(random);

                for (int j = 0; j < 4; j++) {
                    double u = (j & 2) - 1;
                    double v = (j + 1 & 2) - 1;
                    double px = u * cosRot - v * sinRot;
                    double py = v * cosRot + u * sinRot;
                    double qx = px * sinPitch;
                    double qy = -px * cosPitch;
                    double rx = qy * sinYaw - py * cosYaw;
                    double ry = py * sinYaw + qy * cosYaw;
                    builder.addVertex((float) (sx + rx * size), (float) (sy + qx * size), (float) (sz + ry * size))
                            .setColor(color[0], color[1], color[2], 1.0F);
                }
            }
        }

        return AntarchySkyDraw.upload("Antarchy Cavaryn star buffer", builder.buildOrThrow());
    }

    private static float[] pickStarColor(Random random) {
        float roll = random.nextFloat();
        if (roll < 0.70F) return new float[]{1.00F, 0.55F, 0.14F};
        if (roll < 0.90F) return new float[]{1.00F, 0.70F, 0.24F};
        return new float[]{1.00F, 0.84F, 0.40F};
    }

    private static final class State {
        boolean skip;
        float intensity;
        boolean darkDisc;
    }
}
