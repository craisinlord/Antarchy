package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import com.craisinlord.antarchy.content.client.PrimordialGrassTrample;
import com.craisinlord.antarchy.content.client.PrimordialGrassTrampleField;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.craisinlord.antarchy.content.client.PrimordialWindSound;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrimordialGrassRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("antarchy", "block/primordial_grass");
    private static final RenderType GRASS_TYPE = new RenderType("antarchy_primordial_grass", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 1 << 17, false, false,
            () -> {
                RenderType.cutout().setupRenderState();
                RenderSystem.disableCull();
            },
            () -> {
                RenderSystem.enableCull();
                RenderType.cutout().clearRenderState();
            }) {
    };
    private static final float HALF_WIDTH = 0.45F;
    private static final float[] FACE_X = {HALF_WIDTH, -HALF_WIDTH};
    private static final float[] FACE_Z = {HALF_WIDTH, HALF_WIDTH};
    private static final int LOD_FAR = 2;
    private static final int LOD_STATIC = 3;
    private static final int[] LOD_BANDS = {8, 3, 1, 1};
    private static final float[][] BAND_HEIGHT = new float[9][];
    private static final float[][] BAND_FLEX = new float[9][];
    private static final int CPU_BANDS = 6;
    private static final double CPU_RADIUS_SQR = 32.0D * 32.0D;
    private static final long RESCAN_BUDGET_NANOS = 1_000_000L;
    private static final long MESH_BUDGET_NANOS = 1_500_000L;
    private static final int SWEEP_CHECKS = 64;

    private static final Long2ObjectOpenHashMap<GrassSection> SECTIONS = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<LevelChunk> SEEN_CHUNKS = new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet DIRTY_CHUNKS = new LongOpenHashSet();
    private static final LongOpenHashSet DIRTY_SECTIONS = new LongOpenHashSet();
    private static final LongOpenHashSet REMESH = new LongOpenHashSet();
    private static final List<GrassSection> NEAR = new ArrayList<>();
    private static final float[] GUST_TABLE = new float[PrimordialGrassWind.GUST_TABLE_ENTRIES * 2];
    private static final float[] TRAMPLE_POSITIONS = new float[PrimordialGrassTrample.MAX_POINTS * 4];
    private static final float[] TRAMPLE_MOTIONS = new float[PrimordialGrassTrample.MAX_POINTS * 3];
    private static final float[] TRAMPLE_SAMPLE = new float[3];
    private static final float[] LEVEL_DX = new float[CPU_BANDS + 1];
    private static final float[] LEVEL_DY = new float[CPU_BANDS + 1];
    private static final float[] LEVEL_DZ = new float[CPU_BANDS + 1];

    private static ClientLevel trackedLevel;
    private static ByteBufferBuilder meshBytes;
    private static TextureAtlasSprite sprite;
    private static int[] sweepOffsets = new int[0];
    private static int centerX = Integer.MIN_VALUE;
    private static int centerZ;
    private static int radius = -1;
    private static int cursor;
    private static long tick = Long.MIN_VALUE;
    private static long eyeSection = Long.MIN_VALUE;
    private static Vec3 lastEye = Vec3.ZERO;
    private static boolean gpuMode;
    private static boolean lodDirty;
    private static int epoch;
    private static int drawnSections;
    private static int drawnVertices;

    static {
        for (int bands = 1; bands < BAND_HEIGHT.length; bands++) {
            float[] heights = new float[bands + 1];
            float[] flex = new float[bands + 1];
            for (int i = 0; i <= bands; i++) {
                heights[i] = i / (float) bands;
                flex[i] = (float) PrimordialGrassWind.flexibility(heights[i]);
            }
            BAND_HEIGHT[bands] = heights;
            BAND_FLEX[bands] = flex;
        }
    }

    private PrimordialGrassRenderer() {
    }

    public static void blockChanged(BlockPos pos) {
        if (trackedLevel != null) DIRTY_SECTIONS.add(SectionPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4));
    }

    public static void lightChanged(int sectionX, int sectionY, int sectionZ) {
        long key = SectionPos.asLong(sectionX, sectionY, sectionZ);
        if (SECTIONS.containsKey(key)) DIRTY_SECTIONS.add(key);
    }

    public static void chunkLoaded(ChunkPos pos) {
        if (trackedLevel != null) DIRTY_CHUNKS.add(pos.toLong());
    }

    public static String debugLine() {
        if (trackedLevel == null || SECTIONS.isEmpty()) return null;
        int tufts = 0;
        for (GrassSection section : SECTIONS.values()) tufts += section.count;
        return "Primordial grass: " + SECTIONS.size() + " sections, " + tufts + " tufts, "
                + drawnSections + " drawn, " + drawnVertices + " verts, "
                + (gpuMode ? "gpu" : "fallback") + ", " + REMESH.size() + " queued";
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level != trackedLevel) resetLevel(minecraft, level);
        if (level == null || minecraft.player == null || minecraft.isPaused() || tick == level.getGameTime()) return;
        tick = level.getGameTime();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        lastEye = camera.isInitialized() ? camera.getPosition() : minecraft.player.position();
        int cx = minecraft.player.blockPosition().getX() >> 4;
        int cz = minecraft.player.blockPosition().getZ() >> 4;
        int reach = minecraft.options.getEffectiveRenderDistance() + 1;
        if (cx != centerX || cz != centerZ || reach != radius) moveCenter(level, cx, cz, reach);
        long currentEyeSection = SectionPos.asLong(BlockPos.containing(lastEye));
        if (lodDirty || currentEyeSection != eyeSection) {
            eyeSection = currentEyeSection;
            lodDirty = false;
            updateLods();
        }
        processRescans(level);
        processMeshes();
        sweep(level);
        Vec3 player = minecraft.player.position();
        if (grassWithin(player, PrimordialGrassTrampleField.RANGE)) PrimordialGrassTrample.tick(level, player);
        else PrimordialGrassTrample.reset();
        PrimordialWindSound.tick(minecraft, exposedGrassNear(player, 24.0D));
    }

    private static void resetLevel(Minecraft minecraft, ClientLevel level) {
        for (GrassSection section : SECTIONS.values()) section.close();
        SECTIONS.clear();
        SEEN_CHUNKS.clear();
        DIRTY_CHUNKS.clear();
        DIRTY_SECTIONS.clear();
        REMESH.clear();
        NEAR.clear();
        trackedLevel = level;
        centerX = Integer.MIN_VALUE;
        radius = -1;
        tick = Long.MIN_VALUE;
        eyeSection = Long.MIN_VALUE;
        drawnSections = 0;
        drawnVertices = 0;
        if (level == null && meshBytes != null) {
            meshBytes.close();
            meshBytes = null;
        }
        PrimordialGrassTrample.reset();
        PrimordialWindSound.reset(minecraft);
    }

    private static void moveCenter(ClientLevel level, int cx, int cz, int reach) {
        if (reach != radius) buildSweepOffsets(reach);
        centerX = cx;
        centerZ = cz;
        radius = reach;
        cursor = 0;
        Iterator<Long2ObjectMap.Entry<LevelChunk>> seen = SEEN_CHUNKS.long2ObjectEntrySet().fastIterator();
        while (seen.hasNext()) {
            long key = seen.next().getLongKey();
            if (Math.abs(ChunkPos.getX(key) - cx) > reach || Math.abs(ChunkPos.getZ(key) - cz) > reach) {
                removeChunkSections(level, ChunkPos.getX(key), ChunkPos.getZ(key));
                seen.remove();
            }
        }
        DIRTY_CHUNKS.removeIf((long key) -> Math.abs(ChunkPos.getX(key) - cx) > reach || Math.abs(ChunkPos.getZ(key) - cz) > reach);
    }

    private static void buildSweepOffsets(int reach) {
        int side = reach * 2 + 1;
        long[] sorted = new long[side * side];
        int index = 0;
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                long ring = Math.max(Math.abs(x), Math.abs(z));
                sorted[index++] = ring << 32 | (x + reach) << 16 | (z + reach);
            }
        }
        Arrays.sort(sorted);
        sweepOffsets = new int[sorted.length * 2];
        for (int i = 0; i < sorted.length; i++) {
            sweepOffsets[i * 2] = (int) (sorted[i] >> 16 & 0xFFFF) - reach;
            sweepOffsets[i * 2 + 1] = (int) (sorted[i] & 0xFFFF) - reach;
        }
    }

    private static void processRescans(ClientLevel level) {
        long deadline = System.nanoTime() + RESCAN_BUDGET_NANOS;
        LongIterator chunks = DIRTY_CHUNKS.iterator();
        while (chunks.hasNext() && System.nanoTime() < deadline) {
            long key = chunks.nextLong();
            chunks.remove();
            scanChunk(level, ChunkPos.getX(key), ChunkPos.getZ(key));
        }
        LongIterator sections = DIRTY_SECTIONS.iterator();
        while (sections.hasNext() && System.nanoTime() < deadline) {
            long key = sections.nextLong();
            sections.remove();
            int sx = SectionPos.x(key);
            int sz = SectionPos.z(key);
            if (Math.abs(sx - centerX) > radius || Math.abs(sz - centerZ) > radius) continue;
            LevelChunk chunk = level.getChunkSource().getChunk(sx, sz, ChunkStatus.FULL, false);
            if (chunk != null) scanSection(level, chunk, sx, SectionPos.y(key), sz);
            else discardSection(key);
        }
    }

    private static void sweep(ClientLevel level) {
        if (sweepOffsets.length == 0) return;
        long deadline = System.nanoTime() + RESCAN_BUDGET_NANOS;
        int entries = sweepOffsets.length / 2;
        for (int i = 0; i < SWEEP_CHECKS && System.nanoTime() < deadline; i++) {
            int entry = cursor++ % entries;
            int cx = centerX + sweepOffsets[entry * 2];
            int cz = centerZ + sweepOffsets[entry * 2 + 1];
            long key = ChunkPos.asLong(cx, cz);
            LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
            LevelChunk known = SEEN_CHUNKS.get(key);
            if (chunk == known) continue;
            if (chunk == null) {
                SEEN_CHUNKS.remove(key);
                removeChunkSections(level, cx, cz);
            } else scanChunk(level, cx, cz);
        }
    }

    private static void scanChunk(ClientLevel level, int cx, int cz) {
        LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
        long key = ChunkPos.asLong(cx, cz);
        if (chunk == null) {
            SEEN_CHUNKS.remove(key);
            removeChunkSections(level, cx, cz);
            return;
        }
        SEEN_CHUNKS.put(key, chunk);
        for (int sy = level.getMinSection(); sy < level.getMaxSection(); sy++) scanSection(level, chunk, cx, sy, cz);
    }

    private static void removeChunkSections(ClientLevel level, int cx, int cz) {
        for (int sy = level.getMinSection(); sy < level.getMaxSection(); sy++) discardSection(SectionPos.asLong(cx, sy, cz));
    }

    private static void discardSection(long key) {
        GrassSection section = SECTIONS.remove(key);
        if (section != null) section.close();
        REMESH.remove(key);
    }

    private static void scanSection(ClientLevel level, LevelChunk chunk, int sx, int sy, int sz) {
        long key = SectionPos.asLong(sx, sy, sz);
        int index = level.getSectionIndexFromSectionY(sy);
        LevelChunkSection[] sections = chunk.getSections();
        if (index < 0 || index >= sections.length) {
            discardSection(key);
            return;
        }
        LevelChunkSection chunkSection = sections[index];
        if (chunkSection.hasOnlyAir() || !chunkSection.getStates().maybeHas(state -> state.getBlock() instanceof PrimordialGrassBlock)) {
            discardSection(key);
            return;
        }
        int capacity = 32;
        int count = 0;
        int[] packed = new int[capacity];
        int[] light = new int[capacity];
        float[] offsets = new float[capacity * 3];
        boolean exposed = false;
        int minY = 16;
        int maxY = -1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = chunkSection.getBlockState(x, y, z);
                    if (!(state.getBlock() instanceof PrimordialGrassBlock)) continue;
                    if (count == capacity) {
                        capacity *= 2;
                        packed = Arrays.copyOf(packed, capacity);
                        light = Arrays.copyOf(light, capacity);
                        offsets = Arrays.copyOf(offsets, capacity * 3);
                    }
                    pos.set(sx * 16 + x, sy * 16 + y, sz * 16 + z);
                    boolean sky = level.canSeeSky(pos);
                    Vec3 offset = state.getOffset(level, pos);
                    packed[count] = x | y << 4 | z << 8 | (sky ? 1 << 12 : 0);
                    light[count] = LevelRenderer.getLightColor(level, pos);
                    offsets[count * 3] = (float) offset.x;
                    offsets[count * 3 + 1] = (float) offset.y;
                    offsets[count * 3 + 2] = (float) offset.z;
                    exposed |= sky;
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    count++;
                }
            }
        }
        if (count == 0) {
            discardSection(key);
            return;
        }
        GrassSection section = SECTIONS.get(key);
        if (section == null) {
            section = new GrassSection(key, sx, sy, sz);
            SECTIONS.put(key, section);
        }
        section.count = count;
        section.packed = packed;
        section.light = light;
        section.offsets = offsets;
        section.exposed = exposed;
        section.bounds = new AABB(sx * 16 - 2, sy * 16 + minY - 1, sz * 16 - 2, sx * 16 + 18, sy * 16 + maxY + 3, sz * 16 + 18);
        section.targetLod = lodFor(section.meshLod, section.distanceSqr(lastEye));
        REMESH.add(key);
    }

    private static void updateLods() {
        for (GrassSection section : SECTIONS.values()) {
            section.targetLod = lodFor(section.meshLod, section.distanceSqr(lastEye));
            if (section.targetLod != section.meshLod || section.meshEpoch != epoch) REMESH.add(section.key);
        }
    }

    private static int lodFor(int current, double distanceSqr) {
        double distance = Math.sqrt(distanceSqr);
        if (!gpuMode) return distance < (current == LOD_STATIC ? 120.0D : 112.0D) ? LOD_STATIC : LOD_FAR;
        if (distance < (current == 0 ? 56.0D : 48.0D)) return 0;
        return distance < (current == 0 || current == 1 ? 120.0D : 112.0D) ? 1 : LOD_FAR;
    }

    private static void processMeshes() {
        if (sprite == null || REMESH.isEmpty()) return;
        long deadline = System.nanoTime() + MESH_BUDGET_NANOS;
        while (!REMESH.isEmpty() && System.nanoTime() < deadline) {
            GrassSection nearest = null;
            double best = Double.MAX_VALUE;
            LongIterator keys = REMESH.iterator();
            while (keys.hasNext()) {
                GrassSection section = SECTIONS.get(keys.nextLong());
                if (section == null) {
                    keys.remove();
                    continue;
                }
                double distance = section.distanceSqr(lastEye);
                if (distance < best) {
                    best = distance;
                    nearest = section;
                }
            }
            if (nearest == null) return;
            REMESH.remove(nearest.key);
            buildMesh(nearest);
        }
    }

    private static void buildMesh(GrassSection section) {
        if (meshBytes == null) meshBytes = new ByteBufferBuilder(1 << 18);
        int lod = section.targetLod;
        int bands = LOD_BANDS[lod];
        float[] heights = BAND_HEIGHT[bands];
        float[] flex = BAND_FLEX[bands];
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        boolean encoded = gpuMode;
        BufferBuilder builder = new BufferBuilder(meshBytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        int vertices = 0;
        for (int i = 0; i < section.count; i++) {
            int packed = section.packed[i];
            int lx = packed & 15;
            int ly = packed >> 4 & 15;
            int lz = packed >> 8 & 15;
            if (lod == LOD_FAR && thinned(section.sx * 16 + lx, section.sz * 16 + lz)) continue;
            int exposure = (packed & 1 << 12) != 0 ? 255 : 0;
            int tuftHash = Math.round(PrimordialGrassTrampleField.tuftHash(section.sx * 16 + lx, section.sz * 16 + lz) * 255.0F);
            float rootX = lx + 0.5F + section.offsets[i * 3];
            float rootY = ly + section.offsets[i * 3 + 1];
            float rootZ = lz + 0.5F + section.offsets[i * 3 + 2];
            int light = section.light[i];
            for (int face = 0; face < 2; face++) {
                float hx = FACE_X[face];
                float hz = FACE_Z[face];
                for (int band = 0; band < bands; band++) {
                    float bottom = heights[band];
                    float top = heights[band + 1];
                    float y0 = rootY + bottom * PrimordialGrassTrampleField.GRASS_HEIGHT;
                    float y1 = rootY + top * PrimordialGrassTrampleField.GRASS_HEIGHT;
                    float v0 = sprite.getV(1.0F - bottom);
                    float v1 = sprite.getV(1.0F - top);
                    bakedVertex(builder, encoded, rootX - hx, y0, rootZ - hz, u0, v0, light, -hx, -hz, bottom, flex[band], exposure, tuftHash);
                    bakedVertex(builder, encoded, rootX + hx, y0, rootZ + hz, u1, v0, light, hx, hz, bottom, flex[band], exposure, tuftHash);
                    bakedVertex(builder, encoded, rootX + hx, y1, rootZ + hz, u1, v1, light, hx, hz, top, flex[band + 1], exposure, tuftHash);
                    bakedVertex(builder, encoded, rootX - hx, y1, rootZ - hz, u0, v1, light, -hx, -hz, top, flex[band + 1], exposure, tuftHash);
                    vertices += 4;
                }
            }
        }
        section.meshLod = lod;
        section.meshEpoch = epoch;
        section.vertices = vertices;
        MeshData mesh = builder.build();
        if (mesh == null) {
            section.closeBuffer();
            return;
        }
        if (section.buffer == null) section.buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        section.buffer.bind();
        section.buffer.upload(mesh);
        VertexBuffer.unbind();
    }

    private static void bakedVertex(BufferBuilder builder, boolean encoded, float x, float y, float z, float u, float v, int light,
                                    float normalX, float normalZ, float height, float flex, int exposure, int tuftHash) {
        builder.addVertex(x, y, z);
        if (encoded) {
            builder.setColor(Math.round(height * 255.0F), Math.round(flex * 255.0F), exposure, tuftHash);
            builder.setUv(u, v);
            builder.setLight(light);
            builder.setNormal(normalX, 0.0F, normalZ);
        } else {
            builder.setColor(255, 255, 255, 255);
            builder.setUv(u, v);
            builder.setLight(light);
            builder.setNormal(0.0F, 1.0F, 0.0F);
        }
    }

    private static boolean thinned(int x, int z) {
        int hash = x * 73856093 ^ z * 19349663;
        hash ^= hash >>> 13;
        return Math.floorMod(hash, 3) == 0;
    }

    private static boolean grassWithin(Vec3 center, double reach) {
        double limit = reach * reach;
        for (GrassSection section : SECTIONS.values()) {
            if (section.distanceSqr(center) < limit) return true;
        }
        return false;
    }

    private static boolean exposedGrassNear(Vec3 center, double reach) {
        double limit = reach * reach;
        int sx = SectionPos.blockToSectionCoord(center.x);
        int sy = SectionPos.blockToSectionCoord(center.y);
        int sz = SectionPos.blockToSectionCoord(center.z);
        for (int x = sx - 2; x <= sx + 2; x++) {
            for (int y = sy - 2; y <= sy + 2; y++) {
                for (int z = sz - 2; z <= sz + 2; z++) {
                    GrassSection section = SECTIONS.get(SectionPos.asLong(x, y, z));
                    if (section == null || !section.exposed || section.distanceSqr(center) >= limit) continue;
                    for (int i = 0; i < section.count; i++) {
                        int packed = section.packed[i];
                        if ((packed & 1 << 12) == 0) continue;
                        double dx = section.sx * 16 + (packed & 15) + 0.5D - center.x;
                        double dy = section.sy * 16 + (packed >> 4 & 15) - center.y;
                        double dz = section.sz * 16 + (packed >> 8 & 15) + 0.5D - center.z;
                        if (dx * dx + dy * dy + dz * dz < limit) return true;
                    }
                }
            }
        }
        return false;
    }

    public static void render(MultiBufferSource.BufferSource buffers, Camera camera, Frustum frustum, float partialTick) {
        drawnSections = 0;
        drawnVertices = 0;
        Minecraft minecraft = Minecraft.getInstance();
        tick(minecraft);
        if (minecraft.level == null) return;
        refreshMode(minecraft);
        if (SECTIONS.isEmpty()) return;
        Vec3 eye = camera.getPosition();
        lastEye = eye;
        boolean windy = AntarchySettings.elythiaGrassWindEnabled();
        double time = minecraft.level.getGameTime() + partialTick;
        double drawDistance = AntarchySettings.primordialGrassDrawDistance();
        double drawLimit = drawDistance * drawDistance;
        int trample = PrimordialGrassTrample.write(eye.x, eye.y, eye.z, partialTick, TRAMPLE_POSITIONS, TRAMPLE_MOTIONS);
        if (gpuMode) renderGpu(eye, frustum, time, windy, trample, drawLimit);
        else renderFallback(buffers, minecraft, eye, frustum, time, windy, trample, drawLimit);
    }

    private static void refreshMode(Minecraft minecraft) {
        TextureAtlasSprite current = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        boolean gpu = PrimordialGrassGpuPass.available();
        if (current != sprite || gpu != gpuMode) {
            sprite = current;
            gpuMode = gpu;
            epoch++;
            lodDirty = true;
        }
    }

    private static void renderGpu(Vec3 eye, Frustum frustum, double time, boolean windy, int trample, double drawLimit) {
        PrimordialGrassWind.ShaderFrame frame = PrimordialGrassWind.shaderFrame(time, eye.x, eye.z, GUST_TABLE);
        GRASS_TYPE.setupRenderState();
        PrimordialGrassGpuPass.begin(frame, GUST_TABLE, windy, TRAMPLE_POSITIONS, TRAMPLE_MOTIONS, trample);
        for (GrassSection section : SECTIONS.values()) {
            if (!section.drawable() || section.distanceSqr(eye) > drawLimit || !frustum.isVisible(section.bounds)) continue;
            PrimordialGrassGpuPass.draw(section.buffer, (float) (section.sx * 16 - eye.x), (float) (section.sy * 16 - eye.y), (float) (section.sz * 16 - eye.z));
            drawnSections++;
            drawnVertices += section.vertices;
        }
        PrimordialGrassGpuPass.end();
        GRASS_TYPE.clearRenderState();
    }

    private static void renderFallback(MultiBufferSource.BufferSource buffers, Minecraft minecraft, Vec3 eye, Frustum frustum,
                                       double time, boolean windy, int trample, double drawLimit) {
        NEAR.clear();
        GRASS_TYPE.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            shader.setDefaultUniforms(VertexFormat.Mode.QUADS, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), minecraft.getWindow());
            shader.apply();
            Uniform chunkOffset = shader.CHUNK_OFFSET;
            for (GrassSection section : SECTIONS.values()) {
                double distance = section.distanceSqr(eye);
                if (distance > drawLimit || !frustum.isVisible(section.bounds)) continue;
                if (distance < CPU_RADIUS_SQR) {
                    NEAR.add(section);
                    continue;
                }
                if (!section.drawable()) continue;
                if (chunkOffset != null) {
                    chunkOffset.set((float) (section.sx * 16 - eye.x), (float) (section.sy * 16 - eye.y), (float) (section.sz * 16 - eye.z));
                    chunkOffset.upload();
                }
                section.buffer.bind();
                section.buffer.draw();
                drawnSections++;
                drawnVertices += section.vertices;
            }
            if (chunkOffset != null) chunkOffset.set(0.0F, 0.0F, 0.0F);
            shader.clear();
        }
        VertexBuffer.unbind();
        GRASS_TYPE.clearRenderState();
        if (NEAR.isEmpty()) return;
        VertexConsumer consumer = buffers.getBuffer(GRASS_TYPE);
        for (GrassSection section : NEAR) {
            renderDynamic(consumer, section, eye, time, windy, trample);
            drawnSections++;
        }
        buffers.endBatch(GRASS_TYPE);
    }

    private static void renderDynamic(VertexConsumer out, GrassSection section, Vec3 eye, double time, boolean windy, int trample) {
        float[] heights = BAND_HEIGHT[CPU_BANDS];
        float[] flex = BAND_FLEX[CPU_BANDS];
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        double calmPhase = time * Math.PI * 2.0D / PrimordialGrassWind.CALM_PERIOD;
        for (int i = 0; i < section.count; i++) {
            int packed = section.packed[i];
            int worldX = section.sx * 16 + (packed & 15);
            int worldY = section.sy * 16 + (packed >> 4 & 15);
            int worldZ = section.sz * 16 + (packed >> 8 & 15);
            float exposure = (packed & 1 << 12) != 0 ? 1.0F : 0.0F;
            float x = (float) (worldX + 0.5D + section.offsets[i * 3] - eye.x);
            float y = (float) (worldY + section.offsets[i * 3 + 1] - eye.y);
            float z = (float) (worldZ + 0.5D + section.offsets[i * 3 + 2] - eye.z);
            float pushX = 0.0F;
            float pushZ = 0.0F;
            float lean = 0.0F;
            float variation = 1.0F;
            float flutter = 0.0F;
            if (trample > 0) {
                PrimordialGrassTrampleField.evaluate(TRAMPLE_POSITIONS, TRAMPLE_MOTIONS, trample, x, y, z, TRAMPLE_SAMPLE);
                pushX = TRAMPLE_SAMPLE[0];
                pushZ = TRAMPLE_SAMPLE[1];
                lean = TRAMPLE_SAMPLE[2];
                if (lean > 0.0F) {
                    float tuftHash = PrimordialGrassTrampleField.tuftHash(worldX, worldZ);
                    variation = PrimordialGrassTrampleField.variation(tuftHash);
                    flutter = (float) Math.sin(calmPhase * 3.0D + tuftHash * Math.PI * 2.0D) * PrimordialGrassTrampleField.FLUTTER;
                }
            }
            float windScale = 1.0F - PrimordialGrassTrampleField.WIND_DAMPING * lean;
            PrimordialGrassWind.Sample wind = windy ? PrimordialGrassWind.sample(time, worldX, worldZ) : null;
            for (int level = 0; level <= CPU_BANDS; level++) {
                float h = heights[level];
                float f = flex[level];
                float sway = wind == null ? 0.0F : (float) (wind.calm() * h * h + wind.gust() * f * exposure * 0.34D) * windScale;
                float bend = PrimordialGrassTrampleField.bend(h);
                float side = PrimordialGrassTrampleField.LEAN * variation * bend;
                float twist = flutter * bend;
                LEVEL_DX[level] = (wind == null ? 0.0F : (float) wind.directionX() * sway) + pushX * side - pushZ * twist;
                LEVEL_DZ[level] = (wind == null ? 0.0F : (float) wind.directionZ() * sway) + pushZ * side + pushX * twist;
                LEVEL_DY[level] = h * PrimordialGrassTrampleField.GRASS_HEIGHT * (1.0F - h * PrimordialGrassTrampleField.DROP * lean * lean);
            }
            int light = section.light[i];
            for (int face = 0; face < 2; face++) {
                float hx = FACE_X[face];
                float hz = FACE_Z[face];
                for (int band = 0; band < CPU_BANDS; band++) {
                    float v0 = sprite.getV(1.0F - heights[band]);
                    float v1 = sprite.getV(1.0F - heights[band + 1]);
                    float y0 = y + LEVEL_DY[band];
                    float y1 = y + LEVEL_DY[band + 1];
                    dynamicVertex(out, x - hx + LEVEL_DX[band], y0, z - hz + LEVEL_DZ[band], u0, v0, light);
                    dynamicVertex(out, x + hx + LEVEL_DX[band], y0, z + hz + LEVEL_DZ[band], u1, v0, light);
                    dynamicVertex(out, x + hx + LEVEL_DX[band + 1], y1, z + hz + LEVEL_DZ[band + 1], u1, v1, light);
                    dynamicVertex(out, x - hx + LEVEL_DX[band + 1], y1, z - hz + LEVEL_DZ[band + 1], u0, v1, light);
                    drawnVertices += 4;
                }
            }
        }
    }

    private static void dynamicVertex(VertexConsumer out, float x, float y, float z, float u, float v, int light) {
        out.addVertex(x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static final class GrassSection {
        private final long key;
        private final int sx;
        private final int sy;
        private final int sz;
        private int count;
        private int[] packed;
        private int[] light;
        private float[] offsets;
        private boolean exposed;
        private AABB bounds;
        private VertexBuffer buffer;
        private int meshLod = -1;
        private int targetLod;
        private int meshEpoch = -1;
        private int vertices;

        private GrassSection(long key, int sx, int sy, int sz) {
            this.key = key;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        private boolean drawable() {
            return buffer != null && meshEpoch == epoch;
        }

        private double distanceSqr(Vec3 point) {
            double dx = Math.max(Math.max(bounds.minX - point.x, point.x - bounds.maxX), 0.0D);
            double dy = Math.max(Math.max(bounds.minY - point.y, point.y - bounds.maxY), 0.0D);
            double dz = Math.max(Math.max(bounds.minZ - point.z, point.z - bounds.maxZ), 0.0D);
            return dx * dx + dy * dy + dz * dz;
        }

        private void closeBuffer() {
            if (buffer != null) {
                buffer.close();
                buffer = null;
            }
        }

        private void close() {
            closeBuffer();
        }
    }
}
