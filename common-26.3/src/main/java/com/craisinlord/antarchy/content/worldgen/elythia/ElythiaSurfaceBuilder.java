package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.mixins.SurfaceRulesContextAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;
import org.jetbrains.annotations.Nullable;

public final class ElythiaSurfaceBuilder {
    private static final Constructor<?> CONTEXT_CONSTRUCTOR = contextConstructor();

    private ElythiaSurfaceBuilder() {
    }

    public record Root(MaterialRule rule, int stoneFloor) implements MaterialRule {
        public static final MapCodec<Root> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MaterialRule.CODEC.fieldOf("rule").forGetter(Root::rule),
                Codec.INT.fieldOf("stone_floor").forGetter(Root::stoneFloor)
        ).apply(instance, Root::new));

        @Override
        public MapCodec<Root> codec() {
            return CODEC;
        }

        @Override
        public RuleEvaluator compile(MaterialRuleContext context) {
            return this.rule.compile(context);
        }
    }

    public static void build(
            Root root,
            MaterialSystem system,
            RandomState randomState,
            BiomeManager biomeManager,
            WorldGenerationContext generationContext,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            @Nullable Set<Holder<Biome>> possibleBiomes,
            BlockState defaultBlock
    ) {
        int highestFilledSectionIndex = chunk.getHighestFilledSectionIndex();
        int maxBlockY = chunk.getSectionYFromSectionIndex(highestFilledSectionIndex) * 16 + 15;
        DensityVolume fullVolume = noiseChunk.volume();
        DensityVolume narrowedVolume = new DensityVolume(
                fullVolume.sizeX(),
                Math.max(maxBlockY - fullVolume.minBlockY() + 1, 1),
                fullVolume.sizeZ(),
                fullVolume.minBlockX(),
                fullVolume.minBlockY(),
                fullVolume.minBlockZ()
        );
        Function<BlockPos, Holder<Biome>> biomeGetter = biomeManager::getBiome;
        MaterialRuleContext context = newContext(system, randomState, narrowedVolume, noiseChunk.cachingSamplers(), biomeGetter, generationContext, possibleBiomes);
        SurfaceRulesContextAccessor accessor = (SurfaceRulesContextAccessor) (Object) context;
        RuleEvaluator rule = root.rule().compile(context);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int stoneFloor = root.stoneFloor();
        LevelHeightAccessor heightAccessor = chunk.getHeightAccessorForGeneration();
        int writeMinY = heightAccessor.getMinY();
        int writeMaxY = heightAccessor.getMaxY();
        int minY = chunk.getMinY();
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunkX + localX;
                int z = chunkZ + localZ;
                pos.set(x, 0, z);
                accessor.antarchy$updateXZ(x, z, surfaceGradientX(chunk, localX, localZ), surfaceGradientZ(chunk, localX, localZ));
                int minSurface = context.getMinSurfaceLevel();
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
                        accessor.antarchy$updateY(stoneAbove, stoneBelow, waterHeight, y);
                        if (state == defaultBlock && y >= writeMinY && y <= writeMaxY) {
                            BlockState replacement = rule.tryApply(x, y, z);
                            if (replacement != null) {
                                chunk.setBlockState(pos, replacement);
                                if (!replacement.getFluidState().isEmpty()) {
                                    chunk.markPosForPostProcessing(pos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int surfaceGradientX(ChunkAccess chunk, int localX, int localZ) {
        return chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.min(localX + 1, 15), localZ)
                - chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.max(localX - 1, 0), localZ);
    }

    private static int surfaceGradientZ(ChunkAccess chunk, int localX, int localZ) {
        return chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, Math.min(localZ + 1, 15))
                - chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, Math.max(localZ - 1, 0));
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

    private static MaterialRuleContext newContext(
            MaterialSystem system,
            RandomState randomState,
            DensityVolume volume,
            DensitySamplerSet samplers,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            WorldGenerationContext generationContext,
            @Nullable Set<Holder<Biome>> possibleBiomes
    ) {
        try {
            return (MaterialRuleContext) CONTEXT_CONSTRUCTOR.newInstance(system, randomState, volume, samplers, biomeGetter, generationContext, possibleBiomes);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create material rule context", exception);
        }
    }

    private static Constructor<?> contextConstructor() {
        Constructor<?> constructor = MaterialRuleContext.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor;
    }
}
