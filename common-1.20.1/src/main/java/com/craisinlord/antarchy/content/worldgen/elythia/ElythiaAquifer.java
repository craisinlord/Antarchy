package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.Arrays;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

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
    private final ElythiaHydrology hydrology;
    private final long[] columnKeys = new long[COLUMNS];
    private final double[] reserves = new double[COLUMNS];
    private final double[] levels = new double[COLUMNS];
    private final double[] channels = new double[COLUMNS];
    private final double[] lumens = new double[COLUMNS];
    private final int[] flows = new int[COLUMNS];
    private final BlockState[] waterStates = states(Blocks.WATER.defaultBlockState());
    private final BlockState[] lumenStates = states(AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState());
    private boolean shouldScheduleFluidUpdate;

    public ElythiaAquifer(Aquifer delegate, ElythiaHydrology hydrology) {
        this.delegate = delegate;
        this.hydrology = hydrology;
        Arrays.fill(this.columnKeys, Long.MAX_VALUE);
    }

    @Nullable
    @Override
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
        if (density > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }
        int y = context.blockY();
        int column = this.column(context);
        double reserve = this.reserves[column];
        int seaLevel = this.hydrology.seaLevel();
        if (reserve >= RESERVE_THRESHOLD) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        double level = this.levels[column];
        if (y < level + 1.0D && y >= level - RIVER_DEPTH_BAND && reserve < RIVER_RESERVE_LIMIT && this.channel(column, context) > CHANNEL_THRESHOLD) {
            double lumen = this.lumen(column, context);
            if (lumen > LIP_LOW && lumen < LIP_HIGH) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            BlockState[] states = lumen > LUMEN_THRESHOLD ? this.lumenStates : this.waterStates;
            if (y < level) {
                this.shouldScheduleFluidUpdate = reserve > LIP_RESERVE;
                return states[0];
            }
            int flow = this.flow(column, context, level);
            if (flow > 0) {
                this.shouldScheduleFluidUpdate = false;
                return states[flow];
            }
        }
        if (y >= seaLevel) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        if (y >= seaLevel - SHALLOW_DEPTH) {
            this.shouldScheduleFluidUpdate = false;
            return reserve > RIM_RESERVE ? AIR : this.waterStates[0];
        }
        return this.delegate(context, density);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return this.shouldScheduleFluidUpdate;
    }

    private int column(DensityFunction.FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        int column = (x & 15) << 4 | (z & 15);
        long key = ChunkPos.asLong(x, z);
        if (this.columnKeys[column] != key) {
            this.columnKeys[column] = key;
            this.reserves[column] = this.hydrology.reserve().compute(context);
            this.levels[column] = this.hydrology.riverLevel().compute(context);
            this.channels[column] = Double.NaN;
            this.lumens[column] = Double.NaN;
            this.flows[column] = -1;
        }
        return column;
    }

    private double channel(int column, DensityFunction.FunctionContext context) {
        double value = this.channels[column];
        if (Double.isNaN(value)) {
            value = this.hydrology.riverChannel().compute(context);
            this.channels[column] = value;
        }
        return value;
    }

    private double lumen(int column, DensityFunction.FunctionContext context) {
        double value = this.lumens[column];
        if (Double.isNaN(value)) {
            value = this.hydrology.lumen().compute(context);
            this.lumens[column] = value;
        }
        return value;
    }

    private int flow(int column, DensityFunction.FunctionContext context, double level) {
        int value = this.flows[column];
        if (value < 0) {
            value = 0;
            int x = context.blockX();
            int z = context.blockZ();
            double dx = this.hydrology.riverLevel().compute(new DensityFunction.SinglePointContext(x + 1, 0, z)) - level;
            double dz = this.hydrology.riverLevel().compute(new DensityFunction.SinglePointContext(x, 0, z + 1)) - level;
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

    @Nullable
    private BlockState delegate(DensityFunction.FunctionContext context, double density) {
        BlockState state = this.delegate.computeSubstance(context, density);
        this.shouldScheduleFluidUpdate = this.delegate.shouldScheduleFluidUpdate();
        return state;
    }
}
