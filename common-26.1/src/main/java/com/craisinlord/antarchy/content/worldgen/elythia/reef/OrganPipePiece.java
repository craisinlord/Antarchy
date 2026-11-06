package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;

record OrganPipePiece(int centerX, int centerZ, int radius, List<Pipe> pipes) implements ReefPiece {

    record Pipe(int x, int z, int outer, int baseY, int height, ReefCoral coral, int pickles) {
    }

    static int radiusFor(int field) {
        return field + 5;
    }

    static void create(ReefSite site, int x, int z, int field, List<ReefPiece> out) {
        RandomSource random = site.random();
        ReefCoral primary = ReefCoral.TUBE;
        ReefCoral secondary = ReefCoral.pick(random);
        int tallest = field >= 10 ? 22 + random.nextInt(14) : field >= 6 ? 13 + random.nextInt(8) : 8 + random.nextInt(5);
        int target = Math.max(4, Mth.floor(field * 2.6F));
        List<Pipe> pipes = new ArrayList<>();
        for (int attempt = 0; attempt < target * 12 && pipes.size() < target; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * field;
            int px = Mth.floor(x + Math.cos(angle) * distance + 0.5D);
            int pz = Mth.floor(z + Math.sin(angle) * distance + 0.5D);
            int outer = field >= 10 && distance < field * 0.3D && random.nextFloat() < 0.35F ? 2 : 1;
            boolean clear = true;
            for (Pipe other : pipes) {
                int dx = other.x() - px;
                int dz = other.z() - pz;
                double gap = other.outer() + outer + 0.6D;
                if (dx * dx + dz * dz < gap * gap) {
                    clear = false;
                    break;
                }
            }
            if (!clear) {
                continue;
            }
            int baseY = site.grid().at(px, pz);
            double falloff = 1.0D - 0.45D * Math.pow(distance / field, 1.2D);
            int height = Math.max(3, Mth.floor(tallest * falloff * (0.75D + random.nextDouble() * 0.5D)));
            height = Math.min(height, site.seaLevel() - 2 - baseY);
            ReefCoral coral = random.nextFloat() < 0.7F ? primary : secondary;
            int pickles = random.nextFloat() < 0.35F ? 1 + random.nextInt(3) : 0;
            if (height >= 3) {
                pipes.add(new Pipe(px, pz, outer, baseY, height, coral, pickles));
            }
        }
        out.add(new OrganPipePiece(x, z, radiusFor(field), List.copyOf(pipes)));
    }

    @Override
    public boolean covers(int x, int z, double margin) {
        for (Pipe pipe : this.pipes) {
            double dx = x - pipe.x();
            double dz = z - pipe.z();
            double reach = pipe.outer() + 1.0D + margin;
            if (dx * dx + dz * dz <= reach * reach) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(ReefCanvas canvas) {
        for (Pipe pipe : this.pipes) {
            double inner = pipe.outer() - 0.5D;
            canvas.forColumns(pipe.x(), pipe.z(), pipe.outer() + 0.5D, (x, z, distance) -> {
                int floor = canvas.floor(x, z);
                if (distance > inner) {
                    canvas.coralColumn(x, z, floor, pipe.baseY() + pipe.height(), pipe.coral());
                } else if (pipe.pickles() > 0 && x == pipe.x() && z == pipe.z()) {
                    canvas.decor(x, floor, z, Blocks.SEA_PICKLE.defaultBlockState()
                            .setValue(SeaPickleBlock.PICKLES, pipe.pickles()));
                }
            });
        }
    }
}
