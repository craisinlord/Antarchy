package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynWormBoreDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> BORE_NOISE = ResourceKey.create(
            Registries.NOISE,
            new ResourceLocation(Antarchy.MODID, "cavaryn_worm_bore")
    );
    private static final ResourceLocation LOAM_ID = new ResourceLocation(Antarchy.MODID, "loam");
    private static final ResourceLocation MUCUS_ID = new ResourceLocation(Antarchy.MODID, "mucus");
    private static final Set<ResourceLocation> LINEABLE = Set.of(
            new ResourceLocation(Antarchy.MODID, "myrmite"),
            new ResourceLocation(Antarchy.MODID, "broodstone"),
            new ResourceLocation(Antarchy.MODID, "biomite"),
            new ResourceLocation(Antarchy.MODID, "biomite_turf"),
            new ResourceLocation(Antarchy.MODID, "loam"),
            new ResourceLocation("mud"),
            new ResourceLocation("dirt"),
            new ResourceLocation("coarse_dirt"),
            new ResourceLocation("rooted_dirt"),
            new ResourceLocation("podzol")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double LINING_DEPTH = 1.8;
    private static final float MUD_CHANCE = 0.16F;
    private static final float CASTING_CHANCE = 0.035F;
    private static final float MUCUS_CHANCE = 0.05F;

    public CavarynWormBoreDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(BORE_NOISE);
        CavarynWormBoreGrid grid = CavarynWormBoreGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        Block loam = block(LOAM_ID);
        Block mucus = block(MUCUS_ID);
        if (grid == null || loam == null) {
            return false;
        }

        RandomSource random = context.random();
        int originX = context.origin().getX() & ~15;
        int originZ = context.origin().getZ() & ~15;
        int minBuild = level.getMinBuildHeight() + 6;
        int maxBuild = level.getMaxBuildHeight() - 7;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = originX + dx;
                int z = originZ + dz;
                int[] range = grid.verticalRange(x, z);
                if (range == null) {
                    continue;
                }
                int minY = Math.max(minBuild, range[0]);
                int maxY = Math.min(maxBuild, range[1]);
                for (int y = minY; y <= maxY; y++) {
                    double distance = grid.signedDistance(x, y, z);
                    if (distance > LINING_DEPTH) {
                        continue;
                    }
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (distance > 0.0) {
                        if (isLineable(state)) {
                            level.setBlock(pos, random.nextFloat() < MUD_CHANCE ? Blocks.MUD.defaultBlockState() : loam.defaultBlockState(), 2);
                            placed = true;
                        }
                        continue;
                    }
                    if (distance < -2.5 || !state.isAir()) {
                        continue;
                    }
                    if (random.nextFloat() < CASTING_CHANCE && placeCasting(level, pos, loam, random)) {
                        placed = true;
                    } else if (mucus != null && random.nextFloat() < MUCUS_CHANCE && placeMucus(level, pos, mucus)) {
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private static boolean placeCasting(WorldGenLevel level, BlockPos pos, Block loam, RandomSource random) {
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        level.setBlock(pos, random.nextBoolean() ? Blocks.MUD.defaultBlockState() : loam.defaultBlockState(), 2);
        BlockPos above = pos.above();
        if (random.nextFloat() < 0.35F && level.getBlockState(above).isAir() && level.getBlockState(above.above()).isAir()) {
            level.setBlock(above, loam.defaultBlockState(), 2);
        }
        return true;
    }

    private static boolean placeMucus(WorldGenLevel level, BlockPos pos, Block mucus) {
        for (Direction direction : Direction.values()) {
            BlockPos attached = pos.relative(direction);
            BlockState attachedState = level.getBlockState(attached);
            if (attachedState.isAir() || !attachedState.isFaceSturdy(level, attached, direction.getOpposite())) {
                continue;
            }
            BlockState state = mucus.defaultBlockState();
            if (!state.hasProperty(MultifaceBlock.getFaceProperty(direction))) {
                return false;
            }
            state = state.setValue(MultifaceBlock.getFaceProperty(direction), true);
            if (state.canSurvive(level, pos)) {
                level.setBlock(pos, state, 2);
                return true;
            }
        }
        return false;
    }

    private static boolean isLineable(BlockState state) {
        return LINEABLE_BLOCKS.contains(state);
    }

    private static Block block(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
