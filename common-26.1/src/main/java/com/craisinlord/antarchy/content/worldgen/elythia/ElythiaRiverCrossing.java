package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

public record ElythiaRiverCrossing(
        int minX,
        int minZ,
        int startX,
        int startZ,
        int dx,
        int dz,
        int steps,
        int waterY,
        int startGround,
        int endGround
) {
    private static final int[][] LINES = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
    private static final int MAX_HALF = 12;
    private static final int MIN_WIDTH = 4;
    private static final int MAX_BANK_RISE = 3;
    private static final int ATTEMPTS = 6;
    private static final int SPAN = 48;

    public static Optional<ElythiaRiverCrossing> find(WorldGenLevel level, BlockPos origin, RandomSource random, boolean axisOnly, int margin) {
        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;
        int minX = chunkMinX - 16;
        int minZ = chunkMinZ - 16;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            ElythiaRiverCrossing crossing = findAt(level, minX, minZ, x, z, axisOnly, margin);
            if (crossing != null) {
                return Optional.of(crossing);
            }
        }
        return Optional.empty();
    }

    private static ElythiaRiverCrossing findAt(WorldGenLevel level, int minX, int minZ, int x, int z, boolean axisOnly, int margin) {
        int waterY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        FluidState surface = level.getFluidState(new BlockPos(x, waterY, z));
        if (!isRiverFluid(surface) || !surface.isSource()) {
            return null;
        }

        ElythiaRiverCrossing best = null;
        double bestSpan = Double.MAX_VALUE;
        for (int[] line : LINES) {
            int dx = line[0];
            int dz = line[1];
            boolean diagonal = dx != 0 && dz != 0;
            if (axisOnly && diagonal) {
                continue;
            }

            int forward = scan(level, minX, minZ, x, z, waterY, dx, dz);
            int back = scan(level, minX, minZ, x, z, waterY, -dx, -dz);
            if (forward < 0 || back < 0 || forward + back - 1 < MIN_WIDTH) {
                continue;
            }

            int startX = x - dx * back;
            int startZ = z - dz * back;
            int endX = x + dx * forward;
            int endZ = z + dz * forward;
            if (!contains(minX, minZ, startX - dx * margin, startZ - dz * margin)
                    || !contains(minX, minZ, endX + dx * margin, endZ + dz * margin)) {
                continue;
            }

            int startGround = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, startX, startZ) - 1;
            int endGround = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, endX, endZ) - 1;
            if (!isBank(startGround, waterY) || !isBank(endGround, waterY)) {
                continue;
            }

            double span = (forward + back - 1) * (diagonal ? Math.sqrt(2.0D) : 1.0D);
            if (span < bestSpan) {
                bestSpan = span;
                best = new ElythiaRiverCrossing(minX, minZ, startX, startZ, dx, dz, forward + back, waterY, startGround, endGround);
            }
        }
        return best;
    }

    private static int scan(WorldGenLevel level, int minX, int minZ, int x, int z, int waterY, int dx, int dz) {
        for (int i = 1; i <= MAX_HALF; i++) {
            int px = x + dx * i;
            int pz = z + dz * i;
            if (!contains(minX, minZ, px, pz)) {
                return -1;
            }
            if (!isRiverFluid(level.getFluidState(new BlockPos(px, waterY, pz)))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isBank(int ground, int waterY) {
        return ground >= waterY && ground <= waterY + MAX_BANK_RISE;
    }

    private static boolean contains(int minX, int minZ, int x, int z) {
        return x >= minX && x < minX + SPAN && z >= minZ && z < minZ + SPAN;
    }

    public static boolean isRiverFluid(FluidState state) {
        return !state.isEmpty() && (state.is(FluidTags.WATER) || state.is(AntarchyTags.Fluids.LUMEN));
    }

    public boolean contains(int x, int z) {
        return contains(this.minX, this.minZ, x, z);
    }

    public boolean diagonal() {
        return this.dx != 0 && this.dz != 0;
    }

    public int x(int step) {
        return this.startX + this.dx * step;
    }

    public int z(int step) {
        return this.startZ + this.dz * step;
    }
}
