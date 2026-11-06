package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.mixins.SurfaceRulesContextAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Constructor;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public final class ElythiaSurfaceBuilder extends SurfaceRules {
    private static final Constructor<?> CONTEXT_CONSTRUCTOR = contextConstructor();

    private ElythiaSurfaceBuilder() {
    }

    public record Root(RuleSource rule, int stoneFloor) implements RuleSource {
        public static final MapCodec<Root> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RuleSource.CODEC.fieldOf("rule").forGetter(Root::rule),
                Codec.INT.fieldOf("stone_floor").forGetter(Root::stoneFloor)
        ).apply(instance, Root::new));
        public static final KeyDispatchDataCodec<Root> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return KEY_CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return this.rule.apply(context);
        }
    }

    public static void build(
            Root root,
            SurfaceSystem system,
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            WorldGenerationContext generationContext,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            BlockState defaultBlock
    ) {
        Function<BlockPos, Holder<Biome>> biomeGetter = biomeManager::getBiome;
        Context context = newContext(system, randomState, chunk, noiseChunk, biomeGetter, biomes, generationContext);
        SurfaceRulesContextAccessor accessor = (SurfaceRulesContextAccessor) (Object) context;
        SurfaceRule rule = root.rule().apply(context);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int stoneFloor = root.stoneFloor();
        int minY = chunk.getMinBuildHeight();
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunkX + localX;
                int z = chunkZ + localZ;
                pos.set(x, 0, z);
                accessor.antarchy$updateXZ(x, z);
                int minSurface = accessor.antarchy$getMinSurfaceLevel();
                int stoneAbove = 0;
                int waterHeight = Integer.MIN_VALUE;
                int stoneBottom = Integer.MAX_VALUE;
                for (int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, localZ) + 1; y >= minY; y--) {
                    if (y < minSurface && y >= stoneFloor) {
                        y = stoneFloor;
                        continue;
                    }
                    BlockState state = chunk.getBlockState(pos.setY(y));
                    if (state.isAir()) {
                        stoneAbove = 0;
                        waterHeight = Integer.MIN_VALUE;
                    } else if (!state.getFluidState().isEmpty()) {
                        if (waterHeight == Integer.MIN_VALUE) {
                            waterHeight = y + 1;
                        }
                    } else {
                        int stoneBelow = 0;
                        if (y >= minSurface) {
                            if (stoneBottom >= y) {
                                stoneBottom = findStoneBottom(chunk, pos, y, minY);
                                pos.setY(y);
                            }
                            stoneBelow = y - stoneBottom + 1;
                        }
                        stoneAbove++;
                        accessor.antarchy$updateY(stoneAbove, stoneBelow, waterHeight, x, y, z);
                        if (state == defaultBlock) {
                            BlockState replacement = rule.tryApply(x, y, z);
                            if (replacement != null) {
                                chunk.setBlockState(pos, replacement, false);
                                if (!replacement.getFluidState().isEmpty()) {
                                    chunk.markPosForPostprocessing(pos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int findStoneBottom(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int y, int minY) {
        for (int below = y - 1; below >= minY - 1; below--) {
            BlockState state = chunk.getBlockState(pos.setY(below));
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                return below + 1;
            }
        }
        return DimensionType.WAY_BELOW_MIN_Y;
    }

    private static Context newContext(
            SurfaceSystem system,
            RandomState randomState,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Registry<Biome> biomes,
            WorldGenerationContext generationContext
    ) {
        try {
            return (Context) CONTEXT_CONSTRUCTOR.newInstance(system, randomState, chunk, noiseChunk, biomeGetter, biomes, generationContext);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create surface rule context", exception);
        }
    }

    private static Constructor<?> contextConstructor() {
        Constructor<?> constructor = Context.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor;
    }
}
