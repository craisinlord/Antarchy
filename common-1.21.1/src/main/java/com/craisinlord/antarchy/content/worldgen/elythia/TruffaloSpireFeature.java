package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.worldgen.truffalo.TruffaloTreeGrowers;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class TruffaloSpireFeature extends Feature<NoneFeatureConfiguration> {
    private static final int REACH = 16;
    private static final int BURY_DEPTH = 3;
    private static final int MAX_FILL_DOWN = 10;

    public TruffaloSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        Integer mainGround = groundAt(level, origin.getX(), origin.getZ());
        if (mainGround == null) {
            return false;
        }

        boolean arch = random.nextFloat() < 0.5F;
        int mainHeight = arch ? Mth.nextInt(random, 28, 40) : Mth.nextInt(random, 24, 40);
        double mainShaft = arch ? 5.0D : 3.0D + random.nextDouble();
        double mainBase = arch ? 7.0D : 5.5D + random.nextDouble() * 1.5D;
        Shape main = Shape.create(random, origin.getX(), origin.getZ(), mainGround, mainHeight, mainBase, mainShaft, 0.22D, 2.5D, BURY_DEPTH);

        List<Shape> shapes = new ArrayList<>();
        shapes.add(main);

        int crowns = 2 + random.nextInt(2);
        int crownStart = mainGround + Mth.floor(mainHeight * 0.7D);
        for (int i = 0; i < crowns; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = main.radiusAt(crownStart) * (0.35D + random.nextDouble() * 0.35D);
            double x = main.centerX(crownStart) + Math.cos(angle) * distance;
            double z = main.centerZ(crownStart) + Math.sin(angle) * distance;
            int crownHeight = Mth.nextInt(random, 6, 12);
            shapes.add(Shape.create(random, x, z, crownStart, crownHeight, 1.6D, 1.6D, 0.0D, 1.2D, 0));
        }

        int satellites = random.nextInt(4);
        for (int i = 0; i < satellites; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 5.0D + random.nextDouble() * 3.0D;
            int x = Mth.floor(origin.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(origin.getZ() + Math.sin(angle) * distance);
            Integer ground = groundAt(level, x, z);
            if (ground == null) {
                continue;
            }
            double shaft = 1.2D + random.nextDouble() * 0.9D;
            shapes.add(Shape.create(random, x, z, ground, Mth.nextInt(random, 9, 20), shaft + 1.8D, shaft, 0.25D, 1.5D, BURY_DEPTH));
        }

        BlockState shellstone = AntarchyObjects.SHELLSTONE.get().defaultBlockState();
        BlockState tuff = Blocks.TUFF.defaultBlockState();
        BlockState moss = Blocks.MOSS_BLOCK.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        List<BlockPos> ledges = new ArrayList<>();
        boolean placed = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Shape shape : shapes) {
            int extent = Mth.ceil(shape.baseRadius * 1.4D + shape.lean + 1.0D);
            int minX = Mth.floor(shape.x) - extent;
            int maxX = Mth.floor(shape.x) + extent;
            int minZ = Mth.floor(shape.z) - extent;
            int maxZ = Mth.floor(shape.z) + extent;
            for (int y = shape.bottomY(); y <= shape.topY(); y++) {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!withinReach(origin, x, z) || !shape.contains(x, y, z)) {
                            continue;
                        }
                        mutable.set(x, y, z);
                        if (!canReplace(level.getBlockState(mutable))) {
                            continue;
                        }
                        boolean exposed = !anyContains(shapes, x, y + 1, z);
                        BlockState state;
                        if (exposed && y <= shape.ground + 2) {
                            state = grass;
                        } else if (exposed && random.nextFloat() < 0.8F) {
                            state = moss;
                            if (y > shape.ground + 8 && Math.abs(x - origin.getX()) <= REACH - 3 && Math.abs(z - origin.getZ()) <= REACH - 3) {
                                ledges.add(new BlockPos(x, y + 1, z));
                            }
                        } else {
                            state = random.nextFloat() < 0.6F ? shellstone : tuff;
                        }
                        level.setBlock(mutable, state, 2);
                        placed = true;
                        if (y == shape.bottomY()) {
                            fillDown(level, mutable.immutable(), shellstone, tuff, random);
                        }
                    }
                }
            }
        }

        if (!placed) {
            return false;
        }

        if (arch) {
            carveTunnel(level, random, origin, main);
        }
        if (random.nextFloat() < 0.35F) {
            carveWindow(level, random, origin, main);
        }

        placeTrees(level, context.chunkGenerator(), random, main, ledges);
        return true;
    }

    private static void carveTunnel(WorldGenLevel level, RandomSource random, BlockPos origin, Shape main) {
        Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        int centerY = main.ground + Mth.nextInt(random, 5, Math.max(5, Mth.floor(main.height * 0.4D)));
        double halfWidth = 2.2D + random.nextDouble() * 0.6D;
        double halfHeight = 2.6D + random.nextDouble() * 0.8D;
        int length = Mth.ceil(main.radiusAt(centerY) * 1.4D) + 2;
        int centerX = Mth.floor(main.centerX(centerY));
        int centerZ = Mth.floor(main.centerZ(centerY));
        int spanAcross = Mth.ceil(halfWidth);
        int spanUp = Mth.ceil(halfHeight);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int along = -length; along <= length; along++) {
            for (int across = -spanAcross; across <= spanAcross; across++) {
                int x = axis == Direction.Axis.X ? centerX + along : centerX + across;
                int z = axis == Direction.Axis.X ? centerZ + across : centerZ + along;
                if (!withinReach(origin, x, z)) {
                    continue;
                }
                for (int v = -spanUp; v <= spanUp; v++) {
                    double sx = across / halfWidth;
                    double sy = v / halfHeight;
                    if (sx * sx + sy * sy > 1.0D) {
                        continue;
                    }
                    mutable.set(x, centerY + v, z);
                    if (isSpireBlock(level.getBlockState(mutable))) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void carveWindow(WorldGenLevel level, RandomSource random, BlockPos origin, Shape main) {
        int y = main.ground + Mth.floor(main.height * (0.45D + random.nextDouble() * 0.2D));
        if (main.radiusAt(y) < 2.5D) {
            return;
        }
        Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        double radius = 1.4D + random.nextDouble() * 0.6D;
        int length = Mth.ceil(main.radiusAt(y) * 1.4D) + 2;
        int centerX = Mth.floor(main.centerX(y));
        int centerZ = Mth.floor(main.centerZ(y));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int r = Mth.ceil(radius);
        for (int along = -length; along <= length; along++) {
            for (int across = -r; across <= r; across++) {
                for (int v = -r; v <= r; v++) {
                    if (across * across + v * v > radius * radius) {
                        continue;
                    }
                    int x = axis == Direction.Axis.X ? centerX + along : centerX + across;
                    int z = axis == Direction.Axis.X ? centerZ + across : centerZ + along;
                    if (!withinReach(origin, x, z)) {
                        continue;
                    }
                    mutable.set(x, y + v, z);
                    if (isSpireBlock(level.getBlockState(mutable))) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeTrees(WorldGenLevel level, ChunkGenerator generator, RandomSource random, Shape main, List<BlockPos> ledges) {
        ConfiguredFeature<?, ?> tree = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .get(TruffaloTreeGrowers.TRUFFALO_TREE);
        if (tree == null) {
            return;
        }
        if (random.nextFloat() < 0.6F) {
            int x = Mth.floor(main.centerX(main.topY()));
            int z = Mth.floor(main.centerZ(main.topY()));
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            tree.place(level, generator, random, new BlockPos(x, y, z));
        }
        int attempts = ledges.isEmpty() ? 0 : random.nextInt(3);
        for (int i = 0; i < attempts; i++) {
            BlockPos ledge = ledges.get(random.nextInt(ledges.size()));
            if (level.getBlockState(ledge).isAir()) {
                tree.place(level, generator, random, ledge);
            }
        }
    }

    private static void fillDown(WorldGenLevel level, BlockPos start, BlockState shellstone, BlockState tuff, RandomSource random) {
        BlockPos.MutableBlockPos mutable = start.mutable();
        for (int i = 0; i < MAX_FILL_DOWN; i++) {
            mutable.move(Direction.DOWN);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && !state.canBeReplaced()) {
                return;
            }
            level.setBlock(mutable, random.nextFloat() < 0.6F ? shellstone : tuff, 2);
        }
    }

    private static Integer groundAt(WorldGenLevel level, int x, int z) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (surface != floor) {
            return null;
        }
        int y = floor - 1;
        if (y + 48 >= level.getMaxBuildHeight()) {
            return null;
        }
        return y;
    }

    private static boolean withinReach(BlockPos origin, int x, int z) {
        return Math.abs(x - origin.getX()) <= REACH && Math.abs(z - origin.getZ()) <= REACH;
    }

    private static boolean anyContains(List<Shape> shapes, int x, int y, int z) {
        for (Shape shape : shapes) {
            if (shape.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canReplace(BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.isAir()
                || state.canBeReplaced()
                || LumenWorldgenHelper.isNaturalFloor(state)
                || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isSpireBlock(BlockState state) {
        return state.is(AntarchyObjects.SHELLSTONE.get()) || state.is(Blocks.TUFF) || state.is(Blocks.MOSS_BLOCK);
    }

    private static final class Shape {
        private final double x;
        private final double z;
        private final int ground;
        private final int height;
        private final double baseRadius;
        private final double shaftRadius;
        private final double flare;
        private final double lean;
        private final double leanX;
        private final double leanZ;
        private final int bury;
        private final double phaseA;
        private final double phaseB;
        private final double phaseC;

        private Shape(double x, double z, int ground, int height, double baseRadius, double shaftRadius, double flare,
                      double lean, double leanAngle, int bury, double phaseA, double phaseB, double phaseC) {
            this.x = x;
            this.z = z;
            this.ground = ground;
            this.height = height;
            this.baseRadius = baseRadius;
            this.shaftRadius = shaftRadius;
            this.flare = flare;
            this.lean = lean;
            this.leanX = Math.cos(leanAngle) * lean;
            this.leanZ = Math.sin(leanAngle) * lean;
            this.bury = bury;
            this.phaseA = phaseA;
            this.phaseB = phaseB;
            this.phaseC = phaseC;
        }

        static Shape create(RandomSource random, double x, double z, int ground, int height, double baseRadius,
                            double shaftRadius, double flare, double maxLean, int bury) {
            double tau = Math.PI * 2.0D;
            return new Shape(x, z, ground, height, baseRadius, shaftRadius, flare,
                    random.nextDouble() * maxLean, random.nextDouble() * tau, bury,
                    random.nextDouble() * tau, random.nextDouble() * tau, random.nextDouble() * tau);
        }

        int bottomY() {
            return this.ground - this.bury;
        }

        int topY() {
            return this.ground + this.height;
        }

        double progress(int y) {
            return Mth.clamp((y - this.ground) / (double) this.height, 0.0D, 1.0D);
        }

        double centerX(int y) {
            double t = progress(y);
            return this.x + this.leanX * t * t;
        }

        double centerZ(int y) {
            double t = progress(y);
            return this.z + this.leanZ * t * t;
        }

        double radiusAt(int y) {
            double t = progress(y);
            double radius;
            if (t < this.flare) {
                double f = 1.0D - t / this.flare;
                radius = this.shaftRadius + (this.baseRadius - this.shaftRadius) * f * f;
            } else {
                double f = this.flare >= 1.0D ? 1.0D : (t - this.flare) / (1.0D - this.flare);
                radius = this.shaftRadius * (1.0D - 0.85D * Math.pow(f, 1.6D));
            }
            return radius * (1.0D + 0.12D * Math.sin(y * 0.45D + this.phaseC));
        }

        boolean contains(int bx, int by, int bz) {
            if (by < bottomY() || by > topY()) {
                return false;
            }
            double dx = bx + 0.5D - centerX(by);
            double dz = bz + 0.5D - centerZ(by);
            double radius = radiusAt(by);
            double maxRadius = radius * 1.34D + 0.5D;
            double distanceSq = dx * dx + dz * dz;
            if (distanceSq > maxRadius * maxRadius) {
                return false;
            }
            double angle = Math.atan2(dz, dx);
            double lump = 1.0D
                    + 0.22D * Math.sin(angle * 3.0D + this.phaseA + by * 0.15D)
                    + 0.12D * Math.sin(angle * 5.0D + this.phaseB - by * 0.09D);
            double edge = radius * lump + 0.5D;
            return distanceSq <= edge * edge;
        }
    }
}
