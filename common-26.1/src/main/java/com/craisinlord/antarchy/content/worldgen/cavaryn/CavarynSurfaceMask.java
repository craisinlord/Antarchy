package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CavarynSurfaceMask(DensityFunction temperature, DensityFunction erosion,
                                 float temperatureMin, float temperatureMax, float erosionMin, float erosionMax) {
    public static final Codec<CavarynSurfaceMask> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("temperature").forGetter(CavarynSurfaceMask::temperature),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(CavarynSurfaceMask::erosion),
            Codec.FLOAT.fieldOf("temperature_min").forGetter(CavarynSurfaceMask::temperatureMin),
            Codec.FLOAT.fieldOf("temperature_max").forGetter(CavarynSurfaceMask::temperatureMax),
            Codec.FLOAT.fieldOf("erosion_min").forGetter(CavarynSurfaceMask::erosionMin),
            Codec.FLOAT.fieldOf("erosion_max").forGetter(CavarynSurfaceMask::erosionMax)
    ).apply(instance, CavarynSurfaceMask::new));

    private static final int SAMPLE_Y = 300;

    public CavarynSurfaceMask mapAll(DensityFunction.Visitor visitor) {
        return new CavarynSurfaceMask(this.temperature.mapAll(visitor), this.erosion.mapAll(visitor),
                this.temperatureMin, this.temperatureMax, this.erosionMin, this.erosionMax);
    }

    public boolean accepts(double x, double z) {
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(Mth.floor(x), SAMPLE_Y, Mth.floor(z));
        double t = this.temperature.compute(point);
        if (t < this.temperatureMin || t > this.temperatureMax) {
            return false;
        }
        double e = this.erosion.compute(point);
        return e >= this.erosionMin && e <= this.erosionMax;
    }

    public interface Filter {
        boolean accepts(double x, double z);
    }
}
