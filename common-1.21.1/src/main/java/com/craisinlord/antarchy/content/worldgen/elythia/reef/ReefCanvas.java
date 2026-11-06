package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

final class ReefCanvas {

    interface ColumnVisitor {
        void visit(int x, int z, double distance);
    }

    private static final byte EMPTY = 0;
    private static final byte BLOCKED = -1;
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final ThreadLocal<byte[]> OCCUPANCY = new ThreadLocal<>();

    private final WorldGenLevel level;
    private final ChunkGenerator generator;
    private final ChunkAccess chunk;
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final int minY;
    private final int maxY;
    private final int seaLevel;
    private final int[] floors = new int[256];
    private final int[] columnTops = new int[256];
    private final byte[] occupancy;
    private final List<ReefCoral> palette = new ArrayList<>();
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private ReefCoral lastCoral;
    private byte lastId;
    private int touchedMin = Integer.MAX_VALUE;
    private int touchedMax = Integer.MIN_VALUE;

    ReefCanvas(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunkPos) {
        this.level = level;
        this.generator = generator;
        this.chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY);
        this.minX = chunkPos.getMinBlockX();
        this.minZ = chunkPos.getMinBlockZ();
        this.maxX = chunkPos.getMaxBlockX();
        this.maxZ = chunkPos.getMaxBlockZ();
        this.minY = level.getMinBuildHeight();
        this.maxY = level.getMaxBuildHeight();
        this.seaLevel = generator.getSeaLevel();
        int volume = 256 * (this.maxY - this.minY);
        byte[] buffer = OCCUPANCY.get();
        if (buffer == null || buffer.length < volume) {
            buffer = new byte[volume];
            OCCUPANCY.set(buffer);
        }
        this.occupancy = buffer;
        Arrays.fill(this.columnTops, Integer.MIN_VALUE);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                this.floors[x * 16 + z] = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.minX + x, this.minZ + z);
            }
        }
    }

    WorldGenLevel level() {
        return this.level;
    }

    ChunkGenerator generator() {
        return this.generator;
    }

    int seaLevel() {
        return this.seaLevel;
    }

    int minX() {
        return this.minX;
    }

    int minZ() {
        return this.minZ;
    }

    boolean contains(int x, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    boolean intersects(double x, double z, double radius) {
        return x + radius >= this.minX && x - radius <= this.maxX + 1
                && z + radius >= this.minZ && z - radius <= this.maxZ + 1;
    }

    int floor(int x, int z) {
        return this.floors[(x - this.minX) * 16 + (z - this.minZ)];
    }

    void forColumns(double centerX, double centerZ, double radius, ColumnVisitor visitor) {
        if (!intersects(centerX, centerZ, radius)) {
            return;
        }
        int x0 = Math.max(this.minX, Mth.floor(centerX - radius));
        int x1 = Math.min(this.maxX, Mth.ceil(centerX + radius));
        int z0 = Math.max(this.minZ, Mth.floor(centerZ - radius));
        int z1 = Math.min(this.maxZ, Mth.ceil(centerZ + radius));
        double limit = radius * radius;
        for (int x = x0; x <= x1; x++) {
            double dx = x - centerX;
            for (int z = z0; z <= z1; z++) {
                double dz = z - centerZ;
                double distance = dx * dx + dz * dz;
                if (distance <= limit) {
                    visitor.visit(x, z, Math.sqrt(distance));
                }
            }
        }
    }

    void blob(double x, double y, double z, double radius, ReefCoral coral, boolean rooted) {
        if (!intersects(x, z, radius)) {
            return;
        }
        int x0 = Math.max(this.minX, Mth.floor(x - radius));
        int x1 = Math.min(this.maxX, Mth.ceil(x + radius));
        int z0 = Math.max(this.minZ, Mth.floor(z - radius));
        int z1 = Math.min(this.maxZ, Mth.ceil(z + radius));
        double limit = radius * radius;
        for (int bx = x0; bx <= x1; bx++) {
            double dx = bx - x;
            for (int bz = z0; bz <= z1; bz++) {
                double dz = bz - z;
                double remaining = limit - dx * dx - dz * dz;
                if (remaining < 0.0D) {
                    continue;
                }
                double half = Math.sqrt(remaining);
                int low = Mth.ceil(y - half);
                int high = Mth.floor(y + half);
                if (rooted) {
                    low = Math.min(low, floor(bx, bz));
                }
                coralColumn(bx, bz, low, high, coral);
            }
        }
    }

    void coralColumn(int x, int z, int fromY, int toY, ReefCoral coral) {
        if (!contains(x, z)) {
            return;
        }
        int low = Math.max(fromY, this.minY);
        int high = Math.min(toY, this.maxY - 1);
        if (low > high) {
            return;
        }
        byte id = paletteId(coral);
        BlockState live = coral.block().defaultBlockState();
        BlockState dead = coral.deadBlock().defaultBlockState();
        for (int y = low; y <= high; y++) {
            boolean exposed = y >= this.seaLevel;
            put(x, y, z, exposed ? dead : live, exposed ? BLOCKED : id);
        }
    }

    void coral(int x, int y, int z, ReefCoral coral) {
        coralColumn(x, z, y, y, coral);
    }

    void blockColumn(int x, int z, int fromY, int toY, BlockState state) {
        if (!contains(x, z)) {
            return;
        }
        int low = Math.max(fromY, this.minY);
        int high = Math.min(toY, this.maxY - 1);
        for (int y = low; y <= high; y++) {
            put(x, y, z, state, BLOCKED);
        }
    }

    void block(int x, int y, int z, BlockState state) {
        blockColumn(x, z, y, y, state);
    }

    void decor(int x, int y, int z, BlockState state) {
        if (!contains(x, z) || y < this.minY || y >= this.seaLevel) {
            return;
        }
        int index = index(x, y, z);
        if (this.occupancy[index] != EMPTY) {
            return;
        }
        this.cursor.set(x, y, z);
        if (this.chunk.getBlockState(this.cursor).is(Blocks.WATER) && state.canSurvive(this.level, this.cursor)) {
            this.chunk.setBlockState(this.cursor, state, false);
            mark(index, y, BLOCKED);
        }
    }

    void finish(RandomSource random) {
        if (this.touchedMin > this.touchedMax) {
            return;
        }
        for (int y = this.touchedMin; y <= this.touchedMax; y++) {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int index = ((y - this.minY) * 16 + lz) * 16 + lx;
                    byte id = this.occupancy[index];
                    if (id <= EMPTY) {
                        continue;
                    }
                    decorate(random, this.palette.get(id - 1), this.minX + lx, y, this.minZ + lz);
                }
            }
        }
        clearKelp();
    }

    void release() {
        if (this.touchedMin <= this.touchedMax) {
            int from = (this.touchedMin - this.minY) * 256;
            int to = Math.min(this.occupancy.length, (this.touchedMax + 2 - this.minY) * 256);
            Arrays.fill(this.occupancy, from, to, EMPTY);
        }
    }

    static double noise(long seed, int x, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    private void decorate(RandomSource random, ReefCoral coral, int x, int y, int z) {
        int above = y + 1;
        if (above < this.seaLevel && this.occupancy[index(x, above, z)] == EMPTY) {
            this.cursor.set(x, above, z);
            if (this.chunk.getBlockState(this.cursor).is(Blocks.WATER)) {
                float roll = random.nextFloat();
                if (roll < 0.14F) {
                    placeDecor(x, above, z, coral.plant().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, true));
                } else if (roll < 0.24F) {
                    placeDecor(x, above, z, coral.floorFan().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, true));
                }
            }
        }
        for (Direction direction : HORIZONTAL) {
            int sx = x + direction.getStepX();
            int sz = z + direction.getStepZ();
            if (!contains(sx, sz) || this.occupancy[index(sx, y, sz)] != EMPTY) {
                continue;
            }
            if (random.nextFloat() >= 0.06F) {
                continue;
            }
            this.cursor.set(sx, y, sz);
            if (this.chunk.getBlockState(this.cursor).is(Blocks.WATER)) {
                placeDecor(sx, y, sz, coral.wallFan().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, direction)
                        .setValue(BlockStateProperties.WATERLOGGED, true));
            }
        }
    }

    private void placeDecor(int x, int y, int z, BlockState state) {
        this.cursor.set(x, y, z);
        this.chunk.setBlockState(this.cursor, state, false);
        this.occupancy[index(x, y, z)] = BLOCKED;
    }

    private void put(int x, int y, int z, BlockState state, byte id) {
        int index = index(x, y, z);
        if (this.occupancy[index] != EMPTY) {
            return;
        }
        this.cursor.set(x, y, z);
        if (!canReplace(this.chunk.getBlockState(this.cursor))) {
            mark(index, y, BLOCKED);
            return;
        }
        this.chunk.setBlockState(this.cursor, state, false);
        mark(index, y, id);
        int column = (x - this.minX) * 16 + (z - this.minZ);
        if (y > this.columnTops[column]) {
            this.columnTops[column] = y;
        }
    }

    private void mark(int index, int y, byte id) {
        this.occupancy[index] = id;
        if (y < this.touchedMin) {
            this.touchedMin = y;
        }
        if (y > this.touchedMax) {
            this.touchedMax = y;
        }
    }

    private void clearKelp() {
        BlockState water = Blocks.WATER.defaultBlockState();
        for (int column = 0; column < 256; column++) {
            int top = this.columnTops[column];
            if (top == Integer.MIN_VALUE) {
                continue;
            }
            int x = this.minX + column / 16;
            int z = this.minZ + column % 16;
            for (int y = top + 1; y < this.maxY; y++) {
                this.cursor.set(x, y, z);
                BlockState state = this.chunk.getBlockState(this.cursor);
                if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT) && !state.is(Blocks.TALL_SEAGRASS)) {
                    break;
                }
                this.chunk.setBlockState(this.cursor, water, false);
            }
        }
    }

    private int index(int x, int y, int z) {
        return ((y - this.minY) * 16 + (z - this.minZ)) * 16 + (x - this.minX);
    }

    private byte paletteId(ReefCoral coral) {
        if (coral == this.lastCoral) {
            return this.lastId;
        }
        int found = this.palette.indexOf(coral);
        if (found < 0) {
            this.palette.add(coral);
            found = this.palette.size() - 1;
        }
        this.lastCoral = coral;
        this.lastId = (byte) (found + 1);
        return this.lastId;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.canBeReplaced()
                || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS)
                || state.is(Blocks.SEA_PICKLE)
                || state.is(BlockTags.CORALS)
                || state.is(BlockTags.WALL_CORALS);
    }
}
