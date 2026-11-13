package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;

public final class CavarynCavityUnionFunction implements DensityFunction {
    public static final MapCodec<CavarynCavityUnionFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.nonEmptyList(DensityFunction.HOLDER_HELPER_CODEC.listOf()).fieldOf("arguments").forGetter(CavarynCavityUnionFunction::arguments)
            ).apply(instance, CavarynCavityUnionFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynCavityUnionFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final List<DensityFunction> arguments;
    private final DensityFunction[] array;
    private final double minValue;
    private final double maxValue;
    private final boolean chunkOwned;

    private boolean columnsResolved;
    private CavarynColumn[] columns;
    private CavarynColumn[] active;
    private int activeCount;
    private boolean loaded;
    private int qx;
    private int qz;
    private double activeMinY;
    private double activeMaxY;

    public CavarynCavityUnionFunction(List<DensityFunction> arguments) {
        this(arguments, false);
    }

    private CavarynCavityUnionFunction(List<DensityFunction> arguments, boolean chunkOwned) {
        this.arguments = List.copyOf(arguments);
        this.array = this.arguments.toArray(new DensityFunction[0]);
        this.chunkOwned = chunkOwned;
        double min = Double.MAX_VALUE;
        double max = Double.MAX_VALUE;
        for (DensityFunction argument : this.array) {
            min = Math.min(min, argument.minValue());
            max = Math.min(max, argument.maxValue());
        }
        this.minValue = min;
        this.maxValue = max;
    }

    public List<DensityFunction> arguments() {
        return this.arguments;
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.chunkOwned && context instanceof NoiseChunk) {
            CavarynColumn[] resolved = this.resolveColumns();
            if (resolved != null) {
                return this.computeColumns(context, resolved);
            }
        }
        double floor = this.minValue;
        double best = Double.MAX_VALUE;
        for (DensityFunction argument : this.array) {
            double value = argument.compute(context);
            if (value < best) {
                best = value;
                if (best <= floor) {
                    return best;
                }
            }
        }
        return best;
    }

    private double computeColumns(FunctionContext context, CavarynColumn[] resolved) {
        int x = context.blockX();
        int z = context.blockZ();
        int currentQx = x >> 2;
        int currentQz = z >> 2;
        if (!this.loaded || this.qx != currentQx || this.qz != currentQz) {
            this.loadColumns(resolved, currentQx, currentQz);
        }
        int y = context.blockY();
        double py = y + 0.5D;
        if (py < this.activeMinY || py > this.activeMaxY) {
            return 1.0D;
        }
        double floor = this.minValue;
        double best = 1.0D;
        CavarynColumn[] current = this.active;
        for (int i = 0; i < this.activeCount; i++) {
            double value = CavarynColumn.density(current[i].signedDistance(x, y, z, CavarynColumn.FLOOR));
            if (value < best) {
                best = value;
                if (best <= floor) {
                    return best;
                }
            }
        }
        return best;
    }

    private void loadColumns(CavarynColumn[] resolved, int currentQx, int currentQz) {
        int count = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (CavarynColumn column : resolved) {
            column.load(currentQx, currentQz);
            if (!column.isEmpty()) {
                this.active[count++] = column;
                min = Math.min(min, column.minY());
                max = Math.max(max, column.maxY());
            }
        }
        this.activeCount = count;
        this.activeMinY = min;
        this.activeMaxY = max;
        this.qx = currentQx;
        this.qz = currentQz;
        this.loaded = true;
    }

    private CavarynColumn[] resolveColumns() {
        if (this.columnsResolved) {
            return this.columns;
        }
        this.columnsResolved = true;
        CavarynColumn[] resolved = new CavarynColumn[this.array.length];
        for (int i = 0; i < resolved.length; i++) {
            CavarynColumn column = unwrap(this.array[i]) instanceof CavarynColumn.Source source ? source.newColumn() : null;
            if (column == null) {
                return null;
            }
            resolved[i] = column;
        }
        this.active = new CavarynColumn[resolved.length];
        this.columns = resolved;
        return resolved;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        boolean bound = true;
        DensityFunction[] mapped = new DensityFunction[this.array.length];
        for (int i = 0; i < mapped.length; i++) {
            bound &= unwrap(this.array[i]) instanceof CavarynColumn.Source source && source.bound();
            mapped[i] = this.array[i].mapAll(visitor);
        }
        return visitor.apply(new CavarynCavityUnionFunction(List.of(mapped), bound));
    }

    private static DensityFunction unwrap(DensityFunction function) {
        DensityFunction current = function;
        while (current instanceof DensityFunctions.HolderHolder holder) {
            current = holder.function().value();
        }
        return current;
    }

    @Override
    public double minValue() {
        return this.minValue;
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
