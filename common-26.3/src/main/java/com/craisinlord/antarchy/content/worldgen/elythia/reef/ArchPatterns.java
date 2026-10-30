package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

final class ArchPatterns {

    private static final double MAX_LENGTH = 170.0D;
    private static final double FOOT_JITTER = 3.5D;

    record Span(double ax, double az, double bx, double bz, double height, double sway, double leg, double crest) {

        double reach() {
            double mx = (this.ax + this.bx) / 2.0D;
            double mz = (this.az + this.bz) / 2.0D;
            double length = Math.hypot(this.bx - this.ax, this.bz - this.az);
            double feet = Math.max(Math.hypot(this.ax, this.az), Math.hypot(this.bx, this.bz)) + FOOT_JITTER;
            double wander = Math.abs(this.sway) + 1.0D + length * 0.04D;
            return Math.max(feet, Math.hypot(mx, mz) + wander) + this.leg * 2.4D + 3.0D;
        }
    }

    record Layout(List<Span> spans, int radius, boolean sharedPalette) {
    }

    private ArchPatterns() {
    }

    static Layout pick(RandomSource random, double scale) {
        int roll = random.nextInt(5);
        List<Span> spans = switch (roll) {
            case 0 -> ring(random, scale);
            case 1 -> dome(random, scale);
            case 2 -> avenue(random, scale);
            case 3 -> aqueduct(random, scale);
            default -> field(random, scale);
        };
        return layout(spans, roll != 4);
    }

    static Layout single(RandomSource random, double scale) {
        double span = scaled(28 + random.nextInt(18), scale);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double dx = Math.cos(angle) * span / 2.0D;
        double dz = Math.sin(angle) * span / 2.0D;
        Span arch = new Span(-dx, -dz, dx, dz, span * (0.6D + random.nextDouble() * 0.25D),
                (random.nextDouble() * 2.0D - 1.0D) * span * 0.15D, leg(scale), crest(scale));
        return layout(List.of(arch), true);
    }

    static void build(ReefSite site, int x, int z, Layout layout, List<ReefPiece> out) {
        RandomSource random = site.random();
        ReefCoral primary = ReefCoral.pick(random);
        ReefCoral secondary = ReefCoral.pick(random);
        boolean group = layout.spans().size() > 1;
        for (Span span : layout.spans()) {
            if (!layout.sharedPalette() || random.nextFloat() < 0.3F) {
                primary = ReefCoral.pick(random);
                secondary = ReefCoral.pick(random);
            }
            double ax = x + span.ax() + jitter(random);
            double az = z + span.az() + jitter(random);
            double bx = x + span.bx() + jitter(random);
            double bz = z + span.bz() + jitter(random);
            double sway = span.sway() + (random.nextDouble() * 2.0D - 1.0D) * (1.0D + Math.hypot(bx - ax, bz - az) * 0.05D);
            out.add(ArchPiece.between(site, ax, az, bx, bz, span.height(), sway, span.leg(), span.crest(),
                    primary, secondary, group));
        }
    }

    private static Layout layout(List<Span> spans, boolean sharedPalette) {
        double reach = 0.0D;
        for (Span span : spans) {
            reach = Math.max(reach, span.reach());
        }
        return new Layout(List.copyOf(spans), Mth.ceil(reach), sharedPalette);
    }

    private static List<Span> ring(RandomSource random, double scale) {
        double radius = scaled(34 + random.nextInt(24), scale);
        int count = 6 + random.nextInt(5);
        double phase = random.nextDouble() * Math.PI * 2.0D;
        double step = Math.PI * 2.0D / count;
        double squash = 0.75D + random.nextDouble() * 0.25D;
        double tilt = random.nextDouble() * Math.PI;
        double heightFactor = 0.8D + random.nextDouble() * 0.3D;
        double leg = leg(scale);
        double crest = crest(scale);
        double[] xs = new double[count];
        double[] zs = new double[count];
        for (int i = 0; i < count; i++) {
            double angle = phase + i * step + (random.nextDouble() - 0.5D) * step * 0.45D;
            double r = radius * (0.85D + random.nextDouble() * 0.3D);
            double ux = Math.cos(angle) * r;
            double uz = Math.sin(angle) * r * squash;
            xs[i] = ux * Math.cos(tilt) - uz * Math.sin(tilt);
            zs[i] = ux * Math.sin(tilt) + uz * Math.cos(tilt);
        }
        List<Span> spans = new ArrayList<>();
        int gap = random.nextFloat() < 0.35F ? random.nextInt(count) : -1;
        for (int i = 0; i < count; i++) {
            if (i == gap) {
                continue;
            }
            int next = (i + 1) % count;
            double chord = Math.hypot(xs[next] - xs[i], zs[next] - zs[i]);
            spans.add(new Span(xs[i], zs[i], xs[next], zs[next], chord * heightFactor, -chord * 0.12D, leg, crest));
        }
        if (random.nextFloat() < 0.5F) {
            double inner = radius * (0.45D + random.nextDouble() * 0.2D);
            double cross = random.nextDouble() * Math.PI;
            for (int i = 0; i < 2; i++) {
                double angle = cross + i * (Math.PI / 2.0D + (random.nextDouble() - 0.5D) * 0.6D);
                double reach = inner * (0.8D + random.nextDouble() * 0.4D);
                double dx = Math.cos(angle) * reach;
                double dz = Math.sin(angle) * reach;
                spans.add(new Span(-dx, -dz, dx, dz, reach * 1.3D, 0.0D, leg * 0.85D, crest));
            }
        }
        return spans;
    }

    private static List<Span> dome(RandomSource random, double scale) {
        double diameter = scaled(60 + random.nextInt(40), scale);
        int count = 3 + random.nextInt(3);
        double phase = random.nextDouble() * Math.PI;
        double height = diameter * (0.5D + random.nextDouble() * 0.15D);
        double leg = leg(scale) + 0.5D;
        double crest = crest(scale);
        List<Span> spans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double angle = phase + i * Math.PI / count + (random.nextDouble() - 0.5D) * 0.5D;
            double half = diameter / 2.0D * (0.8D + random.nextDouble() * 0.35D);
            double shift = (random.nextDouble() - 0.5D) * diameter * 0.15D;
            double ox = -Math.sin(angle) * shift;
            double oz = Math.cos(angle) * shift;
            double dx = Math.cos(angle) * half;
            double dz = Math.sin(angle) * half;
            spans.add(new Span(ox - dx, oz - dz, ox + dx, oz + dz, height * (0.85D + random.nextDouble() * 0.3D), 0.0D, leg, crest));
        }
        return spans;
    }

    private static List<Span> avenue(RandomSource random, double scale) {
        double span = scaled(32 + random.nextInt(20), scale);
        double spacing = scaled(11 + random.nextInt(5), scale) + 4.0D;
        int count = Math.max(3, Math.min(6 + random.nextInt(5), (int) (MAX_LENGTH / (spacing * 1.15D))));
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double lean = (2.0D + random.nextDouble() * 3.0D) * scale * (random.nextBoolean() ? 1.0D : -1.0D);
        double bow = (random.nextDouble() * 2.0D - 1.0D) * 0.012D;
        double baseHeight = span * (0.65D + random.nextDouble() * 0.2D);
        double leg = leg(scale);
        double crest = crest(scale);
        double[] offsets = new double[count];
        for (int k = 1; k < count; k++) {
            offsets[k] = offsets[k - 1] + spacing * (0.7D + random.nextDouble() * 0.6D);
        }
        double middle = offsets[count - 1] / 2.0D;
        List<Span> spans = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            double offset = offsets[k] - middle;
            double centrality = middle == 0.0D ? 1.0D : 1.0D - Math.abs(offset) / middle;
            double heading = angle + (random.nextDouble() - 0.5D) * 0.4D;
            double dirX = Math.cos(heading);
            double dirZ = Math.sin(heading);
            double drift = bow * offset * offset;
            double cx = -Math.sin(angle) * offset + Math.cos(angle) * drift;
            double cz = Math.cos(angle) * offset + Math.sin(angle) * drift;
            double half = span / 2.0D * (0.7D + 0.15D * centrality + random.nextDouble() * 0.3D);
            spans.add(new Span(cx - dirX * half, cz - dirZ * half, cx + dirX * half, cz + dirZ * half,
                    baseHeight * (0.6D + 0.35D * centrality + random.nextDouble() * 0.15D),
                    lean * (0.6D + random.nextDouble() * 0.8D), leg, crest));
        }
        return spans;
    }

    private static List<Span> aqueduct(RandomSource random, double scale) {
        double span = scaled(24 + random.nextInt(14), scale);
        int count = Math.max(3, Math.min(5 + random.nextInt(5), (int) (MAX_LENGTH / (span * 1.2D))));
        double heading = random.nextDouble() * Math.PI * 2.0D;
        double turn = (random.nextDouble() * 2.0D - 1.0D) * 0.2D;
        double height = span * (0.75D + random.nextDouble() * 0.2D);
        double leg = leg(scale);
        double crest = crest(scale);
        double[] xs = new double[count + 1];
        double[] zs = new double[count + 1];
        double[] lengths = new double[count];
        for (int i = 1; i <= count; i++) {
            heading += turn + (random.nextDouble() - 0.5D) * 0.35D;
            lengths[i - 1] = span * (0.7D + random.nextDouble() * 0.5D);
            xs[i] = xs[i - 1] + Math.cos(heading) * lengths[i - 1];
            zs[i] = zs[i - 1] + Math.sin(heading) * lengths[i - 1];
        }
        double shiftX = (xs[0] + xs[count]) / 2.0D;
        double shiftZ = (zs[0] + zs[count]) / 2.0D;
        List<Span> spans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double local = height * lengths[i] / span;
            spans.add(new Span(xs[i] - shiftX, zs[i] - shiftZ, xs[i + 1] - shiftX, zs[i + 1] - shiftZ,
                    local * (0.8D + random.nextDouble() * 0.4D), 0.0D, leg, crest));
        }
        return spans;
    }

    private static List<Span> field(RandomSource random, double scale) {
        int columns = 3;
        int rows = 3 + random.nextInt(2);
        double maxSpan = scaled(40, scale);
        double along = maxSpan + 12.0D;
        double across = scaled(18, scale) + 8.0D;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double leg = leg(scale) - 0.5D;
        double crest = crest(scale);
        List<Span> spans = new ArrayList<>();
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                if (random.nextFloat() < 0.3F) {
                    continue;
                }
                double u = (c - (columns - 1) / 2.0D) * along + (random.nextDouble() - 0.5D) * 10.0D;
                double v = (r - (rows - 1) / 2.0D) * across + (random.nextDouble() - 0.5D) * 6.0D;
                double cx = Math.cos(angle) * u - Math.sin(angle) * v;
                double cz = Math.sin(angle) * u + Math.cos(angle) * v;
                double heading = angle + (random.nextDouble() - 0.5D) * 0.7D;
                double dirX = Math.cos(heading);
                double dirZ = Math.sin(heading);
                double half = maxSpan * (0.5D + random.nextDouble() * 0.5D) / 2.0D;
                spans.add(new Span(cx - dirX * half, cz - dirZ * half, cx + dirX * half, cz + dirZ * half,
                        half * 2.0D * (0.55D + random.nextDouble() * 0.4D),
                        (random.nextDouble() * 2.0D - 1.0D) * 4.0D * scale, leg, crest));
            }
        }
        return spans;
    }

    private static double jitter(RandomSource random) {
        return (random.nextDouble() * 2.0D - 1.0D) * FOOT_JITTER;
    }

    private static double scaled(double value, double scale) {
        return Math.max(10.0D, value * scale);
    }

    private static double leg(double scale) {
        return 2.5D + 3.0D * scale;
    }

    private static double crest(double scale) {
        return 1.5D + 1.5D * scale;
    }
}
