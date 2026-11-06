package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

record StarburstPiece(
        int centerX,
        int centerZ,
        int radius,
        int baseY,
        int core,
        int spire,
        ReefCoral coreCoral,
        List<Arm> arms,
        ReefFloorGrid grid
) implements ReefPiece {

    record Arm(double startX, double startZ, double angle, double bend, double length,
               double startRadius, double endRadius, double startLift, double curl, ReefCoral coral) {

        double endX() {
            return this.startX + Math.cos(this.angle + this.bend) * this.length;
        }

        double endZ() {
            return this.startZ + Math.sin(this.angle + this.bend) * this.length;
        }
    }

    static int radiusFor(int core, int length, boolean forks) {
        return core + length + (forks ? length / 2 : 0) + 5;
    }

    static void create(ReefSite site, int x, int z, int core, int length, boolean forks, List<ReefPiece> out) {
        RandomSource random = site.random();
        int baseY = site.grid().at(x, z);
        ReefCoral coreCoral = ReefCoral.star();
        ReefCoral first = ReefCoral.pick(random);
        ReefCoral second = ReefCoral.pick(random);
        int count = 5 + random.nextInt(core >= 6 ? 5 : 4);
        double offset = random.nextDouble() * Math.PI * 2.0D;
        double bend = (random.nextDouble() * 2.0D - 1.0D) * 0.5D;
        double thickness = 1.2D + core * 0.35D;
        double curl = core * 1.3D + random.nextInt(Math.max(2, core));
        int spire = core >= 6 ? Math.min(Mth.floor(core * (1.5D + random.nextDouble())), site.seaLevel() - 3 - baseY - core) : 0;
        List<Arm> arms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double angle = offset + i * Math.PI * 2.0D / count + (random.nextDouble() - 0.5D) * 0.3D;
            double armLength = length * (0.8D + random.nextDouble() * 0.4D);
            ReefCoral coral = i % 2 == 0 ? first : second;
            double startX = x + Math.cos(angle) * (core - 1);
            double startZ = z + Math.sin(angle) * (core - 1);
            if (forks && random.nextFloat() < 0.7F) {
                Arm stem = new Arm(startX, startZ, angle, bend * 0.6D, armLength * 0.6D,
                        thickness, thickness * 0.75D, core * 0.6D, 0.0D, coral);
                arms.add(stem);
                double split = 0.35D + random.nextDouble() * 0.25D;
                double heading = angle + bend * 0.6D;
                for (int side = -1; side <= 1; side += 2) {
                    arms.add(new Arm(stem.endX(), stem.endZ(), heading + side * split, bend * 0.6D, armLength * 0.5D,
                            thickness * 0.75D, 0.9D, 0.0D, curl, coral));
                }
            } else {
                arms.add(new Arm(startX, startZ, angle, bend, armLength, thickness, 0.9D, core * 0.6D, curl, coral));
            }
        }
        out.add(new StarburstPiece(x, z, radiusFor(core, length, forks), baseY, core, Math.max(0, spire),
                coreCoral, List.copyOf(arms), site.grid()));
    }

    @Override
    public boolean covers(int x, int z, double margin) {
        double dx = x - this.centerX;
        double dz = z - this.centerZ;
        double core = this.core + 1.0D + margin;
        if (dx * dx + dz * dz <= core * core) {
            return true;
        }
        for (Arm arm : this.arms) {
            double reach = arm.startRadius() + 1.5D + margin;
            double midHeading = arm.angle() + arm.bend() * 0.5D;
            double midX = arm.startX() + Math.cos(midHeading) * arm.length() * 0.5D;
            double midZ = arm.startZ() + Math.sin(midHeading) * arm.length() * 0.5D;
            if (ReefPiece.segmentDistance(x, z, arm.startX(), arm.startZ(), midX, midZ) <= reach
                    || ReefPiece.segmentDistance(x, z, midX, midZ, arm.endX(), arm.endZ()) <= reach) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(ReefCanvas canvas) {
        double coreRadius = this.core + 0.5D;
        double spireRadius = Math.max(1.5D, this.core * 0.45D);
        canvas.forColumns(this.centerX, this.centerZ, coreRadius, (x, z, distance) -> {
            double half = this.core * 0.85D * Math.sqrt(Math.max(0.0D, 1.0D - distance * distance / (coreRadius * coreRadius)));
            int top = this.baseY + Mth.floor(half + 0.5D);
            if (this.spire > 0 && distance < spireRadius) {
                top += Mth.floor(this.spire * (1.0D - distance / spireRadius) + 0.5D);
            } else if (distance < 1.0D) {
                top += 2;
            }
            canvas.coralColumn(x, z, canvas.floor(x, z), top, this.coreCoral);
        });
        for (Arm arm : this.arms) {
            renderArm(canvas, arm);
        }
    }

    private void renderArm(ReefCanvas canvas, Arm arm) {
        int steps = Math.max(2, Mth.ceil(arm.length() / 0.6D));
        double lastX = Double.NaN;
        double lastY = 0.0D;
        double lastZ = 0.0D;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double heading = arm.angle() + arm.bend() * t;
            double distance = arm.length() * t;
            double x = arm.startX() + Math.cos(heading) * distance;
            double z = arm.startZ() + Math.sin(heading) * distance;
            double r = Mth.lerp(t, arm.startRadius(), arm.endRadius());
            if (!canvas.intersects(x, z, r + 1.0D)) {
                lastX = Double.NaN;
                continue;
            }
            double tip = Math.max(0.0D, (t - 0.6D) / 0.4D);
            double lift = arm.startLift() * (1.0D - t) * (1.0D - t) + arm.curl() * tip * tip;
            double y = this.grid.at(x, z) + r * 0.5D + lift;
            if (i < steps && !Double.isNaN(lastX)) {
                double dx = x - lastX;
                double dy = y - lastY;
                double dz = z - lastZ;
                double spacing = Math.max(0.5D, r * 0.45D);
                if (dx * dx + dy * dy + dz * dz < spacing * spacing) {
                    continue;
                }
            }
            canvas.blob(x, y, z, r, arm.coral(), true);
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }
}
