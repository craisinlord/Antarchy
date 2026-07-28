package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class OuranwoodCocoonTreeFeature extends OuranwoodTreeFeature {

    public OuranwoodCocoonTreeFeature(Codec<OuranwoodTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OuranwoodTreeConfiguration> context) {
        if (!super.place(context)) return false;
        placeGuaranteedCocoon(context.level(), context.origin(), context.config(), context.random());
        return true;
    }

    private void placeGuaranteedCocoon(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random) {
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int branchY = 10 + random.nextInt(8);
        int branchLength = 4 + random.nextInt(3);

        for (int i = 1; i <= branchLength; i++) {
            BlockPos p = origin.above(branchY).relative(dir, i);
            setBlock(level, p, config.trunkProvider().getState(random, p));
        }
        BlockPos anchor = origin.above(branchY).relative(dir, branchLength);

        BrutalflyCocoonFeature.clearCocoonVolume(level, anchor);
        BrutalflyCocoonFeature.setCocoonSpawnerMarker(level, anchor);
    }
}
