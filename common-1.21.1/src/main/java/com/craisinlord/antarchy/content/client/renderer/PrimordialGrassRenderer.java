package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.PrimordialGrassBlock;
import com.craisinlord.antarchy.content.client.PrimordialGrassTrample;
import com.craisinlord.antarchy.content.client.PrimordialGrassTrampleField;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.craisinlord.antarchy.content.client.PrimordialGrassWindLines;
import com.craisinlord.antarchy.content.client.PrimordialWindSound;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public final class PrimordialGrassRenderer {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath("antarchy", "block/primordial_grass"),
            ResourceLocation.fromNamespaceAndPath("antarchy", "block/primordial_grass_short"),
            ResourceLocation.fromNamespaceAndPath("antarchy", "block/primordial_grass_tall")
    };
    private static final int SECTION_BLOCKS = 4096;
    private static final int EXPOSED_BIT = 1 << 12;
    private static final int VARIANT_SHIFT = 13;
    private static final int VARIANT_MASK = 3;
    private static final int HASH_SHIFT = PrimordialGrassLod.HASH_SHIFT;
    private static final double DYNAMIC_HYSTERESIS = 8.0D;
    private static final double URGENT_NEAR_SQR = 48.0D * 48.0D;
    private static final double BEHIND_PENALTY = 4.0D;
    private static final long SWEEP_BUDGET_NANOS = 1_000_000L;
    private static final int SWEEP_CHECKS = 64;
    private static final double TIME_SMOOTHING = 0.05D;
    private static final PrimordialGrassFrameBudget FRAME_BUDGET = new PrimordialGrassFrameBudget();

    private static final Long2ObjectOpenHashMap<GrassSection> SECTIONS = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<LevelChunk> SEEN_CHUNKS = new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet DIRTY_CHUNKS = new LongOpenHashSet();
    private static final LongOpenHashSet DIRTY_SECTIONS = new LongOpenHashSet();
    private static final LongOpenHashSet DYNAMIC = new LongOpenHashSet();
    private static final LongOpenHashSet DYNAMIC_REBUILDS = new LongOpenHashSet();
    private static final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[TEXTURES.length];
    private static final RandomSource MODEL_RANDOM = RandomSource.create();
    private static final List<GrassSection> DRAW_LIST = new ArrayList<>();
    private static final int RESCAN_BATCH = 32;
    private static final long[] RESCAN_KEYS = new long[RESCAN_BATCH];
    private static final double[] RESCAN_SCORES = new double[RESCAN_BATCH];
    private static final Comparator<GrassSection> BY_DISTANCE = Comparator.comparingDouble(section -> section.sortDistance);
    private static final float[] GUST_TABLE = new float[PrimordialGrassWind.GUST_TABLE_ENTRIES * 2];
    private static final float[] TRAMPLE_POSITIONS = new float[PrimordialGrassTrample.MAX_POINTS * 4];
    private static final float[] TRAMPLE_MOTIONS = new float[PrimordialGrassTrample.MAX_POINTS * 3];
    private static final int[] SCAN_PACKED = new int[SECTION_BLOCKS];
    private static final int[] SCAN_LIGHT = new int[SECTION_BLOCKS];
    private static final float[] SCAN_OFFSETS = new float[SECTION_BLOCKS * 3];

    private static ClientLevel trackedLevel;
    private static ByteBuffer instanceBytes;
    private static boolean spritesReady;
    private static boolean staticMode;
    private static boolean dynamicChanged;
    private static int[] sweepOffsets = new int[0];
    private static int centerX = Integer.MIN_VALUE;
    private static int centerZ;
    private static int radius = -1;
    private static int cursor;
    private static long tick = Long.MIN_VALUE;
    private static long eyeSection = Long.MIN_VALUE;
    private static Vec3 lastEye = Vec3.ZERO;
    private static double lookX;
    private static double lookZ;
    private static boolean lodDirty;
    private static int drawnSections;
    private static long drawnVertices;
    private static int lastDrawDistance = -1;
    private static int lastQuality = -1;
    private static int occludedSections;
    private static ObjectArrayList<SectionRenderDispatcher.RenderSection> vanillaVisible;
    private static boolean visibilityDirty;
    private static boolean vanillaListKnown;
    private static int visibleStamp;
    private static boolean sodiumChecked;
    private static boolean sodiumPresent;
    private static String staticReason;
    private static long workNanos;
    private static double frameNanos;
    private static double tickNanos;

    private PrimordialGrassRenderer() {
    }

    public static void blockChanged(BlockPos pos) {
        if (trackedLevel != null && staticReason == null) DIRTY_SECTIONS.add(SectionPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4));
    }

    public static void lightChanged(int sectionX, int sectionY, int sectionZ) {
        long key = SectionPos.asLong(sectionX, sectionY, sectionZ);
        if (SECTIONS.containsKey(key)) DIRTY_SECTIONS.add(key);
    }

    public static void chunkLoaded(ChunkPos pos) {
        if (trackedLevel != null && staticReason == null) DIRTY_CHUNKS.add(pos.toLong());
    }

    public static void visibilityChanged(ObjectArrayList<SectionRenderDispatcher.RenderSection> visible) {
        vanillaVisible = visible;
        visibilityDirty = true;
    }

    public static void addDebugLines(List<String> lines) {
        if (trackedLevel == null) {
            return;
        }
        if (staticReason != null) {
            lines.add("Primordial grass: static (" + staticReason + ")");
            return;
        }
        if (SECTIONS.isEmpty()) {
            return;
        }
        int tufts = 0;
        for (GrassSection section : SECTIONS.values()) tufts += section.count;
        lines.add("Primordial grass: " + SECTIONS.size() + " sections, " + DYNAMIC.size() + " dynamic, " + tufts + " tufts, "
                + drawnSections + " drawn, " + occludedSections + " occluded, " + drawnVertices + " verts");
        lines.add(String.format(Locale.ROOT, "Grass time: cpu %.2f ms/frame, gpu %.2f ms, tick %.2f ms, work %.2f ms, %d queued",
                frameNanos / 1_000_000.0D, PrimordialGrassGpuPass.gpuMillis(), tickNanos / 1_000_000.0D, workNanos / 1_000_000.0D,
                DIRTY_CHUNKS.size() + DIRTY_SECTIONS.size()));
    }

    public static void tick(Minecraft minecraft) {
        long started = System.nanoTime();
        ClientLevel level = minecraft.level;
        if (level != trackedLevel) resetLevel(minecraft, level);
        refreshMode(minecraft);
        if (staticReason != null || level == null || minecraft.player == null || minecraft.isPaused() || tick == level.getGameTime()) {
            return;
        }
        tick = level.getGameTime();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        lastEye = camera.isInitialized() ? camera.getPosition() : minecraft.player.position();
        int cx = minecraft.player.blockPosition().getX() >> 4;
        int cz = minecraft.player.blockPosition().getZ() >> 4;
        int drawDistance = AntarchySettings.primordialGrassDrawDistance();
        int reach = Math.min(minecraft.options.getEffectiveRenderDistance() + 1, (int) Math.ceil((drawDistance + DYNAMIC_HYSTERESIS) / 16.0D) + 1);
        if (cx != centerX || cz != centerZ || reach != radius) moveCenter(level, cx, cz, reach);
        long currentEyeSection = SectionPos.asLong(BlockPos.containing(lastEye));
        int quality = AntarchySettings.primordialGrassQuality();
        if (lodDirty || currentEyeSection != eyeSection || drawDistance != lastDrawDistance || quality != lastQuality) {
            eyeSection = currentEyeSection;
            lastDrawDistance = drawDistance;
            lastQuality = quality;
            lodDirty = false;
            updateLods();
        }
        sweep(level);
        publishDynamic(minecraft);
        Vec3 player = minecraft.player.position();
        if (grassWithin(player, PrimordialGrassTrampleField.RANGE)) PrimordialGrassTrample.tick(level, player);
        else PrimordialGrassTrample.reset();
        PrimordialWindSound.tick(minecraft, exposedGrassNear(player, 24.0D));
        PrimordialGrassWindLines.tick(minecraft, level, player);
        tickNanos += (System.nanoTime() - started - tickNanos) * TIME_SMOOTHING;
    }

    public static boolean sampleExposedTuft(RandomSource random, Vec3 center, double reach, double[] out) {
        int sectionX = SectionPos.blockToSectionCoord(center.x) + random.nextInt(5) - 2;
        int sectionY = SectionPos.blockToSectionCoord(center.y) + random.nextInt(3) - 1;
        int sectionZ = SectionPos.blockToSectionCoord(center.z) + random.nextInt(5) - 2;
        GrassSection section = SECTIONS.get(SectionPos.asLong(sectionX, sectionY, sectionZ));
        if (section == null || !section.exposed || section.count == 0) {
            return false;
        }
        int packed = section.packed[random.nextInt(section.count)];
        if ((packed & EXPOSED_BIT) == 0) {
            return false;
        }
        double x = section.sx * 16 + (packed & 15) + 0.5D;
        double y = section.sy * 16 + (packed >> 4 & 15);
        double z = section.sz * 16 + (packed >> 8 & 15) + 0.5D;
        double dx = x - center.x;
        double dy = y - center.y;
        double dz = z - center.z;
        if (dx * dx + dy * dy + dz * dz > reach * reach) {
            return false;
        }
        out[0] = x;
        out[1] = y;
        out[2] = z;
        return true;
    }

    private static void resetLevel(Minecraft minecraft, ClientLevel level) {
        clearSections(minecraft);
        trackedLevel = level;
        vanillaVisible = null;
        visibilityDirty = false;
        vanillaListKnown = false;
    }

    private static void clearSections(Minecraft minecraft) {
        for (GrassSection section : SECTIONS.values()) section.close();
        SECTIONS.clear();
        SEEN_CHUNKS.clear();
        DIRTY_CHUNKS.clear();
        DIRTY_SECTIONS.clear();
        DYNAMIC.clear();
        DYNAMIC_REBUILDS.clear();
        dynamicChanged = false;
        PrimordialGrassBlock.setDynamicSections(LongSets.EMPTY_SET);
        DRAW_LIST.clear();
        occludedSections = 0;
        drawnSections = 0;
        drawnVertices = 0;
        centerX = Integer.MIN_VALUE;
        radius = -1;
        tick = Long.MIN_VALUE;
        eyeSection = Long.MIN_VALUE;
        PrimordialGrassTrample.reset();
        PrimordialWindSound.reset(minecraft);
    }

    private static void refreshMode(Minecraft minecraft) {
        String reason = !AntarchySettings.primordialGrassDynamic() ? "setting"
                : PrimordialGrassGpuPass.shaderPackActive() ? "shader pack"
                : !PrimordialGrassGpuPass.available() ? "unsupported" : null;
        staticReason = reason;
        boolean nowStatic = reason != null;
        if (nowStatic != staticMode) {
            staticMode = nowStatic;
            dropDynamic(minecraft);
            clearSections(minecraft);
        }
        if (nowStatic || minecraft.level == null) {
            return;
        }
        refreshSprites(minecraft);
    }

    private static void refreshSprites(Minecraft minecraft) {
        Function<ResourceLocation, TextureAtlasSprite> atlas = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        boolean changed = !spritesReady;
        for (int i = 0; i < TEXTURES.length; i++) {
            TextureAtlasSprite current = atlas.apply(TEXTURES[i]);
            if (current != SPRITES[i]) {
                SPRITES[i] = current;
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        spritesReady = true;
        PrimordialGrassGpuPass.setVariants(SPRITES);
        DIRTY_SECTIONS.addAll(SECTIONS.keySet());
    }

    private static void setDynamic(GrassSection section, boolean dynamic) {
        if (section.dynamic == dynamic) {
            return;
        }
        section.dynamic = dynamic;
        if (dynamic) DYNAMIC.add(section.key);
        else DYNAMIC.remove(section.key);
        DYNAMIC_REBUILDS.add(section.key);
        dynamicChanged = true;
    }

    private static void publishDynamic(Minecraft minecraft) {
        if (!dynamicChanged) {
            return;
        }
        dynamicChanged = false;
        PrimordialGrassBlock.setDynamicSections(DYNAMIC.isEmpty() ? LongSets.EMPTY_SET : new LongOpenHashSet(DYNAMIC));
        LongIterator keys = DYNAMIC_REBUILDS.iterator();
        while (keys.hasNext()) {
            long key = keys.nextLong();
            minecraft.levelRenderer.setSectionDirty(SectionPos.x(key), SectionPos.y(key), SectionPos.z(key));
        }
        DYNAMIC_REBUILDS.clear();
    }

    private static void dropDynamic(Minecraft minecraft) {
        PrimordialGrassBlock.setDynamicSections(LongSets.EMPTY_SET);
        LongIterator keys = DYNAMIC.iterator();
        while (keys.hasNext()) {
            long key = keys.nextLong();
            minecraft.levelRenderer.setSectionDirty(SectionPos.x(key), SectionPos.y(key), SectionPos.z(key));
        }
        DYNAMIC.clear();
        DYNAMIC_REBUILDS.clear();
        dynamicChanged = false;
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

    private static void processFrameWork(ClientLevel level) {
        long start = System.nanoTime();
        workNanos = 0L;
        boolean queued = radius >= 0 && !(DIRTY_CHUNKS.isEmpty() && DIRTY_SECTIONS.isEmpty());
        FRAME_BUDGET.refill(start, queued && urgentWorkQueued());
        if (!queued) {
            return;
        }
        long available = FRAME_BUDGET.available();
        if (available <= 0L) {
            return;
        }
        processRescans(level, start + available);
        workNanos = System.nanoTime() - start;
        FRAME_BUDGET.spend(workNanos);
    }

    private static boolean urgentWorkQueued() {
        LongIterator chunks = DIRTY_CHUNKS.iterator();
        while (chunks.hasNext()) {
            long key = chunks.nextLong();
            if (urgent(ChunkPos.getX(key), ChunkPos.getZ(key))) {
                return true;
            }
        }
        LongIterator sections = DIRTY_SECTIONS.iterator();
        while (sections.hasNext()) {
            long key = sections.nextLong();
            if (urgent(SectionPos.x(key), SectionPos.z(key))) {
                return true;
            }
        }
        return false;
    }

    private static boolean urgent(int cx, int cz) {
        double dx = cx * 16 + 8 - lastEye.x;
        double dz = cz * 16 + 8 - lastEye.z;
        double distance = dx * dx + dz * dz;
        if (distance < URGENT_NEAR_SQR) {
            return true;
        }
        double limit = lastDrawDistance + DYNAMIC_HYSTERESIS;
        return dx * lookX + dz * lookZ >= 0.0D && distance < limit * limit;
    }

    private static double workScore(int cx, int cz) {
        double dx = cx * 16 + 8 - lastEye.x;
        double dz = cz * 16 + 8 - lastEye.z;
        double distance = dx * dx + dz * dz;
        return dx * lookX + dz * lookZ >= 0.0D || distance < URGENT_NEAR_SQR ? distance : distance * BEHIND_PENALTY;
    }

    private static void processRescans(ClientLevel level, long deadline) {
        while (!DIRTY_CHUNKS.isEmpty() && System.nanoTime() < deadline) {
            int count = bestDirty(DIRTY_CHUNKS, true);
            for (int i = 0; i < count && System.nanoTime() < deadline; i++) {
                long key = RESCAN_KEYS[i];
                DIRTY_CHUNKS.remove(key);
                int cx = ChunkPos.getX(key);
                int cz = ChunkPos.getZ(key);
                if (Math.abs(cx - centerX) > radius || Math.abs(cz - centerZ) > radius) {
                    continue;
                }
                scanChunk(level, cx, cz);
            }
        }
        while (!DIRTY_SECTIONS.isEmpty() && System.nanoTime() < deadline) {
            int count = bestDirty(DIRTY_SECTIONS, false);
            for (int i = 0; i < count && System.nanoTime() < deadline; i++) {
                long key = RESCAN_KEYS[i];
                DIRTY_SECTIONS.remove(key);
                int sx = SectionPos.x(key);
                int sz = SectionPos.z(key);
                if (Math.abs(sx - centerX) > radius || Math.abs(sz - centerZ) > radius) {
                    continue;
                }
                LevelChunk chunk = level.getChunkSource().getChunk(sx, sz, ChunkStatus.FULL, false);
                if (chunk != null) scanSection(level, chunk, sx, SectionPos.y(key), sz);
                else discardSection(key);
            }
        }
    }

    private static int bestDirty(LongOpenHashSet dirty, boolean chunks) {
        int count = 0;
        LongIterator keys = dirty.iterator();
        while (keys.hasNext()) {
            long key = keys.nextLong();
            double score = chunks
                    ? workScore(ChunkPos.getX(key), ChunkPos.getZ(key))
                    : workScore(SectionPos.x(key), SectionPos.z(key));
            if (count == RESCAN_BATCH && score >= RESCAN_SCORES[RESCAN_BATCH - 1]) {
                continue;
            }
            int slot = count < RESCAN_BATCH ? count++ : RESCAN_BATCH - 1;
            while (slot > 0 && RESCAN_SCORES[slot - 1] > score) {
                RESCAN_SCORES[slot] = RESCAN_SCORES[slot - 1];
                RESCAN_KEYS[slot] = RESCAN_KEYS[slot - 1];
                slot--;
            }
            RESCAN_SCORES[slot] = score;
            RESCAN_KEYS[slot] = key;
        }
        return count;
    }

    private static void sweep(ClientLevel level) {
        if (sweepOffsets.length == 0) {
            return;
        }
        long deadline = System.nanoTime() + SWEEP_BUDGET_NANOS;
        int entries = sweepOffsets.length / 2;
        for (int i = 0; i < SWEEP_CHECKS && System.nanoTime() < deadline; i++) {
            int entry = cursor++ % entries;
            int cx = centerX + sweepOffsets[entry * 2];
            int cz = centerZ + sweepOffsets[entry * 2 + 1];
            long key = ChunkPos.asLong(cx, cz);
            if (DIRTY_CHUNKS.contains(key)) {
                continue;
            }
            LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
            LevelChunk known = SEEN_CHUNKS.get(key);
            if (chunk == known) {
                continue;
            }
            if (chunk == null) {
                SEEN_CHUNKS.remove(key);
                removeChunkSections(level, cx, cz);
            } else DIRTY_CHUNKS.add(key);
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
        if (section != null) {
            setDynamic(section, false);
            section.close();
            if (section.listed) {
                DRAW_LIST.remove(section);
                section.listed = false;
            }
            lodDirty = true;
        }
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
        int count = 0;
        int minY = 16;
        int maxY = -1;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    if (!(chunkSection.getBlockState(x, y, z).getBlock() instanceof PrimordialGrassBlock)) {
                        continue;
                    }
                    int hash = Math.round(PrimordialGrassTrampleField.tuftHash(sx * 16 + x, sz * 16 + z) * 255.0F);
                    SCAN_PACKED[count++] = x | y << 4 | z << 8 | hash << HASH_SHIFT;
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (count == 0) {
            discardSection(key);
            return;
        }
        Arrays.sort(SCAN_PACKED, 0, count);
        boolean exposed = false;
        BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < count; i++) {
            int packed = SCAN_PACKED[i];
            int x = packed & 15;
            int y = packed >> 4 & 15;
            int z = packed >> 8 & 15;
            pos.set(sx * 16 + x, sy * 16 + y, sz * 16 + z);
            BlockState state = chunkSection.getBlockState(x, y, z);
            if (level.canSeeSky(pos)) {
                packed |= EXPOSED_BIT;
                exposed = true;
            }
            SCAN_PACKED[i] = packed | variantOf(blocks, state, pos) << VARIANT_SHIFT;
            Vec3 offset = state.getOffset(level, pos);
            SCAN_LIGHT[i] = LevelRenderer.getLightColor(level, pos);
            SCAN_OFFSETS[i * 3] = (float) offset.x;
            SCAN_OFFSETS[i * 3 + 1] = (float) offset.y;
            SCAN_OFFSETS[i * 3 + 2] = (float) offset.z;
        }
        GrassSection section = SECTIONS.get(key);
        if (section != null && section.matches(count)) {
            return;
        }
        if (section == null) {
            section = new GrassSection(key, sx, sy, sz);
            SECTIONS.put(key, section);
            visibilityDirty = true;
        }
        section.count = count;
        section.packed = Arrays.copyOf(SCAN_PACKED, count);
        section.light = Arrays.copyOf(SCAN_LIGHT, count);
        section.offsets = Arrays.copyOf(SCAN_OFFSETS, count * 3);
        section.exposed = exposed;
        section.bounds = new AABB(sx * 16 - 2, sy * 16 + minY - 1, sz * 16 - 2, sx * 16 + 18, sy * 16 + maxY + 3, sz * 16 + 18);
        lodDirty = true;
        double distanceSqr = section.distanceSqr(lastEye);
        if (withinDynamic(section, distanceSqr)) {
            setDynamic(section, true);
            assignTier(section, distanceSqr);
            upload(section);
            if (!section.listed) {
                DRAW_LIST.add(section);
                section.listed = true;
            }
        } else {
            setDynamic(section, false);
            section.tier = -1;
            section.releaseBuffers();
        }
    }

    private static int variantOf(BlockRenderDispatcher blocks, BlockState state, BlockPos pos) {
        MODEL_RANDOM.setSeed(state.getSeed(pos));
        List<BakedQuad> quads = blocks.getBlockModel(state).getQuads(state, null, MODEL_RANDOM);
        if (quads.isEmpty()) {
            return 0;
        }
        TextureAtlasSprite used = quads.get(0).getSprite();
        for (int i = 0; i < SPRITES.length; i++) {
            if (SPRITES[i] == used) {
                return i;
            }
        }
        return 0;
    }

    private static boolean withinDynamic(GrassSection section, double distanceSqr) {
        double limit = AntarchySettings.primordialGrassDrawDistance() + (section.dynamic ? DYNAMIC_HYSTERESIS : 0.0D);
        return distanceSqr <= limit * limit;
    }

    private static void assignTier(GrassSection section, double distanceSqr) {
        PrimordialGrassLod.Tier[] tiers = PrimordialGrassLod.tiers(AntarchySettings.primordialGrassQuality());
        section.tier = PrimordialGrassLod.tierFor(tiers, section.tier, Math.sqrt(distanceSqr));
        PrimordialGrassLod.Tier tier = tiers[section.tier];
        section.first = tier.first();
        section.vertices = tier.vertices();
        section.instances = section.count;
    }

    private static void updateLods() {
        clearDrawList();
        for (GrassSection section : SECTIONS.values()) {
            double distanceSqr = section.distanceSqr(lastEye);
            if (!withinDynamic(section, distanceSqr)) {
                setDynamic(section, false);
                section.tier = -1;
                section.releaseBuffers();
                continue;
            }
            setDynamic(section, true);
            assignTier(section, distanceSqr);
            if (section.vao == 0) upload(section);
            section.sortDistance = distanceSqr;
            DRAW_LIST.add(section);
            section.listed = true;
        }
        DRAW_LIST.sort(BY_DISTANCE);
    }

    private static void clearDrawList() {
        for (int i = 0; i < DRAW_LIST.size(); i++) {
            DRAW_LIST.get(i).listed = false;
        }
        DRAW_LIST.clear();
    }

    private static void upload(GrassSection section) {
        if (instanceBytes == null) instanceBytes = BufferUtils.createByteBuffer(SECTION_BLOCKS * PrimordialGrassGpuPass.INSTANCE_BYTES);
        ByteBuffer data = instanceBytes;
        data.clear();
        for (int i = 0; i < section.count; i++) {
            int packed = section.packed[i];
            int light = section.light[i];
            data.putFloat((packed & 15) + 0.5F + section.offsets[i * 3]);
            data.putFloat((packed >> 4 & 15) + section.offsets[i * 3 + 1]);
            data.putFloat((packed >> 8 & 15) + 0.5F + section.offsets[i * 3 + 2]);
            data.put((byte) (((packed & EXPOSED_BIT) != 0 ? 128 : 0) | packed >> VARIANT_SHIFT & VARIANT_MASK));
            data.put((byte) (packed >>> HASH_SHIFT));
            data.put((byte) Math.min(255, light & 0xFFFF));
            data.put((byte) Math.min(255, light >>> 16 & 0xFFFF));
        }
        data.flip();
        if (section.vao == 0) {
            section.buffer = PrimordialGrassGpuPass.createInstanceBuffer();
            section.vao = PrimordialGrassGpuPass.createVertexArray(section.buffer);
        }
        PrimordialGrassGpuPass.uploadInstances(section.buffer, data);
    }

    private static boolean grassWithin(Vec3 center, double reach) {
        double limit = reach * reach;
        for (GrassSection section : SECTIONS.values()) {
            if (section.distanceSqr(center) < limit) {
                return true;
            }
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
                    if (section == null || !section.exposed || section.distanceSqr(center) >= limit) {
                        continue;
                    }
                    for (int i = 0; i < section.count; i++) {
                        int packed = section.packed[i];
                        if ((packed & EXPOSED_BIT) == 0) {
                            continue;
                        }
                        double dx = section.sx * 16 + (packed & 15) + 0.5D - center.x;
                        double dy = section.sy * 16 + (packed >> 4 & 15) - center.y;
                        double dz = section.sz * 16 + (packed >> 8 & 15) + 0.5D - center.z;
                        if (dx * dx + dy * dy + dz * dz < limit) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void render(Camera camera, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick) {
        drawnSections = 0;
        drawnVertices = 0;
        occludedSections = 0;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (staticReason != null || level == null || level != trackedLevel) {
            return;
        }
        long started = System.nanoTime();
        Vec3 eye = camera.getPosition();
        lastEye = eye;
        Vector3f look = camera.getLookVector();
        lookX = look.x();
        lookZ = look.z();
        processFrameWork(level);
        publishDynamic(minecraft);
        if (!DRAW_LIST.isEmpty() && spritesReady) {
            refreshVanillaVisibility();
            Frustum frustum = new Frustum(frustumMatrix, projectionMatrix);
            frustum.prepare(eye.x, eye.y, eye.z);
            drawSections(minecraft, eye, frustum, level.getGameTime() + partialTick, partialTick);
        }
        frameNanos += (System.nanoTime() - started - frameNanos) * TIME_SMOOTHING;
    }

    private static void drawSections(Minecraft minecraft, Vec3 eye, Frustum frustum, double time, float partialTick) {
        int trample = PrimordialGrassTrample.write(eye.x, eye.y, eye.z, partialTick, TRAMPLE_POSITIONS, TRAMPLE_MOTIONS);
        PrimordialGrassWind.ShaderFrame frame = PrimordialGrassWind.shaderFrame(time, eye.x, eye.z, GUST_TABLE);
        RenderType.cutout().setupRenderState();
        RenderSystem.disableCull();
        PrimordialGrassGpuPass.begin(frame, GUST_TABLE, AntarchySettings.elythiaGrassWindEnabled(), TRAMPLE_POSITIONS, TRAMPLE_MOTIONS, trample,
                minecraft.getDebugOverlay().showDebugScreen());
        for (int i = 0; i < DRAW_LIST.size(); i++) {
            GrassSection section = DRAW_LIST.get(i);
            if (!section.dynamic || section.vao == 0 || section.instances == 0 || culled(section, frustum)) {
                continue;
            }
            PrimordialGrassGpuPass.draw(section.vao, section.first, section.vertices, section.instances,
                    (float) (section.sx * 16 - eye.x), (float) (section.sy * 16 - eye.y), (float) (section.sz * 16 - eye.z));
            drawnSections++;
            drawnVertices += (long) section.vertices * section.instances;
        }
        PrimordialGrassGpuPass.end();
        RenderSystem.enableCull();
        RenderType.cutout().clearRenderState();
    }

    private static void refreshVanillaVisibility() {
        if (!sodiumChecked) {
            sodiumChecked = true;
            try {
                Class.forName("net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer", false, PrimordialGrassRenderer.class.getClassLoader());
                sodiumPresent = true;
            } catch (ClassNotFoundException | LinkageError exception) {
                sodiumPresent = false;
            }
        }
        if (!visibilityDirty || sodiumPresent || vanillaVisible == null) {
            return;
        }
        visibilityDirty = false;
        vanillaListKnown = !vanillaVisible.isEmpty();
        visibleStamp++;
        for (int i = 0; i < vanillaVisible.size(); i++) {
            GrassSection section = SECTIONS.get(SectionPos.asLong(vanillaVisible.get(i).getOrigin()));
            if (section != null) section.visibleStamp = visibleStamp;
        }
    }

    private static boolean culled(GrassSection section, Frustum frustum) {
        return !frustum.isVisible(section.bounds) || occluded(section);
    }

    private static boolean occluded(GrassSection section) {
        boolean hidden = sodiumPresent
                ? !PrimordialGrassSodiumVisibility.visible(section.sx, section.sy, section.sz)
                : vanillaListKnown && section.visibleStamp != visibleStamp;
        if (hidden) occludedSections++;
        return hidden;
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
        private int vao;
        private int buffer;
        private int tier = -1;
        private int first;
        private int vertices;
        private int instances;
        private int visibleStamp;
        private double sortDistance;
        private boolean listed;
        private boolean dynamic;

        private GrassSection(long key, int sx, int sy, int sz) {
            this.key = key;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }

        private boolean matches(int scanned) {
            return count == scanned
                    && Arrays.equals(packed, 0, scanned, SCAN_PACKED, 0, scanned)
                    && Arrays.equals(light, 0, scanned, SCAN_LIGHT, 0, scanned)
                    && Arrays.equals(offsets, 0, scanned * 3, SCAN_OFFSETS, 0, scanned * 3);
        }

        private double distanceSqr(Vec3 point) {
            double dx = Math.max(Math.max(bounds.minX - point.x, point.x - bounds.maxX), 0.0D);
            double dy = Math.max(Math.max(bounds.minY - point.y, point.y - bounds.maxY), 0.0D);
            double dz = Math.max(Math.max(bounds.minZ - point.z, point.z - bounds.maxZ), 0.0D);
            return dx * dx + dy * dy + dz * dz;
        }

        private void releaseBuffers() {
            if (vao != 0) {
                PrimordialGrassGpuPass.deleteSection(vao, buffer);
                vao = 0;
                buffer = 0;
            }
        }

        private void close() {
            releaseBuffers();
            count = 0;
        }
    }
}
