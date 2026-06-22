package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
        spawnGuaranteedCocoon(context.level(), context.origin(), context.config(), context.random());
        return true;
    }

    private void spawnGuaranteedCocoon(WorldGenLevel level, BlockPos origin, OuranwoodTreeConfiguration config, RandomSource random) {
        if (!(level.getLevel() instanceof ServerLevel serverLevel)) return;

        // Pick a branch direction and a Y somewhere in the lower-mid trunk
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int branchY = 10 + random.nextInt(8);
        int branchLength = 4 + random.nextInt(3); // 4–6 logs out from the trunk

        // Place the branch logs, then re-assert the anchor tip so it's always a log
        for (int i = 1; i <= branchLength; i++) {
            BlockPos p = origin.above(branchY).relative(dir, i);
            setBlock(level, p, config.trunkProvider().getState(random, p));
        }
        BlockPos anchor = origin.above(branchY).relative(dir, branchLength);

        // Clear everything below the anchor and spawn the cocooned brutalfly
        BrutalflyCocoonFeature.clearCocoonVolume(level, anchor);
        BrutalflyEntity brutalfly = AntarchyObjects.BRUTALFLY.get().create(serverLevel);
        if (brutalfly == null) return;
        brutalfly.moveTo(anchor.getX() + 0.5, anchor.getY() - 15.0, anchor.getZ() + 0.5,
                random.nextFloat() * 360.0F, 0.0F);
        brutalfly.setCocooned(true, anchor);
        brutalfly.setHealth(brutalfly.getMaxHealth());
        serverLevel.addFreshEntity(brutalfly);
    }
}
