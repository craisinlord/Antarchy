package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

record AtollPiece(
        int centerX,
        int centerZ,
        int radius,
        int ringRadius,
        int width,
        int flank,
        int ringTop,
        int lagoonY,
        int bowl,
        double[] channels,
        double[] channelWidths,
        double[] islets,
        double[] isletWidths,
        ReefCoral[] sectors,
        double sectorOffset,
        double[] knollX,
        double[] knollZ,
        double[] knollRadius,
        long seed
) implements ReefPiece {

    private static final int INNER_SLOPE = 3;

    static int radiusFor(int ringRadius, int width, int flank) {
        return ringRadius + width + flank + 1;
    }

    static void create(ReefSite site, int x, int z, int ringRadius, int width, int flank, List<ReefPiece> out) {
        RandomSource random = site.random();
        int sea = site.seaLevel();
        int lagoonY = sea - 5 - random.nextInt(3);
        int bowl = 2 + random.nextInt(3);
        int channelCount = 1 + random.nextInt(2);
        double[] channels = new double[channelCount];
        double[] channelWidths = new double[channelCount];
        for (int i = 0; i < channelCount; i++) {
            channels[i] = random.nextDouble() * Math.PI * 2.0D;
            channelWidths[i] = (2.5D + random.nextDouble() * 1.5D) / ringRadius;
        }
        int isletCount = 2 + random.nextInt(3);
        double[] islets = new double[isletCount];
        double[] isletWidths = new double[isletCount];
        for (int i = 0; i < isletCount; i++) {
            islets[i] = random.nextDouble() * Math.PI * 2.0D;
            isletWidths[i] = (3.0D + random.nextDouble() * 4.0D) / ringRadius;
        }
        ReefCoral[] sectors = new ReefCoral[5 + random.nextInt(3)];
        for (int i = 0; i < sectors.length; i++) {
            sectors[i] = ReefCoral.pick(random);
        }
        double lagoonRadius = ringRadius - width - INNER_SLOPE;
        int knollCount = Math.max(0, Mth.floor(lagoonRadius / 6.0D)) + random.nextInt(3);
        double[] knollX = new double[knollCount];
        double[] knollZ = new double[knollCount];
        double[] knollRadius = new double[knollCount];
        for (int i = 0; i < knollCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * lagoonRadius * 0.75D;
            knollX[i] = x + Math.cos(angle) * distance;
            knollZ[i] = z + Math.sin(angle) * distance;
            knollRadius[i] = 2.5D + random.nextDouble() * Math.max(1.0D, lagoonRadius * 0.12D);
        }
        out.add(new AtollPiece(x, z, radiusFor(ringRadius, width, flank), ringRadius, width, flank, sea - 2,
                lagoonY, bowl, channels, channelWidths, islets, isletWidths, sectors, random.nextDouble(),
                knollX, knollZ, knollRadius, random.nextLong()));
    }

    @Override
    public boolean covers(int x, int z, double margin) {
        double dx = x - this.centerX;
        double dz = z - this.centerZ;
        double reach = this.radius + margin;
        return dx * dx + dz * dz <= reach * reach;
    }

    @Override
    public void render(ReefCanvas canvas) {
        canvas.forColumns(this.centerX, this.centerZ, this.radius, (x, z, distance) -> column(canvas, x, z, distance));
    }

    private void column(ReefCanvas canvas, int x, int z, double distance) {
        int floor = canvas.floor(x, z);
        double angle = Math.atan2(z - this.centerZ, x - this.centerX);
        double offset = distance - this.ringRadius;
        double roughness = ReefCanvas.noise(this.seed, x, z);
        int crest = crest(canvas.seaLevel(), angle);
        ReefCoral coral = this.sectors[sector(angle)];
        if (Math.abs(offset) <= this.width) {
            double falloff = offset * offset / (this.width * this.width);
            int top = crest - Mth.floor(falloff * 1.5D + 0.5D) - (roughness < 0.2D ? 1 : 0);
            build(canvas, x, z, floor, top, coral);
            return;
        }
        if (offset > this.width) {
            double t = Math.min(1.0D, (offset - this.width) / this.flank);
            double edge = crest - 2;
            int top = Mth.floor(edge + (floor - edge) * Math.pow(t, 0.6D) + 0.5D) - (roughness < 0.3D ? 1 : 0);
            build(canvas, x, z, floor, top, coral);
            return;
        }
        double lagoonRadius = this.ringRadius - this.width;
        int lagoonFloor = this.lagoonY - Mth.floor(this.bowl * (1.0D - Math.min(1.0D, distance / lagoonRadius)) + 0.5D);
        double inner = -offset - this.width;
        if (inner < INNER_SLOPE) {
            double edge = crest - 2;
            int top = Mth.floor(edge + (lagoonFloor - edge) * (inner / INNER_SLOPE) + 0.5D);
            build(canvas, x, z, floor, Math.max(top, lagoonFloor), coral);
            return;
        }
        canvas.blockColumn(x, z, floor, lagoonFloor, Blocks.SAND.defaultBlockState());
        for (int i = 0; i < this.knollX.length; i++) {
            double dx = x - this.knollX[i];
            double dz = z - this.knollZ[i];
            double r = this.knollRadius[i];
            double d2 = (dx * dx + dz * dz) / (r * r);
            if (d2 < 1.0D) {
                int rise = Mth.floor((this.ringTop - lagoonFloor) * Math.sqrt(1.0D - d2) + 0.5D);
                canvas.coralColumn(x, z, lagoonFloor + 1, Math.min(this.ringTop - 1, lagoonFloor + rise), this.sectors[i % this.sectors.length]);
                return;
            }
        }
        double roll = ReefCanvas.noise(this.seed + 1L, x, z);
        int decorY = Math.max(floor, lagoonFloor + 1);
        if (roll < 0.22D) {
            canvas.decor(x, decorY, z, Blocks.SEAGRASS.defaultBlockState());
        } else if (roll < 0.27D) {
            canvas.decor(x, decorY, z, coral.plant().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        } else if (roll < 0.29D) {
            BlockState pickle = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + (int) (roll * 1000) % 4);
            canvas.decor(x, decorY, z, pickle);
        }
    }

    private void build(ReefCanvas canvas, int x, int z, int floor, int top, ReefCoral coral) {
        int sea = canvas.seaLevel();
        int coralTop = Math.min(top, sea - 2);
        canvas.coralColumn(x, z, floor, coralTop, coral);
        if (top > coralTop) {
            canvas.blockColumn(x, z, Math.max(floor, coralTop + 1), top, Blocks.SAND.defaultBlockState());
        }
    }

    private int crest(int sea, double angle) {
        for (int i = 0; i < this.channels.length; i++) {
            if (angularDistance(angle, this.channels[i]) < this.channelWidths[i]) {
                return this.lagoonY - 1;
            }
        }
        for (int i = 0; i < this.islets.length; i++) {
            if (angularDistance(angle, this.islets[i]) < this.isletWidths[i]) {
                return sea + 1;
            }
        }
        return this.ringTop;
    }

    private int sector(double angle) {
        double turn = (angle + Math.PI) / (Math.PI * 2.0D);
        return Math.floorMod(Mth.floor(turn * this.sectors.length + this.sectorOffset), this.sectors.length);
    }

    private static double angularDistance(double a, double b) {
        double difference = Math.abs(Mth.wrapDegrees(Math.toDegrees(a - b)));
        return Math.toRadians(difference);
    }
}
