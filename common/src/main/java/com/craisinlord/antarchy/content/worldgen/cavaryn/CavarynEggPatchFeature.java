package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.CreepingHorrorEggBlock;
import com.craisinlord.antarchy.content.block.LurkingTerrorEggBlock;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CavarynEggPatchFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation AMBER_MOSS_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "amber_moss_block");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;
    private static final int MAX_RADIUS_Y = 3;

    public CavarynEggPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        Block amberMossBlock = getBlock(AMBER_MOSS_BLOCK_ID);
        BlockPos center = findCavityCenter(level, origin, random);
        if (center == null) {
            return false;
        }

        EggType eggType = random.nextBoolean() ? EggType.CREEPING_HORROR : EggType.LURKING_TERROR;
        return carveCavityAndPlaceEggs(level, center, random, eggType, amberMossBlock);
    }

    private static BlockPos findCavityCenter(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int y = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            y = Mth.clamp(y, level.getMinBuildHeight() + MAX_RADIUS_Y + 2, level.getMaxBuildHeight() - MAX_RADIUS_Y - 2);

            mutable.set(x, y, z);
            if (isSolidEnoughForCavity(level, mutable)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private static boolean isSolidEnoughForCavity(WorldGenLevel level, BlockPos center) {
        BlockState centerState = level.getBlockState(center);
        if (!centerState.blocksMotion() || centerState.is(Blocks.BEDROCK)) {
            return false;
        }

        int solidCount = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz += 2) {
                    mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.getBlockState(mutable).blocksMotion()) {
                        solidCount++;
                    }
                }
            }
        }
        return solidCount >= 9;
    }

    private boolean carveCavityAndPlaceEggs(WorldGenLevel level, BlockPos center, RandomSource random, EggType eggType, Block amberMossBlock) {
        int rx = 3 + random.nextInt(3);
        int ry = 2 + random.nextInt(2);
        int rz = 3 + random.nextInt(3);

        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double norm = (double)(dx * dx) / (rx * rx)
                                + (double)(dy * dy) / (ry * ry)
                                + (double)(dz * dz) / (rz * rz);
                    if (norm > 1.0) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState existing = level.getBlockState(pos);
                    if (!existing.is(Blocks.BEDROCK) && existing.blocksMotion() && existing.getFluidState().isEmpty()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        List<BlockPos> eggPositions = new ArrayList<>();
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry - 1; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) {
                        continue;
                    }
                    BlockPos below = pos.below();
                    BlockState belowState = level.getBlockState(below);
                    if (belowState.blocksMotion() && !belowState.isAir() && belowState.isFaceSturdy(level, below, Direction.UP)) {
                        eggPositions.add(pos);
                    }
                }
            }
        }

        if (eggPositions.isEmpty()) {
            return false;
        }

        boolean placedAny = false;
        for (BlockPos eggPos : eggPositions) {
            if (random.nextFloat() > 0.65f) {
                continue;
            }

            // Replace the floor block with amber moss so eggs hatch faster
            if (amberMossBlock != null) {
                BlockPos floorPos = eggPos.below();
                BlockState floorState = level.getBlockState(floorPos);
                if (floorState.blocksMotion() && !floorState.is(Blocks.BEDROCK) && floorState.getFluidState().isEmpty()) {
                    level.setBlock(floorPos, amberMossBlock.defaultBlockState(), 2);
                }
            }

            BlockState eggState = eggType == EggType.CREEPING_HORROR
                    ? AntarchyObjects.CREEPING_HORROR_EGGS.get().defaultBlockState()
                    : AntarchyObjects.LURKING_TERROR_EGGS.get().defaultBlockState();
            eggState = eggState.setValue(
                    eggType == EggType.CREEPING_HORROR ? CreepingHorrorEggBlock.EGGS : LurkingTerrorEggBlock.EGGS,
                    1 + random.nextInt(4));
            level.setBlock(eggPos, eggState, 3);
            placedAny = true;
        }

        return placedAny;
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }

    private enum EggType {
        CREEPING_HORROR,
        LURKING_TERROR
    }
}
