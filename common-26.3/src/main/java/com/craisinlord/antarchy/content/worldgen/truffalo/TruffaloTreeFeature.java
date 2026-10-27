package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record TruffaloTreeFeature(
        Holder<BlockStateProvider> trunkProvider,
        IntProvider height,
        int tuftRadius,
        int tuftHeight
) implements Feature {
    public static final MapCodec<TruffaloTreeFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(TruffaloTreeFeature::trunkProvider),
            IntProviders.codec(4, 18).fieldOf("height").forGetter(TruffaloTreeFeature::height),
            Codec.INT.optionalFieldOf("tuft_radius", 1).forGetter(TruffaloTreeFeature::tuftRadius),
            Codec.INT.optionalFieldOf("tuft_height", 3).forGetter(TruffaloTreeFeature::tuftHeight)
    ).apply(instance, TruffaloTreeFeature::new));

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

    @Override
    public MapCodec<TruffaloTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos supportPos = origin.below();
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
            return false;
        }

        int height = this.height.sample(random);
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
        for (int dy = 0; dy < this.tuftHeight; dy++) {
            for (int dx = -this.tuftRadius; dx <= this.tuftRadius; dx++) {
                for (int dz = -this.tuftRadius; dz <= this.tuftRadius; dz++) {
                    if (!canReplace(level, capBase.offset(dx, dy, dz))) {
                        return false;
                    }
                }
            }
        }

        for (BlockPos pos : trunk) {
            setBlock(level, pos, this.trunkProvider.value().getState(level, random, pos));
        }

        Block tuft = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, color.getName() + "_truffalo_tuft"));
        BlockState tuftState = tuft.defaultBlockState();
        for (int dy = 0; dy < this.tuftHeight; dy++) {
            for (int dx = -this.tuftRadius; dx <= this.tuftRadius; dx++) {
                for (int dz = -this.tuftRadius; dz <= this.tuftRadius; dz++) {
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
