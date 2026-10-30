package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.client.ElythiaEventClient;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

final class ElythiaGiantCloudRenderer {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final float BASE_Y = 234.0F;
    private static final float ALPHA = 0.8F;
    private static final int ANCHOR_DIRECTIONS = 24;
    private static final int MIN_DIRECTION_GAP = 3;
    private static final float GROWTH_TICKS = 1800.0F;
    private static final float CLEARING_FADE_TICKS = 1200.0F;
    private static final int EXPOSURE_STEPS = 8;
    private static final int MIN_TOWERS = 3;
    private static final int MAX_TOWERS = 5;
    private static final int PUFF_CELLS = 2;
    private static final int ANCHOR_RETRY_TICKS = 20;
    private static final double DRIFT_SCALE = 0.03D * 0.8D;
    private static final int[][] NORMALS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    private static final List<Tower> TOWERS = new ArrayList<>();
    private static boolean anchored;
    private static long anchorSeed;
    private static double anchorWindX;
    private static double anchorWindZ;
    private static long anchorRetryAt = Long.MIN_VALUE;
    private static long anchorTime;
    private static float clearingLevel;
    private static double clearingTime = Double.NaN;
    private static final float[] CLEARINGS = new float[ElythiaSoftCloudPass.MAX_CLEARINGS * 4];
    private static int clearingCount;

    private record Mesh(ElythiaGiantCloud cloud, @Nullable MeshData data, ByteBufferBuilder bytes) {
    }

    private static final class Tower {
        private final long seed;
        private final double scale;
        private final double x;
        private final double z;
        @Nullable
        private CompletableFuture<Mesh> pending;
        private boolean pendingSoft;
        @Nullable
        private VertexBuffer buffer;
        @Nullable
        private ElythiaGiantCloud cloud;
        private boolean built;
        private boolean builtSoft;
        private boolean failed;

        private Tower(long seed, double scale, double x, double z) {
            this.seed = seed;
            this.scale = scale;
            this.x = x;
            this.z = z;
        }

        @Nullable
        private VertexBuffer buffer(boolean soft) {
            if (this.built && this.builtSoft == soft) {
                return this.buffer;
            }
            if (this.failed) {
                return null;
            }
            if (this.pending == null || this.pendingSoft != soft) {
                if (this.pending != null) {
                    this.pending.thenAccept(ElythiaGiantCloudRenderer::release);
                }
                this.pendingSoft = soft;
                ElythiaGiantCloud known = this.cloud;
                long seed = this.seed;
                double scale = this.scale;
                this.pending = CompletableFuture.supplyAsync(() -> {
                    ElythiaGiantCloud giant = known != null ? known : ElythiaGiantCloud.generate(seed, scale);
                    return soft ? buildSoftMesh(giant) : buildMesh(giant);
                });
            }
            if (!this.pending.isDone()) {
                return this.built ? this.buffer : null;
            }
            Mesh mesh;
            try {
                mesh = this.pending.join();
            } catch (RuntimeException exception) {
                Antarchy.LOGGER.error("Failed to build Elythia towering cloud", exception);
                this.pending = null;
                this.failed = true;
                return null;
            }
            this.pending = null;
            this.close();
            try {
                if (mesh.data() != null) {
                    VertexBuffer uploaded = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    uploaded.bind();
                    uploaded.upload(mesh.data());
                    VertexBuffer.unbind();
                    this.buffer = uploaded;
                }
            } finally {
                mesh.bytes().close();
            }
            this.cloud = mesh.cloud();
            this.built = true;
            this.builtSoft = soft;
            return this.buffer;
        }

        private void close() {
            if (this.buffer != null) {
                this.buffer.close();
                this.buffer = null;
            }
        }

        private void discard() {
            if (this.pending != null) {
                this.pending.thenAccept(ElythiaGiantCloudRenderer::release);
                this.pending = null;
            }
            this.close();
        }
    }

    private ElythiaGiantCloudRenderer() {
    }

    static void render(
            Minecraft minecraft,
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            ClientLevel level,
            Vec3 cloudColor,
            CloudStatus status,
            double[] wind,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            float terrainFogEnd,
            boolean soft,
            List<ElythiaSoftCloudPass.Draw> softDraws
    ) {
        float strength = ElythiaEventClient.strength(ElythiaEvent.TOWERING_CLOUDS, partialTick);
        long seed = ElythiaEventClient.seed();
        double now = level.getGameTime() + partialTick;
        updateClearing(now, anchored && anchorSeed == seed && strength > 0.0F && anyBuilt());
        clearingCount = 0;
        if (strength <= 0.0F || anchorSeed != seed) {
            if (strength <= 0.0F || anchored) {
                reset();
            }
            if (strength <= 0.0F) {
                return;
            }
        }
        anchorSeed = seed;
        if (!anchored) {
            long time = level.getGameTime();
            if (time < anchorRetryAt && anchorRetryAt - time <= ANCHOR_RETRY_TICKS) {
                return;
            }
            if (!tryAnchor(minecraft, level, seed, wind, camX, camZ)) {
                anchorRetryAt = time + ANCHOR_RETRY_TICKS;
                return;
            }
        }

        float growthProgress = Mth.clamp((float) (now - anchorTime) / GROWTH_TICKS, 0.0F, 1.0F);
        float growth = growthProgress * growthProgress * (3.0F - 2.0F * growthProgress);
        double driftX = (wind[0] - anchorWindX) * DRIFT_SCALE;
        double driftZ = (wind[1] - anchorWindZ) * DRIFT_SCALE;
        for (Tower tower : TOWERS) {
            VertexBuffer vertexBuffer = tower.buffer(soft);
            ElythiaGiantCloud giant = tower.cloud;
            if (vertexBuffer == null || giant == null) {
                continue;
            }
            double dx = tower.x + driftX - camX;
            double dz = tower.z + driftZ - camZ;
            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            float fogEnd = Math.max(terrainFogEnd, distance * 1.9F + 200.0F);
            float offsetX = (float) (dx + giant.originX());
            float offsetY = (float) (BASE_Y - camY);
            float offsetZ = (float) (dz + giant.originZ());
            if (clearingCount < ElythiaSoftCloudPass.MAX_CLEARINGS) {
                int index = clearingCount * 4;
                CLEARINGS[index] = (float) dx;
                CLEARINGS[index + 1] = 0.0F;
                CLEARINGS[index + 2] = (float) dz;
                CLEARINGS[index + 3] = Math.max(giant.sizeX(), giant.sizeZ()) * ElythiaGiantCloud.VOXEL * 0.5F;
                clearingCount++;
            }
            if (tower.builtSoft) {
                float towerHeight = giant.sizeY() * ElythiaGiantCloud.VOXEL;
                float growthTop = offsetY + (0.3F + 0.7F * growth) * towerHeight + 40.0F;
                softDraws.add(new ElythiaSoftCloudPass.Draw(vertexBuffer, offsetX, offsetY, offsetZ, fogEnd, strength,
                        growthTop, false));
                continue;
            }
            RenderSystem.setShaderFogStart(Math.max(terrainFogEnd * 0.5F, distance * 0.55F));
            RenderSystem.setShaderFogEnd(fogEnd);
            RenderSystem.setShaderColor((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, strength);
            poseStack.pushPose();
            poseStack.translate(offsetX, offsetY, offsetZ);
            vertexBuffer.bind();
            for (int pass = status == CloudStatus.FANCY ? 0 : 1; pass < 2; pass++) {
                RenderType renderType = pass == 0 ? RenderType.cloudsDepthOnly() : RenderType.clouds();
                renderType.setupRenderState();
                RenderSystem.disableCull();
                RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
                ShaderInstance shader = RenderSystem.getShader();
                if (shader != null) {
                    vertexBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
                }
                renderType.clearRenderState();
            }
            poseStack.popPose();
        }
    }

    static float clearing() {
        return clearingLevel;
    }

    static float[] clearingData() {
        return CLEARINGS;
    }

    static int clearingCount() {
        return clearingCount;
    }

    private static boolean anyBuilt() {
        for (Tower tower : TOWERS) {
            if (tower.built) {
                return true;
            }
        }
        return false;
    }

    private static void updateClearing(double now, boolean active) {
        double elapsed = Double.isNaN(clearingTime) ? 0.0D : Mth.clamp(now - clearingTime, 0.0D, 20.0D);
        clearingTime = now;
        float step = (float) (elapsed / CLEARING_FADE_TICKS);
        clearingLevel = active ? Math.min(1.0F, clearingLevel + step) : Math.max(0.0F, clearingLevel - step);
    }

    private static void reset() {
        for (Tower tower : TOWERS) {
            tower.discard();
        }
        TOWERS.clear();
        anchored = false;
        anchorRetryAt = Long.MIN_VALUE;
    }

    private static boolean tryAnchor(Minecraft minecraft, ClientLevel level, long seed, double[] wind, double camX, double camZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (!openSky(level, pos.set(camX, 64.0D, camZ))) {
            return false;
        }
        Random random = new Random(seed);
        double distance = Mth.clamp(minecraft.gameRenderer.getDepthFar() * 0.6D, 260.0D, 640.0D);
        double probe = Math.min(distance, Math.max(32.0D, minecraft.options.getEffectiveRenderDistance() * 16.0D - 24.0D));
        int wanted = MIN_TOWERS + random.nextInt(MAX_TOWERS - MIN_TOWERS + 1);
        int offset = random.nextInt(ANCHOR_DIRECTIONS);
        double[] angles = new double[ANCHOR_DIRECTIONS];
        int[] openness = new int[ANCHOR_DIRECTIONS];
        for (int direction = 0; direction < ANCHOR_DIRECTIONS; direction++) {
            double angle = Math.PI * 2.0D * (direction + random.nextDouble() * 0.6D - 0.3D) / ANCHOR_DIRECTIONS;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            angles[direction] = angle;
            openness[direction] = (openSky(level, pos.set(camX + cos * probe, 64.0D, camZ + sin * probe)) ? 1 : 0)
                    + (openSky(level, pos.set(camX + cos * probe * 0.5D, 64.0D, camZ + sin * probe * 0.5D)) ? 1 : 0);
        }

        int main = -1;
        for (int step = 0; step < ANCHOR_DIRECTIONS; step++) {
            int direction = (offset + step) % ANCHOR_DIRECTIONS;
            if (openness[direction] > 0 && (main < 0 || openness[direction] > openness[main])) {
                main = direction;
            }
        }
        if (main < 0) {
            return false;
        }

        List<Integer> chosen = new ArrayList<>();
        chosen.add(main);
        addTower(angles[main], distance, 1.0D, camX, camZ, random);
        for (int step = 0; step < ANCHOR_DIRECTIONS && chosen.size() < wanted; step++) {
            int direction = (offset + step * 7) % ANCHOR_DIRECTIONS;
            if (openness[direction] == 0 || !spaced(direction, chosen)) {
                continue;
            }
            double reach = chosen.size() <= 2 ? 0.5D + random.nextDouble() * 0.2D : 0.8D + random.nextDouble() * 0.3D;
            chosen.add(direction);
            addTower(angles[direction], distance * reach, 0.55D + random.nextDouble() * 0.3D, camX, camZ, random);
        }
        Antarchy.LOGGER.debug("Elythia towering clouds placed {} of {} towers", TOWERS.size(), wanted);
        anchorWindX = wind[0];
        anchorWindZ = wind[1];
        anchorTime = level.getGameTime();
        anchored = true;
        return true;
    }

    private static void addTower(double angle, double reach, double scale, double camX, double camZ, Random random) {
        TOWERS.add(new Tower(random.nextLong(), scale, camX + Math.cos(angle) * reach, camZ + Math.sin(angle) * reach));
    }

    private static boolean spaced(int direction, List<Integer> chosen) {
        for (int other : chosen) {
            int gap = Math.abs(direction - other);
            if (Math.min(gap, ANCHOR_DIRECTIONS - gap) < MIN_DIRECTION_GAP) {
                return false;
            }
        }
        return true;
    }

    private static boolean openSky(ClientLevel level, BlockPos pos) {
        return level.getBiome(pos).is(AntarchyTags.Biomes.ELYTHIA_TOWERING_CLOUD_BIOMES);
    }

    private static void release(Mesh mesh) {
        if (mesh.data() != null) {
            mesh.data().close();
        }
        mesh.bytes().close();
    }

    private static Mesh buildSoftMesh(ElythiaGiantCloud giant) {
        ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 20);
        try {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            float voxel = ElythiaGiantCloud.VOXEL;
            float totalHeight = giant.sizeY() * voxel;
            for (int by = 0; by < giant.sizeY(); by += PUFF_CELLS) {
                for (int bz = 0; bz < giant.sizeZ(); bz += PUFF_CELLS) {
                    for (int bx = 0; bx < giant.sizeX(); bx += PUFF_CELLS) {
                        int filled = 0;
                        boolean surface = false;
                        float sumX = 0.0F;
                        float sumY = 0.0F;
                        float sumZ = 0.0F;
                        for (int y = by; y < by + PUFF_CELLS; y++) {
                            for (int z = bz; z < bz + PUFF_CELLS; z++) {
                                for (int x = bx; x < bx + PUFF_CELLS; x++) {
                                    if (!giant.filled(x, y, z)) {
                                        continue;
                                    }
                                    filled++;
                                    sumX += x + 0.5F;
                                    sumY += y + 0.5F;
                                    sumZ += z + 0.5F;
                                    surface |= !interior(giant, x, y, z);
                                }
                            }
                        }
                        if (filled == 0 || !surface) {
                            continue;
                        }
                        float px = (sumX / filled + (hash(bx, by, bz, 0x61L) - 0.5F) * 0.5F) * voxel;
                        float py = (sumY / filled + (hash(bx, by, bz, 0x62L) - 0.5F) * 0.5F) * voxel;
                        float pz = (sumZ / filled + (hash(bx, by, bz, 0x63L) - 0.5F) * 0.5F) * voxel;
                        float fill = filled / (float) (PUFF_CELLS * PUFF_CELLS * PUFF_CELLS);
                        float half = voxel * PUFF_CELLS * 0.5F * (0.85F + 0.35F * fill + 0.2F * hash(bx, by, bz, 0x64L));
                        boolean openAbove = !giant.filled(bx, by + PUFF_CELLS, bz) && !giant.filled(bx + 1, by + PUFF_CELLS, bz + 1);
                        boolean openBelow = !giant.filled(bx, by - 1, bz) && !giant.filled(bx + 1, by - 1, bz + 1);
                        float light = 0.2F + 0.55F * Mth.clamp(py / totalHeight, 0.0F, 1.0F)
                                + (openAbove ? 0.25F : 0.0F) - (openBelow ? 0.2F : 0.0F);
                        int cx = bx + 1;
                        int cy = by + 1;
                        int cz = bz + 1;
                        softCube(builder, px - half, py - half * 0.85F, pz - half,
                                px + half, py + half * 0.85F, pz + half, Mth.clamp(light, 0.0F, 1.0F),
                                exposure(giant, cx, cy, cz, 1), exposure(giant, cx, cy, cz, -1));
                    }
                }
            }
            return new Mesh(giant, builder.build(), bytes);
        } catch (RuntimeException exception) {
            bytes.close();
            throw exception;
        }
    }

    private static boolean interior(ElythiaGiantCloud giant, int x, int y, int z) {
        for (int[] normal : NORMALS) {
            if (!giant.filled(x + normal[0], y + normal[1], z + normal[2])) {
                return false;
            }
        }
        return true;
    }

    private static float exposure(ElythiaGiantCloud giant, int x, int y, int z, int direction) {
        int blocked = 0;
        for (int step = 1; step <= EXPOSURE_STEPS; step++) {
            if (giant.filled(x + direction * step, y + step / 2, z)) {
                blocked++;
            }
        }
        return 1.0F - Math.min(1.0F, blocked / 3.0F);
    }

    private static void softCube(BufferBuilder builder, float x0, float y0, float z0, float x1, float y1, float z1,
                                 float ambient, float east, float west) {
        softQuad(builder, ambient, east, west, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        softQuad(builder, ambient, east, west, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        softQuad(builder, ambient, east, west, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        softQuad(builder, ambient, east, west, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        softQuad(builder, ambient, east, west, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        softQuad(builder, ambient, east, west, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
    }

    private static void softQuad(BufferBuilder builder, float ambient, float east, float west, int normalX, int normalY, int normalZ,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 float cx, float cy, float cz, float dx, float dy, float dz) {
        builder.addVertex(ax, ay, az).setUv(0.5F, 0.5F).setColor(ambient, east, west, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(bx, by, bz).setUv(0.5F, 0.5F).setColor(ambient, east, west, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(cx, cy, cz).setUv(0.5F, 0.5F).setColor(ambient, east, west, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(dx, dy, dz).setUv(0.5F, 0.5F).setColor(ambient, east, west, 1.0F).setNormal(normalX, normalY, normalZ);
    }

    private static float hash(int x, int y, int z, long salt) {
        long h = x * 0x9E3779B97F4A7C15L ^ y * 0xD6E8FEB86659FD93L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) * 0x1.0p-24F;
    }

    private static Mesh buildMesh(ElythiaGiantCloud giant) {
        ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 20);
        try {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            int[] size = {giant.sizeX(), giant.sizeY(), giant.sizeZ()};
            for (int[] normal : NORMALS) {
                int axis = normal[0] != 0 ? 0 : normal[1] != 0 ? 1 : 2;
                int uAxis = axis == 0 ? 1 : 0;
                int vAxis = axis == 2 ? 1 : 2;
                int width = size[uAxis];
                int height = size[vAxis];
                boolean[] mask = new boolean[width * height];
                int[] cell = new int[3];
                for (int depth = 0; depth < size[axis]; depth++) {
                    cell[axis] = depth;
                    for (int v = 0; v < height; v++) {
                        cell[vAxis] = v;
                        for (int u = 0; u < width; u++) {
                            cell[uAxis] = u;
                            mask[v * width + u] = giant.filled(cell[0], cell[1], cell[2])
                                    && !giant.filled(cell[0] + normal[0], cell[1] + normal[1], cell[2] + normal[2]);
                        }
                    }
                    float plane = (depth + Math.max(normal[axis], 0)) * ElythiaGiantCloud.VOXEL;
                    for (int v = 0; v < height; v++) {
                        for (int u = 0; u < width; u++) {
                            if (!mask[v * width + u]) {
                                continue;
                            }
                            int runU = 1;
                            while (u + runU < width && mask[v * width + u + runU]) {
                                runU++;
                            }
                            int runV = 1;
                            growing:
                            while (v + runV < height) {
                                for (int k = 0; k < runU; k++) {
                                    if (!mask[(v + runV) * width + u + k]) {
                                        break growing;
                                    }
                                }
                                runV++;
                            }
                            for (int dv = 0; dv < runV; dv++) {
                                for (int du = 0; du < runU; du++) {
                                    mask[(v + dv) * width + u + du] = false;
                                }
                            }
                            emit(builder, normal, axis, uAxis, vAxis, plane,
                                    u * ElythiaGiantCloud.VOXEL, (u + runU) * ElythiaGiantCloud.VOXEL,
                                    v * ElythiaGiantCloud.VOXEL, (v + runV) * ElythiaGiantCloud.VOXEL);
                        }
                    }
                }
            }
            return new Mesh(giant, builder.build(), bytes);
        } catch (RuntimeException exception) {
            bytes.close();
            throw exception;
        }
    }

    private static void emit(BufferBuilder builder, int[] normal, int axis, int uAxis, int vAxis, float plane,
                             float u0, float u1, float v0, float v1) {
        float shade = ElythiaGiantCloud.shade(normal[0], normal[1], normal[2]);
        float[][] corners = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
        float[] position = new float[3];
        for (float[] corner : corners) {
            position[axis] = plane;
            position[uAxis] = corner[0];
            position[vAxis] = corner[1];
            builder.addVertex(position[0], position[1], position[2])
                    .setUv(0.5F, 0.5F)
                    .setColor(shade, shade, shade, ALPHA)
                    .setNormal(normal[0], normal[1], normal[2]);
        }
    }
}
