package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.Arrays;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;
import org.jspecify.annotations.Nullable;

public final class ElythiaAquifer implements Aquifer {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final double RESERVE_THRESHOLD = 0.5D;
    private static final double LIP_RESERVE = 0.0D;
    private static final double RIVER_RESERVE_LIMIT = 0.04D;
    private static final double RIM_RESERVE = 0.0D;
    private static final int SHALLOW_DEPTH = 64;
    private static final double CHANNEL_THRESHOLD = 0.05D;
    private static final double LUMEN_THRESHOLD = 0.5D;
    private static final double LIP_LOW = 0.25D;
    private static final double LIP_HIGH = 0.75D;
    private static final int RIVER_DEPTH_BAND = 8;
    private static final int MAX_FLOW = 7;
    private static final double MIN_SLOPE = 1.0E-4D;
    private static final int COLUMNS = 256;

    private final Aquifer delegate;
    private final int seaLevel;
    private final DensitySampler.Bound reserve;
    private final DensitySampler.Bound riverLevel;
    private final DensitySampler.Bound riverChannel;
    private final DensitySampler.Bound lumen;
    private final long[] columnKeys = new long[COLUMNS];
    private final double[] reserves = new double[COLUMNS];
    private final double[] levels = new double[COLUMNS];
    private final double[] channels = new double[COLUMNS];
    private final double[] lumens = new double[COLUMNS];
    private final int[] flows = new int[COLUMNS];
    private final BlockState[] waterStates = states(Blocks.WATER.defaultBlockState());
    private final BlockState[] lumenStates = states(AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState());
    private boolean shouldScheduleFluidUpdate;

    public ElythiaAquifer(Aquifer delegate, ElythiaHydrology hydrology, DensitySamplerSet samplers) {
        this.delegate = delegate;
        this.seaLevel = hydrology.seaLevel();
        this.reserve = samplers.get(hydrology.reserve());
        this.riverLevel = samplers.get(hydrology.riverLevel());
        this.riverChannel = samplers.get(hydrology.riverChannel());
        this.lumen = samplers.get(hydrology.lumen());
        Arrays.fill(this.columnKeys, Long.MAX_VALUE);
    }

    @Override
    public @Nullable BlockState computeSubstance(int blockX, int blockY, int blockZ, double density) {
        if (density > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }
        int column = this.column(blockX, blockY, blockZ);
        double reserve = this.reserves[column];
        if (reserve >= RESERVE_THRESHOLD) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        double level = this.levels[column];
        if (blockY < level + 1.0D && blockY >= level - RIVER_DEPTH_BAND && reserve < RIVER_RESERVE_LIMIT && this.channel(column, blockX, blockY, blockZ) > CHANNEL_THRESHOLD) {
            double lumen = this.lumen(column, blockX, blockY, blockZ);
            if (lumen > LIP_LOW && lumen < LIP_HIGH) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            BlockState[] states = lumen > LUMEN_THRESHOLD ? this.lumenStates : this.waterStates;
            if (blockY < level) {
                this.shouldScheduleFluidUpdate = reserve > LIP_RESERVE;
                return states[0];
            }
            int flow = this.flow(column, blockX, blockY, blockZ, level);
            if (flow > 0) {
                this.shouldScheduleFluidUpdate = false;
                return states[flow];
            }
        }
        if (blockY >= this.seaLevel) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        if (blockY >= this.seaLevel - SHALLOW_DEPTH) {
            this.shouldScheduleFluidUpdate = false;
            return reserve > RIM_RESERVE ? AIR : this.waterStates[0];
        }
        return this.delegate(blockX, blockY, blockZ, density);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return this.shouldScheduleFluidUpdate;
    }

    private int column(int blockX, int blockY, int blockZ) {
        int column = (blockX & 15) << 4 | (blockZ & 15);
        long key = ChunkPos.pack(blockX, blockZ);
        if (this.columnKeys[column] != key) {
            this.columnKeys[column] = key;
            this.reserves[column] = this.reserve.sampleValue(blockX, blockY, blockZ);
            this.levels[column] = this.riverLevel.sampleValue(blockX, blockY, blockZ);
            this.channels[column] = Double.NaN;
            this.lumens[column] = Double.NaN;
            this.flows[column] = -1;
        }
        return column;
    }

    private double channel(int column, int blockX, int blockY, int blockZ) {
        double value = this.channels[column];
        if (Double.isNaN(value)) {
            value = this.riverChannel.sampleValue(blockX, blockY, blockZ);
            this.channels[column] = value;
        }
        return value;
    }

    private double lumen(int column, int blockX, int blockY, int blockZ) {
        double value = this.lumens[column];
        if (Double.isNaN(value)) {
            value = this.lumen.sampleValue(blockX, blockY, blockZ);
            this.lumens[column] = value;
        }
        return value;
    }

    private int flow(int column, int blockX, int blockY, int blockZ, double level) {
        int value = this.flows[column];
        if (value < 0) {
            value = 0;
            double dx = this.riverLevel.sampleValue(blockX + 1, blockY, blockZ) - level;
            double dz = this.riverLevel.sampleValue(blockX, blockY, blockZ + 1) - level;
            double slope = Math.sqrt(dx * dx + dz * dz);
            if (slope > MIN_SLOPE) {
                int distance = Math.max(1, (int) Math.ceil((Math.ceil(level) - level) / slope));
                if (distance <= MAX_FLOW) {
                    value = distance;
                }
            }
            this.flows[column] = value;
        }
        return value;
    }

    private static BlockState[] states(BlockState source) {
        BlockState[] states = new BlockState[MAX_FLOW + 1];
        for (int level = 0; level <= MAX_FLOW; level++) {
            states[level] = source.setValue(LiquidBlock.LEVEL, level);
        }
        return states;
    }

    private @Nullable BlockState delegate(int blockX, int blockY, int blockZ, double density) {
        BlockState state = this.delegate.computeSubstance(blockX, blockY, blockZ, density);
        this.shouldScheduleFluidUpdate = this.delegate.shouldScheduleFluidUpdate();
        return state;
    }
}
