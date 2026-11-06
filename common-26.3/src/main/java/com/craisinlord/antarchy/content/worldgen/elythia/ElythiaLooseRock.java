package com.craisinlord.antarchy.content.worldgen.elythia;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

public final class ElythiaLooseRock {
    public static final int MAX_PIECE = 256;
    public static final int MAX_TOP_RUN = 16;
    public static final int NEEDLE_RADIUS = 3;
    public static final int NEEDLE_RISE = 4;
    private static final int[][] NEIGHBORS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    public enum Cell {
        OPEN,
        TERRAIN,
        ANCHOR
    }

    @FunctionalInterface
    public interface Cells {
        Cell at(int x, int y, int z);
    }

    private ElythiaLooseRock() {
    }

    public static int[] opened(int[] heights, int size, int radius) {
        return sweep(sweep(heights, size, radius, false), size, radius, true);
    }

    private static int[] sweep(int[] values, int size, int radius, boolean takeMax) {
        int limit = radius * radius + radius;
        int[] result = new int[values.length];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                int best = takeMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                for (int nx = Math.max(0, x - radius); nx <= Math.min(size - 1, x + radius); nx++) {
                    for (int nz = Math.max(0, z - radius); nz <= Math.min(size - 1, z + radius); nz++) {
                        int dx = nx - x;
                        int dz = nz - z;
                        if (dx * dx + dz * dz > limit) {
                            continue;
                        }
                        int value = values[nx * size + nz];
                        best = takeMax ? Math.max(best, value) : Math.min(best, value);
                    }
                }
                result[x * size + z] = best;
            }
        }
        return result;
    }

    public static boolean isTopRunThin(Cells cells, int x, int top, int z) {
        if (cells.at(x, top, z) != Cell.TERRAIN) {
            return false;
        }
        for (int y = top - 1; y >= top - MAX_TOP_RUN; y--) {
            Cell cell = cells.at(x, y, z);
            if (cell == Cell.OPEN) {
                return true;
            }
            if (cell == Cell.ANCHOR) {
                return false;
            }
        }
        return false;
    }

    public static LongArrayList floatingPiece(Cells cells, int x, int y, int z, int minX, int maxX, int minY, int minZ, int maxZ, LongSet anchored) {
        long start = BlockPos.asLong(x, y, z);
        LongArrayList piece = new LongArrayList();
        if (anchored.contains(start)) {
            return piece;
        }
        LongOpenHashSet seen = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        seen.add(start);
        queue.enqueue(start);
        boolean connected = false;
        while (!queue.isEmpty() && !connected) {
            long current = queue.dequeueLong();
            piece.add(current);
            int cx = BlockPos.getX(current);
            int cy = BlockPos.getY(current);
            int cz = BlockPos.getZ(current);
            for (int[] offset : NEIGHBORS) {
                int nx = cx + offset[0];
                int ny = cy + offset[1];
                int nz = cz + offset[2];
                if (nx < minX || nx > maxX || nz < minZ || nz > maxZ || ny < minY) {
                    connected = true;
                    break;
                }
                long next = BlockPos.asLong(nx, ny, nz);
                if (seen.contains(next)) {
                    continue;
                }
                Cell cell = cells.at(nx, ny, nz);
                if (cell == Cell.ANCHOR || anchored.contains(next)) {
                    connected = true;
                    break;
                }
                if (cell == Cell.TERRAIN) {
                    seen.add(next);
                    queue.enqueue(next);
                }
            }
            if (piece.size() + queue.size() > MAX_PIECE) {
                connected = true;
            }
        }
        if (connected) {
            anchored.addAll(seen);
            piece.clear();
        }
        return piece;
    }
}
