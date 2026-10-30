package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ThoraxisTerrainFunction(DensityFunction regimeA, DensityFunction regimeB, Holder<NormalNoise> shape, Holder<NormalNoise> detail) implements DensityFunction {
    public static final MapCodec<ThoraxisTerrainFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("regime_a").forGetter(ThoraxisTerrainFunction::regimeA),
                    DensityFunction.CODEC.fieldOf("regime_b").forGetter(ThoraxisTerrainFunction::regimeB),
                    NormalNoise.CODEC.fieldOf("shape").forGetter(ThoraxisTerrainFunction::shape),
                    NormalNoise.CODEC.fieldOf("detail").forGetter(ThoraxisTerrainFunction::detail)
            ).apply(instance, ThoraxisTerrainFunction::new));

    private static final double OUTPUT_SCALE = 1.0D / 14.0D;
    private static final double OUTPUT_LIMIT = 1.25D;
    private static final double ROOF_START_Y = 208.0D;
    private static final double ROOF_Y = 256.0D;
    private static final double ROOF_VALUE = 1.0D;
    private static final double DETAIL_MARGIN = 0.75D;
    private static final double BASIN_THRESHOLD = 0.12D;
    private static final double BASIN_SCALE = 70.0D;
    private static final double BASIN_MAX_DEPTH = 22.0D;
    private static final double FLOOR_Y = 18.0D;

    private static final int TIERS = 4;
    private static final double TIER_BOTTOM_Y = 10.0D;
    private static final double TIER_TOP_Y = 236.0D;
    private static final double TIER_JITTER = 28.0D;
    private static final double MIN_TIER_HEIGHT = 28.0D;
    private static final double WINDOW_THRESHOLD = 0.2D;
    private static final double WINDOW_BLEND = 0.1D;
    private static final double GRAND_THRESHOLD = 0.3D;
    private static final double PINHOLE_SCALE = 2.0D;
    private static final double PINHOLE_THRESHOLD = 0.33D;
    private static final double PINHOLE_EDGE = 0.07D;
    private static final double PINHOLE_DEPTH = 40.0D;
    private static final double GRAND_BLEND = 0.12D;
    private static final double BLEND_LIMIT = 6.0D;

    private static final double STRATA_FIRST_Y = 18.0D;
    private static final double STRATA_SPACING = 42.0D;
    private static final int STRATA_LAYERS = 6;
    private static final double GRAND_STRATA_SPACING = 76.0D;
    private static final int GRAND_STRATA_LAYERS = 3;
    private static final double STRATA_DETAIL = 6.0D;
    private static final int STRAND_CELL = 32;
    private static final double STRAND_JITTER = 8.0D;
    private static final double STRAND_RADIUS = 9.0D;
    private static final double GRAND_STRAND_RADIUS = 12.0D;

    private static final double MIRROR_DETAIL = 11.0D;
    private static final double GRAND_MIRROR_TOP = 240.0D;
    private static final double GRAND_MIRROR_PEAK_SCALE = 360.0D;
    private static final double MIRROR_PEAK_SCALE = 75.0D;

    private static final double GRAND_RIB_SPACING = 30.0D;
    private static final double GRAND_CAGE_SPACING = 136.0D;
    private static final double GRAND_CAGE_BOTTOM = 14.0D;
    private static final double GRAND_CAGE_TOP = 210.0D;
    private static final double GRAND_SPINE_Y = 206.0D;
    private static final double RIB_DETAIL = 3.0D;

    private static final double SPIRE_CHANCE = 0.8D;
    private static final double SPIRE_LOBE = 0.3D;
    private static final double SPIRE_DETAIL = 4.0D;
    private static final double GRAND_SPIRE_BOTTOM = 14.0D;
    private static final int GRAND_SPIRE_CELL = 56;
    private static final double GRAND_SPIRE_JITTER = 36.0D;
    private static final double GRAND_SPIRE_MIN_RADIUS = 10.0D;
    private static final double GRAND_SPIRE_RADIUS_RANGE = 8.0D;
    private static final double GRAND_FLARE_RADIUS = 6.0D;
    private static final double GRAND_FLARE_HEIGHT = 40.0D;
    private static final int SPIRE_CELL = 40;
    private static final double SPIRE_JITTER = 24.0D;
    private static final double SPIRE_MIN_RADIUS = 6.0D;
    private static final double SPIRE_RADIUS_RANGE = 6.0D;
    private static final double FLARE_RADIUS = 5.0D;
    private static final double FLARE_HEIGHT = 16.0D;


    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new RuntimeSampler(this.regimeA.compileSampler(context), this.regimeB.compileSampler(context), context.createNoiseSampler(this.shape), context.createNoiseSampler(this.detail));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ThoraxisTerrainFunction(rule.rewrite(this.regimeA), rule.rewrite(this.regimeB), this.shape, this.detail);
    }

    @Override
    public Interval range() {
        return Interval.of((float) -OUTPUT_LIMIT, (float) OUTPUT_LIMIT);
    }

    @Override
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<ThoraxisTerrainFunction> codec() {
        return CODEC;
    }

    private static final class RuntimeSampler implements DensitySampler {
        private final DensitySampler regimeA;
        private final DensitySampler regimeB;
        private final Noise shape;
        private final Noise detail;
        private final long salt;

        private RuntimeSampler(DensitySampler regimeA, DensitySampler regimeB, Noise shape, Noise detail) {
            this.regimeA = regimeA;
            this.regimeB = regimeB;
            this.shape = shape;
            this.detail = detail;
            this.salt = Double.doubleToLongBits(shape.get(1234.5D, 0.0D, -987.25D)) * 31L
                    ^ Double.doubleToLongBits(detail.get(-321.75D, 17.0D, 655.5D));
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            for (int ix = 0; ix < volume.sizeX(); ix++) {
                for (int iz = 0; iz < volume.sizeZ(); iz++) {
                    int x = volume.blockX(ix);
                    int z = volume.blockZ(iz);
                    Column column = this.column(x, z);
                    for (int iy = 0; iy < volume.sizeY(); iy++) {
                        int y = volume.blockY(iy);
                        outputBuffer.set(volume.indexUnchecked(ix, iy, iz), (float) (y < 0 ? ROOF_VALUE : this.sample(column, x, y, z)));
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            return (float) (y < 0 ? ROOF_VALUE : this.sample(this.column(x, z), x, y, z));
        }

        private double sample(Column column, int x, int y, int z) {
            double grand = column.grandness;
            double total = 0.0D;
            if (grand <= 0.0D) {
                total = styles(column, column.tiered, y);
            } else if (grand >= 1.0D) {
                total = styles(column, column.grand, y);
            } else {
                total = (1.0D - grand) * clampBlend(styles(column, column.tiered, y))
                        + grand * clampBlend(styles(column, column.grand, y));
            }
            double amplitude = column.detailAmplitude;
            double mirrorAmplitude = column.mirrorAmplitude;
            if (Math.abs(total) < amplitude + mirrorAmplitude + DETAIL_MARGIN) {
                if (amplitude > 0.0D) {
                    total += amplitude * this.detailNoise(x, y, z);
                }
                if (mirrorAmplitude > 0.0D) {
                    total += mirrorAmplitude * this.detailNoise(x, fold(grand >= 0.5D ? column.grand : column.tiered, y), z);
                }
            }
            double value = Math.max(-OUTPUT_LIMIT, Math.min(OUTPUT_LIMIT, total * OUTPUT_SCALE));
            if (y > ROOF_START_Y) {
                double roof = Math.min(1.0D, (y - ROOF_START_Y) / (ROOF_Y - ROOF_START_Y));
                value += (ROOF_VALUE - value) * roof;
            }
            return value;
        }

        private static double clampBlend(double density) {
            return Math.max(-BLEND_LIMIT, Math.min(BLEND_LIMIT, density));
        }

        private static double styles(Column column, Layout layout, int y) {
            double[] weights = column.weights;
            double density = 0.0D;
            if (weights[ThoraxisTerrainRegimes.STRATA] > 0.0D) {
                density += weights[ThoraxisTerrainRegimes.STRATA] * strata(column, layout, y);
            }
            if (weights[ThoraxisTerrainRegimes.RIBCAGE] > 0.0D) {
                density += weights[ThoraxisTerrainRegimes.RIBCAGE] * ribcage(column, layout, y);
            }
            if (weights[ThoraxisTerrainRegimes.MIRROR] > 0.0D) {
                density += weights[ThoraxisTerrainRegimes.MIRROR] * mirror(layout, y);
            }
            if (weights[ThoraxisTerrainRegimes.SPIRES] > 0.0D) {
                density += weights[ThoraxisTerrainRegimes.SPIRES] * spires(column, layout, y);
            }
            return density;
        }

        private static int tierAt(Layout layout, double y) {
            for (int i = 0; i < layout.tiers - 1; i++) {
                if (y < layout.bounds[i + 1]) {
                    return i;
                }
            }
            return layout.tiers - 1;
        }

        private static double plates(Layout layout, double y) {
            double best = -1000.0D;
            for (int k = 1; k < layout.tiers; k++) {
                double plate = layout.plateHalf[k] - Math.abs(y - layout.bounds[k]);
                if (plate > best) {
                    best = plate;
                }
            }
            return best;
        }

        private static double fold(Layout layout, int y) {
            if (layout.grand) {
                return Math.min(y, GRAND_MIRROR_TOP - y);
            }
            int tier = tierAt(layout, y);
            double bottom = layout.bounds[tier];
            double top = layout.bounds[tier + 1];
            return y - bottom <= top - y ? y : bottom + top - y;
        }

        private static double strata(Column column, Layout layout, int y) {
            double[] centers = layout.strataCenters;
            double[] halves = layout.strataHalf;
            double best = centers[0] + halves[0] - column.basin - y;
            for (int k = 1; k < centers.length; k++) {
                double layer = halves[k] - Math.abs(y - centers[k]);
                if (layer > best) {
                    best = layer;
                }
            }
            double maxRadius = layout.strandRadius;
            if (column.strandDistance < maxRadius) {
                for (int k = 0; k < centers.length - 1; k++) {
                    double bottom = centers[k];
                    double top = centers[k + 1];
                    if (y >= bottom && y < top) {
                        double inner = layout.strandInner[k];
                        if (inner > -100.0D) {
                            double t = (y - bottom) / (top - bottom) * 2.0D - 1.0D;
                            double strand = inner + (maxRadius - inner) * t * t - column.strandDistance;
                            if (strand > best) {
                                best = strand;
                            }
                        }
                        break;
                    }
                }
            }
            return best;
        }

        private static double ribcage(Column column, Layout layout, int y) {
            double best = column.floor - column.basin - y;
            int tier;
            double bottom;
            double top;
            if (layout.grand) {
                tier = 0;
                bottom = GRAND_CAGE_BOTTOM;
                top = GRAND_CAGE_TOP;
            } else {
                tier = tierAt(layout, y);
                bottom = layout.bounds[tier];
                top = layout.bounds[tier + 1];
                best = Math.max(best, plates(layout, y));
            }
            Rib rib = layout.ribs[tier];
            double center = (bottom + top) * 0.5D;
            double halfHeight = (top - bottom) * 0.5D + (layout.grand ? 0.0D : 3.0D);
            double halfWidth = rib.halfWidth;
            double ey = (y - center) / halfHeight;
            double ev = rib.v / halfWidth;
            double e2 = ev * ev + ey * ey;
            double e = Math.sqrt(e2);
            double vertical = e2 > 1.0E-6D ? ey * ey / e2 : 0.0D;
            double radial = (e - 1.0D) * (halfWidth + (halfHeight - halfWidth) * vertical);
            double grow = Math.min(1.0D, ey * ey);
            double band;
            double spine;
            if (layout.grand) {
                band = Math.min(9.0D + 6.0D * grow - Math.abs(radial), 5.0D + 3.0D * grow - rib.u);
                double dy = y - GRAND_SPINE_Y;
                spine = 13.0D - Math.sqrt(rib.v * rib.v + dy * dy);
            } else {
                band = Math.min(6.0D + 4.0D * grow - Math.abs(radial), 3.5D + 2.5D * grow - rib.u);
                double dy = y - (top - 2.0D);
                spine = 7.0D - Math.sqrt(rib.v * rib.v + dy * dy);
            }
            return Math.max(best, Math.max(band, spine));
        }

        private static double mirror(Layout layout, int y) {
            if (layout.grand) {
                return layout.mirrorHeight[0] - Math.min(y, GRAND_MIRROR_TOP - y);
            }
            int tier = tierAt(layout, y);
            double fromBottom = y - layout.bounds[tier];
            double fromTop = layout.bounds[tier + 1] - y;
            if (fromBottom <= fromTop) {
                return layout.mirrorHeight[tier] - layout.mirrorHole[tier] - fromBottom;
            }
            return layout.mirrorHeight[tier] - layout.mirrorHole[tier + 1] - fromTop;
        }

        private static double spires(Column column, Layout layout, int y) {
            double best = column.floor - column.basin - y;
            int tier = 0;
            if (!layout.grand) {
                tier = tierAt(layout, y);
                best = Math.max(best, plates(layout, y));
            }
            Spire[] spires = layout.spires[tier];
            for (int i = 0; i < layout.spireCount[tier]; i++) {
                double spire = spire(spires[i], y);
                if (spire > best) {
                    best = spire;
                }
            }
            return best;
        }

        private static double spire(Spire spire, int y) {
            double radius;
            double flare;
            if (spire.type == 0) {
                double mid = (spire.bottom + spire.top) * 0.5D;
                double t = (y - mid) / ((spire.top - spire.bottom) * 0.5D);
                radius = spire.radius * (0.62D + 0.38D * t * t);
                flare = flare(spire, y - spire.bottom) + flare(spire, spire.top - y);
            } else if (spire.type == 1) {
                double t = (y - spire.bottom) / (spire.end - spire.bottom);
                if (t > 1.0D) {
                    return -1000.0D;
                }
                radius = spire.radius * Math.sqrt(1.0D - Math.max(0.0D, t));
                flare = flare(spire, y - spire.bottom);
            } else {
                double t = (spire.top - y) / (spire.top - spire.end);
                if (t > 1.0D) {
                    return -1000.0D;
                }
                radius = spire.radius * Math.sqrt(1.0D - Math.max(0.0D, t));
                flare = flare(spire, spire.top - y);
            }
            double angle = spire.phase + spire.twist * y;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double dx = spire.dx - spire.lean * cos;
            double dz = spire.dz - spire.lean * sin;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double lobe = 1.0D;
            if (distance > 1.0E-4D) {
                double localCos = (dx * cos + dz * sin) / distance;
                lobe += SPIRE_LOBE * localCos * (4.0D * localCos * localCos - 3.0D);
            }
            return radius * lobe + flare - distance;
        }

        private static double flare(Spire spire, double height) {
            if (height < 0.0D || height > spire.flareHeight) {
                return 0.0D;
            }
            double t = 1.0D - height / spire.flareHeight;
            return spire.flareRadius * t * t * t;
        }

        private Column column(int x, int z) {
            Column column = new Column(x, z);
            double[] weights = column.weights;
            ThoraxisTerrainRegimes.weights(this.regimeA.sampleValue(SamplerContext.EMPTY_UNCACHED, x, 0, z), this.regimeB.sampleValue(SamplerContext.EMPTY_UNCACHED, x, 0, z), weights);
            double basinNoise = this.shape(x * 0.45D + 2400.0D, z * 0.45D - 6800.0D);
            column.basin = Math.max(0.0D, Math.min(BASIN_MAX_DEPTH, (-basinNoise - BASIN_THRESHOLD) * BASIN_SCALE));
            column.floor = FLOOR_Y + 14.0D * this.shape(x * 0.9D - 3300.0D, z * 0.9D + 6100.0D);
            double grandNoise = this.shape(x * 0.22D - 8100.0D, z * 0.22D + 5300.0D);
            column.grandness = smoothstep((grandNoise - GRAND_THRESHOLD) / GRAND_BLEND + 0.5D);
            column.window = smoothstep((this.shape(x * 0.35D + 4300.0D, z * 0.35D - 2900.0D) - WINDOW_THRESHOLD) / WINDOW_BLEND + 0.5D);
            column.detailAmplitude = weights[ThoraxisTerrainRegimes.STRATA] * STRATA_DETAIL
                    + weights[ThoraxisTerrainRegimes.RIBCAGE] * RIB_DETAIL
                    + weights[ThoraxisTerrainRegimes.SPIRES] * SPIRE_DETAIL;
            column.mirrorAmplitude = weights[ThoraxisTerrainRegimes.MIRROR] * MIRROR_DETAIL;
            if (weights[ThoraxisTerrainRegimes.STRATA] > 0.0D) {
                int cellX = Math.floorDiv(x, STRAND_CELL);
                int cellZ = Math.floorDiv(z, STRAND_CELL);
                double centerX = (cellX + 0.5D) * STRAND_CELL + (ThoraxisTerrainRegimes.hash01(cellX, cellZ, this.salt ^ 0x51L) - 0.5D) * STRAND_JITTER;
                double centerZ = (cellZ + 0.5D) * STRAND_CELL + (ThoraxisTerrainRegimes.hash01(cellX, cellZ, this.salt ^ 0x52L) - 0.5D) * STRAND_JITTER;
                double dx = x - centerX;
                double dz = z - centerZ;
                column.strandDistance = Math.sqrt(dx * dx + dz * dz);
                column.strandCellX = cellX;
                column.strandCellZ = cellZ;
            }
            if (column.grandness < 1.0D) {
                column.tiered = this.layout(column, x, z, false);
            }
            if (column.grandness > 0.0D) {
                column.grand = this.layout(column, x, z, true);
            }
            return column;
        }

        private Layout layout(Column column, int x, int z, boolean grand) {
            Layout layout = new Layout(grand, grand ? 1 : TIERS);
            if (!grand) {
                layout.bounds[0] = TIER_BOTTOM_Y;
                layout.bounds[TIERS] = TIER_TOP_Y;
                double step = (TIER_TOP_Y - TIER_BOTTOM_Y) / TIERS;
                double window = column.window;
                boolean belowOpen = false;
                for (int k = 1; k < TIERS; k++) {
                    double offset = this.shape(x * 0.5D + k * 977.3D, z * 0.5D - k * 1301.7D);
                    double bound = TIER_BOTTOM_Y + k * step + offset * TIER_JITTER;
                    layout.bounds[k] = Math.max(layout.bounds[k - 1] + MIN_TIER_HEIGHT, Math.min(TIER_TOP_Y - (TIERS - k) * MIN_TIER_HEIGHT, bound));
                    double holes = this.shape(x * 0.9D + k * 613.9D, z * 0.9D - k * 457.1D);
                    double open = Math.max(-6.0D, Math.min(12.0D, 10.0D + 34.0D * holes));
                    double solid = Math.max(7.0D, Math.min(12.0D, 10.0D + 12.0D * holes));
                    double half = solid + (open - solid) * window;
                    double hole = window * Math.max(0.0D, (-holes - 0.18D) * 120.0D);
                    double pinhole = belowOpen ? 0.0D : this.pinhole(x, z, k * 1511.3D) * (1.0D - window);
                    if (pinhole > 0.0D) {
                        half -= (half + 6.0D) * pinhole;
                        hole += PINHOLE_DEPTH * pinhole;
                    }
                    layout.plateHalf[k] = half;
                    layout.mirrorHole[k] = hole;
                    belowOpen = pinhole > 0.5D;
                }
                layout.mirrorHole[0] = column.basin * 1.4D;
            }
            double[] weights = column.weights;
            if (weights[ThoraxisTerrainRegimes.STRATA] > 0.0D) {
                this.fillStrata(column, layout, x, z);
            }
            if (weights[ThoraxisTerrainRegimes.RIBCAGE] > 0.0D) {
                this.fillRibcage(layout, x, z);
            }
            if (weights[ThoraxisTerrainRegimes.MIRROR] > 0.0D) {
                this.fillMirror(column, layout, x, z);
            }
            if (weights[ThoraxisTerrainRegimes.SPIRES] > 0.0D) {
                this.fillSpires(layout, x, z);
            }
            return layout;
        }

        private void fillStrata(Column column, Layout layout, int x, int z) {
            int layers = layout.grand ? GRAND_STRATA_LAYERS : STRATA_LAYERS;
            double spacing = layout.grand ? GRAND_STRATA_SPACING : STRATA_SPACING;
            double drift = layout.grand ? 22.0D : 16.0D;
            long modeSalt = layout.grand ? 0x80L : 0x60L;
            layout.strataCenters = new double[layers];
            layout.strataHalf = new double[layers];
            boolean belowOpen = false;
            for (int k = 0; k < layers; k++) {
                double offset = this.shape(x * 0.6D + k * 1931.7D, z * 0.6D - k * 1213.3D);
                layout.strataCenters[k] = STRATA_FIRST_Y + k * spacing + offset * drift;
                if (k == 0) {
                    layout.strataHalf[k] = 6.0D;
                } else {
                    double holes = this.shape(x * 1.5D - k * 733.1D, z * 1.5D + k * 2711.9D);
                    double open = Math.max(-8.0D, Math.min(12.0D, 9.0D + 30.0D * holes));
                    double solid = Math.max(6.0D, Math.min(12.0D, 9.0D + 10.0D * holes));
                    double half = layout.grand ? open : solid + (open - solid) * column.window;
                    if (!layout.grand) {
                        double pinhole = belowOpen ? 0.0D : this.pinhole(x, z, k * 2237.9D + 600.0D) * (1.0D - column.window);
                        if (pinhole > 0.0D) {
                            half -= (half + 6.0D) * pinhole;
                        }
                        belowOpen = pinhole > 0.5D;
                    }
                    layout.strataHalf[k] = half;
                }
            }
            layout.strandRadius = layout.grand ? GRAND_STRAND_RADIUS : STRAND_RADIUS;
            layout.strandInner = new double[layers - 1];
            for (int k = 0; k < layers - 1; k++) {
                double roll = ThoraxisTerrainRegimes.hash01(column.strandCellX, column.strandCellZ, this.salt ^ (modeSalt + k));
                if (roll < 0.42D) {
                    layout.strandInner[k] = roll < 0.14D ? -2.0D : 4.0D;
                } else {
                    layout.strandInner[k] = -1000.0D;
                }
            }
        }

        private void fillRibcage(Layout layout, int x, int z) {
            layout.ribs = new Rib[layout.tiers];
            double nominalHalfHeight = (TIER_TOP_Y - TIER_BOTTOM_Y) / TIERS * 0.5D + 3.0D;
            for (int i = 0; i < layout.tiers; i++) {
                long ribSalt = this.salt ^ (layout.grand ? 0x90L : 0xA0L + i);
                double angle = ThoraxisTerrainRegimes.hash01(i, 17L, ribSalt) * Math.PI;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double ribSpacing;
                double cageSpacing;
                double halfWidth;
                double meander;
                if (layout.grand) {
                    ribSpacing = GRAND_RIB_SPACING;
                    cageSpacing = GRAND_CAGE_SPACING;
                    halfWidth = 56.0D;
                    meander = 60.0D;
                } else {
                    ribSpacing = 22.0D + 12.0D * ThoraxisTerrainRegimes.hash01(i, 29L, ribSalt);
                    double aspect = 0.8D + 0.8D * ThoraxisTerrainRegimes.hash01(i, 41L, ribSalt);
                    cageSpacing = 2.0D * nominalHalfHeight * aspect + 16.0D;
                    double gap = Math.max(4.0D, Math.min(30.0D, 14.0D + 30.0D * this.shape(x * 0.4D + i * 3121.0D, z * 0.4D - i * 1873.0D)));
                    halfWidth = (cageSpacing - gap) * 0.5D;
                    meander = 30.0D;
                }
                double u = x * cos + z * sin + 16.0D * this.shape(x * 0.5D + 9000.0D + i * 311.0D, z * 0.5D - i * 97.0D);
                double v = -x * sin + z * cos + meander * this.shape(x * 0.25D - 7000.0D + i * 523.0D, z * 0.25D + 300.0D - i * 211.0D);
                double uLocal = u - Math.floor(u / ribSpacing) * ribSpacing - ribSpacing * 0.5D;
                double vLocal = v - Math.floor(v / cageSpacing) * cageSpacing - cageSpacing * 0.5D;
                Rib rib = new Rib();
                rib.u = Math.abs(uLocal);
                rib.v = vLocal;
                rib.halfWidth = halfWidth;
                layout.ribs[i] = rib;
            }
        }

        private void fillMirror(Column column, Layout layout, int x, int z) {
            double hills = this.shape(x * 0.8D - 5200.0D, z * 0.8D + 800.0D);
            double peaks = Math.max(0.0D, this.shape(x * 1.3D + 5000.0D, z * 1.3D - 2600.0D));
            double range = this.shape(x * 0.3D - 1700.0D, z * 0.3D + 3900.0D);
            double mask = 0.35D + 0.65D * Math.max(0.0D, Math.min(1.0D, 0.5D + 3.0D * range));
            layout.mirrorHeight = new double[layout.tiers];
            if (layout.grand) {
                double height = 38.0D + 42.0D * hills + mask * peaks * GRAND_MIRROR_PEAK_SCALE;
                layout.mirrorHeight[0] = Math.max(2.0D, Math.min(160.0D, height - column.basin * 1.4D));
                return;
            }
            double base = 8.0D + 12.0D * hills + mask * peaks * MIRROR_PEAK_SCALE;
            for (int i = 0; i < layout.tiers; i++) {
                double variation = 6.0D * this.shape(x * 0.6D + i * 1777.0D, z * 0.6D - i * 901.0D);
                double cap = (layout.bounds[i + 1] - layout.bounds[i]) * 0.5D + 4.0D;
                layout.mirrorHeight[i] = Math.min(cap, base + variation);
            }
        }

        private void fillSpires(Layout layout, int x, int z) {
            layout.spires = new Spire[layout.tiers][4];
            layout.spireCount = new int[layout.tiers];
            if (layout.grand) {
                this.addSpires(layout, 0, x, z, GRAND_SPIRE_BOTTOM, TIER_TOP_Y, GRAND_SPIRE_CELL, GRAND_SPIRE_JITTER,
                        GRAND_SPIRE_MIN_RADIUS, GRAND_SPIRE_RADIUS_RANGE, GRAND_FLARE_RADIUS, GRAND_FLARE_HEIGHT);
                return;
            }
            for (int i = 0; i < layout.tiers; i++) {
                this.addSpires(layout, i, x, z, layout.bounds[i], layout.bounds[i + 1], SPIRE_CELL, SPIRE_JITTER,
                        SPIRE_MIN_RADIUS, SPIRE_RADIUS_RANGE, FLARE_RADIUS, FLARE_HEIGHT);
            }
        }

        private void addSpires(Layout layout, int tier, int x, int z, double bottom, double top, int cell, double jitter,
                               double minRadius, double radiusRange, double flareRadius, double flareHeight) {
            int baseX = Math.floorDiv(x - cell / 2, cell);
            int baseZ = Math.floorDiv(z - cell / 2, cell);
            long cellSalt = this.salt ^ (layout.grand ? 0x7A00L : 0x7B00L + tier * 0x100L);
            for (int ox = 0; ox < 2; ox++) {
                for (int oz = 0; oz < 2; oz++) {
                    int cellX = baseX + ox;
                    int cellZ = baseZ + oz;
                    if (ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt) > SPIRE_CHANCE) {
                        continue;
                    }
                    double centerX = (cellX + 0.5D) * cell + (ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 1L) - 0.5D) * jitter;
                    double centerZ = (cellZ + 0.5D) * cell + (ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 2L) - 0.5D) * jitter;
                    double radius = minRadius + ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 4L) * radiusRange;
                    double lean = 2.0D + ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 7L) * 4.0D;
                    double dx = x - centerX;
                    double dz = z - centerZ;
                    double reach = radius * (1.0D + SPIRE_LOBE) + lean + flareRadius;
                    if (dx * dx + dz * dz > reach * reach) {
                        continue;
                    }
                    double type = ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 3L);
                    double twist = ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 5L);
                    double end = 0.3D + 0.55D * ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 8L);
                    Spire spire = new Spire();
                    spire.type = type < 0.38D ? 0 : type < 0.82D ? 1 : 2;
                    spire.radius = radius;
                    spire.twist = (0.035D + 0.045D * twist) * (twist < 0.5D ? -1.0D : 1.0D);
                    spire.phase = ThoraxisTerrainRegimes.hash01(cellX, cellZ, cellSalt + 6L) * Math.PI * 2.0D;
                    spire.lean = lean;
                    spire.bottom = bottom;
                    spire.top = top;
                    spire.end = spire.type == 1 ? bottom + end * (top - bottom) : top - end * (top - bottom);
                    spire.flareRadius = flareRadius;
                    spire.flareHeight = flareHeight;
                    spire.dx = dx;
                    spire.dz = dz;
                    layout.spires[tier][layout.spireCount[tier]++] = spire;
                }
            }
        }

        private double pinhole(int x, int z, double offset) {
            double value = this.shape(x * PINHOLE_SCALE + offset, z * PINHOLE_SCALE - offset * 0.61D);
            return smoothstep((-value - PINHOLE_THRESHOLD) / PINHOLE_EDGE);
        }

        private static double smoothstep(double t) {
            if (t <= 0.0D) {
                return 0.0D;
            }
            if (t >= 1.0D) {
                return 1.0D;
            }
            return t * t * (3.0D - 2.0D * t);
        }

        private double shape(double x, double z) {
            return this.shape.get(x, 0.0D, z);
        }

        private double detailNoise(double x, double y, double z) {
            return this.detail.get(x * 0.8D, y * 1.5D, z * 0.8D);
        }

        private static final class Column {
            private final int x;
            private final int z;
            private final double[] weights = new double[ThoraxisTerrainRegimes.COUNT];
            private double basin;
            private double floor;
            private double grandness;
            private double window;
            private double detailAmplitude;
            private double mirrorAmplitude;
            private double strandDistance = Double.MAX_VALUE;
            private int strandCellX;
            private int strandCellZ;
            private Layout tiered;
            private Layout grand;

            private Column(int x, int z) {
                this.x = x;
                this.z = z;
            }
        }

        private static final class Layout {
            private final boolean grand;
            private final int tiers;
            private final double[] bounds;
            private final double[] plateHalf;
            private final double[] mirrorHole;
            private double[] strataCenters;
            private double[] strataHalf;
            private double[] strandInner;
            private double strandRadius;
            private Rib[] ribs;
            private double[] mirrorHeight;
            private Spire[][] spires;
            private int[] spireCount;

            private Layout(boolean grand, int tiers) {
                this.grand = grand;
                this.tiers = tiers;
                this.bounds = new double[tiers + 1];
                this.plateHalf = new double[tiers + 1];
                this.mirrorHole = new double[tiers + 1];
            }
        }

        private static final class Rib {
            private double u;
            private double v;
            private double halfWidth;
        }

        private static final class Spire {
            private int type;
            private double radius;
            private double twist;
            private double phase;
            private double lean;
            private double bottom;
            private double top;
            private double end;
            private double flareRadius;
            private double flareHeight;
            private double dx;
            private double dz;
        }
    }
}
