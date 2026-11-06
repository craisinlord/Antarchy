package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class ThoraxisRainRenderer {
    private static final Identifier RAIN_TEXTURE = Identifier.withDefaultNamespace("textures/environment/rain.png");
    private static final float RED = 1.0F;
    private static final float GREEN = 0.22F;
    private static final float BLUE = 0.18F;
    private static final float VIEW_HEIGHT = 20.0F;
    private static final float DROP_SCALE = 0.5F;
    private static final float V_SCALE = 0.25F * DROP_SCALE;
    private static final float HALO_WIDTH = 1.8F;
    private static final float HALO_ALPHA = 0.35F;

    private ThoraxisRainRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        float strength = ThoraxisEventClient.rainStrength(partialTick);
        Minecraft minecraft = Minecraft.getInstance();
        if (strength <= 0.01F || minecraft.level == null) {
            return;
        }
        int radius = ThoraxisEventClient.RAIN_RADIUS;
        int size = radius * 2 + 1;
        int direction = ThoraxisEventClient.rainDirection();
        long ticks = minecraft.level.getGameTime();
        int centerX = ThoraxisEventClient.rainCenterX();
        int centerZ = ThoraxisEventClient.rainCenterZ();
        collector.submitCustomGeometry(poseStack, AntarchySkyDraw.additiveTexture(RAIN_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int index = (dx + radius) * size + dz + radius;
                    if (!ThoraxisEventClient.rainValid(index)) {
                        continue;
                    }
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    double relX = x + 0.5D - cameraPos.x;
                    double relZ = z + 0.5D - cameraPos.z;
                    double distanceSqr = relX * relX + relZ * relZ;
                    if (distanceSqr > radius * radius) {
                        continue;
                    }
                    float low = Math.max(ThoraxisEventClient.rainLow(index), (float) cameraPos.y - VIEW_HEIGHT);
                    float high = Math.min(ThoraxisEventClient.rainHigh(index), (float) cameraPos.y + VIEW_HEIGHT);
                    if (high <= low) {
                        continue;
                    }
                    double length = Math.max(0.01D, Math.sqrt(distanceSqr));
                    float sideX = (float) (-relZ / length * 0.5D);
                    float sideZ = (float) (relX / length * 0.5D);
                    int hash = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761;
                    float speed = 1.5F + ((hash >>> 8) & 15) / 30.0F;
                    float scroll = (((ticks + hash) & 31) + partialTick) / 32.0F * speed;
                    float v = (direction > 0 ? scroll : -scroll) * DROP_SCALE;
                    float fade = 1.0F - (float) (distanceSqr / (radius * radius));
                    float alpha = Math.min(1.0F, strength * (0.55F + 0.45F * fade));
                    float baseX = (float) relX;
                    float baseZ = (float) relZ;
                    float lowY = (float) (low - cameraPos.y);
                    float highY = (float) (high - cameraPos.y);
                    float topV = low * V_SCALE + v;
                    float bottomV = high * V_SCALE + v;
                    addQuad(consumer, matrix, baseX, baseZ, sideX * HALO_WIDTH, sideZ * HALO_WIDTH, lowY, highY, topV, bottomV, alpha * HALO_ALPHA);
                    addQuad(consumer, matrix, baseX, baseZ, sideX, sideZ, lowY, highY, topV, bottomV, alpha);
                }
            }
        });
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, float x, float z, float sideX, float sideZ,
                                float lowY, float highY, float topV, float bottomV, float alpha) {
        consumer.addVertex(matrix, x - sideX, highY, z - sideZ).setUv(0.0F, topV).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x + sideX, highY, z + sideZ).setUv(DROP_SCALE, topV).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x + sideX, lowY, z + sideZ).setUv(DROP_SCALE, bottomV).setColor(RED, GREEN, BLUE, alpha);
        consumer.addVertex(matrix, x - sideX, lowY, z - sideZ).setUv(0.0F, bottomV).setColor(RED, GREEN, BLUE, alpha);
    }
}
