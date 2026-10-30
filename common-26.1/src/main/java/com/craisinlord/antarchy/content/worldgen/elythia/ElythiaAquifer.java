package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jetbrains.annotations.Nullable;

public final class ElythiaAquifer implements Aquifer {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final double RESERVE_THRESHOLD = 0.5D;
    private static final double LIP_RESERVE = 0.0D;
    private static final double RIVER_RESERVE_LIMIT = 0.04D;
    private static final double RIM_RESERVE = 0.0D;
    private static final int SHALLOW_DEPTH = 64;
    private static final double VALLEY_THRESHOLD = 0.0D;
    private static final double CHANNEL_THRESHOLD = 0.0D;
    private static final double LUMEN_THRESHOLD = 0.75D;
    private static final int RIVER_DEPTH_BAND = 8;
    private static final int BANK_BAND = 3;
    private static final int UNDERGROUND_DEPTH = 16;
    private static final int COLUMNS = 256;
    private static final int UNSET = Integer.MIN_VALUE;
    private static final int NO_SEAM = Integer.MIN_VALUE + 1;
    private static final int NO_LIP = Integer.MIN_VALUE + 1;
    private static final int[] NEIGHBOR_X = {1, -1, 0, 0};
    private static final int[] NEIGHBOR_Z = {0, 0, 1, -1};

    private final Aquifer delegate;
    private final NoiseChunk noiseChunk;
    private final ElythiaHydrology hydrology;
    private final long[] columnKeys = new long[COLUMNS];
    private final double[] reserves = new double[COLUMNS];
    private final int[] levels = new int[COLUMNS];
    private final double[] valleys = new double[COLUMNS];
    private final double[] channels = new double[COLUMNS];
    private final double[] lumens = new double[COLUMNS];
    private final int[] spills = new int[COLUMNS];
    private final int[] lips = new int[COLUMNS];
    private final int[] seams = new int[COLUMNS];
    private final int[] floors = new int[COLUMNS];
    private final int[] surfaces = new int[COLUMNS];
    private final BlockState waterState = Blocks.WATER.defaultBlockState();
    private final BlockState lumenState = AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState();
    private final BlockState seamState = Blocks.CALCITE.defaultBlockState();
    private boolean shouldScheduleFluidUpdate;

    public ElythiaAquifer(Aquifer delegate, NoiseChunk noiseChunk, ElythiaHydrology hydrology) {
        this.delegate = delegate;
        this.noiseChunk = noiseChunk;
        this.hydrology = hydrology;
        Arrays.fill(this.columnKeys, Long.MAX_VALUE);
    }

    @Nullable
    @Override
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
        int y = context.blockY();
        int column = this.column(context);
        double reserve = this.reserves[column];
        if (reserve < RIVER_RESERVE_LIMIT && this.valley(column, context) > VALLEY_THRESHOLD) {
            int level = this.level(column, context);
            boolean wet = this.wet(column, context);
            if (y >= this.floor(column, context) && y < this.lip(column, context) && !wet) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            }
            if (wet || this.floor(column, context) >= level) {
                if (y < level + BANK_BAND && y >= level - RIVER_DEPTH_BAND) {
                    return this.river(column, context, y, level, reserve);
                }
                if (y < level - RIVER_DEPTH_BAND && y >= this.floor(column, context)) {
                    this.shouldScheduleFluidUpdate = false;
                    return null;
                }
            }
        }
        if (density > 0.0D) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }
        boolean profiling = ElythiaGenProfiler.enabled();
        if (profiling) {
            ElythiaGenProfiler.countAquifer(false);
        }
        int seaLevel = this.hydrology.seaLevel();
        if (reserve >= RESERVE_THRESHOLD) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        if (y >= seaLevel) {
            this.shouldScheduleFluidUpdate = false;
            return AIR;
        }
        if (y >= seaLevel - SHALLOW_DEPTH) {
            this.shouldScheduleFluidUpdate = false;
            if (reserve > RIM_RESERVE || y < this.surface(column, context) - UNDERGROUND_DEPTH) {
                return AIR;
            }
            return this.waterState;
        }
        if (profiling) {
            ElythiaGenProfiler.countAquifer(true);
        }
        return this.delegate(context, density);
    }

    @Nullable
    private BlockState river(int column, DensityFunction.FunctionContext context, int y, int level, double reserve) {
        int floor = this.floor(column, context);
        if (y < floor) {
            this.shouldScheduleFluidUpdate = false;
            return null;
        }
        if (floor < level && y < this.seam(column, context, level)) {
            this.shouldScheduleFluidUpdate = false;
            return this.seamState;
        }
        if (y >= level) {
            this.shouldScheduleFluidUpdate = false;
            return y < this.hydrology.seaLevel() && reserve <= RIM_RESERVE ? this.waterState : AIR;
        }
        boolean lumenRiver = this.lumen(column, context) > LUMEN_THRESHOLD;
        this.shouldScheduleFluidUpdate = reserve > LIP_RESERVE || y >= this.spill(column, context, level);
        if (ElythiaGenProfiler.enabled()) {
            ElythiaGenProfiler.countFluid(lumenRiver, this.shouldScheduleFluidUpdate);
        }
        return lumenRiver ? this.lumenState : this.waterState;
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return this.shouldScheduleFluidUpdate;
    }

    private int column(DensityFunction.FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        int column = (x & 15) << 4 | (z & 15);
        this.load(column, ChunkPos.pack(x, z), context);
        return column;
    }

    private void load(int column, long key, DensityFunction.FunctionContext context) {
        if (this.columnKeys[column] != key) {
            this.columnKeys[column] = key;
            this.reserves[column] = this.hydrology.reserve().compute(context);
            this.levels[column] = UNSET;
            this.valleys[column] = Double.NaN;
            this.channels[column] = Double.NaN;
            this.lumens[column] = Double.NaN;
            this.spills[column] = UNSET;
            this.lips[column] = UNSET;
            this.seams[column] = UNSET;
            this.floors[column] = UNSET;
            this.surfaces[column] = UNSET;
        }
    }

    private int level(int column, DensityFunction.FunctionContext context) {
        int value = this.levels[column];
        if (value == UNSET) {
            value = level(this.hydrology.riverLevel().compute(context));
            this.levels[column] = value;
        }
        return value;
    }

    private int surface(int column, DensityFunction.FunctionContext context) {
        int value = this.surfaces[column];
        if (value == UNSET) {
            value = this.noiseChunk.preliminarySurfaceLevel(context.blockX(), context.blockZ());
            this.surfaces[column] = value;
        }
        return value;
    }

    private boolean wet(int column, DensityFunction.FunctionContext context) {
        return this.reserves[column] < RIVER_RESERVE_LIMIT
                && this.valley(column, context) > VALLEY_THRESHOLD
                && this.channel(column, context) > CHANNEL_THRESHOLD
                && this.floor(column, context) < this.level(column, context);
    }

    private boolean wet(DensityFunction.FunctionContext point) {
        return this.hydrology.reserve().compute(point) < RIVER_RESERVE_LIMIT
                && this.hydrology.valley().compute(point) > VALLEY_THRESHOLD
                && this.hydrology.riverChannel().compute(point) > CHANNEL_THRESHOLD
                && Mth.ceil(this.hydrology.floor().compute(point)) < level(this.hydrology.riverLevel().compute(point));
    }

    private double channel(int column, DensityFunction.FunctionContext context) {
        double value = this.channels[column];
        if (Double.isNaN(value)) {
            value = this.hydrology.riverChannel().compute(context);
            this.channels[column] = value;
        }
        return value;
    }

    private double valley(int column, DensityFunction.FunctionContext context) {
        double value = this.valleys[column];
        if (Double.isNaN(value)) {
            value = this.hydrology.valley().compute(context);
            this.valleys[column] = value;
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

    private int floor(int column, DensityFunction.FunctionContext context) {
        int value = this.floors[column];
        if (value == UNSET) {
            value = Mth.ceil(this.hydrology.floor().compute(context));
            this.floors[column] = value;
        }
        return value;
    }

    private int spill(int column, DensityFunction.FunctionContext context, int level) {
        int value = this.spills[column];
        if (value == UNSET) {
            value = level;
            int x = context.blockX();
            int z = context.blockZ();
            for (int i = 0; i < NEIGHBOR_X.length; i++) {
                int neighborX = x + NEIGHBOR_X[i];
                int neighborZ = z + NEIGHBOR_Z[i];
                DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(neighborX, 0, neighborZ);
                if (neighborX >> 4 == x >> 4 && neighborZ >> 4 == z >> 4) {
                    int neighborColumn = (neighborX & 15) << 4 | (neighborZ & 15);
                    this.load(neighborColumn, ChunkPos.pack(neighborX, neighborZ), point);
                    int neighbor = this.wet(neighborColumn, point) ? this.level(neighborColumn, point) : this.floor(neighborColumn, point);
                    value = Math.min(value, neighbor);
                    continue;
                }
                int neighbor = this.wet(point)
                        ? level(this.hydrology.riverLevel().compute(point))
                        : Mth.ceil(this.hydrology.floor().compute(point));
                value = Math.min(value, neighbor);
            }
            this.spills[column] = value;
        }
        return value;
    }

    private int lip(int column, DensityFunction.FunctionContext context) {
        int value = this.lips[column];
        if (value == UNSET) {
            value = NO_LIP;
            int x = context.blockX();
            int z = context.blockZ();
            for (int i = 0; i < NEIGHBOR_X.length; i++) {
                int neighborX = x + NEIGHBOR_X[i];
                int neighborZ = z + NEIGHBOR_Z[i];
                DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(neighborX, 0, neighborZ);
                if (neighborX >> 4 == x >> 4 && neighborZ >> 4 == z >> 4) {
                    int neighborColumn = (neighborX & 15) << 4 | (neighborZ & 15);
                    this.load(neighborColumn, ChunkPos.pack(neighborX, neighborZ), point);
                    if (this.wet(neighborColumn, point)) {
                        value = Math.max(value, this.level(neighborColumn, point));
                    }
                    continue;
                }
                if (this.wet(point)) {
                    value = Math.max(value, level(this.hydrology.riverLevel().compute(point)));
                }
            }
            this.lips[column] = value;
        }
        return value;
    }

    private int seam(int column, DensityFunction.FunctionContext context, int level) {
        int value = this.seams[column];
        if (value == UNSET) {
            value = NO_SEAM;
            if (this.lumen(column, context) > LUMEN_THRESHOLD) {
                int x = context.blockX();
                int z = context.blockZ();
                for (int i = 0; i < NEIGHBOR_X.length; i++) {
                    int neighborX = x + NEIGHBOR_X[i];
                    int neighborZ = z + NEIGHBOR_Z[i];
                    DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(neighborX, 0, neighborZ);
                    int neighborLevel;
                    if (neighborX >> 4 == x >> 4 && neighborZ >> 4 == z >> 4) {
                        int neighborColumn = (neighborX & 15) << 4 | (neighborZ & 15);
                        this.load(neighborColumn, ChunkPos.pack(neighborX, neighborZ), point);
                        if (this.wet(neighborColumn, point)) {
                            if (this.lumen(neighborColumn, point) > LUMEN_THRESHOLD) {
                                continue;
                            }
                            neighborLevel = this.level(neighborColumn, point);
                        } else if (this.reserves[neighborColumn] <= RIM_RESERVE) {
                            neighborLevel = this.hydrology.seaLevel();
                        } else {
                            continue;
                        }
                        if (this.floor(neighborColumn, point) >= neighborLevel) {
                            continue;
                        }
                    } else {
                        if (this.wet(point)) {
                            if (this.hydrology.lumen().compute(point) > LUMEN_THRESHOLD) {
                                continue;
                            }
                            neighborLevel = level(this.hydrology.riverLevel().compute(point));
                        } else if (this.hydrology.reserve().compute(point) <= RIM_RESERVE) {
                            neighborLevel = this.hydrology.seaLevel();
                        } else {
                            continue;
                        }
                        if (Mth.ceil(this.hydrology.floor().compute(point)) >= neighborLevel) {
                            continue;
                        }
                    }
                    value = Math.max(value, Math.max(level, neighborLevel) + 1);
                }
            }
            this.seams[column] = value;
        }
        return value;
    }

    public static int level(double riverLevel) {
        return Mth.floor(riverLevel + 0.5D);
    }

    @Nullable
    private BlockState delegate(DensityFunction.FunctionContext context, double density) {
        BlockState state = this.delegate.computeSubstance(context, density);
        this.shouldScheduleFluidUpdate = this.delegate.shouldScheduleFluidUpdate();
        return state;
    }
}
