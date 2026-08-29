package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class TruffaloTreeFeature extends Feature<TruffaloTreeConfiguration> {
    private static final DyeColor[] TUFT_COLORS = {
            DyeColor.MAGENTA,
            DyeColor.ORANGE,
            DyeColor.RED,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.BLUE,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.PINK,
            DyeColor.PURPLE
    };

    public TruffaloTreeFeature(Codec<TruffaloTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TruffaloTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos supportPos = origin.below();
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
            return false;
        }

        RandomSource random = context.random();
        TruffaloTreeConfiguration config = context.config();
        int height = config.height().sample(random);
        DyeColor color = TUFT_COLORS[random.nextInt(TUFT_COLORS.length)];

        BlockPos[] trunk = new BlockPos[height];
        for (int i = 0; i < height; i++) {
            trunk[i] = origin.above(i);
        }
        BlockPos capBase = trunk[height - 1].above();

        for (BlockPos pos : trunk) {
            if (!canReplace(level, pos)) {
                return false;
            }
        }
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!canReplace(level, capBase.offset(dx, dy, dz))) {
                        return false;
                    }
                }
            }
        }

        for (BlockPos pos : trunk) {
            setBlock(level, pos, config.trunkProvider().getState(random, pos));
        }

        Block tuft = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, color.getName() + "_truffalo_tuft"));
        BlockState tuftState = tuft.defaultBlockState();
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setBlock(level, capBase.offset(dx, dy, dz), tuftState);
                }
            }
        }

        return true;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
