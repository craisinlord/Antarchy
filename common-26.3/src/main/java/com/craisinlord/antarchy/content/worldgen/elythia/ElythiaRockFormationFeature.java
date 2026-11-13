package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.block.ElythiaPalette;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ElythiaRockFormationFeature implements Feature {
    public static final MapCodec<ElythiaRockFormationFeature> CODEC = MapCodec.unit(ElythiaRockFormationFeature::new);

    private static final int MAX_RIM_DROP = 14;
    private static final int MAX_FILL_DEPTH = 40;

    @Override
    public MapCodec<ElythiaRockFormationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        if (nearStructure(level, chunkX, chunkZ)) {
            return false;
        }
        int x = (chunkX << 4) + 4 + random.nextInt(8);
        int z = (chunkZ << 4) + 4 + random.nextInt(8);
        Formation formation = new Formation(level, random, chunkX, chunkZ);
        float roll = random.nextFloat();
        if (roll < 0.6F) {
            return formation.spireCluster(x, z);
        }
        return roll < 0.85F ? formation.mesa(x, z) : formation.arch(x, z);
    }

    private static boolean nearStructure(WorldGenLevel level, int chunkX, int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (LongSet references : level.getChunk(chunkX + dx, chunkZ + dz).getAllReferences().values()) {
                    if (!references.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final class Formation {
        private final WorldGenLevel level;
        private final RandomSource random;
        private final int chunkX;
        private final int chunkZ;
        private final BlockState rockite = ElythiaPalette.ROCKITE.get().defaultBlockState();
        private final BlockState zebraStone = ElythiaPalette.ZEBRA_STONE.get().defaultBlockState();
        private boolean placed;

        private Formation(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
            this.level = level;
            this.random = random;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        private boolean spireCluster(int x, int z) {
            int baseRadius = 6 + this.random.nextInt(5);
            BlockPos ground = this.site(x, z, baseRadius);
            if (ground == null) {
                return false;
            }
            int height = 40 + this.random.nextInt(45);
            this.spire(ground, height, baseRadius, 1.2D + this.random.nextDouble() * 1.2D, 3.0D + this.random.nextDouble() * 9.0D);
            int satellites = 2 + this.random.nextInt(4);
            for (int i = 0; i < satellites; i++) {
                double angle = this.random.nextDouble() * Mth.TWO_PI;
                double distance = 6.0D + this.random.nextDouble() * 5.0D;
                int radius = 3 + this.random.nextInt(4);
                BlockPos base = this.site(x + (int) (Math.cos(angle) * distance), z + (int) (Math.sin(angle) * distance), radius);
                if (base != null) {
                    int satelliteHeight = (int) (height * (0.35D + this.random.nextDouble() * 0.35D));
                    this.spire(base, satelliteHeight, radius, 0.8D + this.random.nextDouble() * 0.8D, this.random.nextDouble() * 5.0D);
                }
            }
            return this.placed;
        }

        private boolean mesa(int x, int z) {
            int baseRadius = 9 + this.random.nextInt(4);
            BlockPos ground = this.site(x, z, baseRadius);
            if (ground == null) {
                return false;
            }
            int height = 22 + this.random.nextInt(23);
            double[] ledges = this.ledges(height, 1.6D);
            double phaseA = this.random.nextDouble() * Mth.TWO_PI;
            double phaseB = this.random.nextDouble() * Mth.TWO_PI;
            for (int y = 0; y <= height; y++) {
                double progress = y / (double) height;
                double radius = baseRadius * (1.0D - 0.22D * progress * progress) + Math.pow(1.0D - progress, 6.0D) * 3.0D + ledges[y];
                this.layer(ground, y, 0.0D, 0.0D, radius, phaseA, phaseB, y == 0);
            }
            return this.placed;
        }

        private boolean arch(int x, int z) {
            int halfSpan = 9 + this.random.nextInt(5);
            BlockPos ground = this.site(x, z, halfSpan);
            if (ground == null) {
                return false;
            }
            int rise = 18 + this.random.nextInt(17);
            double thickness = 3.0D + this.random.nextDouble() * 1.5D;
            double axis = this.random.nextDouble() * Mth.TWO_PI;
            double axisX = Math.cos(axis);
            double axisZ = Math.sin(axis);
            double phase = this.random.nextDouble() * Mth.TWO_PI;
            int steps = (halfSpan + rise) * 3;
            for (int step = 0; step <= steps; step++) {
                double angle = Math.PI * step / steps;
                double along = Math.cos(angle) * halfSpan;
                double leg = Math.abs(Math.cos(angle));
                double radius = thickness * (1.0D + 0.6D * leg * leg * leg) + Math.sin(angle * 7.0D + phase) * 0.5D;
                this.stamp(ground, axisX * along, Math.sin(angle) * rise, axisZ * along, radius);
            }
            double feet = thickness * 1.6D;
            for (int side = -1; side <= 1; side += 2) {
                double footX = axisX * halfSpan * side;
                double footZ = axisZ * halfSpan * side;
                this.layer(ground, 0, footX, footZ, feet, phase, phase * 2.0D, true);
            }
            return this.placed;
        }

        private void spire(BlockPos ground, int height, int baseRadius, double topRadius, double lean) {
            double[] ledges = this.ledges(height, 1.2D);
            double leanAngle = this.random.nextDouble() * Mth.TWO_PI;
            double leanX = Math.cos(leanAngle) * lean;
            double leanZ = Math.sin(leanAngle) * lean;
            double phaseA = this.random.nextDouble() * Mth.TWO_PI;
            double phaseB = this.random.nextDouble() * Mth.TWO_PI;
            for (int y = 0; y <= height; y++) {
                double progress = y / (double) height;
                double taper = Math.pow(1.0D - progress, 2.4D);
                double radius = topRadius + (baseRadius - topRadius) * taper + ledges[y] * (0.3D + taper);
                double bend = progress * progress;
                this.layer(ground, y, leanX * bend, leanZ * bend, Math.max(0.6D, radius), phaseA, phaseB, y == 0);
            }
        }

        private double[] ledges(int height, double strength) {
            double[] ledges = new double[height + 1];
            int y = 0;
            while (y <= height) {
                int band = 2 + this.random.nextInt(5);
                double ledge = (this.random.nextDouble() - 0.4D) * strength;
                for (int i = 0; i < band && y <= height; i++, y++) {
                    ledges[y] = ledge;
                }
            }
            return ledges;
        }

        private void layer(BlockPos ground, int y, double centerX, double centerZ, double radius, double phaseA, double phaseB, boolean fill) {
            int reach = Mth.ceil(radius * 1.3D);
            int originX = Mth.floor(centerX);
            int originZ = Mth.floor(centerZ);
            for (int dx = originX - reach; dx <= originX + reach; dx++) {
                for (int dz = originZ - reach; dz <= originZ + reach; dz++) {
                    double offsetX = dx - centerX;
                    double offsetZ = dz - centerZ;
                    double angle = Math.atan2(offsetZ, offsetX);
                    double limit = radius * (1.0D + 0.16D * Math.sin(3.0D * angle + phaseA) + 0.09D * Math.sin(5.0D * angle + phaseB));
                    if (offsetX * offsetX + offsetZ * offsetZ > limit * limit + this.random.nextDouble() * 0.8D) {
                        continue;
                    }
                    this.placeBlock(ground.getX() + dx, ground.getY() + y, ground.getZ() + dz);
                    if (fill) {
                        this.fillDown(ground.getX() + dx, ground.getY() + y - 1, ground.getZ() + dz);
                    }
                }
            }
        }

        private void stamp(BlockPos ground, double centerX, double centerY, double centerZ, double radius) {
            int reach = Mth.ceil(radius);
            int originX = Mth.floor(centerX);
            int originY = Mth.floor(centerY);
            int originZ = Mth.floor(centerZ);
            double limit = radius * radius;
            for (int dx = originX - reach; dx <= originX + reach; dx++) {
                for (int dy = originY - reach; dy <= originY + reach; dy++) {
                    for (int dz = originZ - reach; dz <= originZ + reach; dz++) {
                        double offsetX = dx - centerX;
                        double offsetY = dy - centerY;
                        double offsetZ = dz - centerZ;
                        if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= limit) {
                            this.placeBlock(ground.getX() + dx, ground.getY() + dy, ground.getZ() + dz);
                        }
                    }
                }
            }
        }

        private void placeBlock(int x, int y, int z) {
            if (!this.canWrite(x, z)) {
                return;
            }
            BlockPos pos = new BlockPos(x, y, z);
            BlockState current = this.level.getBlockState(pos);
            BlockState next = this.strataState(x, y, z);
            if (current != next && ElythiaStoneTorFeature.canCarve(current)) {
                this.level.setBlock(pos, next, 2);
                this.placed = true;
            }
        }

        private void fillDown(int x, int startY, int z) {
            if (!this.canWrite(x, z)) {
                return;
            }
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
            for (int depth = 0; depth < MAX_FILL_DEPTH && pos.getY() > this.level.getMinY(); depth++) {
                BlockState current = this.level.getBlockState(pos);
                if (!current.isAir() && !current.canBeReplaced()) {
                    return;
                }
                this.level.setBlock(pos, this.rockite, 2);
                pos.move(0, -1, 0);
            }
        }

        private BlockState strataState(int x, int y, int z) {
            long seed = this.level.getSeed();
            ElythiaStrataProfile profile = ElythiaStrataProfile.FORMATION;
            double offset = ElythiaStrata.bedOffset(seed, profile, x, z);
            return ElythiaStrata.layerAt(seed, profile, y, offset) == ElythiaStrata.Layer.ZEBRA ? this.zebraStone : this.rockite;
        }

        private boolean canWrite(int x, int z) {
            return Math.abs((x >> 4) - this.chunkX) <= 1 && Math.abs((z >> 4) - this.chunkZ) <= 1;
        }

        private BlockPos site(int x, int z, int radius) {
            if (!this.canWrite(x, z)) {
                return null;
            }
            int groundY = this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            BlockPos ground = new BlockPos(x, groundY, z);
            if (!ElythiaStoneTorFeature.isGround(this.level.getBlockState(ground))) {
                return null;
            }
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0D;
                int rimX = x + (int) (Math.cos(angle) * radius);
                int rimZ = z + (int) (Math.sin(angle) * radius);
                if (!this.canWrite(rimX, rimZ)) {
                    continue;
                }
                int rimY = this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, rimX, rimZ) - 1;
                if (groundY - rimY > MAX_RIM_DROP || !this.level.getFluidState(new BlockPos(rimX, rimY, rimZ)).isEmpty()) {
                    return null;
                }
            }
            return ground;
        }
    }
}
