package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ThoraxisRainRenderer {
    private static final ResourceLocation RAIN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
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

    public static void render(float partialTick, double camX, double camY, double camZ) {
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

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean any = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int index = (dx + radius) * size + dz + radius;
                if (!ThoraxisEventClient.rainValid(index)) {
                    continue;
                }
                int x = centerX + dx;
                int z = centerZ + dz;
                double relX = x + 0.5D - camX;
                double relZ = z + 0.5D - camZ;
                double distanceSqr = relX * relX + relZ * relZ;
                if (distanceSqr > radius * radius) {
                    continue;
                }
                float low = Math.max(ThoraxisEventClient.rainLow(index), (float) camY - VIEW_HEIGHT);
                float high = Math.min(ThoraxisEventClient.rainHigh(index), (float) camY + VIEW_HEIGHT);
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
                float lowY = (float) (low - camY);
                float highY = (float) (high - camY);
                float topV = low * V_SCALE + v;
                float bottomV = high * V_SCALE + v;
                addQuad(buffer, baseX, baseZ, sideX * HALO_WIDTH, sideZ * HALO_WIDTH, lowY, highY, topV, bottomV, alpha * HALO_ALPHA);
                addQuad(buffer, baseX, baseZ, sideX, sideZ, lowY, highY, topV, bottomV, alpha);
                any = true;
            }
        }
        MeshData mesh = buffer.build();
        if (!any || mesh == null) {
            if (mesh != null) {
                mesh.close();
            }
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RAIN_TEXTURE);
        BufferUploader.drawWithShader(mesh);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void addQuad(BufferBuilder buffer, float x, float z, float sideX, float sideZ, float lowY, float highY,
                                float topV, float bottomV, float alpha) {
        buffer.addVertex(x - sideX, highY, z - sideZ).setUv(0.0F, topV).setColor(RED, GREEN, BLUE, alpha);
        buffer.addVertex(x + sideX, highY, z + sideZ).setUv(DROP_SCALE, topV).setColor(RED, GREEN, BLUE, alpha);
        buffer.addVertex(x + sideX, lowY, z + sideZ).setUv(DROP_SCALE, bottomV).setColor(RED, GREEN, BLUE, alpha);
        buffer.addVertex(x - sideX, lowY, z - sideZ).setUv(0.0F, bottomV).setColor(RED, GREEN, BLUE, alpha);
    }
}
