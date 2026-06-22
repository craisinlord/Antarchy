package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.WaspNestBlock;
import com.craisinlord.antarchy.content.block.OuranwoodLeavesBlock;
import com.craisinlord.antarchy.content.entity.flying_squirrel.FlyingSquirrelEntity;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class OuranwoodTreeFeature extends Feature<OuranwoodTreeConfiguration> {
    public OuranwoodTreeFeature(Codec<OuranwoodTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OuranwoodTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        OuranwoodTreeConfiguration config = context.config();
        RandomSource random = context.random();

        if (!canGrowOn(level, origin.below())) {
            return false;
        }

        int height = config.height().sample(random);
        int trunkRadius = config.trunkRadius().sample(random);
        int canopyRadius = config.canopyRadius().sample(random);
        int canopyDepth = Math.min(config.canopyDepth().sample(random), height / 2);
        int buttressHeight = config.buttressHeight().sample(random);

        if (origin.getY() + height + canopyDepth + 4 >= level.getMaxBuildHeight()) {
            return false;
        }

        if (!config.juvenile()) {
            this.placeButtresses(level, origin, config, random, trunkRadius, buttressHeight);
            this.stabilizeBase(level, origin, config, random, trunkRadius + 1);
        }

        this.placeTrunk(level, origin, config, random, trunkRadius, height);
        this.placeWaspNest(level, origin, config, random, trunkRadius, height);
        this.placeBaseTrunkVines(level, origin, config, random, trunkRadius, height);
        if (!config.juvenile()) {
            this.adaptAndRefreshTerrain(level, origin, random, trunkRadius);
        }
        BlockPos canopyCenter = this.resolveCanopyCenter(origin, height, canopyDepth);
        BlockPos crownCenter = canopyCenter.above(canopyDepth / 3);
        int branchThickness = !config.juvenile() && trunkRadius >= 3 ? 2 : 1;
        this.placeUpperBranches(level, origin, canopyCenter, config, random, height, canopyRadius, branchThickness);
        this.placeCanopyScaffolding(level, canopyCenter, config, random, canopyRadius, branchThickness);
        this.placeCanopy(level, canopyCenter, config, random, canopyRadius, canopyDepth);
        this.placeMidCanopy(level, origin, config, random, canopyRadius, height, trunkRadius, branchThickness);
        this.placeTopCrown(level, crownCenter, config, random, canopyRadius, random.nextInt(4));
        this.placeHiddenCanopySupports(level, canopyCenter, crownCenter, config, random, canopyRadius, canopyDepth);
        this.coverExposedBranchSides(level, origin, canopyCenter, config, random, trunkRadius, canopyRadius, canopyDepth, height);
        this.pruneExposedTopLogs(level, origin, canopyCenter, config, random, trunkRadius, canopyRadius, canopyDepth, height);
        this.updateLeafDistances(level, origin, height, canopyRadius, canopyDepth);
        this.placeHangingAcorns(level, canopyCenter, config, random, canopyRadius, canopyDepth);
        this.placeTrunkVines(level, origin, config, random, trunkRadius, height);
        this.placeVinesAndGlowBerries(level, canopyCenter, config, random, canopyRadius, canopyDepth, height);
        this.placeFlyingSquirrelNest(level, origin, config, random, trunkRadius, height);
        this.placeCaterpillarChrysalis(level, origin, config, random, canopyCenter, canopyRadius, canopyDepth);
        return true;
    }

    protected BlockPos resolveCanopyCenter(BlockPos origin, int height, int canopyDepth) {
        int canopyBaseY = height - Math.max(2, canopyDepth / 4);
        return origin.above(canopyBaseY);
    }

    protected void placeTrunk(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int height) {
        for (int y = 0; y < height; y++) {
            int taper = 0;
            if (config.juvenile()) {
                taper = Math.max(0, y - (height - 10)) / 6;
            } else {
                int distanceFromTop = height - y;
                if (distanceFromTop <= 5) {
                    taper = trunkRadius >= 4 ? 2 : 1;
                } else if (distanceFromTop <= 14) {
                    taper = 1;
                }
            }
            int currentRadius = config.juvenile()
                    ? Math.max(1, trunkRadius - Math.max(0, y - (height - 10)) / 6)
                    : Math.max(1, trunkRadius - taper);
            this.fillIrregularTrunkCircle(level, origin.above(y), currentRadius, config, random, origin.getY(), height);
        }
    }

    protected void placeButtresses(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int buttressHeight) {
        int buttressRadius = trunkRadius + 2;
        int buttressRadiusSq = buttressRadius * buttressRadius;
        int trunkRadiusSq = trunkRadius * trunkRadius;
        int profile = random.nextInt(4);
        int targetHeight = Math.max(2, Math.min(5, buttressHeight + random.nextInt(2) - 1));
        for (int x = -buttressRadius; x <= buttressRadius; x++) {
            for (int z = -buttressRadius; z <= buttressRadius; z++) {
                int distanceSq = x * x + z * z;
                if (distanceSq > buttressRadiusSq) {
                    continue;
                }

                int columnHeight = this.computeButtressColumnHeight(profile, x, z, distanceSq, trunkRadiusSq, targetHeight);
                for (int y = 0; y < columnHeight; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (canReplace(level, pos)) {
                        setBlock(level, pos, this.selectTrunkState(config, random, pos, pos.getY() - origin.getY(), 32));
                    }
                }
            }
        }
    }

    protected int computeButtressColumnHeight(int profile, int x, int z, int distanceSq, int trunkRadiusSq, int targetHeight) {
        int columnHeight;
        double radialDistance = Math.sqrt(distanceSq);
        switch (profile) {
            case 0 -> {
                int falloff = (int) Math.floor(radialDistance / 2.35D);
                columnHeight = targetHeight - falloff;
                if (distanceSq <= trunkRadiusSq) {
                    columnHeight = Math.min(5, columnHeight + 1);
                }
            }
            case 1 -> {
                int falloff = (int) Math.floor(radialDistance / 2.2D);
                columnHeight = targetHeight - falloff;
                if (Math.abs(x) > Math.abs(z) && distanceSq > trunkRadiusSq) {
                    columnHeight -= 1;
                }
            }
            case 2 -> {
                int falloff = (int) Math.floor(radialDistance / 2.2D);
                columnHeight = targetHeight - falloff;
                if (Math.abs(z) > Math.abs(x) && distanceSq > trunkRadiusSq) {
                    columnHeight -= 1;
                }
            }
            default -> {
                int falloff = (int) Math.floor(radialDistance / 2.5D);
                columnHeight = targetHeight - falloff;
                if (distanceSq > trunkRadiusSq && Math.abs(Math.abs(x) - Math.abs(z)) == 0) {
                    columnHeight -= 1;
                }
            }
        }

        return Math.max(1, Math.min(5, columnHeight));
    }

    protected void placeCanopy(WorldGenLevel level, BlockPos center, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int canopyDepth) {
        int minRadius = config.juvenile() ? 2 : 4;
        int lowerDepth = Math.max(2, canopyDepth * 3 / 4);
        int upperDepth = Math.max(2, canopyDepth / 2);
        for (int y = -lowerDepth; y <= upperDepth; y++) {
            float normalized = y < 0
                    ? Math.abs(y) / (float) (lowerDepth + 1)
                    : y / (float) (upperDepth + 1);
            float layerScale = y < 0
                    ? 1.08F - normalized * 0.72F
                    : 1.02F - normalized * 0.9F;
            int layerRadius = Math.max(minRadius, Math.round(canopyRadius * layerScale));
            BlockPos layerCenter = center.above(y);
            boolean dense = y <= -lowerDepth + 2 || y >= upperDepth - 1;
            this.fillLeafCircle(level, layerCenter, config, random, layerRadius, dense);

            if (layerRadius > minRadius + 1) {
                int roundedRadius = Math.max(2, layerRadius - (y < 0 ? 2 : 1));
                BlockPos roundedCenter = y < 0 ? layerCenter.above() : layerCenter.above(random.nextFloat() < 0.55F ? 1 : 0);
                this.fillLeafCircle(level, roundedCenter, config, random, roundedRadius, true);
            }
        }

        int hangerAttempts = config.juvenile() ? 3 + random.nextInt(3) : 8 + random.nextInt(5);
        for (int i = 0; i < hangerAttempts; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int distance = Math.max(2, canopyRadius - 1 + random.nextInt(3));
            BlockPos leafPos = center.relative(direction, distance).below(1 + random.nextInt(2));
            this.tryPlaceGlowVineUnderLeaf(level, leafPos, random, config.juvenile() ? 2 + random.nextInt(3) : 4 + random.nextInt(5));
        }
    }

    protected void placeMidCanopy(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int height, int trunkRadius, int branchThickness) {
        int layers = config.juvenile() ? 2 + random.nextInt(2) : 5 + random.nextInt(3);
        int verticalSpacing = config.juvenile() ? Math.max(4, height / 5) : Math.max(8, height / 9);
        int horizontalSpread = config.juvenile() ? 1 : Math.max(2, trunkRadius + canopyRadius / 6);
        for (int i = 0; i < layers; i++) {
            int y = height / 4 + i * verticalSpacing + random.nextInt(config.juvenile() ? 3 : 6);
            BlockPos layerCenter = origin.offset(
                    random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread,
                    y,
                    random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread
            );
            int radius = config.juvenile()
                    ? Math.max(2, canopyRadius - 2 - i - random.nextInt(2))
                    : Math.max(4, canopyRadius - 3 - i - random.nextInt(2));
            this.placeBranchHub(level, layerCenter, config, random, radius, branchThickness);
            this.fillLeafCircle(level, layerCenter, config, random, radius, false);
            if (radius > 3) {
                this.fillLeafCircle(level, layerCenter.above(), config, random, radius - 2, true);
            }
        }
    }

    protected void placeUpperBranches(WorldGenLevel level, BlockPos origin, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int height, int canopyRadius, int branchThickness) {
        int branches = config.juvenile() ? 4 + random.nextInt(3) : 7 + random.nextInt(4);
        for (int i = 0; i < branches; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int startY = config.juvenile()
                    ? Math.max(3, height - canopyRadius - 6 - random.nextInt(6))
                    : height - 32 - random.nextInt(28);
            int branchLength = config.juvenile()
                    ? Math.max(4, canopyRadius / 2 + 1 + random.nextInt(3))
                    : Math.max(7, canopyRadius / 2 + 2 + random.nextInt(Math.max(3, canopyRadius / 2)));
            for (int step = 1; step <= branchLength; step++) {
                BlockPos branchPos = origin.above(startY + step / (config.juvenile() ? 3 : 4)).relative(direction, step);
                this.placeBranchSegment(level, branchPos, config, random, direction, branchThickness);
                this.placeBranchLeafCover(level, branchPos, config, random, direction);
                if (step >= branchLength / 3 && random.nextFloat() < 0.72F) {
                    this.fillBranchLeafCluster(level, branchPos, config, random, 2 + random.nextInt(config.juvenile() ? 2 : 3), false);
                }
            }

            BlockPos branchTip = canopyCenter.relative(direction, Math.max(2, branchLength - 2)).above(random.nextInt(3) - 1);
            this.fillBranchLeafCluster(level, branchTip, config, random, config.juvenile() ? 3 + random.nextInt(2) : 4 + random.nextInt(3), true);
            if (random.nextFloat() < 0.65F) {
                Direction sideDirection = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
                int sideLength = 2 + random.nextInt(3);
                for (int sideStep = 1; sideStep <= sideLength; sideStep++) {
                    BlockPos sideBranchPos = branchTip.relative(sideDirection, sideStep).below(sideStep / 2);
                    this.placeBranchSegment(level, sideBranchPos, config, random, sideDirection, branchThickness);
                    this.placeBranchLeafCover(level, sideBranchPos, config, random, sideDirection);
                }
                this.fillBranchLeafCluster(level, branchTip.relative(sideDirection, sideLength), config, random, 3 + random.nextInt(2), false);
            }
        }
    }

    protected void placeTopCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int variant) {
        int crownRadius = config.juvenile() ? Math.max(3, canopyRadius / 2) : Math.max(5, canopyRadius / 2);
        if (config.juvenile()) {
            this.placeRoundedJuvenileCrown(level, crownCenter, config, random, crownRadius);
            return;
        }

        switch (variant) {
            case 0 -> this.placeTieredCrown(level, crownCenter, config, random, crownRadius);
            case 1 -> this.placeSplitCrown(level, crownCenter, config, random, crownRadius);
            case 2 -> this.placeWindsweptCrown(level, crownCenter, config, random, crownRadius);
            default -> this.placeClusteredCrown(level, crownCenter, config, random, crownRadius);
        }
    }

    protected void placeRoundedJuvenileCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int crownRadius) {
        int lowerRadius = Math.max(2, crownRadius);
        int middleRadius = Math.max(2, crownRadius - 1);
        int upperRadius = Math.max(1, crownRadius - 2);
        this.fillLeafCircle(level, crownCenter.below(), config, random, lowerRadius, false);
        this.fillLeafCircle(level, crownCenter, config, random, middleRadius, true);
        this.fillLeafCircle(level, crownCenter.above(), config, random, upperRadius, true);
        if (upperRadius > 1 && random.nextFloat() < 0.45F) {
            this.fillLeafCircle(level, crownCenter.above(2), config, random, upperRadius - 1, true);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.75F) {
                BlockPos lobeCenter = crownCenter.relative(direction, Math.max(1, crownRadius - 1));
                this.fillLeafCircle(level, lobeCenter, config, random, Math.max(2, crownRadius - 2), true);
            }
        }
    }

    protected void placeCanopyScaffolding(WorldGenLevel level, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int branchThickness) {
        int spokeLength = config.juvenile() ? Math.max(2, canopyRadius / 3) : Math.max(4, canopyRadius / 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos cursor = canopyCenter;
            for (int step = 1; step <= spokeLength; step++) {
                cursor = cursor.relative(direction).above(step % (config.juvenile() ? 4 : 3) == 0 ? 1 : 0);
                this.placeBranchSegment(level, cursor, config, random, direction, branchThickness);
                this.placeBranchLeafCover(level, cursor, config, random, direction);
                if (step >= 2 && random.nextFloat() < 0.55F) {
                    this.fillBranchLeafCluster(level, cursor, config, random, 2 + random.nextInt(2), false);
                }
            }
        }
    }

    protected void placeHiddenCanopySupports(WorldGenLevel level, BlockPos canopyCenter, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int canopyDepth) {
        int spokeLength = config.juvenile() ? Math.max(2, canopyRadius / 3) : Math.max(3, canopyRadius / 2);
        int verticalSpread = Math.max(2, canopyDepth / 3);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int branches = config.juvenile() ? 1 : 2;
            for (int branch = 0; branch < branches; branch++) {
                BlockPos start = (branch == 0 ? canopyCenter : crownCenter).above(random.nextInt(verticalSpread + 1) - verticalSpread / 2);
                for (int step = 1; step <= spokeLength; step++) {
                    BlockPos pos = start.relative(direction, step).above(step > 2 && random.nextFloat() < 0.35F ? 1 : 0);
                    this.tryPlaceHiddenSupportLog(level, pos, config, random);
                }
            }
        }
    }

    protected void placeBranchHub(WorldGenLevel level, BlockPos center, OuranwoodTreeConfiguration config, RandomSource random, int radius, int branchThickness) {
        this.placeBranchSegment(level, center, config, random, null, branchThickness);
        this.placeBranchLeafCover(level, center, config, random, null);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int length = Math.max(2, radius / 2 + random.nextInt(2));
            for (int step = 1; step <= length; step++) {
                BlockPos branchPos = center.relative(direction, step).above(step / 3);
                this.placeBranchSegment(level, branchPos, config, random, direction, branchThickness);
                this.placeBranchLeafCover(level, branchPos, config, random, direction);
            }
        }
        this.fillBranchLeafCluster(level, center, config, random, Math.max(2, radius - 1), false);
    }

    protected void placeBranchSegment(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random, Direction axisDirection, int thickness) {
        this.placeLog(level, pos, config, random);
        if (thickness <= 1) {
            return;
        }

        if (axisDirection == null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.placeLog(level, pos.relative(direction), config, random);
            }
            return;
        }

        if (axisDirection.getAxis() == Direction.Axis.X) {
            this.placeLog(level, pos.north(), config, random);
            this.placeLog(level, pos.south(), config, random);
        } else {
            this.placeLog(level, pos.east(), config, random);
            this.placeLog(level, pos.west(), config, random);
        }

        if (random.nextFloat() < 0.35F) {
            this.placeLog(level, pos.below(), config, random);
        }
    }

    protected void placeLog(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random) {
        if (canReplace(level, pos)) {
            setBlock(level, pos, config.trunkProvider().getState(random, pos));
        }
    }

    protected void placeBranchLeafCover(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random, Direction axisDirection) {
        this.placeBranchTopLeafPad(level, pos.above(), config, random, axisDirection);
        this.tryPlaceLeaf(level, pos.below(), config, random);

        if (axisDirection == null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.tryPlaceLeaf(level, pos.relative(direction), config, random);
            }
            this.placeBranchSideLeafWrap(level, pos, config, random, null);
            return;
        }

        if (axisDirection.getAxis() == Direction.Axis.X) {
            this.tryPlaceLeaf(level, pos.north(), config, random);
            this.tryPlaceLeaf(level, pos.south(), config, random);
        } else {
            this.tryPlaceLeaf(level, pos.east(), config, random);
            this.tryPlaceLeaf(level, pos.west(), config, random);
        }

        this.placeBranchSideLeafWrap(level, pos, config, random, axisDirection);
    }

    protected void placeBranchTopLeafPad(WorldGenLevel level, BlockPos center, OuranwoodTreeConfiguration config, RandomSource random, Direction axisDirection) {
        this.tryPlaceLeaf(level, center, config, random);

        if (axisDirection == null || random.nextFloat() < 0.9F) {
            this.fillLeafCircle(level, center, config, random, 1, true);
            return;
        }

        if (axisDirection.getAxis() == Direction.Axis.X) {
            this.tryPlaceLeaf(level, center.north(), config, random);
            this.tryPlaceLeaf(level, center.south(), config, random);
            if (random.nextFloat() < 0.45F) {
                this.tryPlaceLeaf(level, center.east(), config, random);
            }
            if (random.nextFloat() < 0.45F) {
                this.tryPlaceLeaf(level, center.west(), config, random);
            }
            return;
        }

        this.tryPlaceLeaf(level, center.east(), config, random);
        this.tryPlaceLeaf(level, center.west(), config, random);
        if (random.nextFloat() < 0.45F) {
            this.tryPlaceLeaf(level, center.north(), config, random);
        }
        if (random.nextFloat() < 0.45F) {
            this.tryPlaceLeaf(level, center.south(), config, random);
        }
    }

    protected void placeBranchSideLeafWrap(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random, Direction axisDirection) {
        if (axisDirection == null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.tryPlaceLeaf(level, pos.relative(direction).above(), config, random);
            }
            return;
        }

        if (axisDirection.getAxis() == Direction.Axis.X) {
            this.tryPlaceLeaf(level, pos.north(2), config, random);
            this.tryPlaceLeaf(level, pos.south(2), config, random);
            this.tryPlaceLeaf(level, pos.north().above(), config, random);
            this.tryPlaceLeaf(level, pos.south().above(), config, random);
            if (random.nextFloat() < 0.6F) {
                this.tryPlaceLeaf(level, pos.north().below(), config, random);
            }
            if (random.nextFloat() < 0.6F) {
                this.tryPlaceLeaf(level, pos.south().below(), config, random);
            }
            return;
        }

        this.tryPlaceLeaf(level, pos.east(2), config, random);
        this.tryPlaceLeaf(level, pos.west(2), config, random);
        this.tryPlaceLeaf(level, pos.east().above(), config, random);
        this.tryPlaceLeaf(level, pos.west().above(), config, random);
        if (random.nextFloat() < 0.6F) {
            this.tryPlaceLeaf(level, pos.east().below(), config, random);
        }
        if (random.nextFloat() < 0.6F) {
            this.tryPlaceLeaf(level, pos.west().below(), config, random);
        }
    }

    protected void placeTrunkVines(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int height) {
        int attempts = config.juvenile() ? 4 + random.nextInt(4) : 10 + random.nextInt(8);
        int minY = config.juvenile() ? Math.max(2, height / 5) : Math.max(6, height / 6);
        int maxY = Math.max(minY, height - Math.max(5, height / 4));
        for (int i = 0; i < attempts; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int y = minY + random.nextInt(maxY - minY + 1);
            int lateralOffset = random.nextInt(trunkRadius * 2 + 1) - trunkRadius;
            BlockPos supportPos = switch (direction) {
                case NORTH -> origin.offset(lateralOffset, y, -trunkRadius);
                case SOUTH -> origin.offset(lateralOffset, y, trunkRadius);
                case EAST -> origin.offset(trunkRadius, y, lateralOffset);
                case WEST -> origin.offset(-trunkRadius, y, lateralOffset);
                default -> origin.above(y);
            };
            this.tryPlaceAttachedVine(level, supportPos, direction, config.trunkVineLength().sample(random));
        }
    }

    protected void placeBaseTrunkVines(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int height) {
        int topBand = Math.min(10, Math.max(4, height / 5));
        int attempts = config.juvenile() ? 2 + random.nextInt(3) : 6 + random.nextInt(6);
        for (int i = 0; i < attempts; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int y = random.nextInt(topBand + 1);
            int lateralOffset = random.nextInt(trunkRadius * 2 + 1) - trunkRadius;
            BlockPos supportPos = switch (direction) {
                case NORTH -> origin.offset(lateralOffset, y, -trunkRadius);
                case SOUTH -> origin.offset(lateralOffset, y, trunkRadius);
                case EAST -> origin.offset(trunkRadius, y, lateralOffset);
                case WEST -> origin.offset(-trunkRadius, y, lateralOffset);
                default -> origin.above(y);
            };
            if (!this.isBaseTrunkBlock(level.getBlockState(supportPos))) {
                continue;
            }

            this.tryPlaceAttachedVine(level, supportPos, direction, 2 + random.nextInt(4));
        }
    }

    protected void placeVinesAndGlowBerries(WorldGenLevel level, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int canopyDepth, int height) {
        int vineAttempts = config.juvenile() ? 8 + random.nextInt(6) : 20 + random.nextInt(12);
        for (int i = 0; i < vineAttempts; i++) {
            BlockPos anchor = canopyCenter.offset(
                    random.nextInt(canopyRadius * 2 + 1) - canopyRadius,
                    random.nextInt(canopyDepth + height / 6) - canopyDepth / 2,
                    random.nextInt(canopyRadius * 2 + 1) - canopyRadius
            );
            this.tryPlaceVine(level, anchor, random);
        }

        int berryAttempts = config.juvenile() ? 3 + random.nextInt(3) : 8 + random.nextInt(6);
        for (int i = 0; i < berryAttempts; i++) {
            BlockPos leafPos = canopyCenter.offset(
                    random.nextInt(canopyRadius * 2 + 1) - canopyRadius,
                    random.nextInt(canopyDepth + height / 7) - canopyDepth / 2,
                    random.nextInt(canopyRadius * 2 + 1) - canopyRadius
            );
            this.tryPlaceGlowVineUnderLeaf(level, leafPos, random, 2 + random.nextInt(5));
        }
    }

    protected void placeFlyingSquirrelNest(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int height) {
        if (config.juvenile() || trunkRadius < 2 || random.nextFloat() >= config.flyingSquirrelNestChance()) {
            return;
        }

        Direction opening = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction sideways = opening.getClockWise();
        boolean expandedHollow = random.nextFloat() < config.flyingSquirrelHollowChance();
        int chamberDepth = expandedHollow ? Math.min(trunkRadius, 3) : 1;
        int halfWidth = expandedHollow ? 1 : 0;
        int chamberHeight = expandedHollow ? 3 : 2;
        int minFloorY = Math.max(3, height / 7);
        int maxFloorY = Math.max(minFloorY, Math.min(height - chamberHeight - 4, height / 2));
        int floorY = minFloorY + random.nextInt(maxFloorY - minFloorY + 1);
        List<BlockPos> floorBlocks = new ArrayList<>();

        for (int lateral = -halfWidth; lateral <= halfWidth; lateral++) {
            for (int depth = 0; depth <= chamberDepth; depth++) {
                int radialDistance = trunkRadius - depth;
                if (radialDistance * radialDistance + lateral * lateral > trunkRadius * trunkRadius) {
                    continue;
                }
                BlockPos floorPos = origin.above(floorY - 1).relative(opening, trunkRadius - depth).relative(sideways, lateral);
                floorBlocks.add(floorPos.immutable());
                for (int y = 0; y < chamberHeight; y++) {
                    setBlock(level, floorPos.above(y + 1), Blocks.AIR.defaultBlockState());
                }
            }
        }

        if (floorBlocks.isEmpty()) {
            return;
        }

        // Place the marker block at the first floor position; it will spawn squirrels and
        // convert itself to COARSE_DIRT shortly after the chunk loads.
        // The tick is scheduled here on the WorldGenLevel because onPlace() is never
        // called during world generation, so the block cannot self-schedule.
        int squirrelCount = 1 + random.nextInt(3);
        BlockPos markerPos = floorBlocks.get(0);
        setBlock(level, markerPos, AntarchyObjects.SQUIRREL_NEST_BLOCK.get().defaultBlockState()
                .setValue(com.craisinlord.antarchy.content.block.OuranwoodSquirrelNestBlock.SQUIRREL_COUNT, squirrelCount));
        level.scheduleTick(markerPos, AntarchyObjects.SQUIRREL_NEST_BLOCK.get(), 20);
        for (int i = 1; i < floorBlocks.size(); i++) {
            setBlock(level, floorBlocks.get(i), Blocks.COARSE_DIRT.defaultBlockState());
        }

        this.placeNestAcorns(level, floorBlocks, random);
    }

    protected void placeWaspNest(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int height) {
        if (config.juvenile() || trunkRadius < 2 || random.nextFloat() >= config.waspNestChance()) {
            return;
        }

        Direction opening = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction span = opening.getClockWise();
        int nestY = 3 + random.nextInt(Math.max(1, Math.min(7, height / 8)));
        BlockPos nestPos = origin.above(nestY).relative(opening, trunkRadius - 1);
        BlockPos nestPos2 = nestPos.relative(span);
        BlockPos nestPos3 = nestPos.above();
        BlockPos nestPos4 = nestPos3.relative(span);
        if (!this.canFitWaspNest(level, nestPos, nestPos2, nestPos3, nestPos4)) {
            return;
        }

        this.clearWaspNestCavity(level, nestPos, opening, span);

        BlockState nestState = AntarchyObjects.WASP_NEST.get().defaultBlockState().setValue(WaspNestBlock.FACING, opening);
        setBlock(level, nestPos, nestState);
        setBlock(level, nestPos2, nestState);
        setBlock(level, nestPos3, nestState);
        setBlock(level, nestPos4, nestState);
    }

    protected boolean canFitWaspNest(WorldGenLevel level, BlockPos nestPos, BlockPos nestPos2, BlockPos nestPos3, BlockPos nestPos4) {
        return this.canFitWaspNestBlock(level, nestPos)
                && this.canFitWaspNestBlock(level, nestPos2)
                && this.canFitWaspNestBlock(level, nestPos3)
                && this.canFitWaspNestBlock(level, nestPos4);
    }

    protected boolean canFitWaspNestBlock(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LOGS) || canReplace(level, pos);
    }

    protected void clearWaspNestCavity(WorldGenLevel level, BlockPos nestPos, Direction opening, Direction span) {
        BlockPos anchor = nestPos.relative(opening);
        for (int forward = 0; forward <= 3; forward++) {
            BlockPos front = anchor.relative(opening, forward);
            for (int up = -1; up <= 3; up++) {
                for (int side = -1; side <= 2; side++) {
                    BlockPos clearPos = front.above(up).relative(span, side);
                    if (!level.getBlockState(clearPos).isAir()) {
                        setBlock(level, clearPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    protected void placeNestAcorns(WorldGenLevel level, List<BlockPos> floorBlocks, RandomSource random) {
        List<BlockPos> plantablePositions = new ArrayList<>();
        BlockState acornState = AntarchyObjects.OURANWOOD_ACORN_BLOCK.get().defaultBlockState();
        for (BlockPos floorPos : floorBlocks) {
            BlockPos acornPos = floorPos.above();
            if (!level.isEmptyBlock(acornPos) || !acornState.canSurvive(level, acornPos)) {
                continue;
            }
            plantablePositions.add(acornPos.immutable());
        }

        int acornsToPlace = Math.min(plantablePositions.size(), 1 + random.nextInt(3));
        for (int i = 0; i < acornsToPlace; i++) {
            int index = random.nextInt(plantablePositions.size());
            BlockPos acornPos = plantablePositions.remove(index);
            setBlock(level, acornPos, acornState);
        }
    }

    protected void spawnFlyingSquirrels(WorldGenLevel level, List<BlockPos> interiorSpaces, RandomSource random) {
        if (interiorSpaces.isEmpty() || !(level.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        int squirrelCount = 1 + random.nextInt(3);
        for (int i = 0; i < squirrelCount; i++) {
            FlyingSquirrelEntity squirrel = AntarchyObjects.FLYING_SQUIRREL.get().create(serverLevel);
            if (squirrel == null) {
                continue;
            }

            BlockPos targetPos = interiorSpaces.get(random.nextInt(interiorSpaces.size()));
            if (!this.canSpawnSquirrelInHollow(level, targetPos)) {
                squirrel.discard();
                continue;
            }
            squirrel.moveTo(
                    targetPos.getX() + 0.2D + random.nextDouble() * 0.6D,
                    targetPos.getY() + 0.05D,
                    targetPos.getZ() + 0.2D + random.nextDouble() * 0.6D,
                    random.nextFloat() * 360.0F,
                    0.0F
            );
            serverLevel.addFreshEntity(squirrel);
        }
    }

    protected void placeCaterpillarChrysalis(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, BlockPos canopyCenter, int canopyRadius, int canopyDepth) {
        // Tree-grown chrysalis placement is intentionally disabled for now.
    }

    /**
     * Finds a log or leaf block in the canopy that has sufficient clear space beneath it.
     * {@code clearBelow} is the vertical depth to check; {@code clearRadius} is the XZ half-width (0 = 1x1, 1 = 3x3).
     */
    protected @Nullable BlockPos findCanopyAnchor(WorldGenLevel level, RandomSource random, BlockPos canopyCenter, int canopyRadius, int canopyDepth, int clearBelow, int clearRadius, int attempts) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r = Math.max(1, canopyRadius - 1);
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(r * 2 + 1) - r;
            int dz = random.nextInt(r * 2 + 1) - r;

            int cx = canopyCenter.getX() + dx;
            int cz = canopyCenter.getZ() + dz;

            for (int dy = canopyDepth / 4; dy >= -canopyDepth / 2; dy--) {
                pos.set(cx, canopyCenter.getY() + dy, cz);
                BlockState anchorState = level.getBlockState(pos);
                if (!anchorState.is(BlockTags.LOGS) && !anchorState.is(BlockTags.LEAVES)) continue;

                boolean clear = true;
                outer:
                for (int drop = 1; drop <= clearBelow; drop++) {
                    for (int bx = -clearRadius; bx <= clearRadius; bx++) {
                        for (int bz = -clearRadius; bz <= clearRadius; bz++) {
                            pos.set(cx + bx, canopyCenter.getY() + dy - drop, cz + bz);
                            BlockState below = level.getBlockState(pos);
                            if (below.isSolid() && !below.is(BlockTags.LEAVES)) { clear = false; break outer; }
                        }
                    }
                }
                if (clear) return new BlockPos(cx, canopyCenter.getY() + dy, cz);
            }
        }
        return null;
    }

    protected boolean canSpawnSquirrelInHollow(WorldGenLevel level, BlockPos targetPos) {
        return level.isEmptyBlock(targetPos)
                && level.isEmptyBlock(targetPos.above())
                && !level.getBlockState(targetPos.below()).canBeReplaced();
    }

    protected void adaptTerrain(WorldGenLevel level, BlockPos origin, RandomSource random, int trunkRadius) {
        int moundRadius = trunkRadius + 4;
        int moundRadiusSq = moundRadius * moundRadius;
        int baseY = origin.getY() - 1;
        for (int x = -moundRadius; x <= moundRadius; x++) {
            for (int z = -moundRadius; z <= moundRadius; z++) {
                int distanceSq = x * x + z * z;
                if (distanceSq > moundRadiusSq) {
                    continue;
                }

                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                int currentTopY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) - 1;
                int falloff = (int) Math.floor(Math.sqrt(distanceSq) / 2.1D);
                int desiredTopY = baseY - falloff + (distanceSq <= trunkRadius * trunkRadius ? 0 : random.nextInt(2));
                if (currentTopY > desiredTopY) {
                    continue;
                }

                for (int y = currentTopY + 1; y <= desiredTopY; y++) {
                    BlockPos targetPos = new BlockPos(worldX, y, worldZ);
                    if (canReplaceTerrain(level, targetPos)) {
                        setBlock(level, targetPos, soilState(random));
                    }
                }
            }
        }

    }

    protected void refreshTerrainSurface(WorldGenLevel level, BlockPos origin, RandomSource random, int radius) {
        int radiusSq = radius * radius;
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - 6);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 4);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSq) {
                    continue;
                }

                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                BlockPos topSoilPos = null;

                for (int y = maxY; y >= minY; y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = level.getBlockState(pos);
                    if (!isTreeSurfaceSoil(state)) {
                        continue;
                    }

                    BlockPos abovePos = pos.above();
                    if (!level.getBlockState(abovePos).isAir()) {
                        if (state.is(Blocks.MOSS_BLOCK) && isBaseTrunkBlock(level.getBlockState(abovePos))) {
                            topSoilPos = pos;
                            break;
                        }
                        if (isSurfaceCoverState(state)) {
                            setBlock(level, pos, soilState(random));
                        }
                        continue;
                    }

                    topSoilPos = pos;
                    break;
                }

                if (topSoilPos == null) {
                    continue;
                }

                BlockState topState = level.getBlockState(topSoilPos);
                if (!isSurfaceCoverState(topState)) {
                    setBlock(level, topSoilPos, surfaceState(random));
                }
            }
        }
    }

    protected void adaptAndRefreshTerrain(WorldGenLevel level, BlockPos origin, RandomSource random, int trunkRadius) {
        int radius = trunkRadius + 4;
        int radiusSq = radius * radius;
        int trunkRadiusSq = trunkRadius * trunkRadius;
        int baseY = origin.getY() - 1;
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - 6);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 4);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distanceSq = x * x + z * z;
                if (distanceSq > radiusSq) {
                    continue;
                }

                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;

                // Raise soil to form a mound around the base (from adaptTerrain)
                int currentTopY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) - 1;
                int falloff = (int) Math.floor(Math.sqrt(distanceSq) / 2.1D);
                int desiredTopY = baseY - falloff + (distanceSq <= trunkRadiusSq ? 0 : random.nextInt(2));
                for (int y = currentTopY + 1; y <= desiredTopY; y++) {
                    BlockPos targetPos = new BlockPos(worldX, y, worldZ);
                    if (canReplaceTerrain(level, targetPos)) {
                        setBlock(level, targetPos, soilState(random));
                    }
                }

                // Set correct surface type on top soil block (from refreshTerrainSurface)
                BlockPos topSoilPos = null;
                for (int y = maxY; y >= minY; y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = level.getBlockState(pos);
                    if (!isTreeSurfaceSoil(state)) {
                        continue;
                    }
                    BlockPos abovePos = pos.above();
                    if (!level.getBlockState(abovePos).isAir()) {
                        if (state.is(Blocks.MOSS_BLOCK) && isBaseTrunkBlock(level.getBlockState(abovePos))) {
                            topSoilPos = pos;
                            break;
                        }
                        if (isSurfaceCoverState(state)) {
                            setBlock(level, pos, soilState(random));
                        }
                        continue;
                    }
                    topSoilPos = pos;
                    break;
                }
                if (topSoilPos != null) {
                    BlockState topState = level.getBlockState(topSoilPos);
                    if (!isSurfaceCoverState(topState)) {
                        setBlock(level, topSoilPos, surfaceState(random));
                    }
                }
            }
        }
    }

    protected void stabilizeBase(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random, int radius) {
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSq) {
                    continue;
                }

                BlockPos topPos = origin.offset(x, 0, z);
                for (int y = topPos.getY() - 1; y > level.getMinBuildHeight(); y--) {
                    BlockPos belowPos = new BlockPos(topPos.getX(), y, topPos.getZ());
                    BlockState belowState = level.getBlockState(belowPos);
                    if (belowState.isFaceSturdy(level, belowPos, Direction.UP) && !canReplaceTerrain(level, belowPos)) {
                        break;
                    }

                    if (canReplaceTerrain(level, belowPos)) {
                        setBlock(level, belowPos, this.selectTrunkState(config, random, belowPos, belowPos.getY() - origin.getY(), 32));
                    } else {
                        break;
                    }
                }
            }
        }
    }

    protected void fillCircle(WorldGenLevel level, BlockPos center, int radius, StateFactory stateFactory) {
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSq) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                BlockState state = stateFactory.create(targetPos);
                if (canReplace(level, targetPos)) {
                    setBlock(level, targetPos, state);
                }
            }
        }
    }

    protected void fillIrregularTrunkCircle(WorldGenLevel level, BlockPos center, int radius, OuranwoodTreeConfiguration config, RandomSource random, int baseY, int height) {
        int radiusSq = radius * radius;
        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                int distanceSq = x * x + z * z;
                if (distanceSq > radiusSq + 1) {
                    continue;
                }

                if (distanceSq == radiusSq + 1 && random.nextFloat() < 0.86F) {
                    continue;
                }

                if (distanceSq >= Math.max(1, radiusSq - radius) && random.nextFloat() < 0.04F) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                if (canReplace(level, targetPos)) {
                    setBlock(level, targetPos, this.selectTrunkState(config, random, targetPos, targetPos.getY() - baseY, height));
                }
            }
        }
    }

    protected BlockState selectTrunkState(OuranwoodTreeConfiguration config, RandomSource random, BlockPos pos, int localY, int height) {
        BlockState baseState = config.trunkProvider().getState(random, pos);
        if (localY <= 1) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }

        if (localY <= 4) {
            float mossChance = Mth.lerp((localY - 2) / 2.0F, 0.8F, 0.2F);
            if (random.nextFloat() < mossChance) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            return this.getMossyTrunkState(baseState);
        }

        if (localY <= 10) {
            float mossyChance = Mth.lerp((localY - 5) / 5.0F, 0.95F, 0.2F);
            if (random.nextFloat() < mossyChance) {
                return this.getMossyTrunkState(baseState);
            }
            return baseState;
        }

        float mossChance = this.getMossChance(localY, height);
        if (mossChance <= 0.0F || random.nextFloat() > mossChance) {
            return baseState;
        }

        return this.getMossyTrunkState(baseState);
    }

    protected BlockState getMossyTrunkState(BlockState baseState) {
        return switch (baseState.getValue(RotatedPillarBlock.AXIS)) {
            case X, Y, Z -> AntarchyObjects.MOSSY_OURANWOOD_WOOD.get().defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, baseState.getValue(RotatedPillarBlock.AXIS));
        };
    }

    protected float getMossChance(int localY, int height) {
        int fullyMossyBand = Mth.clamp(height / 20, 2, 4);
        int gradientTop = Mth.clamp(height / 12, 5, 10);
        if (localY <= fullyMossyBand) {
            return 0.9F;
        }

        if (localY <= gradientTop) {
            float t = (localY - fullyMossyBand) / (float) Math.max(1, gradientTop - fullyMossyBand);
            return Mth.lerp(t, 0.75F, 0.08F);
        }

        return 0.03F;
    }

    protected void fillLeafCircle(WorldGenLevel level, BlockPos center, OuranwoodTreeConfiguration config, RandomSource random, int radius, boolean dense) {
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distanceSq = x * x + z * z;
                if (distanceSq > radiusSq) {
                    continue;
                }

                if (!dense && distanceSq > radiusSq - radius && random.nextFloat() < 0.18F) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                if (!canReplace(level, targetPos)) {
                    continue;
                }

                this.setLeaf(level, targetPos, config, random);
            }
        }
    }

    protected void fillBranchLeafCluster(WorldGenLevel level, BlockPos center, OuranwoodTreeConfiguration config, RandomSource random, int radius, boolean dense) {
        this.fillLeafCircle(level, center, config, random, radius, dense);
        if (radius > 2) {
            this.fillLeafCircle(level, center.above(), config, random, radius - 1, true);
            this.fillLeafCircle(level, center.below(), config, random, radius - 1, true);
        }
        if (radius >= 6) {
            this.tryPlaceHiddenSupportLog(level, center, config, random);
        }
    }

    protected void setLeaf(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random) {
        setBlock(level, pos, config.foliageProvider().getState(random, pos));
    }

    protected void tryPlaceLeaf(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random) {
        if (canReplace(level, pos)) {
            this.setLeaf(level, pos, config, random);
        }
    }

    protected void tryPlaceHiddenSupportLog(WorldGenLevel level, BlockPos pos, OuranwoodTreeConfiguration config, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LeavesBlock)) {
            return;
        }

        int leafNeighbors = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.getBlock() instanceof LeavesBlock) {
                leafNeighbors++;
            }
        }

        if (leafNeighbors < 5) {
            return;
        }

        setBlock(level, pos, config.trunkProvider().getState(random, pos));
    }

    protected void pruneExposedTopLogs(WorldGenLevel level, BlockPos origin, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int canopyRadius, int canopyDepth, int height) {
        int horizontalRadius = canopyRadius + 6;
        int minY = canopyCenter.getY() - Math.max(4, canopyDepth / 2 + 1);
        int maxY = origin.getY() + height + canopyDepth + 6;
        for (int x = origin.getX() - horizontalRadius; x <= origin.getX() + horizontalRadius; x++) {
            for (int z = origin.getZ() - horizontalRadius; z <= origin.getZ() + horizontalRadius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.LOGS)) {
                        continue;
                    }

                    if (this.isWithinTrunkCore(origin, pos, trunkRadius, canopyCenter.getY())) {
                        continue;
                    }

                    int leafNeighbors = this.countLeafNeighbors(level, pos);
                    int openFaces = this.countOpenFaces(level, pos);
                    boolean topExposed = level.getBlockState(pos.above()).isAir();
                    boolean lowerCanopyStray = y <= canopyCenter.getY()
                            && leafNeighbors >= 3
                            && openFaces >= 2;
                    if (!(topExposed && leafNeighbors >= 1) && !lowerCanopyStray) {
                        continue;
                    }

                    this.setLeaf(level, pos, config, random);
                }
            }
        }
    }

    protected void coverExposedBranchSides(WorldGenLevel level, BlockPos origin, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int trunkRadius, int canopyRadius, int canopyDepth, int height) {
        int horizontalRadius = canopyRadius + 6;
        int minY = Math.max(origin.getY() + Math.max(4, height / 4), canopyCenter.getY() - canopyDepth - 3);
        int maxY = origin.getY() + height + canopyDepth + 4;

        for (int x = origin.getX() - horizontalRadius; x <= origin.getX() + horizontalRadius; x++) {
            for (int z = origin.getZ() - horizontalRadius; z <= origin.getZ() + horizontalRadius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.LOGS) || this.isWithinTrunkCore(origin, pos, trunkRadius, canopyCenter.getY())) {
                        continue;
                    }

                    if (this.countLeafNeighbors(level, pos) < 1) {
                        continue;
                    }

                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos sidePos = pos.relative(direction);
                        if (!level.getBlockState(sidePos).isAir()) {
                            continue;
                        }

                        if (!this.canWrapBranchSide(level, pos, direction)) {
                            continue;
                        }

                        this.tryPlaceLeaf(level, sidePos, config, random);
                    }
                }
            }
        }
    }

    protected void updateLeafDistances(WorldGenLevel level, BlockPos origin, int height, int canopyRadius, int canopyDepth) {
        int horizontalRadius = canopyRadius + 2;
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - 2);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + height + canopyDepth + 8);
        List<BlockPos> leafPositions = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = origin.getX() - horizontalRadius; x <= origin.getX() + horizontalRadius; x++) {
            for (int z = origin.getZ() - horizontalRadius; z <= origin.getZ() + horizontalRadius; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).getBlock() instanceof LeavesBlock) {
                        leafPositions.add(cursor.immutable());
                    }
                }
            }
        }

        for (int pass = 1; pass <= 16; pass++) {
            boolean anyChanged = false;
            for (BlockPos pos : leafPositions) {
                BlockState state = level.getBlockState(pos);
                IntegerProperty distanceProperty = this.getLeafDistanceProperty(state);
                if (!(state.getBlock() instanceof LeavesBlock) || distanceProperty == null) {
                    continue;
                }

                int computedDistance = this.computeLeafDistance(level, pos, state);
                if (computedDistance == state.getValue(distanceProperty)) {
                    continue;
                }

                BlockState updatedState;
                if (state.getBlock() instanceof OuranwoodLeavesBlock) {
                    updatedState = OuranwoodLeavesBlock.setOuranwoodDistanceForWorldgen(state, computedDistance);
                } else {
                    updatedState = state.setValue(distanceProperty, computedDistance);
                }
                setBlock(level, pos, updatedState);
                anyChanged = true;
            }
            if (!anyChanged) break;
        }
    }

    protected void placeHangingAcorns(WorldGenLevel level, BlockPos canopyCenter, OuranwoodTreeConfiguration config, RandomSource random, int canopyRadius, int canopyDepth) {
        int maxAcorns = config.juvenile() ? 12 + random.nextInt(6) : 40 + random.nextInt(20);
        int attempts = maxAcorns * 12;
        int horizontalRadius = canopyRadius + (config.juvenile() ? 2 : 6);
        int minYOffset = -Math.max(3, canopyDepth);
        int maxYOffset = Math.max(2, canopyDepth / 2);
        int placed = 0;

        for (int i = 0; i < attempts && placed < maxAcorns; i++) {
            int offsetX = random.nextInt(horizontalRadius * 2 + 1) - horizontalRadius;
            int offsetZ = random.nextInt(horizontalRadius * 2 + 1) - horizontalRadius;
            int offsetY = minYOffset + random.nextInt(maxYOffset - minYOffset + 1);
            if (random.nextFloat() < 0.72F) {
                int branchBandMin = -Math.max(3, canopyDepth * 3 / 4);
                int branchBandMax = Math.max(-1, canopyDepth / 5);
                offsetY = branchBandMin + random.nextInt(branchBandMax - branchBandMin + 1);
            }

            BlockPos leafPos = canopyCenter.offset(offsetX, offsetY, offsetZ);
            BlockState leafState = level.getBlockState(leafPos);
            if (!(leafState.getBlock() instanceof LeavesBlock)) {
                continue;
            }

            BlockPos acornPos = leafPos.below();
            if (!level.isEmptyBlock(acornPos)) {
                continue;
            }

            if (!this.canHangAcornFromLeaf(level, leafPos)) {
                continue;
            }

            int openSides = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(acornPos.relative(direction)).isAir()) {
                    openSides++;
                }
            }

            if (openSides < 1) {
                continue;
            }

            setBlock(level, acornPos, OuranwoodLeavesBlock.createHangingAcornState(random.nextInt(3)));
            placed++;
        }
    }

    protected boolean canHangAcornFromLeaf(WorldGenLevel level, BlockPos leafPos) {
        if (level.getBlockState(leafPos.above()).is(BlockTags.LOGS) || level.getBlockState(leafPos.above(2)).is(BlockTags.LOGS)) {
            return true;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(leafPos.relative(direction)).is(BlockTags.LOGS)
                    || level.getBlockState(leafPos.relative(direction).above()).is(BlockTags.LOGS)) {
                return true;
            }
        }

        int nearbyLeaves = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(leafPos.relative(direction)).getBlock() instanceof LeavesBlock) {
                nearbyLeaves++;
            }
            if (level.getBlockState(leafPos.relative(direction).above()).getBlock() instanceof LeavesBlock) {
                nearbyLeaves++;
            }
        }
        return nearbyLeaves >= 2;
    }

    protected int countLeafNeighbors(WorldGenLevel level, BlockPos pos) {
        int leafNeighbors = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof LeavesBlock) {
                leafNeighbors++;
            }
        }
        return leafNeighbors;
    }

    protected int countOpenFaces(WorldGenLevel level, BlockPos pos) {
        int openFaces = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.isAir()) {
                openFaces++;
            }
        }
        return openFaces;
    }

    protected boolean canWrapBranchSide(WorldGenLevel level, BlockPos logPos, Direction direction) {
        BlockPos sidePos = logPos.relative(direction);
        return this.isLeafOrLog(level, sidePos.above())
                || this.isLeafOrLog(level, sidePos.below())
                || this.isLeafOrLog(level, logPos.relative(direction.getClockWise()))
                || this.isLeafOrLog(level, logPos.relative(direction.getCounterClockWise()))
                || this.isLeafOrLog(level, logPos.above())
                || this.isLeafOrLog(level, logPos.below());
    }

    protected boolean isWithinTrunkCore(BlockPos origin, BlockPos pos, int trunkRadius, int canopyCenterY) {
        if (pos.getY() > canopyCenterY) {
            return false;
        }

        int dx = pos.getX() - origin.getX();
        int dz = pos.getZ() - origin.getZ();
        int protectedRadius = Math.max(1, trunkRadius - 1);
        return dx * dx + dz * dz <= protectedRadius * protectedRadius;
    }

    protected boolean isLeafOrLog(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof LeavesBlock || state.is(BlockTags.LOGS);
    }

    protected IntegerProperty getLeafDistanceProperty(BlockState state) {
        if (state.getBlock() instanceof OuranwoodLeavesBlock && state.hasProperty(OuranwoodLeavesBlock.OURANWOOD_DISTANCE)) {
            return OuranwoodLeavesBlock.OURANWOOD_DISTANCE;
        }

        if (state.hasProperty(LeavesBlock.DISTANCE)) {
            return LeavesBlock.DISTANCE;
        }

        return null;
    }

    protected int getLeafDistanceLimit(BlockState state) {
        if (state.getBlock() instanceof OuranwoodLeavesBlock) {
            return OuranwoodLeavesBlock.OURANWOOD_MAX_DISTANCE + 1;
        }

        return 7;
    }

    protected int computeLeafDistance(WorldGenLevel level, BlockPos pos, BlockState state) {
        int bestDistance = this.getLeafDistanceLimit(state);
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(BlockTags.LOGS)) {
                return 1;
            }

            IntegerProperty neighborProperty = this.getLeafDistanceProperty(neighbor);
            if (neighbor.getBlock() instanceof LeavesBlock && neighborProperty != null) {
                bestDistance = Math.min(bestDistance, neighbor.getValue(neighborProperty) + 1);
            }
        }

        return Math.min(this.getLeafDistanceLimit(state), bestDistance);
    }

    protected boolean hasLeafNeighbor(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof LeavesBlock) {
                return true;
            }
        }
        return false;
    }

    protected void tryPlaceVine(WorldGenLevel level, BlockPos anchor, RandomSource random) {
        if (!this.canSupportVine(level, anchor)) {
            return;
        }

        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        this.tryPlaceAttachedVine(level, anchor, side, 2 + random.nextInt(5));
    }

    protected void tryPlaceAttachedVine(WorldGenLevel level, BlockPos supportPos, Direction outwardDirection, int length) {
        if (!this.canSupportVine(level, supportPos)) {
            return;
        }

        BlockPos vinePos = supportPos.relative(outwardDirection);
        if (!level.isEmptyBlock(vinePos)) {
            return;
        }

        this.placeVineColumn(level, vinePos, outwardDirection.getOpposite(), length);
    }

    protected void placeVineColumn(WorldGenLevel level, BlockPos startPos, Direction attachedFace, int length) {
        BlockState vineState = vineStateForAttachment(attachedFace);
        if (vineState == null) {
            return;
        }

        for (int step = 0; step < length; step++) {
            BlockPos targetPos = startPos.below(step);
            if (!level.isEmptyBlock(targetPos)) {
                break;
            }
            setBlock(level, targetPos, vineState);
        }
    }

    protected void tryPlaceGlowVineUnderLeaf(WorldGenLevel level, BlockPos leafPos, RandomSource random, int length) {
        if (!AntarchySettings.glowVinesUnderLeaves()) {
            return;
        }

        BlockPos anchorPos = this.findLeafAnchor(level, leafPos);
        if (anchorPos == null || !level.isEmptyBlock(anchorPos.below())) {
            return;
        }

        for (int step = 1; step <= length; step++) {
            BlockPos targetPos = anchorPos.below(step);
            if (!level.isEmptyBlock(targetPos)) {
                break;
            }

            BlockState vineState = step == length
                    ? Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, Boolean.TRUE)
                    : Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, random.nextFloat() < 0.2F);
            setBlock(level, targetPos, vineState);
        }
    }

    protected boolean canSupportVine(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(Blocks.MOSS_BLOCK);
    }

    protected boolean canSupportGlowVine(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LEAVES);
    }

    @Nullable
    protected BlockPos findLeafAnchor(WorldGenLevel level, BlockPos pos) {
        for (int offset = 0; offset <= 2; offset++) {
            BlockPos candidate = pos.above(offset);
            if (this.canSupportGlowVine(level, candidate)) {
                return candidate;
            }
        }

        return null;
    }

    protected void placeTieredCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int crownRadius) {
        int crownHeight = config.juvenile() ? 3 : 5;
        for (int y = 0; y <= crownHeight; y++) {
            int layerRadius = Math.max(2, crownRadius - y + random.nextInt(3) - 1);
            BlockPos layerCenter = crownCenter.offset(random.nextInt(3) - 1, y, random.nextInt(3) - 1);
            this.fillLeafCircle(level, layerCenter, config, random, layerRadius, y >= crownHeight - 1);
            if (layerRadius > 3 && random.nextFloat() < 0.75F) {
                this.fillLeafCircle(level, layerCenter.above(), config, random, layerRadius - 2, true);
            }
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.8F) {
                BlockPos lobeCenter = crownCenter.relative(direction, Math.max(2, crownRadius - 1)).above(random.nextInt(3));
                this.fillLeafCircle(level, lobeCenter, config, random, Math.max(2, crownRadius - 2 + random.nextInt(2)), true);
            }
        }
    }

    protected void placeSplitCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int crownRadius) {
        int lobes = 3 + random.nextInt(3);
        this.fillLeafCircle(level, crownCenter, config, random, Math.max(3, crownRadius - 2), false);
        for (int i = 0; i < lobes; i++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int distance = Math.max(2, crownRadius - 2 + random.nextInt(2));
            BlockPos lobeCenter = crownCenter.relative(direction, distance).above(random.nextInt(3));
            int lobeRadius = Math.max(2, crownRadius - 2 + random.nextInt(3) - 1);
            this.fillLeafCircle(level, lobeCenter, config, random, lobeRadius, true);
            this.fillLeafCircle(level, lobeCenter.above(), config, random, Math.max(2, lobeRadius - 1), true);
        }
    }

    protected void placeWindsweptCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int crownRadius) {
        Direction sweep = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int crownHeight = config.juvenile() ? 3 : 5;
        for (int y = 0; y <= crownHeight; y++) {
            int offset = Math.min(2, y / 2);
            BlockPos layerCenter = crownCenter.relative(sweep, offset).offset(random.nextInt(3) - 1, y, random.nextInt(3) - 1);
            int layerRadius = Math.max(2, crownRadius - y + (y < 2 ? 1 : 0));
            this.fillLeafCircle(level, layerCenter, config, random, layerRadius, y >= crownHeight - 1);
        }

        BlockPos tailCenter = crownCenter.relative(sweep.getOpposite(), Math.max(2, crownRadius - 2));
        this.fillLeafCircle(level, tailCenter, config, random, Math.max(2, crownRadius - 3), false);
    }

    protected void placeClusteredCrown(WorldGenLevel level, BlockPos crownCenter, OuranwoodTreeConfiguration config, RandomSource random, int crownRadius) {
        this.fillLeafCircle(level, crownCenter, config, random, Math.max(3, crownRadius - 1), false);
        int clusters = 4 + random.nextInt(3);
        for (int i = 0; i < clusters; i++) {
            BlockPos clusterCenter = crownCenter.offset(
                    random.nextInt(crownRadius + 1) - random.nextInt(crownRadius + 1),
                    random.nextInt(4),
                    random.nextInt(crownRadius + 1) - random.nextInt(crownRadius + 1)
            );
            int clusterRadius = Math.max(2, crownRadius - 3 + random.nextInt(3));
            this.fillLeafCircle(level, clusterCenter, config, random, clusterRadius, true);
            if (random.nextFloat() < 0.65F) {
                this.fillLeafCircle(level, clusterCenter.above(), config, random, Math.max(2, clusterRadius - 1), true);
            }
        }
    }

    protected static BlockState vineStateForAttachment(Direction attachedFace) {
        BlockState state = Blocks.VINE.defaultBlockState();
        return switch (attachedFace) {
            case NORTH -> state.setValue(VineBlock.NORTH, Boolean.TRUE);
            case SOUTH -> state.setValue(VineBlock.SOUTH, Boolean.TRUE);
            case EAST -> state.setValue(VineBlock.EAST, Boolean.TRUE);
            case WEST -> state.setValue(VineBlock.WEST, Boolean.TRUE);
            default -> null;
        };
    }

    protected static boolean canGrowOn(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    protected static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof LeavesBlock;
    }

    protected static boolean canReplaceTerrain(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof LeavesBlock || !state.getFluidState().isEmpty();
    }

    protected static BlockState soilState(RandomSource random) {
        return random.nextFloat() < 0.35F ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState();
    }

    protected static BlockState surfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.45F) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        if (roll < 0.8F) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        return Blocks.ROOTED_DIRT.defaultBlockState();
    }

    protected static boolean isSurfaceCoverState(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK);
    }

    protected static boolean isBaseTrunkBlock(BlockState state) {
        return state.is(Blocks.MOSS_BLOCK) || state.is(BlockTags.LOGS);
    }

    protected static boolean isTreeSurfaceSoil(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.ROOTED_DIRT) || isSurfaceCoverState(state);
    }

    @FunctionalInterface
    protected interface StateFactory {
        BlockState create(BlockPos pos);
    }

    protected record NestPlacement(List<BlockPos> interiorSpaces) {
    }
}
