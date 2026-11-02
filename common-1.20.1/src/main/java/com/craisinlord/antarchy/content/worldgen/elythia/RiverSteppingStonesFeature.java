package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RiverSteppingStonesFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MAX_DEPTH = 10;
    private static final double BOAT_LANE_HALF_WIDTH = 2.0D;

    public RiverSteppingStonesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Optional<ElythiaRiverCrossing> found = ElythiaRiverCrossing.find(level, context.origin(), random, false, 1);
        if (found.isEmpty()) {
            return false;
        }

        ElythiaRiverCrossing crossing = found.get();
        int perpX = -crossing.dz();
        int perpZ = crossing.dx();
        int offset = 0;
        boolean placed = false;
        double laneCenter = crossing.steps() / 2.0D;
        int step = 1 + random.nextInt(2);
        while (step < crossing.steps()) {
            if (random.nextFloat() < 0.3F) {
                offset = Mth.clamp(offset + (random.nextBoolean() ? 1 : -1), -1, 1);
            }
            int x = crossing.x(step) + perpX * offset;
            int z = crossing.z(step) + perpZ * offset;
            placed |= this.placeStone(level, random, crossing, x, z, laneCenter);
            step += random.nextFloat() < (crossing.diagonal() ? 0.15F : 0.4F) ? 3 : 2;
        }
        return placed;
    }

    private boolean placeStone(WorldGenLevel level, RandomSource random, ElythiaRiverCrossing crossing, int x, int z, double laneCenter) {
        int waterY = crossing.waterY();
        int topY = random.nextFloat() < 0.15F ? waterY : waterY + 1;
        boolean large = random.nextFloat() < 0.7F;
        int dropped = large && random.nextFloat() < 0.2F ? 1 + random.nextInt(3) : -1;
        boolean placed = false;

        for (int cell = 0; cell < (large ? 4 : 1); cell++) {
            if (cell == dropped) {
                continue;
            }
            int cx = x + (cell & 1);
            int cz = z + (cell >> 1);
            if (!crossing.contains(cx, cz)) {
                continue;
            }
            int cellTop = cell > 0 && random.nextFloat() < 0.25F ? topY - 1 : topY;
            if (Math.abs(alongCrossing(crossing, cx, cz) - laneCenter) <= BOAT_LANE_HALF_WIDTH) {
                cellTop = Math.min(cellTop, waterY - 1);
            }
            placed |= this.placeColumn(level, random, cx, cz, waterY, cellTop);
        }
        return placed;
    }

    private static double alongCrossing(ElythiaRiverCrossing crossing, int x, int z) {
        int dot = (x - crossing.x(0)) * crossing.dx() + (z - crossing.z(0)) * crossing.dz();
        return dot / (double) (crossing.dx() * crossing.dx() + crossing.dz() * crossing.dz());
    }

    private boolean placeColumn(WorldGenLevel level, RandomSource random, int x, int z, int waterY, int topY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, waterY, z);
        int bedY = waterY;
        while (bedY > waterY - MAX_DEPTH && ElythiaRiverCrossing.isRiverFluid(level.getFluidState(pos.setY(bedY)))) {
            bedY--;
        }
        if (ElythiaRiverCrossing.isRiverFluid(level.getFluidState(pos.setY(bedY)))) {
            return false;
        }

        boolean placed = false;
        for (int y = bedY + 1; y <= topY; y++) {
            pos.setY(y);
            if (!canReplace(level.getBlockState(pos))) {
                continue;
            }
            level.setBlock(pos, y == topY ? topState(random) : bodyState(random), 2);
            placed = true;
        }

        if (placed && topY > waterY && random.nextFloat() < 0.15F) {
            BlockState top = level.getBlockState(pos.setY(topY));
            pos.setY(topY + 1);
            if (!top.is(Blocks.MOSS_BLOCK) && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.MOSS_CARPET.defaultBlockState(), 2);
            }
        }
        return placed;
    }

    private static BlockState bodyState(RandomSource random) {
        return random.nextFloat() < 0.65F ? Blocks.TUFF.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    }

    private static BlockState topState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.3F) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        return roll < 0.65F ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty();
    }
}
