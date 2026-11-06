package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public class OuranwoodCocoonTreeFeature extends OuranwoodTreeFeature {
    public static final MapCodec<OuranwoodCocoonTreeFeature> CODEC = OuranwoodTreeConfiguration.CODEC.xmap(
            OuranwoodCocoonTreeFeature::new,
            OuranwoodCocoonTreeFeature::config
    );

    public OuranwoodCocoonTreeFeature(OuranwoodTreeConfiguration config) {
        super(config, Variant.COCOON);
    }

    public MapCodec<OuranwoodCocoonTreeFeature> codec() {
        return CODEC;
    }


    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!super.place(level, chunkGenerator, random, origin)) return false;
        placeGuaranteedCocoon(level, origin, this.config(), random);
        return true;
    }

    private void placeGuaranteedCocoon(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random) {
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int branchY = 10 + random.nextInt(8);
        int branchLength = 4 + random.nextInt(3);

        for (int i = 1; i <= branchLength; i++) {
            BlockPos p = origin.above(branchY).relative(dir, i);
            setBlock(level, p, config.trunkProvider().value().getState(level, random, p));
        }
        BlockPos anchor = origin.above(branchY).relative(dir, branchLength);

        BrutalflyCocoonFeature.clearCocoonVolume(level, anchor);
        BrutalflyCocoonFeature.setCocoonSpawnerMarker(level, anchor);
    }
}
