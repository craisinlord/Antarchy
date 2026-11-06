package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import com.craisinlord.antarchy.content.worldgen.elythia.CoralSpikeFeature;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

record SpikePiece(int centerX, int centerZ, long seed) implements ReefPiece {

    static final int RADIUS = 26;
    private static final double SPIKE_REACH = 19.0D;

    private static final CoralSpikeFeature SPIKES = new CoralSpikeFeature(NoneFeatureConfiguration.CODEC);

    static void create(ReefSite site, int x, int z, List<ReefPiece> out) {
        int snappedX = (x & ~15) + 8;
        int snappedZ = (z & ~15) + 8;
        out.add(new SpikePiece(snappedX, snappedZ, site.random().nextLong()));
    }

    @Override
    public int radius() {
        return RADIUS;
    }

    @Override
    public boolean covers(int x, int z, double margin) {
        double dx = x - this.centerX;
        double dz = z - this.centerZ;
        double reach = SPIKE_REACH + margin;
        return dx * dx + dz * dz <= reach * reach;
    }

    @Override
    public void render(ReefCanvas canvas) {
        if (!canvas.contains(this.centerX, this.centerZ)) {
            return;
        }
        SPIKES.place(NoneFeatureConfiguration.INSTANCE, canvas.level(), canvas.generator(),
                RandomSource.create(this.seed), new BlockPos(this.centerX, canvas.seaLevel(), this.centerZ));
    }
}
